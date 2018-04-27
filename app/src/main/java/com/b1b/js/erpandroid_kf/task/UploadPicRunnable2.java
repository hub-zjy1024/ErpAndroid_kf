package com.b1b.js.erpandroid_kf.task;

import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;

import java.io.IOException;

import utils.FTPUtils;

/**
 Created by 张建宇 on 2018/3/9. */
public abstract class UploadPicRunnable2 extends UpLoadPicRunable {
    public long start = System.currentTimeMillis();
    public UploadPicRunnable2(String upLoadPath, String insertpath, FTPUtils ftpUtils) {
        super(upLoadPath, insertpath, ftpUtils, null);
    }

    @Override
    public void run() {
        long start2 = System.currentTimeMillis();
        String str = "";
        int what = ERROR;
        boolean uploaded = false;
        try {
            ftpUtils.login();
            fio = getInputStream();
            uploaded = ftpUtils.upload(fio, upLoadPath);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            str = "连接错误：" + e.getMessage();
            if (cause instanceof OutOfMemoryError) {
                str = "一次上传的图片过多，请稍后再传";
            }
            e.printStackTrace();
        } catch (Exception e) {
            str = "其他错误：" + e.getMessage();
            e.printStackTrace();
        } finally {
            ftpUtils.exitServer();
        }
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
        double timeUpload = ((double) (end - start2)) / 1000;
        String remoteName = getRemoteName();
        Log.e("zjy", "UploadPicRunnable2->run()"+remoteName+": time==" + timeUpload + "/" + totalTime);
        MyApp.myLogger.writeInfo("upload time :" + remoteName + "===" + timeUpload + "/" + totalTime);
        onResult(what, str);
    }

    public abstract void onResult(int code, String err);
}
