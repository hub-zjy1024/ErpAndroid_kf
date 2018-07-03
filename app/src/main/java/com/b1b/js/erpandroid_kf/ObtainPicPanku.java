package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UploadPicRunnable2;

import java.io.InputStream;

import utils.FTPUtils;
import utils.FtpManager;
import utils.UploadUtils;

public class ObtainPicPanku extends ReUploadActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean checkPid(int len) {
        return super.checkPid(3);
    }

    public void upload(final int position, final boolean isMulti) {
        if (!CheckUtils.checkUID(mContext, "当前登陆人为空，请重启程序尝试")) {
            return;
        }
        final UploadPicInfo item =  uploadPicInfos.get(position);
        String insertPath = "";
        String remoteName = "";
        String remotePath = "";
        String mUrl = "";
        FTPUtils ftpUtil = null;
        String fileName = UploadUtils.getPankuRemoteName(pid);
        fileName = getRemarkName(fileName, true);
        remoteName = fileName + ".jpg";
        mUrl = MyApp.ftpUrl;
        ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,
                FtpManager.ftpPassword);
        remotePath = "/" + UploadUtils.getCurrentDate() + "/pk/" + remoteName;
        if (isTest) {
            mUrl = FtpManager.mainAddress;
            ftpUtil =  FtpManager.getTestFTP();
            remotePath = UploadUtils.getTestPath(pid);
        }
        insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
        UploadPicRunnable2 runable = new UploadPicRunnable2(remotePath, insertPath, ftpUtil) {
            @Override
            public void onResult(int code, String err) {
                Message msg = nHandler.obtainMessage(MSG_SUCCESS);
                msg.arg1 = position;
                msg.arg2 = 1;
                if (code == SUCCESS) {
                    if (isMulti) {
                        msg.arg2 = 2;
                    }
                    msg.sendToTarget();
                } else {
                    msg.what = MSG_ERROR;
                    msg.obj = err;
                    msg.sendToTarget();
                }
            }

            @Override
            public boolean getInsertResult() throws Exception {
                String remoteName = getRemoteName();
                String insertPath = getInsertpath();
                String res = "";
                if (isTest) {
                    return true;
                } else {
                    String flag = "PK";
                    res = setInsertPicInfo("", cid, did, Integer.parseInt(loginID), pid, remoteName,
                            insertPath, flag);
                }
                return res.equals("操作成功");
            }

            @Override
            public InputStream getInputStream() throws Exception {
                String fPath = item.getPath();
                return getTransferedImg(fPath);
            }
        };
        TaskManager.getInstance().execute(runable);
    }

}
