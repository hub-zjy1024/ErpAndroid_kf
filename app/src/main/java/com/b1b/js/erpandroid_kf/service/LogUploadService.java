package com.b1b.js.erpandroid_kf.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.entity.SpSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import utils.net.ftp.FTPUtils;
import utils.common.UploadUtils;
import utils.net.wsdelegate.WebserviceUtils;

public class LogUploadService extends Service {
    //配置url、log文件名称、log保存地址、ftp用户名密码
    final String targeUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/logcheck.txt";
    final String logFileName = "dyj_log.txt";
    final String savedDir = "/Zjy/log_kf/" + UploadUtils.getyyMM() + "/";
    private  final String tagStr = "date";

    private int startTime = 9;
    private SharedPreferences sp;

    private int endTime = 20;
    private long fileSize = 0L;
    private int count = 0;
    private long lastUpTime;

    public LogUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IUploadBinder(this);
    }

    @Override
    public void onCreate() {
        //contextWrapper此时创建
        sp = getSharedPreferences(SpSettings.PREF_LOGUPLOAD, MODE_PRIVATE);
        MyApp.myLogger.writeInfo("UploadService start");
        super.onCreate();
    }

    public boolean checkDate() {
        Date d = new Date();
        int h = d.getHours();
        return h < startTime || h > endTime;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                if (!checkDate()) {
                    return;
                }
                lastUpTime = sp.getLong("lasttime", 0);
                double timeDur = (System.currentTimeMillis() - lastUpTime) / 1000 / 60;
                MyApp.myLogger.writeInfo("upload dur :" + timeDur);
                if (timeDur < 120) {
                    return;
                }
                final File root = Environment.getExternalStorageDirectory();
                final File log = new File(root, logFileName);
                final String date = sp.getString(tagStr, "");
                String remoteName = getRemoteName(date);
                fileSize = sp.getLong("logsize", 0);
                final String current = UploadUtils.getDD(new Date());
                if (!date.equals(current)) {
                    fileSize = 0;
                }
                if (!log.exists()) {
                    sp.edit().putString(tagStr, current).apply();
                } else {
                    final String remotePath = savedDir + remoteName;
                    if (fileSize < log.length()) {
                        boolean b = uploadLogFile(targeUrl, current, date, remotePath, log);
                        if (b) {
                            sp.edit().putLong("logsize", log.length())
                                    .putLong("lasttime", System.currentTimeMillis()).
                                    commit();
                        }
                    }
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean upload(File log, String remotePath) {
        FTPUtils utils = FTPUtils.getGlobalFTP();
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

    public boolean uploadLogFile(String targeUrl, String current, String date, String remotePath, File log
    ) {
        boolean upOK = false;
        HashMap<String, String> map = new HashMap<>();
        try {
            URL urll = new URL(targeUrl);
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
                    if (line.length < 2) {
                        map.put(line[0], "");
                    } else {
                        map.put(line[0], line[1]);
                    }
                    stringBuilder.append(len);
                    len = reader.readLine();
                }
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String checkid = map.get("checkid");
        String deviceID = map.get("deviceID");
        String localID = UploadUtils.getDeviceID(getApplicationContext());
        boolean nomarlUpload = true;
        if ("1".equals(checkid)) {
            nomarlUpload = false;
        }
        if (!date.equals(current)) {
            upOK = upload(log, remotePath);
            if (upOK) {
                Log.e("zjy", "LogUploadService->uploadLogFile(): UploadLog Finish==");
                sp.edit().putString(tagStr, current).apply();
                MyApp.myLogger.close();
                MyApp.myLogger.init(false);
                MyApp.myLogger.writeInfo("new Logger");
            }
        } else if (nomarlUpload) {
            return true;
        } else {
            if ("all".equals(deviceID) || localID.equals(deviceID)) {
                upOK = upload(log, remotePath);
            }
        }
        return upOK;
    }

    public String getRemoteName(String dd) {
        String phoneCode = UploadUtils.getPhoneCode(getApplicationContext());
        String remoteName = dd + "_" + phoneCode + "_log.txt";
        return remoteName;
    }

    static class IUploadBinder extends Binder {
        LogUploadService service;

        public IUploadBinder(LogUploadService service) {
            this.service = service;
        }

        public void uploadFile() {

            new Thread() {
                @Override
                public void run() {
                    final File root = Environment.getExternalStorageDirectory();
                    final File log = new File(root, service.logFileName);
                    final String date = service.sp.getString(service.tagStr, "");
                    final String current = UploadUtils.getCurrentDate();
                    String remoteName = service.getRemoteName(date);
                    final String remotePath = service.savedDir + remoteName;
                    if (!service.checkDate()) {
                        return;
                    }
                    if (!log.exists()) {
                        service.sp.edit().putString(service.tagStr, current).apply();
                    } else {
                        if (service.fileSize < log.length()) {
                            service.uploadLogFile(service.targeUrl, current, date, remotePath, log);
                        }
                    }
                }
            }.start();
        }
    }
}
