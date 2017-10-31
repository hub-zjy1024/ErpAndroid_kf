package com.b1b.js.erpandroid_kf.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
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
import java.util.Date;
import java.util.HashMap;

import utils.FTPUtils;
import utils.UploadUtils;

public class LogUploadService extends Service {
    //配置url、log文件名称、log保存地址、ftp用户名密码
    final String targeUrl = "http://172.16.6.160:8006/DownLoad/dyj_kf/logcheck.txt";
    final String logFileName = "dyj_log.txt";
    final String savedDir = "/Zjy/log_kf/"+ UploadUtils.getCurrentYearAndMonth() + "/";
    private int startTime = 9;

    private int endTime = 20;

    private long fileSize = 0L;
    private int count =0;

    public LogUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.e("zjy", "LogUploadService->onCreate(): ==");
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    final SharedPreferences sp = getSharedPreferences("uploadlog", MODE_PRIVATE);
                    final File root = Environment.getExternalStorageDirectory();
                    final SharedPreferences userInfo = getSharedPreferences("UserInfo", MODE_PRIVATE);
                    String id = userInfo.getString("name", "");
                    String phoneCode = UploadUtils.getPhoneCode(getApplicationContext());
                    final File log = new File(root, logFileName);
                    final String date = sp.getString("date", "");
                    final String current = UploadUtils.getCurrentDate();
                    String remoteName = UploadUtils.getCurrentDay() + "_" + id + "_" + phoneCode +
                            "_log.txt";
                    final String remotePath = savedDir + remoteName;
                    if (log.exists()) {
                        Date d = new Date();
                        int h = d.getHours();
                        if (h >= startTime && h <= endTime) {
                            if (fileSize < log.length()) {
                                if (uploadLogFile(targeUrl, current, date, remotePath, log)) {
                                    if (!date.equals(current)) {
                                        sp.edit().putString("date", current).apply();
                                        log.delete();
                                        Log.e("zjy", "LogUploadService->run()oncreate: ==deletfile");
                                    }
                                }
                            }
                        } else {
                            stopSelf();
                        }
                    } else {
                        sp.edit().putString("date", current).apply();
                    }
                    fileSize = log.length();
                    try {
                        Thread.sleep(60 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("zjy", "LogUploadService->onStartCommand(): count==" + count);
        if (count > 0) {
            new Thread() {
                @Override
                public void run() {
                    final SharedPreferences sp = getSharedPreferences("uploadlog", MODE_PRIVATE);
                    final String date = sp.getString("date", "");
                    final String current = UploadUtils.getCurrentDate();
                    final File root = Environment.getExternalStorageDirectory();
                    final SharedPreferences userInfo = getSharedPreferences("UserInfo", MODE_PRIVATE);
                    String id = userInfo.getString("name", "");
                    String phoneCode = UploadUtils.getPhoneCode(getApplicationContext());
                    String remoteName = UploadUtils.getCurrentDay() + "_" + id + "_" + phoneCode +
                            "_log.txt";
                    final String remotePath = savedDir + remoteName;
                    final File log = new File(root, logFileName);
                    if (log.exists()) {
                        if (uploadLogFile(targeUrl, current, date, remotePath, log)) {
                            if (!date.equals(current)) {
                                sp.edit().putString("date", current).apply();
                                log.delete();
                                Log.e("zjy", "LogUploadService->run()Command: ==deletfile");
                            }
                        }
                    } else {
                        sp.edit().putString("date", current).apply();
                    }
                }
            }.start();
        }
        count++;
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean upload(File log, String remotePath) {
        FTPUtils utils = new FTPUtils(FTPUtils.mainAddress, 21, FTPUtils.mainName,
                FTPUtils.mainPwd);
        boolean upOK = false;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(log);
            utils.login();
            upOK = utils.upload(fis, remotePath);
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

    public boolean uploadLogFile(String targeUrl, String current, Object date, String remotePath, File log
    ) {
        boolean upOK = false;
        HashMap<String, String> map = new HashMap<>();
        URL urll = null;
        try {
            urll = new URL(targeUrl);
            HttpURLConnection conn = (HttpURLConnection) urll
                    .openConnection();
            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(15 * 1000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(is));
                String len = reader.readLine();
                StringBuilder stringBuilder = new StringBuilder();
                while (len != null) {
                    String[] line = len.split("=");
                    map.put(line[0], line[1]);
                    stringBuilder.append(len);
                    len = reader.readLine();
                }
                is.close();
                Log.e("zjy", "LogUploadService.java->uploadLogFile(): " +
                        "readme==" + stringBuilder.toString());
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
                upOK = upload(log, remotePath);
            }
        } else {
            if ("all".equals(deviceID) || localID.equals(deviceID)) {
                upOK = upload(log, remotePath);
            }
        }
        return upOK;
    }
}
