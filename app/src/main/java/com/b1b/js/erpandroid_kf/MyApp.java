package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.b1b.js.erpandroid_kf.utils.LogRecoder;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;

import java.io.File;
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
        SharedPreferences sp = getSharedPreferences("uploadlog", MODE_PRIVATE);
        String date = sp.getString("date", "");
        String current = UploadUtils.getRemoteDir();
        File root = Environment.getExternalStorageDirectory();
        if (root.length() > 0) {
            File log = new File(root, "dyj_log.txt");
            if (!date.equals(current)) {
                if (log.exists()) {
                    log.delete();
                }
                sp.edit().putString("date", current).apply();
            } else {
                Log.e("zjy", "MyApp->onCreate(): ====");
            }
        }
        myLogger = new LogRecoder("dyj_log.txt", null);
    }
}
