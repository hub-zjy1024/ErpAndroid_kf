package com.b1b.js.erpandroid_kf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MyFileUtils {
    /**
     获取当前连接的wifi地址
     @return 获取当前连接的wifi地址
     */
    private static String getLocalIpAddress(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    public static void saveImg(String name, Bitmap bitmap, Context context) {
        File file;
        if (isMonuted()) {
            file = new File(Environment.getDataDirectory(), name);
        } else {
            file = new File(context.getCacheDir(), name);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            BufferedOutputStream fio = new BufferedOutputStream(new FileOutputStream(file));
            //            FileOutputStream fio = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, fio);
            bitmap.recycle();
            Bitmap newBitmap = MyImageUtls.getSmallBitmap(file.getAbsolutePath(), 800, 480);
            newBitmap.compress(CompressFormat.JPEG, 100, fio);
            Log.e("length", "" + file.length());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean isMonuted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


}
