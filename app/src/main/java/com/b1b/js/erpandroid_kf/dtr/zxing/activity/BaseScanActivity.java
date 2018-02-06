package com.b1b.js.erpandroid_kf.dtr.zxing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.dev.BarcodeAPI;

import utils.CameraScanInterface;

public abstract class BaseScanActivity extends AppCompatActivity {
    protected Handler scanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BarcodeAPI.BARCODE_READ:
                    if (msg.obj != null) {
                        Log.e("zjy", "BaseScanActivity->handleMessage(): code==" + msg.obj.toString());
                        resultBack(msg.obj.toString());
                    }
                    break;
            }
        }
    };
    boolean hasScanBtn = false;
    boolean hasInit = false;
    protected BarcodeAPI scanTool = null;
    CameraScanInterface cScanInterface;


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
    public static final int REQ_CODE = 400;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            if(cScanInterface!=null)
            cScanInterface.getCameraScanResult(result);
        }
    }

    public void startScanActivity() {
        startActivityForResult(new Intent(this, CaptureActivity.class), REQ_CODE);
    }
    public void setcScanInterface(CameraScanInterface cScanInterface) {
        this.cScanInterface = cScanInterface;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanTool != null) {
            scanTool.close();
        }
    }
}
