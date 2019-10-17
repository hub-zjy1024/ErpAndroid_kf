package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import utils.common.ImageWaterUtils;
import utils.common.MyFileUtils;
import utils.common.MyImageUtls;
import utils.common.UploadUtils;
import utils.handler.NoLeakHandler;
import utils.net.ftp.FTPUtils;
import utils.net.wsdelegate.WebserviceUtils;

public class CaigouTakePic2Activity extends TakePicActivity implements View.OnClickListener {

    NotificationManager notificationManager;
    private final static int ERROR_NO_SD = 2;
    //TakePic2Ac
    //    @Override
    //    public String getUploadFlag() {
    //        return "SCCG";
    //    }
    //
    //    @Override
    //    public void initUploadInfos() {
    //        super.initUploadInfos();
    //        mUrl = FTPUtils.DB_HOST;
    //    }
    //
    //    @Override
    //    protected PicUploader getUpLoader() {
    //        return new CaigouFtpUploader(mUrl);
    //    }
    //
    //    @Override
    //    public String getUploadRemotePath() {
    //        String remoteName = UploadUtils.createSCCGRemoteName(pid) + ".jpg";
    //        String remotePath = UploadUtils.getCaigouRemoteDir(remoteName);
    //        return remotePath;
    //    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PICUPLOAD_ERROR:
                btn_takepic.setEnabled(true);
                toolbar.setVisibility(View.GONE);
                break;
            case PICUPLOAD_SUCCESS:
                Object obj = msg.obj;
                final TextView textView = (TextView) obj;
                String nowTag = textView.getTag().toString();
                textView.setText("图片:" + nowTag + "上传完成 OK···");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        llResult.removeView(textView);
                    }
                }, 2000);
                showMsgToast("后台剩余图片：" + (MyApp.cachedThreadPool.getActiveCount() - 1));
                break;
            case ERROR_NO_SD:
                showMsgToast("sd卡不存在，不可用后台上传");
                btn_commit.setEnabled(false);
                break;
        }
    }

    protected Handler mHandler = new NoLeakHandler(this);
    private HashMap<Integer, String> map = new HashMap<>();
    private LinearLayout llResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llResult = (LinearLayout) findViewById(R.id.take_pic2_result_containner);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.main_commit) {
            final File sFile = MyFileUtils.getFileParent();
            if (sFile == null) {
                showMsgToast("无法获取存储路径，请换用普通拍照功能");
                return;
            }
            toolbar.setVisibility(View.GONE);
            btn_takepic.setEnabled(true);
            mCamera.startPreview();
            auto.start();
            isPreview = true;
            int id = (int) (Math.random() * 1000000);
            while (true) {
                if (map.containsKey(id)) {
                    id = (int) (Math.random() * 1000000);
                } else {
                    map.put(id, String.valueOf(id));
                    break;
                }
            }
            final int finalId = id;
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.notify_icon_large);
            builder.setContentTitle("采购上传" + pid + "的图片").setSmallIcon(R.mipmap.notify_icon)
                    .setContentText("图片正在上传").setProgress(100, 0, false).setLargeIcon(largeIcon);
            //载入水印图
            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
            final byte[] cData = Arrays.copyOf(tempBytes, tempBytes.length);
            final int cRotate = super.tempRotate;
            final TextView textView = getUploadTv(mContext);
            llResult.addView(textView);
            Runnable tempThread = new Runnable() {
                @Override
                public void run() {
                    final Bitmap textBitmap;
                    try {
                        Bitmap bmp = BitmapFactory.decodeByteArray(cData, 0, cData.length);
                        Matrix matrixs = new Matrix();
                        matrixs.setRotate(90 + cRotate);
                        Bitmap photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                                matrixs, true);
                        Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext, photo,
                                bitmap);
                        textBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap, pid, (int)
                                (photo
                                        .getWidth() * 0.015), Color.RED, 20, 20);
                        MyImageUtls.releaseBitmap(bitmap);
                        MyImageUtls.releaseBitmap(photo);
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showMsgToast("请选择合适的尺寸，重新拍摄!!");
                                showFinalDialog("请选择合适的尺寸，重新拍摄!!");
                            }
                        });
                        return;
                    }
                    String remoteName = UploadUtils.createSCCGRemoteName(pid);
                    String notifyName = remoteName;
                    String localPath = remoteName;
                    final File upFile = new File(sFile, "dyj_img/" + localPath + ".jpg");
                    File dyjImgDir = upFile.getParentFile();
                    if (!dyjImgDir.exists()) {
                        dyjImgDir.mkdirs();
                    }
                    FileOutputStream fio = null;
                    try {
                        fio = new FileOutputStream(upFile);
                        MyImageUtls.compressBitmapAtsize(textBitmap, fio, 0.4f);
                        MyImageUtls.releaseBitmap(textBitmap);
                        fio.close();
                        String insertPath = null;
                        Intent mIntent = new Intent(mContext,
                                ReUploadActivity.class);
                        mIntent.putExtra("failPid", pid);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mIntent.putExtra("failPath", upFile.getAbsolutePath());
                        mIntent.putExtra("nfId", finalId);
                        mIntent.putExtra("flag", "caigou");
                        PendingIntent pIntent = PendingIntent.getActivity
                                (mContext, 100, mIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                        pIntent = null;
                        boolean isStop = false;
                        String commonMessage = "上传失败,等待再次上传";
                        String message = "";
                        while (!isStop) {
                            FileInputStream localInputStream = null;
                            boolean upSuccess = false;
                            try {
                                localInputStream = new FileInputStream(upFile);
                                String remotePath;
                                remoteName = UploadUtils.createSCCGRemoteName(pid);
                                String mUrl = null;
                                FTPUtils ftpUtil = null;
                                if (CheckUtils.isAdmin()) {
                                    mUrl = FTPUtils.mainAddress;
                                    ftpUtil = FTPUtils.getTestFTP();
                                    remotePath = UploadUtils.getTestPath(pid);
                                } else {
                                    mUrl = FTPUtils.CaigouFTPAddr;
                                    ftpUtil = FTPUtils.getGlobalFTP();
                                    remotePath = UploadUtils.getCaigouRemoteDir(remoteName + ".jpg");
                                }
                                insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                                ftpUtil.login();
                                Log.e("zjy", "TakePic2Activity->run(): InsertPath==" + insertPath);
                                changeNotificationMsg(builder, finalId, notifyName + "正在准备上传", 0, pIntent);
                                upSuccess = ftpUtil.upload(localInputStream, remotePath);
                                ftpUtil.exitServer();
                            } catch (IOException e) {
                                e.printStackTrace();
                                message = commonMessage;
                            } finally {
                                if (localInputStream != null) {
                                    try {
                                        localInputStream.close();
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                            }
                            if (upSuccess) {
                                while (true) {
                                    //更新服务器信息
                                    try {
                                        String res = "连接失败";
                                        if (CheckUtils.isAdmin()) {
                                            res = "操作成功";
                                        } else {
                                            res = ObtainPicFromPhone.setSSCGPicInfo
                                                    (WebserviceUtils.WebServiceCheckWord, cid, did,
                                                            Integer.parseInt(loginID), pid,
                                                            remoteName + ".jpg", insertPath, "SCCG");
                                        }
                                        Log.e("zjy", "TakePic2Activit.java->setInsertPicInfo == " + res);
                                        //                   String res = "操作成功";
                                        if (res.equals("操作成功")) {
                                            isStop = true;
                                            notificationManager.cancel(finalId);
                                            map.remove(finalId);
                                            MyApp.myLogger.writeInfo("background upload caigou success：" +
                                                    pid + "\t" +
                                                    remoteName);
                                            Message hMsg = Message.obtain(mHandler, PICUPLOAD_SUCCESS);
                                            hMsg.obj = textView;
                                            hMsg.sendToTarget();
                                            break;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    }
                                    message = "关联图片失败";
                                    changeNotificationMsg(builder, finalId, notifyName + message, 0, null);
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                message = commonMessage;
                            }
                            changeNotificationMsg(builder, finalId, notifyName + message, 0, null);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(ERROR_NO_SD);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(ERROR_NO_SD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fio != null) {
                            try {
                                fio.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            MyApp.cachedThreadPool.execute(tempThread);
        } else {
            super.onClick(v);
        }
    }

    @NonNull
    protected TextView getUploadTv(Context mContext) {
        final TextView textView = new TextView(mContext);
        Date date = new Date();
        int minute = date.getMinutes();
        int ss = date.getSeconds();
        String upTime = minute + ":" + ss;
        textView.setBackgroundColor(getResources().getColor(R.color.color_tv_result_transparent));
        textView.setText("图片:" + upTime + "正在上传");
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        float fontSize = 18;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setTag(upTime);
        return textView;
    }

    private void changeNotificationMsg(NotificationCompat.Builder builder, int finalId, String msg, int
            progress, PendingIntent
                                               pIntent) {
        if (pIntent != null) {
            builder.setContentIntent(pIntent);
        }
        builder.setProgress(100, progress, false).setContentText(msg);
        notificationManager.notify(finalId, builder.build());
    }


}
