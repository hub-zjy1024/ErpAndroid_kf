package com.b1b.js.erpandroid_kf.scancode.zbar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.b1b.js.erpandroid_kf.scancode.zbar.view.MZbarScannerView;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by 张建宇 on 2019/3/20.
 */
public class ZbarScanActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    public static int REQ_CODE = 200;

    private MZbarScannerView mScannerView;

    public static final List<BarcodeFormat> QRForamter = new ArrayList<BarcodeFormat>();
    public static final List<BarcodeFormat> BARCODE_FORMATS = new ArrayList<BarcodeFormat>();
static {
    BARCODE_FORMATS.add(BarcodeFormat.CODABAR);
    BARCODE_FORMATS.add(BarcodeFormat.CODE39);
    BARCODE_FORMATS.add(BarcodeFormat.CODE93);
    BARCODE_FORMATS.add(BarcodeFormat.CODE128);
    BARCODE_FORMATS.add(BarcodeFormat.DATABAR);
    BARCODE_FORMATS.add(BarcodeFormat.DATABAR_EXP);
    BARCODE_FORMATS.add(BarcodeFormat.UPCE);
    BARCODE_FORMATS.add(BarcodeFormat.UPCA);
    BARCODE_FORMATS.add(BarcodeFormat.EAN8);
    BARCODE_FORMATS.add(BarcodeFormat.EAN13);
    BARCODE_FORMATS.add(BarcodeFormat.I25);
//    BARCODE_FORMATS.add(BarcodeFormat.ISBN10);
//    BARCODE_FORMATS.add(BarcodeFormat.ISBN13);
    BARCODE_FORMATS.add(BarcodeFormat.PARTIAL);


    QRForamter.add(BarcodeFormat.PDF417);
    QRForamter.add(BarcodeFormat.QRCODE);

}
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new MZbarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        mScannerView.setFormats(BARCODE_FORMATS);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e("zbarscan", String.format("type=%s value=%s", rawResult
                .getBarcodeFormat()
                .getName(), rawResult.getContents())); // Prints scan results
        // If you would like to resume scanning, call this method below:
        Intent intent = getIntent();
        intent.putExtra("result", rawResult.getContents());
        setResult(RESULT_OK, intent);
        finish();
        //        mScannerView.resumeCameraPreview(this);
    }
}
