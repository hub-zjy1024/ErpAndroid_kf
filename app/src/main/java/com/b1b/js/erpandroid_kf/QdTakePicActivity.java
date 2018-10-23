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
import android.os.Message;
import android.support.v7.app.NotificationCompat;
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
import java.util.Map;

import utils.common.ImageWaterUtils;
import utils.common.MyFileUtils;
import utils.common.MyImageUtls;
import utils.common.UploadUtils;
import utils.handler.NoLeakHandler;
import utils.net.ftp.FTPUtils;
import utils.net.ftp.FtpManager;
import utils.net.wsdelegate.MartService;

/**
 * Created by 张建宇 on 2018/10/31.
 */
public class QdTakePicActivity extends TakePicActivity {
    private Context mContext = QdTakePicActivity.this;
    private LinearLayout llResult;
    private NotificationManager notificationManager;

    public String getUploadRemotePath() {
        String remoteName = UploadUtils.createSHQD_Rm(pid);
        return  "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
    }

    private NoLeakHandler mHandler = new NoLeakHandler(this);

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
        }
    }

    private Map<Integer, String> map = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llResult = (LinearLayout) findViewById(R.id.take_pic2_result_containner);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void upLoadPic(final int cRotate, final byte[] picData) {
        final long first = System.currentTimeMillis();
        final File sFile = MyFileUtils.getFileParent();
        if (sFile == null) {
            showMsgToast("无法获取存储路径，请换用普通拍照功能");
            return;
        }
        mCamera.startPreview();
        auto.start();
        isPreview = true;
        toolbar.setVisibility(View.GONE);
        btn_takepic.setEnabled(true);
        int id = (int) (Math.random() * 1000000);
        while (true) {
            if (map.get(id) != null) {
                id = (int) (Math.random() * 1000000);
            } else {
                map.put(id, String.valueOf(id));
                break;
            }
        }
        final int finalId = id;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.notify_icon_large);
        builder.setContentTitle("上传" + pid + "的图片").setSmallIcon(R.mipmap.notify_icon)
                .setContentText("图片正在上传").setProgress(100, 0, false).setLargeIcon(largeIcon);
        //载入水印图
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
        final byte[] nDatas = Arrays.copyOf(picData, picData.length);
        Date date = new Date();
        int minute = date.getMinutes();
        int ss = date.getSeconds();
        String upTime = minute + ":" + ss;
        final TextView textView = new TextView(mContext);
        textView.setBackgroundColor(getResources().getColor(R.color.color_tv_result_transparent));
        textView.setText("图片:" + upTime + "正在上传");
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        float fontSize = 18;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setTag(upTime);
        llResult.addView(textView);
        Runnable tempThread = new Runnable() {
            @Override
            public void run() {

                long time2 = System.currentTimeMillis();
                Bitmap bmp = BitmapFactory.decodeByteArray(nDatas, 0, nDatas.length);
                Matrix matrixs = new Matrix();
                matrixs.setRotate(90 + cRotate);
                Bitmap photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);
                Bitmap textBitmap;
                Bitmap waterBitmap;
                try {
                    waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext, photo, bitmap);
                    textBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap, pid, (int)
                            (photo.getWidth()
                                    * 0.015), Color.RED, 20, 20);
                } catch (OutOfMemoryError error) {
                    error.printStackTrace();
                    showMsgToast("请选择合适的尺寸，重新拍摄");
                    showSizeChoiceDialog(parameters);
                    return;
                }
                String rmPath = getUploadRemotePath();
                String remoteName = rmPath.substring(rmPath.lastIndexOf("/") + 1);
                String notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                final File upFile = new File(sFile, "dyj_img/" + remoteName);
                File dyjImgDir = upFile.getParentFile();
                if (!dyjImgDir.exists()) {
                    dyjImgDir.mkdirs();
                }
                FileOutputStream fio = null;
                try {
                    fio = new FileOutputStream(upFile);
                    MyImageUtls.compressBitmapAtsize(textBitmap, fio, 0.4f);
                    MyImageUtls.releaseBitmap(bmp);
                    MyImageUtls.releaseBitmap(textBitmap);
                    MyImageUtls.releaseBitmap(waterBitmap);
                    MyImageUtls.releaseBitmap(photo);
                    fio.close();
                    String insertPath;
                    Intent mIntent = new Intent(mContext, ReUploadActivity.class);
                    mIntent.putExtra("failPid", pid);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra("failPath", upFile.getAbsolutePath());
                    mIntent.putExtra("nfId", finalId);
                    PendingIntent pIntent = PendingIntent.getActivity(mContext, 100, mIntent, PendingIntent
                            .FLAG_UPDATE_CURRENT);
                    boolean isStop = false;
                    int counts = 0;
                    final String tag = textView.getTag().toString();
                    FTPUtils ftpUtil = null;
                    while (!isStop) {
                        String remotePath = getUploadRemotePath();
                        remoteName = remotePath.substring(remotePath.lastIndexOf("/") + 1);
                        notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                        String msg = "";
                        try {
                            FileInputStream fis = new FileInputStream(upFile);
                            boolean upSuccess = false;
                            String mUrl;
                            mUrl = FtpManager.mainAddress;
                            ftpUtil = FtpManager.getTestFTPMain();
                            ftpUtil.login(30);
                            insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                            Log.e("zjy", "QdTakePicActivity->run(): InsertPath==" + insertPath);
                            upSuccess = ftpUtil.upload(fis, remotePath);
                            if (upSuccess) {
                                while (true) {
                                    //更新服务器信息
                                    if (!CheckUtils.isAdmin()) {
                                        isStop = true;
                                        notificationManager.cancel(finalId);
                                        map.remove(finalId);
                                        Message message = Message.obtain(mHandler, PICUPLOAD_SUCCESS);
                                        message.obj = textView;
                                        message.sendToTarget();
                                        break;
                                    }
                                    try {
                                        String res = setInsertPicInfo("", cid, did,
                                                Integer.parseInt(loginID), pid, remoteName
                                                , insertPath, "SHQD");
                                        Log.e("zjy", "QdTakePicActivity.java-> setInsertPicInfo==" + res);
                                        if (res.equals("1")) {
                                            double totalTime = (double) (System.currentTimeMillis() -
                                                    first) / 1000;
                                            double runTime = (double) (System.currentTimeMillis() - time2)
                                                    / 1000;
                                            isStop = true;
                                            notificationManager.cancel(finalId);
                                            map.remove(finalId);
                                            if (totalTime - runTime > 1) {
                                                MyApp.myLogger.writeBug("Task Wait SoLong");
                                            }
                                            String strCounts = ",counts=";
                                            if (counts > 0) {
                                                strCounts += counts;
                                            } else {
                                                strCounts = "";
                                            }
                                            double checkRate = 1.6;
                                            if (runTime > checkRate) {
                                                MyApp.myLogger.writeInfo("chuku Qd2 finish：" +
                                                        remoteName + "\ttime=" + runTime + "/" +
                                                        totalTime + strCounts);
                                            } else {
                                                MyApp.myLogger.writeInfo("chuku Qd2 finish：" +
                                                        remoteName + " time<" + checkRate);
                                            }
                                            Log.e("zjy", "QdTakePicActivity->run(): upload " +
                                                    "succes time=="
                                                    + runTime + "/" + totalTime + strCounts);
                                            Message message = Message.obtain(mHandler, PICUPLOAD_SUCCESS);
                                            message.obj = textView;
                                            message.sendToTarget();
                                            break;
                                        } else {
                                            msg = "插入图片信息失败,多次出现请联系后台";
                                        }
                                    } catch (IOException e) {
                                        msg = "连接服务器失败,正在重试";
                                        String ioMsg = e.getMessage();
                                        MyApp.myLogger.writeError("Qd2 upload Exception:" + pid + "\t" +
                                                remoteName + "-" + ioMsg);
                                        if (ioMsg.contains("EHOSTUNREACH")) {
                                            msg = "网络连接有误，正在重试";
                                        }
                                        e.printStackTrace();
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText("图片:" + tag + "重新关联中....");
                                        }
                                    });
                                    changeNotificationMsg(builder, finalId, notifyName + msg, 0, pIntent);
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                MyApp.myLogger.writeError("Qd2 upload false:" + remoteName);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            MyApp.myLogger.writeError("Qd2 upload Exception:" + remoteName + "-" + e
                                    .getMessage());
                            changeNotificationMsg(builder, finalId, notifyName + "上传失败，正在重新上传", 0, pIntent);
                        }
                        if (ftpUtil != null) {
                            ftpUtil.exitServer();
                        }
                        if (isStop) {
                            break;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("图片:" + tag + "重新上传中....");
                            }
                        });
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        counts++;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(4);
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(4);
                }
            }
        };
        MyApp.cachedThreadPool.execute(tempThread);
    }

    private void changeNotificationMsg(NotificationCompat.Builder builder, int finalId, String msg, int
            progress, PendingIntent
            pIntent) {
        if (pIntent != null) {
            builder.setContentIntent(pIntent);
        }
        builder.setProgress(100, progress, false).setSubText(msg);
        notificationManager.notify(finalId, builder.build());
    }

    @Override
    public String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String
            fileName, String filePath, String stypeID) throws IOException, XmlPullParserException {
        return MartService.InsertPicInfo(String.valueOf(uid), fileName, filePath, stypeID);
    }

    @Override
    public boolean getInsertResultMain(String remoteName, String insertPath) throws IOException,
            XmlPullParserException {
        String result = setInsertPicInfo("", cid, did, Integer
                .parseInt(loginID), pid, remoteName, insertPath, "SHQD");
        MyApp.myLogger.writeInfo("takepic QD insert:" + remoteName + "=" + result);
        return "操作成功".equals(result);
    }
}
