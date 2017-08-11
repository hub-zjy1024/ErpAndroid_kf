package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.b1b.js.erpandroid_kf.utils.DownUtils;
import com.b1b.js.erpandroid_kf.utils.FtpManager;
import com.b1b.js.erpandroid_kf.utils.LogRecoder;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 Created by js on 2016/12/27. */

public class MyApp extends Application {
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
        if (root.length() > 0) {
            final File log = new File(root, "dyj_log.txt");
            if (!date.equals(current)) {
                if (log.exists()) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            boolean upOK = false;
                            DownUtils utils = new DownUtils(FtpManager.mainAddress, 21, "NEW_DYJ", "GY8Fy2Gx");
                            try {
                                FileInputStream fis = new FileInputStream(log);
                                String id = sp.getString("name", "");
                                  String phoneCode= CaigoudanEditActivity.getPhoneCode(getApplicationContext()) + Build.BRAND;
                                String name = id + "_" + phoneCode + "_log.txt";
                                String remotePath = "Zjy/log_kf/" + UploadUtils.getCurrentDate() + "/" + name;
                                Log.e("zjy", "MyApp->run(): start upload log==");
                                utils.login();
                                boolean isUploaded = utils.upload(fis, remotePath);
                                fis.close();
                                if (isUploaded) {
                                    log.delete();
                                    upOK = true;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (upOK) {
                                sp.edit().putString("date", current).apply();
                            }
                        }
                    }.start();
                } else {
                    sp.edit().putString("date", current).apply();
                }
            }
        }
        myLogger = new LogRecoder("dyj_log.txt", null);
    }
}
