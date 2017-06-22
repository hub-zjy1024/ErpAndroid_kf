package com.b1b.js.erpandroid_kf.utils;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 Created by 张建宇 on 2017/2/21.
 主要是一些路径的获取 */

public class UploadUtils {
    public static String getRomoteName(String id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "android_" + id + "_" + sdf.format(new Date());
    }

    public static String getRomoteName2(String id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "a_" + id + "_" + sdf.format(new Date());
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        String str = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar
                .DAY_OF_MONTH);
        return str;
    }

    /**
     @param ftpUrl
     @param remoteDir
     @param fileName
     @param extension
     @return 插入到数据库的图片地址
     */
    public static String createInsertPath(String ftpUrl, String remoteDir, String fileName, String extension) {
        StringBuilder builder = new StringBuilder();
        builder.append("ftp://");
        builder.append(ftpUrl);
        builder.append("/");
        builder.append(remoteDir);
        builder.append("/");
        builder.append(fileName);
        builder.append(".");
        builder.append(extension);
        return builder.toString();
    }

    public static void createDirs(FTPClient ftpClient, String remoteUpLoadPath) throws IOException {

        //根据路径逐层判断目录是否存在，如果不存在则创建
        //1.首先进入ftp的根目录
        ftpClient.changeWorkingDirectory("/");
        String[] dirs = remoteUpLoadPath.split("/");
        for (String dir : dirs) {
            //2.创建并进入不存在的目录
            if (!ftpClient.changeWorkingDirectory(dir)) {
                ftpClient.mkd(dir);
                ftpClient.changeWorkingDirectory(dir);
            }
        }
    }
}
