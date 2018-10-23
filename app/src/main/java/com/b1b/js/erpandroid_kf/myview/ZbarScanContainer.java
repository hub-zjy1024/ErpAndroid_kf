package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;

/**
 * Created by 张建宇 on 2019/7/24.
 */
public class ZbarScanContainer extends ScanViewContainer {
    private ImageScanner zbarDecoder;
    private List<BarcodeFormat> mFormats;
    public ZbarScanContainer(@NonNull Context context) {
        super(context);
    }

    public ZbarScanContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZbarScanContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        super.init();
        initParser();
    }

    public void setmFormats(List<BarcodeFormat> mFormats) {
        this.mFormats = mFormats;
    }
    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            return BarcodeFormat.ALL_FORMATS;
        }
        return mFormats;
    }
    private void initParser() {
        zbarDecoder = new ImageScanner();
        zbarDecoder.setConfig(0, Config.X_DENSITY, 3);
        zbarDecoder.setConfig(0, Config.Y_DENSITY, 3);
        zbarDecoder.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (BarcodeFormat format : getFormats()) {
            zbarDecoder.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }
    @Override
    protected String startDecode(byte[] data, Rect rect, int width, int height) {
        String codeVal = null;
//        Log.e("zjy", getClass() + "->startDecode(): ==1" );
        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        //指定截取范围
        barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
        int result = zbarDecoder.scanImage(barcode);
//        Log.e("zjy", getClass() + "->startDecode(): ==2" );
        if (result != 0) {
            SymbolSet syms = zbarDecoder.getResults();
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
            codeVal = rawResult.getContents();
            Log.e("zjy", getClass() + "->startDecode(): ==" + rawResult.getContents() + ",type=" +
                    rawResult.getBarcodeFormat().getName());
        }
        return codeVal;
    }
}
