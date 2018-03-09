package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import utils.LogRecoder;

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
        myLogger = new LogRecoder(logFileName, null);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ByteArrayOutputStream bao=new ByteArrayOutputStream();
        PrintWriter writer=new PrintWriter(bao);
        ex.printStackTrace(writer);
        writer.flush();
        String error="";
        try {
            error = new String(bao.toByteArray(), "utf-8");
            myLogger.writeError("===Error-uncaughtException-:" +error);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.close();
        Log.e("zjy", "MyApp->uncaughtException(): detail==" + error);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}