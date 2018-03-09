package com.b1b.js.erpandroid_kf.task;

import java.io.IOException;

import utils.FTPUtils;

/**
 Created by 张建宇 on 2018/3/9. */
public abstract class UploadPicRunnable2 extends UpLoadPicRunable {
    public UploadPicRunnable2(String upLoadPath, String insertpath, FTPUtils ftpUtils) {
        super(upLoadPath, insertpath, ftpUtils, null);
    }

    @Override
    public void run() {
        String str = "";
        int what = ERROR;
        boolean uploaded = false;
        try {
            ftpUtils.login();
            fio = getInputStream();
            uploaded = ftpUtils.upload(fio, upLoadPath);
        } catch (IOException e) {
            str = "连接错误：" + e.getMessage();
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
        onResult(what, str);
    }

    public abstract void onResult(int code, String err);
}
