package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.scancode.zbar.ZbarScanActivity;

public  class KyExpressAcitivity extends BaseScanActivity {
    private static final String TAG = "Zbar_Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ky_express_acitivity);
        startScanActivity();
    }

    public void startScanActivity() {
        startActivityForResult(new Intent(this, ZbarScanActivity.class), REQ_CODE);
    }

    public void startScanActivity(int code) {
        startActivityForResult(new Intent(this, ZbarScanActivity.class), code);
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }
}
