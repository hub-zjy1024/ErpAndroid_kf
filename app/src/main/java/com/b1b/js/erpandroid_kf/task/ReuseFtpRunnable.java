package com.b1b.js.erpandroid_kf.task;

import com.b1b.js.erpandroid_kf.MyApp;

import java.io.IOException;

import utils.net.ftp.FTPUtils;

/**
 Created by 张建宇 on 2018/3/19. */
public abstract class ReuseFtpRunnable extends UploadPicRunnable2 {
    public ReuseFtpRunnable(String upLoadPath, String insertpath, FTPUtils ftpUtils) {
        super(upLoadPath, insertpath, ftpUtils);
    }

    @Override
    public void run() {
        long start1 = System.currentTimeMillis();
        String str = "";
        int what = ERROR;
        boolean uploaded = false;
        try {
            if (!ftpUtils.serverIsOpen()) {
                ftpUtils.login();
            }
            fio = getInputStream();
            uploaded = ftpUtils.upload(fio, upLoadPath);
        } catch (IOException e) {
            str = "连接错误：" + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            str = "其他错误：" + e.getMessage();
            e.printStackTrace();
        }
        long start2 = System.currentTimeMillis();
        if (uploaded) {
            boolean result = false;
            try {
                result = getInsertResult();
                if (result) {
                    what = SUCCESS;
                } else {
                    str = "关联图片失败";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        double totalTime = ((double) (end - start)) / 1000;
        double timeUpload = ((double) (start2 - start1)) / 1000;
        MyApp.myLogger.writeInfo("TakepicChuku upload time :" + getRemoteName() + "===" + timeUpload + "/" + totalTime);
        onResult(what, str);
    }

}
