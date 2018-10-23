package com.sunmi.scanner;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;

/**
 * Created by 张建宇 on 2019/4/26.
 */
public class ScanController {
    //    com.sunmi.scanner.IScanInterface
    //    com.sunmi.scanner
    private Context mContext;
    private static final String ACTION = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED";
    private static final String DATAKey = "data";
    private ScanListener mListener;

    public interface ScanListener {
        void onScanResult(String code);
    }

    public ScanController(Context mContext, ScanListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
        registerListener();
        bindService();
    }

    private IScanInterface mScanner = null;

    //    IScanInterface iScanInterface;
    private BroadcastReceiver mScanRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra(DATAKey);

            if (code != null && !code.isEmpty()) {
                Log.e("zjy", "ScanController->onReceive(): SunmiScan==" + code);
                stop();
                if (mListener != null) {
                    mListener.onScanResult(code);
                }
            }
        }
    };
    private ServiceConnection mConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScanner = IScanInterface.Stub.asInterface(service);
            Log.e("zjy", "ScanController->onServiceConnected(): SunmiScanDev==" + getScannerModel());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("zjy", "com.sunmi.scanner.ScanController->onServiceDisconnected(): ==");

        }
    };

    public void bindScanKey(KeyEvent keyEvent) {
        if (mScanner != null) {
            try {
                mScanner.sendKeyEvent(keyEvent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public String getScannerModel() {
        if (mScanner != null) {
            try {
                int scannerModel = mScanner.getScannerModel();
                SparseArray<String> mArray = new SparseArray<>();
                mArray.put(100, "none");
                mArray.put(101, "P2Lite");
                mArray.put(102, "l2-newland");
                mArray.put(103, "l2-zabra");
                return mArray.get(scannerModel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            return "scanner not init";
        }
        return "unknown";
    }

    public void scan() throws Exception {
        if (mScanner != null) {
            try {
                mScanner.scan();
                //                mScanner.getScannerModel();
            } catch (RemoteException e) {
                e.printStackTrace();
                throw new Exception("初始化sunmi扫码失败");
            }
        }else{
            throw new Exception("还未初始化sunmi扫码功能");
        }

        //        scanInterface.scan();
    }

    public void stop() {
        if (mScanner != null) {
            try {
                mScanner.stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void registerListener() {
        //        com.sunmi.scanner
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ACTION);
        mContext.registerReceiver(mScanRec, mFilter);
    }

    void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.sunmi.scanner");
        intent.setAction("com.sunmi.scanner.IScanInterface");
        mContext.bindService(intent, mConection, Service.BIND_AUTO_CREATE);
    }

    public void release() {
        mContext.unbindService(mConection);
        mContext.unregisterReceiver(mScanRec);
    }
}
