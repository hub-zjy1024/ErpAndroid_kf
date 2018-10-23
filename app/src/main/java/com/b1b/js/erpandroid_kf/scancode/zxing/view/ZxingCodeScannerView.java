package com.b1b.js.erpandroid_kf.scancode.zxing.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;

import com.b1b.js.erpandroid_kf.dtr.zxing.decode.DecodeFormatManager;
import com.b1b.js.erpandroid_kf.dtr.zxing.decode.DecodeThread;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.common.GlobalHistogramBinarizer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;

import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * Created by 张建宇 on 2019/3/21.
 */
public class ZxingCodeScannerView extends BarcodeScannerView {
    public static final int BARCODE_MODE = 0X100;
    public static final int QRCODE_MODE = 0X200;
    public static final int ALL_MODE = 0X300;

    private static String TAG = "mZxing2";

    public interface ZxingResultHandler {
        void handleResult(com.google.zxing.Result result, Bundle bundle);
    }
    private ZxingResultHandler mResultHandler;
    private MultiFormatReader multiFormatReader;

    public void setmResultHandler(ZxingResultHandler mResultHandler) {
        this.mResultHandler = mResultHandler;
    }

    public ZxingCodeScannerView(Context context) {
        super(context);
        init();
    }
    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height, Camera camera) {
        long time1 = System.currentTimeMillis();
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = data;
        com.google.zxing.Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(rotatedData, width, height);
        if (source != null) {
            //            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }
        Log.e("zjy", "DecodeHandler->decode():Zxing use==" + (System.currentTimeMillis() - time1));
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            final Bundle bundle = new Bundle();
            bundleThumbnail(source, bundle);
            final com.google.zxing.Result finalRawResult = rawResult;
            ZxingResultHandler tmpResultHandler = mResultHandler;
            mResultHandler = null;
            stopCameraPreview();
            if (tmpResultHandler != null) {
                tmpResultHandler.handleResult(finalRawResult, bundle);
            }
        } else {
            try {
                camera.setOneShotPreviewCallback(this);
            } catch (Throwable e) {
                Log.e("zjy", "ZxingCodeScannerView->decode(): ==setOneShotPreviewCallback failed", e);
            }

        }
    }
    public static void rotateYUV240SP(byte[] src,byte[] des,int width,int height)
    {
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=0;i<width;i+=2) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[wh+ width*j + i];
                des[k+1]=src[wh + width*j + i+1];
                k+=2;
            }
        }
    }
    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
    }

    @IntDef({BARCODE_MODE, QRCODE_MODE, ALL_MODE})
    @interface DecodeMode {

    }
    public void setFormat(@DecodeMode int decodeMode) {
        EnumMap<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.AZTEC));
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.PDF_417));
        switch (decodeMode) {
            case BARCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                break;

            case QRCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            case ALL_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;
            default:
                break;
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
    }

    public void init() {
        multiFormatReader = new MultiFormatReader();
        setFormat(ALL_MODE);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mResultHandler == null) {
            return;
        }
        Camera.Size size = camera.getParameters().getPreviewSize();
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
        decode(data, width, height, camera);
    }

    @Override
    protected void resumeCameraPreview() {
        super.resumeCameraPreview();
    }
}
