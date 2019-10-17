package com.android.dev;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BarcodeAPI {

    private static final String TAG = "BarcodeAPI";
    public static BarcodeAPI instance = null;
    public Handler m_handler = null;
    public static final int BARCODE_READ = 10;

    public static synchronized BarcodeAPI getInstance() {
        if (instance == null) {
            instance = new BarcodeAPI();
        }
        return instance;
    }

    private BarcodeAPI() {
    }

    // 扫描相关
    public native void open();

    public native void scan();

    public native void stopScan();

    public native void setScanMode(boolean isContinue);

    /**
     当前模式,是否是连扫
     @return 1:连扫
     */
    public native int isContinueModel();

    public native void close();

    /**
     获取设备唯一ID
     @return
     */
    public native String getMachineCode();

    public void setScanResults(String info) {
        if (info == null) {
            return;
        }
        Log.i("barcode", info);
        if (m_handler != null) {
            Message m = Message.obtain(m_handler, BARCODE_READ);
            m.obj = info;
            m_handler.sendMessage(m);
        }

    }

    static {
        Log.e("zjy", "Barcode()->static initializer(): ==");
        System.loadLibrary("Barcode");
    }
}
