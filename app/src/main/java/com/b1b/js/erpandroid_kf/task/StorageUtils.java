package com.b1b.js.erpandroid_kf.task;

import java.io.IOException;

import utils.HttpUtils;

/**
 Created by 张建宇 on 2018/4/17. */
public class StorageUtils {
    public static String getCurrentIp() {
        String url = "http://172.16.6.101:802/ErpV5IP.asp";
        try {
            return HttpUtils.create(url).getBodyString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
