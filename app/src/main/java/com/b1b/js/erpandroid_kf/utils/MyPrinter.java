package com.b1b.js.erpandroid_kf.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 Created by 张建宇 on 2017/4/28. */

public class MyPrinter {
    private static int[] CMD_INIT = new int[]{27, 64};
    private static int[] CMD_PRINT_GO = new int[]{27, 100, 0};
    private static int[] CMD_COD128 = new int[]{29, 107, 74};
    public static final byte ESC = 27;//换码
    public static final byte FS = 28;//文本分隔符
    public static final byte GS = 29;//组分隔符
    public static final byte DLE = 16;//数据连接换码
    public static final byte EOT = 4;//传输结束
    public static final byte ENQ = 5;//询问字符
    private static final byte LF = 10;//打印并换行（水平定位）
    public static final int BARCODE_FLAG_TOP = 1;
    public static final byte BARCODE_FLAG_BOTTOM = 2;
    public static final byte BARCODE_FLAG_BOTH = 3;
    public static final byte BARCODE_FLAG_NONE = 0;
    private Socket mSocket = new Socket();
    private OutputStream mOut;
    private final Object obj = new Object();
    int imageWidth = 40;

    public MyPrinter(String address) {
        SocketAddress s;
        if (address == null) {
           s = new InetSocketAddress("192.168.199.200", 9100);
//            s = new InetSocketAddress("192.168.9.101", 0);
        } else {
            s = new InetSocketAddress(address, 9100);
        }
        try {
            mSocket.connect(s, 5 * 1000);
            mOut = mSocket.getOutputStream();
            Log.e("zjy", "MyPrinter->conn(): connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OutputStream getmOut() {
        return mOut;
    }


    public synchronized void printText(String data) throws IOException {
        if (mOut != null) {
            mOut.write(data.getBytes("GBK"));
        }
    }

    public synchronized void printByte(byte[] data) throws IOException {
        if (mOut != null) {
            mOut.write(data);
        }
    }

    public synchronized void printTextLn(String data) throws IOException {
        printText(data);
        newLine();
    }

    public void nextLine(int lineNum) throws IOException {
        byte[] result = new byte[lineNum];
        for (int i = 0; i < lineNum; i++) {
            result[i] = LF;
        }
        mOut.write(result);
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > w) {
            float widthScale = (float) w / (float) width;
            float canvas1 = (float) h / (float) height + 24.0F;
            Matrix matrix = new Matrix();
            matrix.postScale(widthScale, widthScale);
            matrix.postScale(1, 1);
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } else {
            Bitmap resizedBitmap = Bitmap.createBitmap(w, height + 24, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(resizedBitmap);
            Paint paint = new Paint();
            canvas.drawColor(-1);
            canvas.drawBitmap(bitmap, (float) ((w - width) / 2), 0.0F, paint);
            return resizedBitmap;
        }
    }

    public void printCode(String code, int flag) throws IOException {
        //            29    76   nL   nH
        if (mOut == null) {
            return;
        }
        //        byte[] cmd_title = printBarCodeTitle(flag);
        //        mOut.write(cmd_title);

        //设置左边距和右边距
        mOut.write(new byte[]{(byte) 29, (byte) 80, (byte) 20, (byte) 0});
        mOut.write(new byte[]{(byte) 29, (byte) 76, (byte) 1, (byte) 0});
        //设置条码高度
        mOut.write((byte) 29);
        mOut.write((byte) 104);
        mOut.write((byte) 70);
        //设置条码宽度
        mOut.write((byte) 29);
        mOut.write((byte) 119);
        mOut.write((byte) 3);
        //选择COD128条码格式进行打印
        mOut.write(new byte[]{0x1D, 0x6B, (byte) 73});
        byte[] bytes = code.getBytes("utf-8");
        //数据长度=条码长度+加上字符集选择指令的长度
        mOut.write((byte) (bytes.length + 2));
        //选择字符集CODEB
        mOut.write(new byte[]{(byte) 123, (byte) 66});
        //打印条码内容
        mOut.write(bytes);
        //恢复条码高度
        mOut.write((byte) 29);
        mOut.write((byte) 104);
        mOut.write((byte) 1);
    }

    /**
     打印条码内容，暂时不可用
     @param flag
     @return
     */
    public byte[] printBarCodeTitle(int flag) {
        byte[] cmd;
        switch (flag) {
            case BARCODE_FLAG_NONE:
                cmd = new byte[]{(byte) 29, (byte) 72, (byte) 0, (byte) 48};
                break;
            case BARCODE_FLAG_TOP:
                cmd = new byte[]{(byte) 29, (byte) 72, (byte) 1, (byte) 49};
                break;
            case BARCODE_FLAG_BOTTOM:
                cmd = new byte[]{(byte) 29, (byte) 72, (byte) 2, (byte) 50};
                break;
            case BARCODE_FLAG_BOTH:
                cmd = new byte[]{(byte) 29, (byte) 72, (byte) 3, (byte) 51};
                break;
            default:
                cmd = new byte[]{(byte) 29, (byte) 72, (byte) 0, (byte) 48};
                break;
        }
        return cmd;
    }

    public synchronized boolean cutPaper() throws IOException {
        //        byte[] cutCmd=new byte[]{0x1D,0x56,0x0,0x48};
        //        byte[] cutCmd = new byte[]{(byte) 29, (byte) 86, (byte) 0};
        byte[] cutCmd = new byte[]{(byte) 29, (byte) 86, (byte) 66, (byte) 2};
        //        mOut.printText(go);
        if( mOut!= null){
            mOut.write(cutCmd);
            return true;
        }
        return false;
    }

    public void newLine() throws IOException {
        //        byte[] cmd = new byte[]{(byte) 27, (byte) 100, (byte) 5};
        byte[] cmd = new byte[]{(byte) 10};
        if (mOut != null) {
            mOut.write(cmd);
        }
    }

    /**
     初始化打印机，还原初始设置，清除格式
     @throws IOException
     */
    public void initPrinter() throws IOException {
        if (mOut != null) {
            synchronized (obj) {
                //        byte[] cmd = new byte[]{(byte) 27, (byte) 100, (byte) 5};
                byte[] cmd = intArray2ByteArray(CMD_INIT);
                mOut.write(cmd);
            }
        }

    }

    /**
     @param num 1为正常宽度，最大为8
     @throws IOException
     */
    public void setCharWidth(int num) throws IOException {
        if (num >= 1 && num <= 8) {
            num = num - 1;
            byte[] cmd = new byte[]{(byte) 29, (byte) 33, (byte) (num*16)};
            mOut.write(cmd);
        }
    }

    /**
     @param num  1为正常高度，最大为8
     @throws IOException
     */
    public void setCharHeight(int num) throws IOException {
        if (mOut == null) {
            return;
        }
        if (num >= 1 && num <= 8) {
            byte[] cmd = new byte[]{(byte) 29, (byte) 33, (byte) num};
            mOut.write(cmd);
        }

    }

    /**
     同时放大宽高
     @param size
     @throws IOException
     */
    public void setFont(int size) throws IOException {
        if (mOut == null) {
            return;
        }
        byte[] cmd;
        switch (size) {
            case 1:
                //字符2倍宽倍高
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 17};
                break;
            case 0:
                //字符倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 0};
                break;
            case 2:
                //字符3倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 34};
                break;
            case 3:
                //字符4倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 51};
                break;
            case 4:
                //字符5倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 68};
                break;
            case 5:
                //字符6倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 85};
                break;
            case 6:
                //字符7倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 102};
                break;
            case 7:
                //字符8倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 119};
                break;
            default:
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 0};
                break;
        }
        mOut.write(cmd);
    }

