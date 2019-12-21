package utils.btprint.suofang;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.List;

import utils.btprint.BtHelper;
import utils.btprint.SPrinter2;
import utils.btprint.utils.PrinterUtils2;

/**
 * Created by 张建宇 on 2019/12/17.
 */
public class SuoFangPrinter extends SPrinter2 {
    public SuoFangPrinter(BtHelper helper) {
        super(helper);
    }

    private int maxWidth = PrinterUtils2.width;
    private int maxHeight = 215;

    Bitmap bitmap;
    Canvas canvas ;
    int y = 0;
    int x = 0;
    int textSzie = 20;
    int textWidth = 0;
    float lineHeight = 18;
    float picMargin = 2;

    private Paint mPaint;
    private int marginVetical = 5;
    private int marginHorizontal = 8;
    private int labelSize = 8;

    public static boolean isSuoFang(String devName) {
        if (devName == null) {
            return false;
        }
        if (devName.startsWith("T50") || devName.startsWith("T80")) {
            return true;
        }
        return false;
    }
    public synchronized void init(){
        mPaint = new Paint();
        mPaint.setTextSize(textSzie);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        //        fontMetrics.bottom - fontMetrics.top + fontMetrics.leading;
        getLineHeight();
        y = marginVetical;
        x = marginHorizontal;
        bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
    }

    @Override
    public synchronized boolean initPrinter() {
        init();
        return super.initPrinter();
    }


    @Override
    public synchronized void printBarCode(String code, int lablePlace, int width, int height) {
        float rate = width / 1f / (maxWidth - 2 * marginVetical);
        int realW = (int) (maxWidth * 0.8);
        int realH = 50;
        int picY = (int) (y + picMargin);
        Bitmap bitmap = null;

        if (width == 1 && height == 43) {
            bitmap = PrinterUtils2.newBarCode(helper.mContext, code, lablePlace,labelSize, realW, realH);
            canvas.drawBitmap(bitmap, x, picY, mPaint);
        } else  if (width == 1 && height == 80) {
            realH = realH * 2;
            bitmap = PrinterUtils2.newBarCode(helper.mContext, code, lablePlace, labelSize,realW, realH);
            canvas.drawBitmap(bitmap, x, picY, mPaint);
        } else if (height > maxHeight || width > maxWidth) {
            printText("暂不支持此条码尺寸");
            newLine();
        }
        if (bitmap != null) {
            y = (int) (picY + bitmap.getHeight() + picMargin);
        }
    }


    public synchronized boolean printTextByLength(String[] str, int[] len) {
        int tempX = x;
        int max = ( maxWidth - 2 * marginHorizontal )/ 2;
        for (int i = 0; i < str.length; i++) {
            String tstr = str[i];
            char[] chars = tstr.toCharArray();
            Rect boundsAll = new Rect();
            mPaint.getTextBounds(tstr, 0, tstr.length(), boundsAll);

            if (boundsAll.width() > max) {
                for (int k = 0; k < tstr.length(); k++) {
                    Rect bounds = new Rect();
                    mPaint.getTextBounds(tstr, 0, k, bounds);
                    if (bounds.width() > max) {
                        tstr = tstr.substring(0, k);
                        tstr += "..";
                        break;
                    }
                }
            } else {
            }
            printText(tstr);
            x = x + max;
        }
        x = tempX;
        return true;
    }

    @Override
    protected synchronized boolean printBitmap(Bitmap bitmap) {
        List<byte[]> bmp = PrinterUtils2.PrintBitmap(bitmap);
        for (byte[] b : bmp) {
            write(b);
        }
        return true;
    }

    @Override
    public synchronized boolean setZiTiSize(int size) {
        if (size == 0) {
            mPaint.setTextSize(textSzie - 2);
        } else if (size == 1) {
            mPaint.setTextSize(textSzie);
        }
        getLineHeight();
        return true;
    }

    private void getLineHeight() {
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        lineHeight = metrics.bottom - metrics.ascent + metrics.leading;
    }

    public int getTopY() {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        int top = (int) (-1 * fontMetrics.ascent);
        return top;
    }

    @Override
    public synchronized boolean printText(String content) {
        Rect bounds = new Rect();
        mPaint.getTextBounds(content, 0, content.length(), bounds);
        int textWidth = bounds.width();
        if (textWidth + x > maxWidth) {
            Log.e("zjy", "SuoFangPrinter->printText(): text tooLong==" + content);
        } else {
        }
        int topY = getTopY();
        canvas.drawText(content, x, y + topY, mPaint);
//        y = (int) (y + lineHeight);
        return true;
    }

    @Override
    public synchronized boolean newLine() {
        y += lineHeight;
        return true;
    }


    @Override
    public synchronized void  commit() {
        if (bitmap != null) {
//            PrinterUtils2.addBorder(bitmap);
            List<byte[]> bytes = PrinterUtils2.PrintBitmap(bitmap, PrinterUtils2.algn_right);
            bitmap.recycle();
            bitmap = null;
            canvas = null;
            for (byte[] temp : bytes) {
                write(temp);
            }
            cutPaper();
        }
    }

    @Override
    public synchronized void cutPaper() {
        write(PrinterUtils2.feedAndCut2());
    }
}
