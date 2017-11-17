package utils.btprint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.UnsupportedEncodingException;

/**
 Created by 张建宇 on 2017/4/20. */

public abstract class MyPrinterParent {
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_SCAN_FINISHED = 3;
    public static final int STATE_OPEN = 4;
    public static final int STATE_FAIL = 5;
    public static final int STATE_SUCCESS = 6;
    public static final int STATE_STARTSCAN = 7;
    private int mState = STATE_DISCONNECTED;
    byte[] CMD_CHECK_TYPE = new byte[]{(byte) 27, (byte) 43};
    /**
     水平制表符
     */
    byte[] CMD_HORIZONTAL_TAB = new byte[]{(byte) 9};
    /**
     换行
     */
    byte[] CMD_NEWLINE = new byte[]{(byte) 10};
    byte[] CMD_PRINT_CURRENT_CONTEXT = new byte[]{(byte) 13};
    byte[] CMD_INIT_PRINTER = new byte[]{(byte) 27, (byte) 64};
    byte[] CMD_UNDERLINE_ON = new byte[]{(byte) 28, (byte) 45, (byte) 1};
    byte[] CMD_UNDERLINE_OFF = new byte[]{(byte) 28, (byte) 45, (byte) 0};
    byte[] CMD_Blod_ON = new byte[]{(byte) 27, (byte) 69, (byte) 1};
    byte[] CMD_BLOD_OFF = new byte[]{(byte) 27, (byte) 69, (byte) 0};
    byte[] CMD_SET_FONT_24x24 = new byte[]{(byte) 27, (byte) 77, (byte) 0};
    byte[] CMD_SET_FONT_16x16 = new byte[]{(byte) 27, (byte) 77, (byte) 1};
    byte[] CMD_FONTSIZE_NORMAL = new byte[]{(byte) 29, (byte) 33, (byte) 0};
    byte[] CMD_FONTSIZE_DOUBLE_HIGH = new byte[]{(byte) 29, (byte) 33, (byte) 1};
    byte[] CMD_FONTSIZE_DOUBLE_WIDTH = new byte[]{(byte) 29, (byte) 33, (byte) 16};
    byte[] CMD_FONTSIZE_DOUBLE = new byte[]{(byte) 29, (byte) 33, (byte) 17};
    byte[] CMD_ALIGN_LEFT = new byte[]{(byte) 27, (byte) 97, (byte) 0};
    byte[] CMD_ALIGN_MIDDLE = new byte[]{(byte) 27, (byte) 97, (byte) 1};
    byte[] CMD_ALIGN_RIGHT = new byte[]{(byte) 27, (byte) 97, (byte) 2};
    byte[] CMD_BLACK_LOCATION = new byte[]{(byte) 12};
    public static final int imageWidth = 48;

    public abstract void open();

    public abstract void close();

    public abstract void scan();

    public abstract void connect(String var1);

    public abstract boolean write(byte[] var1);

    public abstract boolean isOpen();

    public abstract boolean newLine();

    public int getState() {
        return mState;
    }

    public void setState(int var1) {
        mState = var1;
    }

    public synchronized boolean printText(String textStr) {
        return write(getText(textStr));
    }

    public synchronized boolean printTextLn(String textStr) {
        write(getText(textStr));
        return newLine();
    }

    public synchronized boolean setZiTiSize(int size) {
        byte[] small = new byte[]{0x1B, 0x4D, 0x01};
        byte[] large = new byte[]{0x1B, 0x4D, 0x00};
        if (size == 0) {
            return write(small);
        } else {
            return write(large);
        }
    }

    public synchronized boolean printTextByLength(String[] str, int[] len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            String tstr = str[i];
            char[] chars = tstr.toCharArray();
            int max = len[i];
            int blank = max - chars.length;
            if (blank <= 0) {
                if (max != 0) {
                    tstr = tstr.substring(0, max);
                }
            } else {
                for (int j = 0; j < blank; j++) {
                    tstr += " ";
                }
            }
            builder.append(tstr);
        }
        return write(getText(builder.toString()));
    }

    protected boolean printBitmap(Bitmap bitmap) {
        write(getImage(bitmap));
        return newLine();
    }

    private byte[] getImage(Bitmap bitmap) {
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        bitmap = resizeImage(bitmap, imageWidth * 8, mHeight);
        byte[] bt = BitmapUtils.getBitmapData(bitmap);
        bitmap.recycle();
        return bt;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > w) {
            float resizedBitmap2 = (float) w / (float) width;
            Matrix paint1 = new Matrix();
            paint1.postScale(resizedBitmap2, resizedBitmap2);
            Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmap, 0, 0, width, height, paint1, true);
            return resizedBitmap1;
        } else {
            Bitmap resizedBitmap = Bitmap.createBitmap(w, height + 24, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(resizedBitmap);
            Paint paint = new Paint();
            canvas.drawColor(-1);
            canvas.drawBitmap(bitmap, (float) ((w - width) / 2), 0.0F, paint);
            return resizedBitmap;
        }
    }

    public boolean printQrcode(Bitmap bitmap) {
        //        if (msgSize > 0) {
        //            byte[] btcmd = new byte[]{(byte) 31, (byte) 17, (byte) (msgSize >>> 8), (byte) (msgSize & 255)};
        //            write(btcmd);
        //        }
        return printBitmap(bitmap);
    }

    private byte[] getText(String textStr) {
        byte[] send1;
        try {
            send1 = textStr.getBytes("GBK");
        } catch (UnsupportedEncodingException var4) {
            send1 = textStr.getBytes();
        }
        return send1;
    }

    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];

        for (int i = 0; i < hexStrings.length; ++i) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }


}