    /**
     @param x 横向移动单位
     @param y 纵向移动单位
     @throws IOException
     */
    public void setDistance(int x, int y) throws IOException {
        mOut.write(new byte[]{(byte) 29, (byte) 80, (byte) x, (byte) y,});
    }

    public byte[] getResponse() {
        //        byte[] cmd = new byte[]{(byte) 29, (byte) 144, (byte) 1, (byte) 49};
        byte[] cmd = new byte[]{(byte) 16, (byte) 4, (byte) 4};
        try {
            mOut.write(cmd);
            InputStream in = mSocket.getInputStream();
            byte[] res = new byte[in.available()];
            in.read(res);
            for (byte b : res) {
                Log.e("zjy", "MyPrinter->getResponse(): res==" + b);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printAndGo(int lines) throws IOException {
        byte[] cmd = intArray2ByteArray(CMD_PRINT_GO);
        cmd[2] = (byte) lines;
        mOut.write(cmd);
    }
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    public void printBitmap(Bitmap bmp) throws IOException {
//        29  118   48   m    xL    xH    yL   yH   d1......dk
        //设置字符行间距为n点行
        //byte[] data = new byte[] { 0x1B, 0x33, 0x00 };
        String send = "" + (char) (27) + (char) (51) + (char) (0);
        byte[] data = new byte[send.length()];
        for (int i = 0; i < send.length(); i++) {
            data[i] = (byte) send.charAt(i);
        }
        mOut.write(data);
        data[0] = (byte) ('0');
        data[1] = (byte) '0';
        data[2] = (byte) '0';    // Clear to Zero.
        int pixelColor;
        //ESC * m nL nH d1…dk   选择位图模式
        // ESC * m nL nH
        byte[] escBmp = new byte[]{0x1B, 0x2A, 0x00, 0x00, 0x00};
        escBmp[2] = (byte) 21;
        //nL, nH
        escBmp[3] = (byte) (bmp.getWidth()% 256);
        escBmp[4] = (byte) (bmp.getWidth() / 256);
        //循环图片像素打印图片
        //循环高
        for (int i = 0; i < (bmp.getHeight() / 24 + 1); i++) {
            //设置模式为位图模式
            mOut.write(escBmp);
            //循环宽
            for (int j = 0; j < bmp.getWidth(); j++) {
                for (int k = 0; k < 24; k++) {
                    if (((i * 24) + k) < bmp.getHeight())  // if within the BMP size
                    {
                        pixelColor = bmp.getPixel(j, (i * 24) + k);
                        if (pixelColor == 0) {
                            data[k / 8] += (byte) (128 >> (k % 8));
                        }
                    }
                }
                //一次写入一个data，24个像素
                mOut.write(data);
                data[0] = (byte)0;
                data[1] = (byte)0;
                data[2] = (byte)0;    // Clear to Zero.
            }
            //换行，打印第二行
            byte[] data2 = {0xA};
            mOut.write(data2);
        } // data
        mOut.write("\n\n".getBytes());
    }

    public byte[] intArray2ByteArray(int[] array) {
        byte[] cmd;
        if (array != null) {
            cmd = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                cmd[i] = (byte) array[i];
            }
        }
        cmd = new byte[1];
        return cmd;
    }

    /**
     字体变大为标准的n倍
     @param num
     @return
     */
    public static byte[] setFontAt(int num) {
        byte realSize = 0;
        switch (num) {
            case 1:
                realSize = 0;
                break;
            case 2:
                realSize = 17;
                break;
            case 3:
                realSize = 34;
                break;
            case 4:
                realSize = 51;
                break;
            case 5:
                realSize = 68;
                break;
            case 6:
                realSize = 85;
                break;
            case 7:
                realSize = 102;
                break;
            case 8:
                realSize = 119;
                break;
        }
        byte[] result = new byte[3];
        result[0] = 29;
        result[1] = 33;
        result[2] = realSize;
        return result;
    }


    public synchronized void close() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
