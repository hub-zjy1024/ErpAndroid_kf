package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoActivity;
import com.b1b.js.erpandroid_kf.entity.PicUploadInfo;
import com.b1b.js.erpandroid_kf.receiver.NetBroadcastReceiver;
import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import utils.camera.AutoFoucusMgr;
import utils.camera.CamRotationManager;
import utils.camera.CustomAutoFocus;
import utils.common.ImageWaterUtils;
import utils.common.MyFileUtils;
import utils.common.MyImageUtls;
import utils.common.UploadUtils;
import utils.dbutils.PicUploadDB;
import utils.handler.NoLeakHandler;
import utils.net.ftp.FTPUtils;
import utils.net.wsdelegate.ChuKuServer;

public class TakePic2Activity extends SavedLoginInfoActivity implements View.OnClickListener, NoLeakHandler
        .NoLeakCallback {

    private int rotation = 0;
    private SurfaceView surfaceView;
    private Button btn_tryagain;
    private Button btn_setting;
    private Button btn_commit;
    private Button btn_takepic;
    private SurfaceHolder mHolder;
    private LinearLayout toolbar;
    private Camera.Parameters parameters;
    private Camera camera;
    private boolean isPreview = true;
    private Bitmap photo;
    private List<Camera.Size> picSizes;
    private String pid;
    NotificationManager notificationManager;
    private final static int FTP_CONNECT_FAIL = 3;
    private final static int PICUPLOAD_SUCCESS = 0;
    private final static int PICUPLOAD_ERROR = 1;
    private String kfFTP = MyApp.ftpUrl;
    private int tempRotate = 0;
    int cid;
    int did;
    private Context mContext = TakePic2Activity.this;
    private Handler mHandler = new NoLeakHandler(this);
    private NetBroadcastReceiver netWorkChecker;
    PicUploadDB picDb;

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
                //                    MyToast.showToast(mContext, "后台剩余图片：" + MyApp.totoalTask.size());
                showMsgToast( "上传成功，后台剩余图片：" + (MyApp.cachedThreadPool.getActiveCount() - 1));
                break;
            case FTP_CONNECT_FAIL:
                showMsgToast( "连接ftp服务器失败，请检查网络");
                break;
            case 4:
                showMsgToast( "sd卡不存在，不可用后台上传");
                btn_commit.setEnabled(false);
                break;
        }
    }

    private CamRotationManager rotationManager;
    private SharedPreferences sp;
    private int itemPosition;
    private AlertDialog inputDialog;
    private SparseArray<String> map = new SparseArray<>();
    private AutoFoucusMgr auto;
    private CustomAutoFocus cAutoFocusMgr;
    private byte[] tempBytes;
    private LinearLayout llResult;
    private TextView tvPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepic_main);
        final FrameLayout container = (FrameLayout) findViewById(R.id.take_pic2_container);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        btn_tryagain = (Button) findViewById(R.id.main_tryagain);
        btn_commit = (Button) findViewById(R.id.main_commit);
        btn_takepic = (Button) findViewById(R.id.btn_takepic);
        btn_setting = (Button) findViewById(R.id.takepic_btn_setting);
        toolbar = (LinearLayout) findViewById(R.id.main_toolbar);
        llResult = (LinearLayout) findViewById(R.id.take_pic2_result_containner);
        tvPid = (TextView) findViewById(R.id.activity_take_pic_tvpid);
        btn_setting.setOnClickListener(this);
        btn_takepic.setOnClickListener(this);
        btn_tryagain.setOnClickListener(this);
        btn_commit.setOnClickListener(this);
        surfaceView.setZOrderMediaOverlay(false);
        surfaceView.setZOrderOnTop(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("请输入单据号");
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_inputpid, null);
        final EditText dialogPid = (EditText) v.findViewById(R.id.dialog_inputpid_ed);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pid = dialogPid.getText().toString();
                checkPid(mContext, pid);
            }
        });
        //         netWorkChecker = new NetBroadcastReceiver(new NetBroadcastReceiver.StateCallback() {
        //            @Override
        //            public void onNetChange(int state) {
        //                if (state == NetBroadcastReceiver.NETWORK_NONE) {
        //                    showMsgToast( "网络连接断开！！！！");
        //                } else if (state == NetBroadcastReceiver.NETWORK_WIFI) {
        //                    showMsgToast( "连接到WIFI");
        //                } else if (state == NetBroadcastReceiver.NETWORK_MOBILE) {
        //                    showMsgToast( "连接到移动网络");
        //                }
        //            }
        //        });
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder.setNegativeButton("取消", null);
        builder.setView(v);
        inputDialog = builder.create();
        pid = getIntent().getStringExtra("pid");
        if (pid != null) {
            tvPid.setText(pid);
            dialogPid.setText(pid);
        }
        rotationManager = new CamRotationManager(this);
        rotationManager.attachToSensor();
        //成功或失败的提示框
        SharedPreferences userInfoSp = getSharedPreferences(SettingActivity.PREF_USERINFO, 0);
        cid = userInfoSp.getInt("cid", -1);
        did = userInfoSp.getInt("did", -1);
        //获取surfaceholder
        mHolder = surfaceView.getHolder();
        //添加SurfaceHolder回调
        if (mHolder != null) {
            mHolder.setKeepScreenOn(true);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    int counts = Camera.getNumberOfCameras();
                    if (counts == 0) {
                        showMsgToast( "设备无摄像头");
                        return;
                    }
                    camera = Camera.open(0); // 打开摄像头
                    if (camera == null) {
                        showMsgToast( "检测不到摄像头");
                        return;
                    }
                    //设置旋转角度
                    camera.setDisplayOrientation(TakePicActivity.getPreviewDegree((TakePic2Activity)
                            mContext));
                    //设置parameter注意要检查相机是否支持，通过parameters.getSupportXXX()
                    parameters = camera.getParameters();
                    String brand = Build.BRAND;
                    sp = getSharedPreferences(SettingActivity.PREF_CAMERA_INFO, 0);
                    try {
                        // 设置用于显示拍照影像的SurfaceHolder对象
                        camera.setPreviewDisplay(holder);
                        int sw = getWindowManager().getDefaultDisplay().getWidth();
                        int sh = getWindowManager().getDefaultDisplay().getHeight();
                        Point finalSize = TakePicActivity.getSuitablePreviewSize(parameters, sw, sh);
                        if (finalSize != null) {
                            parameters.setPreviewSize(finalSize.x, finalSize.y);
                        }
                        //初始化操作在开始预览之前完成
                        if (sp.getInt("width", -1) != -1) {
                            int width = sp.getInt("width", -1);
                            int height = sp.getInt("height", -1);
                            Log.e("zjy", "TakePic2Activity.java->surfaceCreated(): ==readCacheSize");
                            parameters.setPictureSize(width, height);
                            camera.setParameters(parameters);
                        } else {
                            showSizeChoiceDialog(parameters);
                        }
                        camera.startPreview();
                        isPreview = true;
                        container.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //                                auto.stop();
                                if (camera != null && isPreview) {
                                    camera.autoFocus(null);
                                }
                            }
                        });
                        auto = new AutoFoucusMgr(camera);
                        //                        cAutoFocusMgr = new CustomAutoFocus(camera);
                        //                        cAutoFocusMgr.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    if (pid == null) {
                        inputDialog.show();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    auto.stop();
                    //                    cAutoFocusMgr.stop();
                    releaseCamera();
                }
            });
        }
        picDb = new PicUploadDB(this);
    }

    /**
     * 弹出尺寸选择对话框
     * 防止照出的图片太大，内存溢出
     */
    private void showSizeChoiceDialog(final Camera.Parameters parameters) {

        picSizes = parameters.getSupportedPictureSizes();
        //剔除出尺寸太小的，和尺寸太大的，宽度（1280-2048)
        for (int i = picSizes.size() - 1; i >= 0; i--) {
            int width = picSizes.get(i).width;
            Log.e("zjy", "TakePic2Activity.java->showProgressDialog(): size==" + picSizes.get(i).width +
                    "\t" + picSizes.get(i)
                    .height);
            if (width < 1920 || width > 2592) {
                picSizes.remove(i);
            }
        }
        if (picSizes.size() > 0) {
            String[] strs = new String[picSizes.size()];
            for (int i = 0; i < picSizes.size(); i++) {
                Camera.Size size = picSizes.get(i);
                String item = size.width + "X" + size.height;
                strs[i] = item;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("选择照片大小(尽量选择大的值)");//窗口名
            dialog.setSingleChoiceItems(strs, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemPosition = which;
                        }
                    }
            );
            dialog.setNegativeButton("完成", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int width = picSizes.get(itemPosition).width;
                    int height = picSizes.get(itemPosition).height;
                    Log.e("zjy", "TakePic2Activity.java->selectSize: width==" + width + "\t" + height);
                    parameters.setPictureSize(width, height);
                    camera.setParameters(parameters);
                }
            });

            dialog.setPositiveButton("设为默认尺寸", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("zjy", "TakePic2Activity.java->onClick(): default size pos==" + itemPosition);
                    SharedPreferences.Editor editor = sp.edit();
                    int width = picSizes.get(itemPosition).width;
                    int height = picSizes.get(itemPosition).height;
                    editor.putInt("width", width);
                    editor.putInt("height", height);
                    editor.apply();
                    parameters.setPictureSize(width, height);
                    camera.setParameters(parameters);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        } else {
            showMsgToast( "没有可选的尺寸");
        }
    }



    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    public  boolean checkPid(Context mContext, String pid) {
        if (pid == null || "".equals(pid)) {
            showMsgToast( "请输入单据号");
            return true;
        } else {
            if (pid.length() < 7) {
                showMsgToast( "请输入7位单据号");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            //拍照
            case R.id.btn_takepic:
                //禁止点击拍照按钮
                if (checkPid(mContext, pid))
                    return;
                btn_takepic.setEnabled(false);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        auto.stop();
                        //                        cAutoFocusMgr.stop();
                        try {
                            camera.stopPreview();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            MyApp.myLogger.writeError(throwable);
                        }
                        tempRotate = rotationManager.getRotation();
                        isPreview = false;
                        if (data == null || data.length == 0) {
                            showMsgToast( "拍照出现错误，请重启程序");
                            tempBytes = null;
                            return;
                        }
                        tempBytes = data;
                        toolbar.setVisibility(View.VISIBLE);
                    }
                });
                break;
            //重新拍摄
            case R.id.main_tryagain:
                if (photo != null && !photo.isRecycled()) {
                    photo.recycle();
                    photo = null;
                }
                camera.startPreview();
                auto.start();
                //                cAutoFocusMgr.start();
                isPreview = true;
                btn_takepic.setEnabled(true);
                toolbar.setVisibility(View.GONE);
                break;
            //提交
            case R.id.main_commit:
                final long first = System.currentTimeMillis();
                if (tempBytes == null) {
                    showMsgToast( "当前程序出现错误,请重新进入");
                    return;
                }
                final File sFile = MyFileUtils.getFileParent();
                if (sFile == null) {
                    showMsgToast( "无法获取存储路径，请换用普通拍照功能");
                    return;
                }
                if (kfFTP == null || "".equals(kfFTP)) {

                    if (!CheckUtils.isAdmin()) {
                        showMsgToast( "读取上传地址失败，请重启程序");
                        return;
                    }
                }
                camera.startPreview();
                auto.start();
                //                cAutoFocusMgr.start();
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
                final int cRotate = tempRotate;
                final byte[] nDatas = Arrays.copyOf(tempBytes, tempBytes.length);
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
                        Bitmap photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                                matrixs, true);
                        Bitmap textBitmap;
                        Bitmap waterBitmap;
                        try {
                            waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext, photo, bitmap);
                            textBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap, pid,
                                    (int) (photo.getWidth()
                                    * 0.015), Color.RED, 20, 20);
                        } catch (OutOfMemoryError error) {
                            error.printStackTrace();
                            showMsgToast( "请选择合适的尺寸，重新拍摄");
                            showSizeChoiceDialog(parameters);
                            return;
                        }
                        String remoteName = UploadUtils.getChukuRemoteName(pid);
                        String notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                        final File upFile = new File(sFile, "dyj_img/" + remoteName + ".jpg");
                        File dyjImgDir = upFile.getParentFile();
                        if (!dyjImgDir.exists()) {
                            dyjImgDir.mkdirs();
                        }
                        FileOutputStream fio = null;
                        int okCount = 0;
                        try {
                            fio = new FileOutputStream(upFile);
                            MyImageUtls.compressBitmapAtsize(textBitmap, fio, 0.4f);
                            MyImageUtls.releaseBitmap(bmp);
                            MyImageUtls.releaseBitmap(textBitmap);
                            MyImageUtls.releaseBitmap(waterBitmap);
                            MyImageUtls.releaseBitmap(photo);
                            fio.close();
                            String insertPath = "";
                            Intent mIntent = new Intent(mContext, ReUploadActivity.class);
                            mIntent.putExtra("failPid", pid);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mIntent.putExtra("failPath", upFile.getAbsolutePath());
                            mIntent.putExtra("nfId", finalId);
                            PendingIntent pIntent = PendingIntent.getActivity(mContext, 100, mIntent,
                                    PendingIntent
                                    .FLAG_UPDATE_CURRENT);
                            boolean isStop = false;
                            int counts = 0;
                            final String tag = textView.getTag().toString();
                            FTPUtils ftpUtil = null;
                            String remotePath = "";
                            String uploadTag = "CKTZ";

                            String mUrl = FTPUtils.mainAddress;
                            String ftpStr = FTPUtils.getMainStr();
                            while (!isStop) {
                                remoteName = UploadUtils.getChukuRemoteName(pid);
                                notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                                String msg = "";
                                try {
                                    FileInputStream fis = new FileInputStream(upFile);
                                    boolean upSuccess = false;
                                    if (CheckUtils.isAdmin()) {
                                        ftpUtil = FTPUtils.getTestFTP();
                                        mUrl  = FTPUtils.TEST_FTP_ULR;
                                        ftpStr = FTPUtils.getTestFTPStr();
                                        remotePath = UploadUtils.getTestPath(pid);
                                    } else {
                                        mUrl = kfFTP;
                                        ftpStr = FTPUtils.getLocalFTPStr(mUrl);
                                        ftpUtil = FTPUtils.getLocalFTP(mUrl);
                                        remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName
                                                + ".jpg";
                                    }
                                    insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                                    Log.e("zjy", "TakePic2Activity->run(): InsertPath==" + insertPath);

                                    ftpUtil.login(15);
                                    upSuccess = ftpUtil.upload(fis, remotePath);
                                    if (upSuccess) {
                                        okCount ++;
                                        while (true) {
                                            //更新服务器信息
                                            if (CheckUtils.isAdmin()) {
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
                                                        Integer.parseInt(loginID), pid, remoteName + ".jpg"
                                                        , insertPath, uploadTag);
                                                Log.e("zjy", "TakePic2Activity.java-> setInsertPicInfo==" +
                                                        res);
                                                if (res.equals("操作成功")) {
                                                    notificationManager.cancel(finalId);
                                                    map.remove(finalId);
                                                    okCount++;

                                                    double totalTime = (double) (System.currentTimeMillis()
                                                            - first) / 1000;
                                                    double runTime = (double) (System.currentTimeMillis() -
                                                            time2) / 1000;
                                                    isStop = true;

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
                                                        MyApp.myLogger.writeInfo("chuku takepic2 finish：" +
                                                                remoteName + "\ttime=" + runTime + "/" +
                                                                totalTime + strCounts);
                                                    } else {
                                                        MyApp.myLogger.writeInfo("chuku takepic2 finish：" +
                                                                remoteName + " time<" + checkRate);
                                                    }
                                                    Log.e("zjy", "TakePic2Activity->run(): upload " +
                                                            "succes time=="
                                                            + runTime + "/" + totalTime + strCounts);
                                                    Message message = Message.obtain(mHandler,
                                                            PICUPLOAD_SUCCESS);
                                                    message.obj = textView;
                                                    message.sendToTarget();
                                                    break;
                                                } else {
                                                    msg = "插入图片信息失败,多次出现请联系后台";
                                                }
                                            } catch (IOException e) {
                                                msg = "连接服务器失败,正在重试";
                                                String ioMsg = e.getMessage();
                                                MyApp.myLogger.writeError("takepic2 upload Exception:" +
                                                        pid + "\t" +
                                                        remoteName + "-" + ioMsg);
                                                if (ioMsg.contains("EHOSTUNREACH")) {
                                                    msg = "网络连接有误，正在重试";
                                                }
                                                //                                mHandler.sendEmptyMessage
                                                // (PICUPLOAD_ERROR);
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
                                            changeNotificationMsg(builder, finalId, notifyName + msg, 0,
                                                    pIntent);
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        MyApp.myLogger.writeError("takepic2 upload false:" + remoteName);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    MyApp.myLogger.writeError("takepic2 upload Exception:" + remoteName +
                                            "-" + e.getMessage());
                                    changeNotificationMsg(builder, finalId, notifyName + "上传失败，正在重新上传", 0,
                                            pIntent);
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
                                if (counts >= 3) {
                                    PicUploadInfo upInfo = new PicUploadInfo(upFile.getAbsolutePath(), pid,
                                            uploadTag, remotePath, String.valueOf(cid), String.valueOf(did)
                                            , loginID,
                                            remoteName, insertPath);
                                    upInfo.okcount = okCount;
                                    upInfo.ftpurl = ftpStr;
                                    int res = picDb.insertRecord(upInfo);
                                    if (res != 1) {
                                        MyApp.myLogger.writeError("reupload insert=" + res);
                                    }
                                    notificationManager.cancel(finalId);
                                    map.remove(finalId);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String nowTag = textView.getTag().toString();
                                            textView.setText("图片:" + nowTag + ",多次上传失败，请检查网络后，在菜单-图片重传中再次上传");
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    llResult.removeView(textView);
                                                }
                                            }, 8000);
                                        }
                                    });
                                    Log.e("zjy", "TakePic2Activity->run(): insertLocal==" + res);
                                    break;
                                }
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
                break;
            //设置照片大小
            case R.id.takepic_btn_setting:
                showSizeChoiceDialog(parameters);
                break;
        }
    }


    public static class NotificationDeleteBroadcast extends BroadcastReceiver {
        public NotificationDeleteBroadcast() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("id", 0);
            if (("deleteNotification").equals(intent.getAction()))
                if (id != 0) {
                    NotificationManager manager = (NotificationManager) context.getSystemService
                            (NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.cancel(id);
                    }
                }
        }
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


    public static synchronized String setInsertPicInfo(String checkWord, int cid, int did, int uid, String
            pid, String fileName, String
            filePath, String stypeID) throws IOException, XmlPullParserException {
        String str = ChuKuServer.SetInsertPicInfo(checkWord, cid, did, uid, pid, fileName, filePath, stypeID);
        return str;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rotationManager != null) {
            rotationManager.disable();
        }
        //        unregisterReceiver(netWorkChecker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rotationManager.attachToSensor();
        //        netWorkChecker.getLastState(mContext);
        //        registerReceiver(netWorkChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

}
