package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.FTPUtils;
import utils.LogRecoder;
import utils.UploadUtils;

/**
 Created by js on 2016/12/27. */

public class MyApp extends Application implements Thread.UncaughtExceptionHandler{
    public static String id;
    public static String ftpUrl;
    public static List<Thread> totoalTask = new ArrayList<>();
    public static LogRecoder myLogger;

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences sp = getSharedPreferences("uploadlog", MODE_PRIVATE);
        final String date = sp.getString("date", "");
        final String current = UploadUtils.getCurrentDate();
        final File root = Environment.getExternalStorageDirectory();
        final String targeUrl = "http://172.16.6.160:8006/DownLoad/dyj_kf/logcheck.txt";
        final String logFileName = "dyj_log.txt";
        final SharedPreferences userInfo = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String id = userInfo.getString("name", "");
        String phoneCode = CaigoudanEditActivity.getPhoneCode(getApplicationContext());
        String remoteName = UploadUtils.getCurrentDay() + "_" + id + "_" + phoneCode + "_log.txt";
        final String remotePath = "/Zjy/log_kf/" + UploadUtils.getCurrentYearAndMonth() + "/" + remoteName;
        if (root.length() > 0) {
            final File log = new File(root, logFileName);
            if (log.exists()) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        boolean upOK = false;
                        HashMap<String, String> map = new HashMap<String, String>();
                        URL urll = null;
                        try {
                            urll = new URL(targeUrl);
                            HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
                            conn.setConnectTimeout(15 * 1000);
                            conn.setReadTimeout(15 * 1000);
                            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                InputStream is = conn.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                String len = reader.readLine();
                                StringBuilder stringBuilder = new StringBuilder();
                                while (len != null) {
                                    String[] line = len.split("=");
                                    map.put(line[0], line[1]);
                                    stringBuilder.append(len);
                                    len = reader.readLine();
                                }
                                is.close();
                                Log.e("zjy", "MainActivity.java->checkVersion(): readme==" + stringBuilder.toString());
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //                            uploadby=daycheckid=0
                        String checkid = map.get("checkid");
                        String deviceID = map.get("deviceID");
                        String localID = UploadUtils.getDeviceID(getApplicationContext());
                        boolean nomarlUpload = true;
                        if ("1".equals(checkid)) {
                            nomarlUpload = false;
                        }
                        boolean delete = !date.equals(current);
                        if (nomarlUpload) {
                            if (delete) {
                                upOK = upload( log, remotePath);
                            }
                        } else {
                            if ("all".equals(deviceID)) {
                                upOK = upload( log, remotePath);

                            }else if (localID.equals(deviceID)) {
                                upOK = upload( log, remotePath);
                            }
                        }
                        if (upOK) {
                            sp.edit().putString("date", current).apply();
                            if (delete) {
                                log.delete();
                            }
                        }
                    }
                }.start();
            } else {
                sp.edit().putString("date", current).apply();
            }
        }
        myLogger = new LogRecoder(logFileName, null);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private boolean upload(File log, String remotePath) {
        FTPUtils utils = new FTPUtils(FTPUtils.mainAddress, 21, FTPUtils.mainName, FTPUtils.mainPwd);
        boolean upOK = false;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(log);
            utils.login();
            upOK= utils.upload(fis, remotePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        utils.exitServer();
        return upOK;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        StringBuilder sb = new StringBuilder();
        String exMsg = ex.getMessage();
        Throwable cause = ex.getCause();
        StackTraceElement[] stacks = ex.getStackTrace();
        sb.append(exMsg+"\n");
        if (cause != null) {
            sb.append("caused by:"+cause.getMessage()+"\n");
            StackTraceElement[] cStackTraces = cause.getStackTrace();
            for (StackTraceElement e : cStackTraces) {
                String className = e.getClassName();
                if (className.contains("b1b") || className.contains("utils") || className.contains("printer")
                        || className.contains("zhy")) {
                    sb.append(className + "." + e.getMethodName() +"("+e.getFileName()+":"+ e.getLineNumber()+")\n");
                }
            }
        }
        for (StackTraceElement s : stacks) {
            String className = s.getClassName();
            if (className.contains("b1b") || className.contains("utils") || className.contains("printer")
                    || className.contains("zhy")) {
                sb.append(className + "." + s.getMethodName() +"("+s.getFileName()+":"+ s.getLineNumber()+")\n");
            }
        }
        myLogger.writeError("uncatch exception:" + sb.toString());
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}