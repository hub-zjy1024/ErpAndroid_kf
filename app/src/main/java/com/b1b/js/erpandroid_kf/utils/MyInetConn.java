package com.b1b.js.erpandroid_kf.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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

public class MyInetConn {
    private static int[] ZHISTATE = new int[]{1, 2, 1};
    private static int[] CMD_INIT = new int[]{27, 64};
    private static int[] CMD_FONT_1 = new int[]{27, 64};
    private static int[] CMD_P_STATE = new int[]{27, 64};
    private static int[] CMD_PRINT_GO = new int[]{27, 100, 0};
    private static int[] CMD_COD128 = new int[]{29, 107, 74};
    public static final byte ESC = 27;//换码
    public static final byte FS = 28;//文本分隔符
    public static final byte GS = 29;//组分隔符
    public static final byte DLE = 16;//数据连接换码
    public static final byte EOT = 4;//传输结束
    public static final byte ENQ = 5;//询问字符
    public static final byte LF = 10;//打印并换行（水平定位）
    public static final int BARCODE_FLAG_TOP = 1;//走纸控制
    public static final byte BARCODE_FLAG_BOTTOM = 2;//走纸控制
    public static final byte BARCODE_FLAG_BOTH = 3;//走纸控制
    public static final byte BARCODE_FLAG_NONE = 0;//走纸控制
    public Socket mSocket = new Socket();
    private OutputStream mOut;
    int imageWidth = 40;

    public MyInetConn() {
        SocketAddress s = new InetSocketAddress("192.168.199.200", 9100);
        try {
            mSocket.connect(s, 5 * 1000);
            mOut = mSocket.getOutputStream();
            Log.e("zjy", "MyInetConn->conn(): connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void printTextLn(String data) throws IOException {
        if (mOut != null) {
            mOut.write(data.getBytes("GBK"));
        }
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
            float resizedBitmap2 = (float) w / (float) width;
            float canvas1 = (float) h / (float) height + 24.0F;
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
        mOut.write(new byte[]{0x1d, 0x6B, (byte) 73});
        int len = code.length();
        mOut.write(len + 2);
        //选择字符集CODEB
        mOut.write(new byte[]{(byte) 123, (byte) 66});
        //打印条码内容
        mOut.write(code.getBytes());
        //恢复条码高度
        mOut.write((byte) 29);
        mOut.write((byte) 104);
        mOut.write((byte) 1);
    }

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

    public synchronized void cutPaper() throws IOException {
        //        byte[] cutCmd=new byte[]{0x1D,0x56,0x0,0x48};
        //        byte[] cutCmd = new byte[]{(byte) 29, (byte) 86, (byte) 0};
        byte[] cutCmd = new byte[]{(byte) 29, (byte) 86, (byte) 66, (byte) 2};
        //        mOut.printText(go);
        mOut.write(cutCmd);
        //        mOut.printText(new byte[]{});
    }

    public void newLine() throws IOException {
        //        byte[] cmd = new byte[]{(byte) 27, (byte) 100, (byte) 5};
        byte[] cmd = new byte[]{(byte) 10};
        mOut.write(cmd);
    }

    public void initPrinter() throws IOException {
        if (mOut != null) {
            synchronized (mOut) {
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
        if (num >= 1 && num <= 8) {
            byte[] cmd = new byte[]{(byte) 29, (byte) 33, (byte) num};
            mOut.write(cmd);
        }

    }

    public void setFont(int size) throws IOException {
        byte[] cmd;
        switch (size) {
            case 1:
                //字符倍宽倍高
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 17};
                break;
            case 0:
                //字符倍取消倍宽
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 0};
                break;
            default:
                cmd = new byte[]{(byte) 29, (byte) 33, (byte) 0};
                break;
        }
        mOut.write(cmd);
    }

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
                Log.e("zjy", "MyInetConn->getResponse(): res==" + b);

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
