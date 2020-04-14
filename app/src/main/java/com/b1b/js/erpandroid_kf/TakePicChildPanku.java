package com.b1b.js.erpandroid_kf;

import android.os.Bundle;

import com.b1b.js.erpandroid_kf.task.CheckUtils;

import utils.common.UploadUtils;
import utils.net.ftp.FTPUtils;

public class TakePicChildPanku extends TakePicBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.myLogger.writeInfo("takepic pk page");
    }

    @Override
    public String getUploadRemotePath() {
        String remotePath = UploadUtils.getPkRemotePath(pid);
        if (CheckUtils.isAdmin()) {
            remotePath = UploadUtils.getTestPath(pid);
        }
        return remotePath;
    }

    @Override
    public void initUploadInfos() {
        mUrl = FTPUtils.mainAddress;
    }
    @Override
    public String getUploadFlag() {
        return "PK";
    }



    @Override
    public boolean check(int len) {
        return super.check(3);
    }
}
