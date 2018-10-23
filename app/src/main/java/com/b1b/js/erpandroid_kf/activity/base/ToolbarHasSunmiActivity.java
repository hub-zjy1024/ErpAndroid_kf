package com.b1b.js.erpandroid_kf.activity.base;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.dev.BarcodeAPI;
import com.sunmi.scanner.ScanController;

/**
 * Created by 张建宇 on 2019/7/18.
 */
public class ToolbarHasSunmiActivity extends ToobarSaveWithScanAc implements ScanController.ScanListener {
    private ScanController sunmiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void init() {
        super.init();
        scanTool = BarcodeAPI.getInstance();
        sunmiController = new ScanController(mContext, this);
        sunmiController.open();
    }

    public void openSunmiScan(){
        sunmiController.open();
    }

    public void stopSunmiScan() {
        sunmiController.release();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public String setTitle() {
        return null;
    }

    public boolean isSunmiScan() {
        String brand = Build.BRAND;
        if (brand.contains("SUNMI")) {
            return true;
        }
        return false;
    }

    @Override
    public void onScanResult(String code) {
        resultBack(code);
        Log.e("zjy", getClass() + "->onScanResult(): sunmiScan==" + code);
    }

    public void sunmiScan() {
        try {
            sunmiController.scan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sunmiController.release();
    }

    /**
     * This method will be invoked when a menu item is clicked if the item itself did
     * not already handle the event.
     *
     * @param item {@link MenuItem} that was clicked
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
