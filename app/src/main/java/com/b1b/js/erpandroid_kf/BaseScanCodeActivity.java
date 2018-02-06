package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;

/**
 Created by 张建宇 on 2018/1/30. */

public abstract class BaseScanCodeActivity extends AppCompatActivity {
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
            getCodeResult(result);
        }
    }

    public void startScanActivity() {
        startActivityForResult(new Intent(this, CaptureActivity.class), REQ_CODE);
    }

    public abstract void getCodeResult(String result);

}
