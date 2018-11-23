package com.b1b.js.erpandroid_kf.dtr.zxing.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 张建宇 on 2018/11/23.
 */
public abstract class CamScanActivity extends AppCompatActivity {
    public static final int REQ_CODE = 400;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            getCameraScanResult(result, requestCode);
            if (requestCode == REQ_CODE) {
                getCameraScanResult(result);
            }
        }
    }

    public void startScanActivity() {
        startActivityForResult(new Intent(this, CaptureActivity.class), REQ_CODE);
    }

    public void startScanActivity(int code) {
        startActivityForResult(new Intent(this, CaptureActivity.class), code);
    }

    public abstract void getCameraScanResult(String result, int code) ;

    public abstract void getCameraScanResult(String result) ;
}
