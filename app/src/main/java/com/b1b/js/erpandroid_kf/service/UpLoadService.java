package com.b1b.js.erpandroid_kf.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class UpLoadService extends Service {
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public UpLoadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("zjy", "UpLoadService.java->onCreate(): ==");
    }

    private MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("zjy", "UpLoadService.java->onBind(): ==");
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("zjy", "UpLoadService.java->onStartCommand(): ==");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("zjy", "UpLoadService.java->onDestroy(): service ondestory==");
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        public void upLoad() {
            Log.e("zjy", "UpLoadService.java->upLoad(): ==");
        }
    }
}
