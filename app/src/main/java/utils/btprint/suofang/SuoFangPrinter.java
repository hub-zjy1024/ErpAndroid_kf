package utils.btprint.suofang;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import utils.btprint.BtHelper;
import utils.btprint.SPrinter2;
import utils.btprint.utils.PrinterUtils2;

/**
 * Created by 张建宇 on 2019/12/17.
 */
public class SuoFangPrinter extends SPrinter2 implements Serializable {
    public SuoFangPrinter(BtHelper helper) {
        super(helper);
    }

    private int maxWidth = PrinterUtils2.width;
    private int defHeight = 30 * 8;
    private int maxHeight = defHeight;

    Bitmap bitmap;
    Canvas canvas;
    int y = 0;
    int x = 0;
    int textSzie = 20;
    int textWidth = 0;
    float lineHeight = 18;
    float picMargin = 2;

    private Paint mPaint;
    private int marginVetical = 5;
    private int marginHorizontal = 8;
    private int labelSize = 22;
    public static int MODE_LIANXU = 1;
    public static int MODE_Dur = 0;
    public static int VeticalMargin_lianxu = 15;
    public static int VeticalMargin_continue = 5;
    private int mode = MODE_LIANXU;
    public static final int maxOffset = 1 * 8;

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
        if (mode == MODE_LIANXU) {
            marginVetical = VeticalMargin_lianxu;
            maxHeight = defHeight + 2 * marginVetical;
        } else {
            marginVetical = VeticalMargin_continue;
            maxHeight = defHeight - maxOffset;
        }
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
//        测试条码长度
        int samples = 7;
        int oldWidth = (int) (312 * 0.8f);
        int sigleWidth = (int) (312 * 0.8f / samples);
        int realW = (int) (maxWidth * 0.8);
        if (code != null) {
            realW = oldWidth;
            if (realW >= maxWidth) {
                realW = maxWidth - marginHorizontal * 2;
            }
        }
        int realH = 50;
        int picY = (int) (y + picMargin);
        Bitmap bitmap = null;

        if (width == 1 && height == 43) {
            bitmap = PrinterUtils2.newBarCode( code, realW, realH);
            canvas.drawBitmap(bitmap, x, picY, mPaint);
        } else  if (width == 1 && height == 80) {
            realH = realH * 2;
            bitmap = PrinterUtils2.newBarCode( code,realW, realH);
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
            if (mode == MODE_LIANXU) {
                Log.d("zjy", SuoFangPrinter.class.getName() + "->commit(): ==use lianxu");
            } else if (mode == MODE_Dur) {
                cutPaper();
            }
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void flush() {
        Log.d("zjy", "SuoFangPrinter->flush(): start flush==");
        helper.flush();
    }
    private void jumpLine2(int lines) {
        byte[] mdata = new byte[]{27, 100  , (byte) lines};
        write(mdata);
//        1B 61 01：表示进纸一行。
//        byte[] mdata = new byte[]{27, 0x64  , (byte) lines};
//        write(mdata);
        Log.e("zjy", "SuoFangPrinter->jumpLine2(): ==");
    }
    private void jumpLine(int lines) {
        //ESC j n
        if (lines > 255) {
            lines = 255;
        }
//        byte[] mdata = new byte[]{27, 74, (byte) lines};
//                write(mdata);
        for (int i = 0; i < lines; i++) {
            byte[] mdata = new byte[]{10};
            write(mdata);
        }
    }

    @Override
    public synchronized void cutPaper() {
        write(PrinterUtils2.feedAndCut2());
    }

    @Override
    public Bitmap preView() {
        PrinterUtils2.addBorder(bitmap);
        return bitmap;
    }

}
