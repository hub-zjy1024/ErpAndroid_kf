package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.b1b.js.erpandroid_kf.adapter.UploadPicAdapter;
import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UpLoadPicRunable;
import com.b1b.js.erpandroid_kf.task.UploadPicRunnable2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import utils.FTPUtils;
import utils.FtpManager;
import utils.MyToast;
import utils.UploadUtils;
import zhy.imageloader.PickPicActivity;

public class ReUploadActivity extends ObtainPicFromPhone {
    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    showFinalDialog("上传成功");
                    int nfId = getIntent().getIntExtra("nfId", 0);
                    int index = msg.arg1;
                    int okTimes = counts.incrementAndGet();
                    UploadPicInfo upInfo = uploadPicInfos.get(index);
                    if (nfId != 0) {
                        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        nManager.cancel(nfId);
                    }
                    upInfo.setState("1");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case MSG_ERROR:
                    showFinalDialog("上传失败");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case MSG_SUCCESS_All:
                    int okCounts = counts.incrementAndGet();
                    if (okCounts == uploadPicInfos.size()) {
                        showFinalDialog("上传成功");
                    } else {
                        int size = ((int) TaskManager.getInstance().getExecutor().getTaskCount());
                        Log.e("zjy", "ReUploadActivity->handleMessage(): nowTask==" + size);
                        if (size == 0) {
                            showFinalDialog("上传失败");
                            mGvAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    };
    AtomicInteger counts = new AtomicInteger(1);
    private final int MSG_SUCCESS = 0;
    private final int MSG_ERROR = 1;
    private final int MSG_SUCCESS_All = 2;
    private String typeFlag;
    private boolean isTest = false;
    private boolean isCaigou = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        failPath = intent.getStringExtra("failPath");
        typeFlag = intent.getStringExtra("caigou");
        isCaigou = "caigou".equals(typeFlag);
        if (failPath != null) {
            uploadPicInfos.add(new UploadPicInfo("-1", failPath));
            btn_commit.setEnabled(true);
        }
        if ("101".equals(MyApp.id)) {
            isTest = true;
        }
        mGvAdapter = new UploadPicAdapter(mContext, uploadPicInfos, new UploadPicAdapter.OnItemBtnClickListener() {
            @Override
            public void onClick(View v, final int position) {
                final UploadPicInfo uploadPicInfo = uploadPicInfos.get(position);
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(mContext, pid, 5))
                    return;
                if (!uploadPicInfo.getState().equals("-1")) {
                    MyToast.showToast(mContext, "当前图片已经上传完成");
                    return;
                }
                showProgressDialog();
                upload(position, false);
            }
        });
        gv.setAdapter(mGvAdapter);
    }

    private void upload(final int position, final boolean isMulti) {
        if (MyApp.id == null) {
            showFinalDialog("当前登陆人为空，请重启程序尝试");
            return;
        }
        final UploadPicInfo item = (UploadPicInfo) uploadPicInfos.get(position);
        String insertPath = "";
        String remoteName = "";
        String remotePath = "";
        String mUrl = "";
        FTPUtils ftpUtil = null;
        //重新上传失败的文件
        String nowPath = item.getPath();
        if (isCaigou) {
            mUrl = CaigouActivity.ftpAddress;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,FtpManager.ftpPassword);
            String fileName = nowPath.substring(nowPath.lastIndexOf("/") + 1, nowPath.lastIndexOf("."));
            remoteName = getRemarkName(fileName, false);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
        } else {
            mUrl = MyApp.ftpUrl;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,FtpManager.ftpPassword);
            String fileName = nowPath.substring(nowPath.lastIndexOf("/") + 1, nowPath.lastIndexOf("."));
            remoteName = getRemarkName(fileName, false);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
        }
        if (isTest) {
            mUrl = FtpManager.mainAddress;
            ftpUtil = new FTPUtils(mUrl, FtpManager.mainName,
                    FtpManager.mainPwd);
            remotePath = UploadUtils.KF_DIR + remoteName + ".jpg";
        }
        insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
        UploadPicRunnable2 runable = new UploadPicRunnable2(remotePath, insertPath, ftpUtil) {
            @Override
            public void onResult(int code, String err) {
                Message msg = nHandler.obtainMessage(MSG_SUCCESS);
                msg.arg1 = position;
                if (code == UpLoadPicRunable.SUCCESS) {
                    if (isMulti) {
                        msg.what = MSG_SUCCESS_All;
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
                    String flag = "";
                    if (isCaigou) {
                        res = setSSCGPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, remoteName,
                                insertPath, flag);
                    } else {
                        res = setInsertPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, remoteName,
                                insertPath, flag);
                    }
                }
                return res.equals("操作成功");
            }

            @Override
            public InputStream getInputStream() throws Exception {
                String fPath = item.getPath();
                return new FileInputStream(fPath);
            }
        };
        TaskManager.getInstance().execute(runable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_commit:
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(mContext, pid, 5))
                    return;
                showProgressDialog();
                counts.set(0);
                for (int i = 0; i < uploadPicInfos.size(); i++) {
                    upload(i, true);
                }
                break;
            case R.id.review_getFromPhone:
                Intent intent = new Intent(mContext, PickPicActivity.class);
                startActivityForResult(intent, 100);
                break;
        }
    }
}
