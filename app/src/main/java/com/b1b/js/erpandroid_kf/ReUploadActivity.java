package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.b1b.js.erpandroid_kf.adapter.UploadPicAdapter;
import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UploadPicRunnable2;

import java.io.FileInputStream;
import java.io.InputStream;

import utils.FTPUtils;
import utils.FtpManager;
import utils.MyToast;
import utils.handler.NoLeakHandler;
import utils.handler.SafeHandler;
import utils.UploadUtils;

public class ReUploadActivity extends ObtainPicFromPhone implements NoLeakHandler.NoLeakCallback {
    @Override
    public void handleMessage(Message msg) {
        int arg1 = msg.arg1;
        int arg2 = msg.arg2;
        Object obj = msg.obj;
        int picSize = uploadPicInfos.size();
        UploadPicInfo nowInfo = uploadPicInfos.get(msg.arg1);
        String err = "";
        switch (msg.what) {
            case MSG_SUCCESS:
                int nfId = getIntent().getIntExtra("nfId", 0);
                if (nfId != 0) {
                    NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (nManager != null) {
                        nManager.cancel(nfId);
                    }
                }
                err = "上传成功";
                uploadResult += "图片" + arg1 + ":" + err + "\n";
                solveResult(this, arg1, arg2, picSize, err);
                nowInfo.setState("1");
                mGvAdapter.notifyDataSetChanged();
                break;
            case MSG_ERROR:
                if (obj != null) {
                    err = obj.toString();
                }
                err = "上传失败:" + err;
                uploadResult += "图片" + arg1 + ":" + err + "！！！\n";
                solveResult(this, arg1, arg2, picSize, err);
                break;
        }
    }
    private void solveResult(ReUploadActivity activity ,int arg1, int arg2, int picSize, String err) {
        activity.count++;
        if (arg2 == 1) {
            activity.uploadResult = "图片" + arg1 + ":" + err + "\n";
            activity.showFinalDialog(activity.uploadResult);
        } else {
            activity.pd.setMessage("上传了" + (activity.count) + "/" + activity.uploadPicInfos.size());
            if (activity.count >= picSize) {
                activity.showFinalDialog(activity.uploadResult);
            }
        }
    }

    static class LHanlder extends SafeHandler<ReUploadActivity> {
        LHanlder(ReUploadActivity mContext) {
            super(mContext);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                int arg1 = msg.arg1;
                int arg2 = msg.arg2;
                Object obj = msg.obj;
            ReUploadActivity activity = getActivity();
            if (activity == null) {
                return;
            }
                int picSize = activity.uploadPicInfos.size();
                UploadPicInfo nowInfo = activity.uploadPicInfos.get(arg1);
                String err = "";
                switch (msg.what) {
                    case MSG_SUCCESS:
                        int nfId = activity.getIntent().getIntExtra("nfId", 0);
                        if (nfId != 0) {
                            NotificationManager nManager = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
                            if (nManager != null) {
                                nManager.cancel(nfId);
                            }
                        }
                        err = "上传成功";
                        activity.uploadResult += "图片" + arg1 + ":" + err + "\n";
                        solveResult(activity,arg1, arg2, picSize, err);
                        nowInfo.setState("1");
                        activity.mGvAdapter.notifyDataSetChanged();
                        break;
                    case MSG_ERROR:
                        if (obj != null) {
                            err = obj.toString();
                        }
                        err = "上传失败:" + err;
                        activity.uploadResult += "图片" + arg1 + ":" + err + "！！！\n";
                        solveResult(activity,arg1, arg2, picSize, err);
                        break;
                }

        }
        private void solveResult(ReUploadActivity activity ,int arg1, int arg2, int picSize, String err) {
            activity.count++;
            if (arg2 == 1) {
                activity.uploadResult = "图片" + arg1 + ":" + err + "\n";
                activity.showFinalDialog(activity.uploadResult);
            } else {
                activity.pd.setMessage("上传了" + (activity.count) + "/" + activity.uploadPicInfos.size());
                if (activity.count >= picSize) {
                    activity.showFinalDialog(activity.uploadResult);
                }
            }
        }
    }

    protected Handler nHandler = new NoLeakHandler(this);
    protected static final int MSG_SUCCESS = 0;
    protected static final int MSG_ERROR = 1;
    private String typeFlag;
    protected boolean isTest = false;
    private boolean isCaigou = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        failPath = intent.getStringExtra("failPath");
        typeFlag = intent.getStringExtra("caigou");
        isCaigou = "caigou".equals(typeFlag);
        String failPid = intent.getStringExtra("failPid");
        if (failPid != null) {
            edPid.setText(failPid);
        }
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
                if (checkPid(5)) {
                    return;
                }
                if (!uploadPicInfo.getState().equals("-1")) {
                    MyToast.showToast(mContext, "当前图片已经上传完成");
                    return;
                }
                Button btn = (Button) v;
                btn.setText("正在上传");
                showProgressDialog();
                upload(position, false);
            }
        });
        gv.setAdapter(mGvAdapter);
    }

    protected void upload(final int position, final boolean isMulti) {
        if (MyApp.id == null) {
            showFinalDialog("当前登陆人为空，请重启程序尝试");
            return;
        }
        final UploadPicInfo item = uploadPicInfos.get(position);
        String insertPath = "";
        String remoteName = "";
        String remotePath = "";
        String mUrl = "";
        FTPUtils ftpUtil = null;
        //重新上传失败的文件
        String nowPath = item.getPath();
        if (isCaigou) {
            mUrl = CaigouActivity.ftpAddress;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName, FtpManager.ftpPassword);
            String fileName = nowPath.substring(nowPath.lastIndexOf("/") + 1, nowPath.lastIndexOf("."));
            remoteName = getRemarkName(fileName, false);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
        } else {
            mUrl = MyApp.ftpUrl;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName, FtpManager.ftpPassword);
            String fileName = nowPath.substring(nowPath.lastIndexOf("/") + 1, nowPath.lastIndexOf("."));
            remoteName = getRemarkName(fileName, false);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
        }
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
                } else {
                    msg.what = MSG_ERROR;
                    msg.obj = err;
                }
                msg.sendToTarget();
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
                        flag = "SCCG";
                        res = setSSCGPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, remoteName,
                                insertPath, flag);
                    } else {
                        flag = "CKTZ";
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
                if (checkPid(5)) {
                    return;
                }
                showProgressDialog();
                count = 0;
                uploadResult = "";
                for (int i = 0; i < uploadPicInfos.size(); i++) {
                    UploadPicInfo item = uploadPicInfos.get(i);
                    if (item.getState().equals("-1")) {
                        upload(i, true);
                    } else {
                        count++;
                    }
                }
                if (count == uploadPicInfos.size()) {
                    MyToast.showToast(mContext, "所有图片已上传完成");
                    pd.cancel();
                }
                break;
            case R.id.review_getFromPhone:
                super.onClick(v);
                break;
        }
    }
}
