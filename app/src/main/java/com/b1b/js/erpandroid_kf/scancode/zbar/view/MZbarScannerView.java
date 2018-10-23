package com.b1b.js.erpandroid_kf.scancode.zbar.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by 张建宇 on 2019/3/21.
 */
public class MZbarScannerView extends BarcodeScannerView {
    private static final String TAG = "ZBarScannerView";

    public interface ResultHandler {
        public void handleResult(Result rawResult);
    }

    static {
        try {
            System.loadLibrary("iconv");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private ImageScanner mScanner;
    private List<BarcodeFormat> mFormats;
    private ZBarScannerView.ResultHandler mResultHandler;

    public MZbarScannerView(Context context) {
        super(context);
        setupScanner();
    }

    public MZbarScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupScanner();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        setupScanner();
    }

    public void setResultHandler(ZBarScannerView.ResultHandler resultHandler) {
        mResultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            return BarcodeFormat.ALL_FORMATS;
        }
        return mFormats;
    }

    public void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (BarcodeFormat format : getFormats()) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("zjy", "MZbarScannerView->onDraw(): getWidth-getHeight==" + getWidth() + "-" + getHeight());
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mResultHandler == null) {
            return;
        }
        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            int width = size.width;
            int height = size.height;

            if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
                int rotationCount = getRotationCount();
                if (rotationCount == 1 || rotationCount == 3) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
                data = getRotatedData(data, camera);
            }

            Rect rect = getFramingRectInPreview(width, height);
//            Image barcode = new Image(width, height, "Y800");
//            barcode.setData(data);
//            //指定截取范围
//            barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
//            int result = mScanner.scanImage(barcode);
            if (!startDecode(data,rect,width,height)) {
                camera.setOneShotPreviewCallback(this);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public boolean startDecode(byte[] roateData, Rect rect, int realWidth, int realHeight) {
        byte[] data = roateData;
        int width = realWidth;
        int height = realHeight;
        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        //指定截取范围
        barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
        int width1 = barcode.getWidth();
        int height2 = barcode.getHeight();
        //            byte[] data1 = barcode.getData();
        //            if (data1 == data) {
        //                Log.e("zjy", "MZbarScannerView->onPreviewFrame(): 1 data==");
        //            }else{
        //                Log.e("zjy", "MZbarScannerView->onPreviewFrame(): 2data==" + data.length + "\t" + data1.length);
        //            }
        Log.e("zjy", "MZbarScannerView->onPreviewFrame(): CropWh==" + width1 + "x" + height2);
        int result = mScanner.scanImage(barcode);
        if (result != 0) {
            SymbolSet syms = mScanner.getResults();
            final Result rawResult = new Result();
            for (Symbol sym : syms) {
                // In order to retreive QR codes containing null bytes we need to
                // use getDataBytes() rather than getData() which uses C strings.
                // Weirdly ZBar transforms all data to UTF-8, even the data returned
                // by getDataBytes() so we have to decode it as UTF-8.
                String symData;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    symData = new String(sym.getDataBytes(), StandardCharsets.UTF_8);
                } else {
                    symData = sym.getData();
                }
                if (!TextUtils.isEmpty(symData)) {
                    rawResult.setContents(symData);
                    rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                    break;
                }
            }

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Stopping the preview can take a little long.
                    // So we want to set result handler to null to discard subsequent calls to
                    // onPreviewFrame.
                    ZBarScannerView.ResultHandler tmpResultHandler = mResultHandler;
                    mResultHandler = null;

                    stopCameraPreview();
                    if (tmpResultHandler != null) {
                        tmpResultHandler.handleResult(rawResult);
                    }

                }
            });
            return true;
        }
        return false;
    }
    public void resumeCameraPreview(ZBarScannerView.ResultHandler resultHandler) {
        mResultHandler = resultHandler;
        super.resumeCameraPreview();
    }
}
