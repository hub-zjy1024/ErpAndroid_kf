package com.b1b.js.erpandroid_kf.scancode.zxing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoWithScanActivity;
import com.b1b.js.erpandroid_kf.scancode.zxing.view.ZxingCodeScannerView;

/**
 * Created by 张建宇 on 2019/3/22.
 */
public class ZxingScanActivcity extends SavedLoginInfoWithScanActivity implements ZxingCodeScannerView.ZxingResultHandler{
    public static int REQ_CODE = 200;

    private ZxingCodeScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZxingCodeScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        mScannerView.setFormat(ZxingCodeScannerView.BARCODE_MODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setmResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    @Override
    public void handleResult(com.google.zxing.Result result, Bundle bundle) {
        String txtResult = result.getText();
        Log.e("zbar_zxing", String.format("type=%s value=%s",
                result.getBarcodeFormat().name(), result.getText()));
        // Prints scan results
        // If you would like to resume scanning, call this method below:
        Intent intent = new Intent();
        intent.putExtra("result",txtResult);
        setResult(RESULT_OK, intent);
        finish();
    }
}
