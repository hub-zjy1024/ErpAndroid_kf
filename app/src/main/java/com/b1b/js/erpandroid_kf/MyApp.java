package com.b1b.js.erpandroid_kf;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by js on 2016/12/27.
 */

public class MyApp extends Application {
    public static String id;
    public static String ftpUrl;
    public static List<Thread> totoalTask = new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
