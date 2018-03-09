package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UpLoadPicRunable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import utils.FTPUtils;
import utils.FtpManager;
import utils.ImageWaterUtils;
import utils.MyImageUtls;
import utils.UploadUtils;
import utils.WebserviceUtils;

public class TakePicChildPanku extends TakePicActivity {

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PICUPLOAD_ERROR:
                    String msgReason = "上传图片失败，请检查网络并重新拍摄";
                    String str = msg.obj != null ? msg.obj.toString() : null;
                    if (str != null) {
                        msgReason = str;
                    }
                    showFinalDialog(msgReason + "!!!");
                    btn_takepic.setEnabled(true);
                    toolbar.setVisibility(View.GONE);
                    break;
                case PICUPLOAD_SUCCESS:
                    String strOk = msg.obj != null ? msg.obj.toString() : null;
                    String msgOk = "上传成功";
                    if (strOk != null) {
                        msgOk = strOk;
                    }
                    showFinalDialog(msgOk);
                    btn_takepic.setEnabled(true);
                    toolbar.setVisibility(View.GONE);
                    break;
            }

        }
    };

    private Bitmap mWBitmap = null;
    public Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                final int mRotate = tempRotate;
                final byte[] tempBytes = Arrays.copyOf(TakePicChildPanku.super.tempBytes, TakePicChildPanku.super
                        .tempBytes.length);
                String remotePath = "";
                String insertPath = "";
                FTPUtils ftpUtil = null;
                String mUrl = "";
                String remoteName = UploadUtils.getPankuRemoteName(pid);
                if ("101".equals(MyApp.id)) {
                    mUrl = FtpManager.mainAddress;
                    ftpUtil = new FTPUtils(mUrl, FtpManager.mainName, FtpManager.mainPwd);
                    remotePath = UploadUtils.KF_DIR + "pk/" + remoteName + ".jpg";
                } else {
                    mUrl = MyApp.ftpUrl;
                    ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,
                            FtpManager.ftpPassword);
                    remotePath = "/" + UploadUtils.getCurrentDate() + "/pk/" + remoteName + ".jpg";
                }
                insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                Runnable mRunnable = new UpLoadPicRunable(remotePath, insertPath, ftpUtil, mHandler) {



                    @Override
                    public boolean getInsertResult() throws Exception {
                        SharedPreferences sp = TakePicChildPanku.super
                                .userInfo;
                        final int cid = sp.getInt("cid", -1);
                        final int did = sp.getInt("did", -1);
                        String remoteName = getRemoteName();
                        String insertPath = getInsertpath();
                        String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer
                                .parseInt(MyApp.id), pid, remoteName, insertPath, "PK");
                        Log.e("zjy", "TakePicActivity.java->run(): insertPath result==" + insertPath + "\t" + res);
                        return "操作成功".equals(res);
                    }

                    @Override
                    public InputStream getInputStream() throws Exception {
                        if (mWBitmap == null) {
                            mWBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                        }
                        Bitmap bmp = BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.length);
                        Matrix matrixs = new Matrix();
                        matrixs.setRotate(90 + mRotate);
                        Bitmap photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);
                        Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext,
                                photo, mWBitmap, 0, 0);
                        Bitmap TextBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap,
                                pid, (int) (photo.getWidth() * 0.015), Color.RED, 20, 20);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        //图片质量压缩到bao数组
                        MyImageUtls.compressBitmapAtsize(TextBitmap, bao, 0.4f);
                        MyImageUtls.releaseBitmap(photo);
                        MyImageUtls.releaseBitmap(waterBitmap);
                        MyImageUtls.releaseBitmap(TextBitmap);
                        return new ByteArrayInputStream(bao.toByteArray());
                    }
                };
                TaskManager.getInstance().execute(mRunnable);
            }
        });
    }
}
