package com.b1b.js.erpandroid_kf.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.ObtainPicFromPhone;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

public class UpLoadService extends Service {
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private NotificationManager notifyManager;
    SharedPreferences userInfoSp ;
    public UpLoadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        userInfoSp = getSharedPreferences("UserInfo", 0);
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("zjy", "UpLoadService.java->onCreate(): ==");
    }

    private MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("zjy", "UpLoadService.java->onBind(): ==");
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("zjy", "UpLoadService.java->onStartCommand(): ==" + this.toString());
        final String imgName = intent.getStringExtra("imgName");
        final String pid = intent.getStringExtra("pid");
        final int finalId = (int) System.currentTimeMillis();
        final int cid = userInfoSp.getInt("cid", -1);
        final int did = userInfoSp.getInt("did", -1);
        new Thread(){
            @Override
            public void run() {
                FTPClient mFtpClient = new FTPClient();
                String remoteName =imgName;
                String insertPath = UploadUtils.createInsertPath(MyApp.ftpUrl, UploadUtils.getRemoteDir(), remoteName, "jpg");
                File sFile = new File(Environment.getExternalStorageDirectory(), "dyj_img/");
                if (!sFile.exists()) {
                    sFile.mkdirs();
                }
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(UpLoadService.this);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.notify_icon_large);
                builder.setContentTitle("上传" + pid + "的图片").setSmallIcon(R.mipmap.notify_icon)
                        .setContentText("图片正在上传").setProgress(100, 0, false).setLargeIcon(largeIcon);
                File upFile = new File(sFile, remoteName + ".jpg");
                //存储水印图
                FileOutputStream fio = null;
                //失败重新上传
                Intent mIntent = new Intent(UpLoadService.this, ObtainPicFromPhone.class);
                mIntent.putExtra("failPid", pid);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.putExtra("failPath", upFile.getAbsolutePath());
                mIntent.putExtra("nfId", finalId);
                PendingIntent pIntent = PendingIntent.getActivity(UpLoadService.this, 100, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                boolean isStop = false;
                while (!isStop) {
                    try {
                        //连接服务器
                        mFtpClient.connect(MyApp.ftpUrl, 21);
                        notifyManager.notify(finalId, builder.build());
                        boolean isConnected;
                        if ("101".equals(MyApp.id)) {
                            isConnected = mFtpClient.login("NEW_DYJ", "GY8Fy2Gx");
                        } else {
                            isConnected = mFtpClient.login("dyjftp", "dyjftp");
                        }
                        if (isConnected) {
                            Log.e("zjy", "UploadService.java->connectAndLogin(): connSuccess" + Runtime.getRuntime().maxMemory());
                            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
                            mFtpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                            mFtpClient.enterLocalPassiveMode();
                        } else {
                            changeNotificationMsg(builder, finalId, "登录服务器失败", 0, pIntent);
//                            mHandler.sendEmptyMessage(FTP_CONNECT_FAIL);
                        }
                    } catch (IOException e) {
                        changeNotificationMsg(builder, finalId, "连接服务器失败", 0, pIntent);
//                        mHandler.sendEmptyMessage(FTP_CONNECT_FAIL);
                        e.printStackTrace();
                    }
                    try {
                        FileInputStream fis = new FileInputStream(upFile);
                        OutputStream outputStream;
                        if ("101".equals(MyApp.id)) {
                            //测试专用
                            insertPath = "ftp://" + MyApp.ftpUrl + "/ZJy/" + remoteName + ".jpg";
                            outputStream = mFtpClient.storeFileStream("/ZJy/" + remoteName + ".jpg");
                        } else {
                            if (!mFtpClient.changeWorkingDirectory("/" + UploadUtils.getRemoteDir())) {
                                mFtpClient.makeDirectory("/" + UploadUtils.getRemoteDir());
                                mFtpClient.changeWorkingDirectory("/" + UploadUtils.getRemoteDir());
                            }
                            outputStream = mFtpClient.storeFileStream("/" + UploadUtils.getRemoteDir() + "/" + remoteName + ".jpg");
                        }
                        boolean success = false;
                        if (outputStream != null) {
                            int len;
                            byte[] buf = new byte[10 * 1024];
                            int all = fis.available();
                            int writed = 0;
                            while ((len = fis.read(buf)) != -1) {
                                outputStream.write(buf, 0, len);
                                writed += len;
                                int progress = writed * 100 / all;
                                changeNotificationMsg(builder, finalId, remoteName + "上传了" + progress + "%", progress, null);
                            }
                            fis.close();
                            outputStream.close();
                            success = true;
                        } else {
                            changeNotificationMsg(builder, finalId, remoteName + "写入服务器失败，点击重新上传", 0, pIntent);
                        }
                        mFtpClient.completePendingCommand();
                        //                            mFtpClient.logout();
                        //                            mFtpClient.disconnect();
                        if (success) {
                            //更新服务器信息
                            Log.e("zjy", "UploadService.java->run(): insertPath==" + insertPath + "\t" + cid + "\t" + did);
                            String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer.parseInt(MyApp.id), pid, remoteName + ".jpg", insertPath, "CKTZ");
                            if (res.equals("操作成功")) {
                                isStop = true;
                                notifyManager.cancel(finalId);
                                MyApp.totoalTask.remove(this);
                                Log.e("zjy", "UploadService.java->run(): insertSuccess==");
                            } else {
                                changeNotificationMsg(builder, finalId, remoteName + "上传失败,点击重新上传", 0, pIntent);
                            }
//                            Message msg = mHandler.obtainMessage(PICUPLOAD_SUCCESS);
//                            msg.obj = res;
//                            mHandler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        changeNotificationMsg(builder, finalId, remoteName + "上传失败,点击重新上传", 0, pIntent);
                        //                                mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        changeNotificationMsg(builder, finalId, remoteName + "上传失败,点击重新上传", 0, pIntent);
                        //                                mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        return Service.START_STICKY;
    }
    private void changeNotificationMsg(NotificationCompat.Builder builder, int finalId, String msg, int progress, PendingIntent pIntent) {
        if (pIntent != null) {
            builder.setContentIntent(pIntent);
        }
        builder.setProgress(100, progress, false).setContentText(msg);
        notifyManager.notify(finalId, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.e("zjy", "UpLoadService.java->onDestroy(): service ondestory==");
        super.onDestroy();
    }
    public String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath, String stypeID) throws IOException, XmlPullParserException {
        String str = "";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("cid", cid);
        map.put("did", did);
        map.put("uid", uid);
        map.put("pid", pid);
        map.put("filename", fileName);
        map.put("filepath", filePath);
        map.put("stypeID", stypeID);//标记，固定为"CKTZ"
        SoapObject request = WebserviceUtils.getRequest(map, "SetInsertPicInfo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        str = response.toString();
        return str;
    }
    public class MyBinder extends Binder {
        public void upLoad() {
            Log.e("zjy", "UpLoadService.java->upLoad(): ==");
        }
    }
}
