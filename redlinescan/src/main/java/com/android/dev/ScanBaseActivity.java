package com.android.dev;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.dev.handler.NoLeakHandler;

public abstract class ScanBaseActivity extends Activity implements NoLeakHandler.NoLeakCallback {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BarcodeAPI.BARCODE_READ:
                if (msg.obj != null) {
                    Log.e("zjy", "ScanBaseActivity->handleMessage(): code==" + msg.obj.toString());
                    resultBack(msg.obj.toString());
                }
                break;
        }
    }

    protected Handler scanHandler = new NoLeakHandler(this);
    boolean hasScanBtn = false;
    boolean hasInit = false;
    protected BarcodeAPI scanTool = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(getLayoutResId());
    }
    public abstract int getLayoutResId();

    public abstract void resultBack(String result);

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_INFO:
            case KeyEvent.KEYCODE_MUTE:
                hasScanBtn = true;
                if (!hasInit) {
                    scanTool = BarcodeAPI.getInstance();
                    scanTool.open();
                    scanTool.m_handler = scanHandler;
                    hasInit = true;
                }
                scanTool.scan();
                return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanTool != null) {
            scanTool.close();
        }
    }
}
