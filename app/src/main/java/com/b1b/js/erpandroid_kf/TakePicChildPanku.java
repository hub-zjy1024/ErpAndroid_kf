package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.util.Log;

import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.FTPUtils;
import utils.FtpManager;
import utils.UploadUtils;
import utils.WebserviceUtils;

public class TakePicChildPanku extends TakePicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.myLogger.writeInfo("takepic pk page");
    }

    @Override
    public String getUploadRemotePath() {
        String remotePath;
        String remoteName = UploadUtils.getPankuRemoteName(pid);;
        if (CheckUtils.isAdmin()) {
            remotePath = UploadUtils.getTestPath(pid);
        } else {
            remotePath = "/" + UploadUtils.getCurrentDate() + "/pk/" + remoteName + ".jpg";
        }
        return remotePath;
    }

    @Override
    public boolean getInsertResultMain(String remoteName, String insertPath) throws IOException, XmlPullParserException {
        String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer
                .parseInt(loginID), pid, remoteName, insertPath, "PK");
        Log.e("zjy", "TakePicActivity.java->run(): insertPath result==" + insertPath + "\t" + res);
        MyApp.myLogger.writeInfo("TakePic PK " + insertPath + "\t" + res);
        return "操作成功".equals(res);
    }

    @Override
    public void getUrlAndFtp() {
        if (CheckUtils.isAdmin()) {
            mUrl = FtpManager.mainAddress;
            ftpUtil = FtpManager.getTestFTP();
        } else {
            mUrl = kfFTP;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,
                    FtpManager.ftpPassword);
        }
    }

    @Override
    public boolean check(int len) {
        return super.check(3);
    }
}
