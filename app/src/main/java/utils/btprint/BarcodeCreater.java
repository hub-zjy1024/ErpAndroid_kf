package utils.btprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 Created by 张建宇 on 2017/4/20. */

public class BarcodeCreater {
    private static int marginW = 20;
    public static BarcodeFormat barcodeFormat;

    static {
        barcodeFormat = BarcodeFormat.CODE_128;
    }

    public BarcodeCreater() {
    }


    public static Bitmap createQRCode(Context context, String contents, int desiredWidth, int desiredHeight, boolean
            displayCode, float belowSize) {
        Bitmap resultBitmap = null;
        Bitmap e = null;
        try {
            e = encodeAsBitmap(contents, BarcodeFormat.QR_CODE, desiredWidth, desiredHeight);
            resultBitmap = e;
        } catch (WriterException var10) {
            var10.printStackTrace();
        }
        if (displayCode) {
            Bitmap codeBitmap = creatCodeBitmapSize(contents, desiredWidth, desiredHeight, context, belowSize);
            resultBitmap = mixtureBitmap(e, codeBitmap, new PointF(0.0F, (float) desiredHeight));
            codeBitmap.recycle();
            codeBitmap = null;
            e.recycle();
            e = null;
        }
        return resultBitmap;
    }

    public static Bitmap creatBarcode(Context context, String contents, int desiredWidth, int desiredHeight, boolean
            displayCode, float belowSize) {
        Bitmap resultBitmap = null;
        Bitmap e = null;
        try {
            e = encodeAsBitmap(contents, BarcodeFormat.CODE_128, desiredWidth, desiredHeight);
            resultBitmap = e;
        } catch (WriterException var10) {
            var10.printStackTrace();
        }
        if (displayCode) {
            Bitmap codeBitmap = creatCodeBitmapSize(contents, desiredWidth, desiredHeight, context, belowSize);
            Log.d("zjy", "BarcodeCreater->creatBarcode(): =codeHeight=" + codeBitmap.getHeight());
            resultBitmap = mixtureBitmap(e, codeBitmap, new PointF(0.0F, (float) desiredHeight));
            codeBitmap.recycle();
            codeBitmap = null;
            e.recycle();
            e = null;
        }
        Log.d("zjy", "BarcodeCreater->creatBarcode(): resultHeight==" + resultBitmap.getHeight());
        return resultBitmap;
    }

    public static Bitmap creatBarcode(Context context, String contents, int desiredWidth, int desiredHeight, String belowStr
            , float belowSize) {
        Bitmap resultBitmap = null;
        Bitmap e = null;
        try {
            e = encodeAsBitmap(contents, BarcodeFormat.CODE_128, desiredWidth, desiredHeight);
            resultBitmap = e;
        } catch (WriterException var10) {
            var10.printStackTrace();
        }
        if (belowStr != null) {
            Bitmap codeBitmap = creatCodeBitmapSize(belowStr, desiredWidth, desiredHeight, context, belowSize);
            if (codeBitmap != null) {
                Log.d("zjy", "BarcodeCreater->creatBarcode(): =codeHeight=" + codeBitmap.getHeight());
                resultBitmap = mixtureBitmap(e, codeBitmap, new PointF(0.0F, (float) desiredHeight));
                codeBitmap.recycle();
                codeBitmap = null;
                e.recycle();
                e = null;
            } else {
                Log.e("zjy", "BarcodeCreater->creatBarcode(): ==creatCodeBitmapSize failed");
            }
        }
        Log.d("zjy", "BarcodeCreater->creatBarcode(): resultHeight==" + resultBitmap.getHeight());
        return resultBitmap;
    }

    private static Bitmap creatCodeBitmapSize(String contents, int width, int height, Context context, float size) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(size);
        tv.setGravity(Gravity.CENTER);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(-1);
        tv.measure(0, 0);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        //        Bitmap bitmapCode = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //        Canvas c = new Canvas();
        //        Paint p = new Paint();
        //        p.setTextSize(size);
        //        p.setColor(Color.BLACK);
        //        c.drawText(contents, 0, height, p);
        return bitmapCode;
    }

    public static Bitmap encode2dAsBitmap(String contents, int desiredWidth, int desiredHeight, int barType) {
        if (barType == 1) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (barType == 2) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        }

        Bitmap barcodeBitmap = null;

        try {
            barcodeBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
        } catch (WriterException var6) {
            var6.printStackTrace();
        }

        return barcodeBitmap;
    }

    private static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
        if (first != null && second != null && fromPoint != null) {
            Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(), first.getHeight() + second.getHeight(), Bitmap.Config
                    .ARGB_4444);
            Canvas cv = new Canvas(newBitmap);
            cv.drawBitmap(first, 0.0F, 0.0F, (Paint) null);
            cv.drawBitmap(second, fromPoint.x, fromPoint.y, (Paint) null);
//            cv.save(Canvas.ALL_SAVE_FLAG);api-28被删除
            cv.save();
            cv.restore();
            return newBitmap;
        } else {
            return null;
        }
    }

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) throws
            WriterException {
        boolean WHITE = true;
        int BLACK = -16777216;
        HashMap hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new HashMap(2);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, format, desiredWidth, desiredHeight, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];

        for (int bitmap = 0; bitmap < height; ++bitmap) {
            int offset = bitmap * width;

            for (int x = 0; x < width; ++x) {
                pixels[offset + x] = result.get(x, bitmap) ? -16777216 : -1;
            }
        }

        Bitmap var16 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var16.setPixels(pixels, 0, width, 0, 0, width, height);
        return var16;
    }

    public static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); ++i) {
            if (contents.charAt(i) > 255) {
                return "UTF-8";
            }
        }

        return null;
    }

    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        byte quality = 100;
        FileOutputStream stream = null;

        try {
            stream = new FileOutputStream("/sdcard/" + filename);
        } catch (FileNotFoundException var6) {
            var6.printStackTrace();
        }

        return bmp.compress(format, quality, stream);
    }
}
