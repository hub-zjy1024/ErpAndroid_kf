package com.b1b.js.erpandroid_kf;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;

import com.b1b.js.erpandroid_kf.utils.LogRecoder;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

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
        SharedPreferences sp = getSharedPreferences("uploadlog", MODE_PRIVATE);
        String date = sp.getString("date", "");
        String current = UploadUtils.getCurrentDate();
        final File root = Environment.getExternalStorageDirectory();
        if (root.length() > 0) {
            final File log = new File(root, "dyj_log.txt");
            if (!date.equals(current)) {
                if (log.exists()) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            FTPClient client = new FTPClient();
                            try {
                                client.setConnectTimeout(10 * 1000);
                                client.connect("172.16.6.22", 21);
                                boolean login = client.login("NEW_DYJ", "GY8Fy2Gx");
                                if (!login) {
                                    return;
                                }
                                client.setFileType(FTP.BINARY_FILE_TYPE);
                                client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                                FileInputStream fis = new FileInputStream(log);
                                String dir = UploadUtils.getCurrentDate();
                                client.changeWorkingDirectory("ZJy");
                                UploadUtils.createDirs(client, "Zjy/log_kf/" + dir);
                                SharedPreferences sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                String id = sp.getString("name", "");
                                String name = id + "_log.txt";
                                String[] names = client.listNames();
                                for (String s : names) {
                                    if (s.equals(name)) {
                                        return;
                                    }
                                }
                                boolean isFalse = client.storeFile(name, fis);
                                if (isFalse) {
                                    log.delete();
                                }
                                client.completePendingCommand();
                                client.disconnect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                sp.edit().putString("date", current).apply();
            }
        }
        myLogger = new LogRecoder("dyj_log.txt", null);
    }
}
