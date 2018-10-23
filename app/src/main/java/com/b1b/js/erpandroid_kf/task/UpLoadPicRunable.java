package com.b1b.js.erpandroid_kf.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import utils.net.ftp.FTPUtils;

/**
 Created by 张建宇 on 2018/2/28. */
public abstract class UpLoadPicRunable implements Runnable {
    protected String upLoadPath;
    private String insertpath;
    protected FTPUtils ftpUtils;
    protected InputStream fio;
    protected Handler mHandler;
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    public UpLoadPicRunable(String upLoadPath, String insertpath, FTPUtils ftpUtils, Handler mHandler) {
        this.upLoadPath = upLoadPath;
        this.insertpath = insertpath;
        this.ftpUtils = ftpUtils;
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        Message msg = mHandler.obtainMessage(ERROR);
        boolean uploaded = false;
        try {
            ftpUtils.login();
            fio = getInputStream();
            uploaded = ftpUtils.upload(fio, upLoadPath);
        } catch (IOException e) {
            msg.obj = "连接错误：" + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            msg.obj = "其他错误：" + e.getMessage();
            e.printStackTrace();
        } finally {
            ftpUtils.exitServer();
        }
        if (uploaded) {
            boolean result = false;
            try {
                result = getInsertResult();
            } catch (Exception e) {
                msg.obj = "关联图片失败:" + e.getMessage();
                e.printStackTrace();
            }
            if (result) {
                msg.what = SUCCESS;
            } else {
                msg.obj = "关联图片失败";
            }
        }
        mHandler.sendMessage(msg);
    }

    public abstract boolean getInsertResult() throws Exception;

    public abstract InputStream getInputStream() throws Exception;

    public String getRemoteName() {
        Log.e("zjy", "UpLoadPicRunable->getRemoteName(): insertPath==" + insertpath);
        return upLoadPath.substring(upLoadPath.lastIndexOf("/") + 1);
    }

    public String getInsertpath() {
        return insertpath;
    }
}
