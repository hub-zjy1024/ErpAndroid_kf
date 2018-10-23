package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.util.Log;

import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import utils.common.LogRecoder;
import utils.common.log.EmailLogger;

/**
 Created by js on 2016/12/27. */

public class MyApp extends Application implements Thread.UncaughtExceptionHandler{
    public static String id;
    public static String ftpUrl;
    public static LogRecoder myLogger;
    public static ThreadPoolExecutor cachedThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    @Override
    public void onCreate() {
        super.onCreate();
        final String logFileName = "dyj_log.txt";
//        final String logFileName = "dyjlog/dyj_log.txt";
        myLogger = new LogRecoder(logFileName);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final String debugMsg = myLogger.getAllStackInfo(ex);
        Log.e("zjy", getClass() + "->uncaughtException(): ==" + debugMsg);
        myLogger.writeError(ex, "===[AppCrash]=====\n");
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                EmailLogger.sendLog(debugMsg, getApplicationContext());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
        TaskManager.getInstance().execute(mRun);
    }
}