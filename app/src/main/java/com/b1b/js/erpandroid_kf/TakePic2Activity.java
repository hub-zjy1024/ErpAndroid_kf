package com.b1b.js.erpandroid_kf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import utils.FTPUtils;
import utils.FtpManager;
import utils.ImageWaterUtils;
import utils.MyFileUtils;
import utils.MyImageUtls;
import utils.MyToast;
import utils.UploadUtils;
import utils.WebserviceUtils;
import utils.camera.AutoFoucusMgr;

public class TakePic2Activity extends AppCompatActivity implements View.OnClickListener {

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
    private ProgressDialog pd;
    private String pid;
    private int commitTimes = 0;
    private final Object lock=new Object();
    NotificationManager notificationManager;
    private MaterialDialog resultDialog;
    private final static int FTP_CONNECT_FAIL = 3;
    private final static int PICUPLOAD_SUCCESS = 0;
    private final static int PICUPLOAD_ERROR = 1;
    int cid;
    int did;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICUPLOAD_ERROR:
                    //                    showFinalDialog("上传图片失败，请重新拍摄或检查网络");
                    btn_takepic.setEnabled(true);
                    toolbar.setVisibility(View.GONE);
                    break;
                case PICUPLOAD_SUCCESS:
                    MyToast.showToast(TakePic2Activity.this, "后台剩余图片：" + MyApp.totoalTask.size());
                    break;
                case FTP_CONNECT_FAIL:
                    MyToast.showToast(TakePic2Activity.this, "连接ftp服务器失败，请检查网络");
                    break;
                case 4:
                    MyToast.showToast(TakePic2Activity.this, "sd卡不存在，不可用后台上传");
                    btn_commit.setEnabled(false);
                    break;
            }
        }
    };

    private OrientationEventListener mOrientationListener;
    private SharedPreferences sp;
    private int itemPosition;
    private AlertDialog inputDialog;
    private HashMap<Integer, String> map = new HashMap<>();
    private FTPUtils FTPUtils;
    private AutoFoucusMgr auto;

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
        btn_setting.setOnClickListener(this);
        btn_takepic.setOnClickListener(this);
        btn_tryagain.setOnClickListener(this);
        btn_commit.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(TakePic2Activity.this);
        builder.setTitle("请输入单据号");
        View v = LayoutInflater.from(TakePic2Activity.this).inflate(R.layout.dialog_inputpid, null);
        final EditText dialogPid = (EditText) v.findViewById(R.id.dialog_inputpid_ed);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pid = dialogPid.getText().toString();
                checkPid(TakePic2Activity.this, pid);
            }
        });
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(v);
        inputDialog = builder.create();
        pid = getIntent().getStringExtra("pid");
        if (pid != null) {
            dialogPid.setText(pid);
        }
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotation = getProperRotation(orientation);
            }
        };
        //成功或失败的提示框
        SharedPreferences userInfoSp = getSharedPreferences("UserInfo", 0);
        cid = userInfoSp.getInt("cid", -1);
        did = userInfoSp.getInt("did", -1);
        attachToSensor(mOrientationListener);
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
                        MyToast.showToast(TakePic2Activity.this, "设备无摄像头");
                        return;
                    }
                    camera = Camera.open(0); // 打开摄像头
                    if (camera == null) {
                        MyToast.showToast(TakePic2Activity.this, "检测不到摄像头");
                        return;
                    }
                    //设置旋转角度
                    camera.setDisplayOrientation(getPreviewDegree(TakePic2Activity.this));
                    //设置parameter注意要检查相机是否支持，通过parameters.getSupportXXX()
                    parameters = camera.getParameters();
                    String brand = Build.BRAND;
//                    if (brand != null) {
//                        if (brand.toUpperCase().equals("HONOR")) {
//                            container.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    camera.autoFocus(null);
//                                }
//                            });
//                        } else {
//                            setAutoFoucs(parameters);
//                        }
//                    }
//                    setPreViewSize(parameters);//默认为屏幕大小
                    sp = getSharedPreferences("cameraInfo", 0);
                    try {
                        // 设置用于显示拍照影像的SurfaceHolder对象
                        camera.setPreviewDisplay(holder);
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
                        container.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                camera.autoFocus(null);
                            }
                        });
                         auto = new AutoFoucusMgr(camera);
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
                    releaseCamera();
                }
            });
        }
    }


    /**
     默认使用最大的预览尺寸，以便于获取最清晰的预览画面
     @param parameters
     */
    private void setPreViewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size firstSize = supportedPreviewSizes.get(0);
        Camera.Size lastSize = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
        int firstWidth = firstSize.width;
        int firstHeight = firstSize.height;
        int lastWidth = lastSize.width;
        int lastHeight = lastSize.height;
        if (firstWidth > lastWidth) {
            parameters.setPreviewSize(firstWidth, firstHeight);
        } else {
            parameters.setPreviewSize(lastWidth, lastHeight);
        }
    }

    /**
     如果相机支持设置自动聚焦
     @param parameters
     */
    private void setAutoFoucs(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for (int i = 0; i < supportedFocusModes.size(); i++) {
            if (supportedFocusModes.get(i).equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                //如果支持自动聚焦，必须设定回调
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            //聚焦成功记得取消，不然不会自动聚焦了
                            camera.cancelAutoFocus();
                        }
                    }
                });
                break;
            }
        }
    }

    /**
     弹出尺寸选择对话框
     防止照出的图片太大，内存溢出
     */
    private void showSizeChoiceDialog(final Camera.Parameters parameters) {

        picSizes = parameters.getSupportedPictureSizes();
        //剔除出尺寸太小的，和尺寸太大的，宽度（1280-2048)
        for (int i = picSizes.size() - 1; i >= 0; i--) {
            int width = picSizes.get(i).width;
            Log.e("zjy", "TakePic2Activity.java->showProgressDialog(): size==" + picSizes.get(i).width + "\t" + picSizes.get(i).height);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(TakePic2Activity.this);
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
            MyToast.showToast(TakePic2Activity.this, "没有可选的尺寸");
        }
    }

    /**
     添加屏幕旋转监听
     @param mOrientationListener
     */
    private void attachToSensor(OrientationEventListener mOrientationListener) {
        if (mOrientationListener != null) {
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable();
            } else {
                Log.e("zjy", "TakePicActivity->attachToSensor(): 获取相机方向失败==");
                mOrientationListener.disable();
                rotation = 0;
            }
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
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                if (MyApp.totoalTask.size() == 0) {
//                    FTPUtils.exitServer();
//                }
//            }
//        }.start();
    }

    public static boolean checkPid(Context mContext, String pid) {
        if (pid == null || "".equals(pid)) {
            MyToast.showToast(mContext, "请输入单据号");
            return true;
        } else {
            if (pid.length() < 7) {
                MyToast.showToast(mContext, "请输入7位单据号");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //拍照
            case R.id.btn_takepic:
                //禁止点击拍照按钮
                if (checkPid(TakePic2Activity.this, pid))
                    return;
                btn_takepic.setEnabled(false);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Matrix matrixs = new Matrix();
                            matrixs.setRotate(90 + rotation);
                            photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);
                            //显示工具栏
                            toolbar.setVisibility(View.VISIBLE);
                        } catch (OutOfMemoryError error) {
                            error.printStackTrace();
                            MyToast.showToast(TakePic2Activity.this, "当前尺寸太大，请选择合适的尺寸");
                            if (photo != null && !photo.isRecycled()) {
                                photo.recycle();
                            }
                            camera.startPreview();
                            showSizeChoiceDialog(parameters);
                            toolbar.setVisibility(View.GONE);
                        }
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
                btn_takepic.setEnabled(true);
                toolbar.setVisibility(View.GONE);
                break;
            //提交
            case R.id.main_commit:
                commitTimes++;
                if (photo == null || photo.isRecycled()) {
                    MyToast.showToast(TakePic2Activity.this, "图片过大，请选择合适的尺寸");
                    btn_takepic.setEnabled(true);
                    camera.startPreview();
                    toolbar.setVisibility(View.GONE);
                    showSizeChoiceDialog(parameters);
                    return;
                }
                final File sFile = MyFileUtils.getFileParent();
                if (sFile == null) {
                    MyToast.showToast(TakePic2Activity.this, "无法获取存储路径，请换用普通拍照功能");
                    return;
                }
                if (MyApp.ftpUrl == null || "".equals(MyApp.ftpUrl)) {
                    if(!"101".equals(MyApp.id)){
                        MyToast.showToast(TakePic2Activity.this, "读取上传地址失败，请重启程序");
                        return;
                    }
                }
                camera.startPreview();
                toolbar.setVisibility(View.GONE);
                btn_takepic.setEnabled(true);
                int id = (int) (Math.random() * 1000000);
                while (1 == 1) {
                    if (map.containsKey(id)) {
                        id = (int) (Math.random() * 1000000);
                    } else {
                        map.put(id, String.valueOf(id));
                        break;
                    }
                }
                final int finalId = id;
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(TakePic2Activity.this);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.notify_icon_large);
                builder.setContentTitle("上传" + pid + "的图片").setSmallIcon(R.mipmap.notify_icon)
                        .setContentText("图片正在上传").setProgress(100, 0, false).setLargeIcon(largeIcon);
                //载入水印图
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                final Bitmap textBitmap;
                try {
                    Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(TakePic2Activity.this, photo, bitmap, 0, 0);
                    textBitmap = ImageWaterUtils.drawTextToRightTop(TakePic2Activity.this, waterBitmap, pid, (int) (photo.getWidth() * 0.015), Color.RED, 20, 20);
                    if (photo != null && !photo.isRecycled()) {
                        photo.recycle();
                    }
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                } catch (OutOfMemoryError error) {
                    error.printStackTrace();
                    MyToast.showToast(TakePic2Activity.this, "请选择合适的尺寸，重新拍摄");
                    showSizeChoiceDialog(parameters);
                    return;
                }
                Thread tempThread = new Thread() {
                    @Override
                    public void run() {
                         String remoteName = UploadUtils.getChukuRemoteName(pid);
                        String notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                        final File upFile = new File(sFile, "dyj_img/" + remoteName + ".jpg");
                        FileOutputStream fio = null;
                        try {
                            fio = new FileOutputStream(upFile);
                            MyImageUtls.compressBitmapAtsize(textBitmap, fio, 0.4f);
                            if (textBitmap != null && !textBitmap.isRecycled()) {
                                textBitmap.recycle();
                            }
                            fio.close();
                            String insertPath ;
                            Intent mIntent = new Intent(TakePic2Activity.this, ObtainPicFromPhone.class);
                            mIntent.putExtra("failPid", pid);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mIntent.putExtra("failPath", upFile.getAbsolutePath());
                            mIntent.putExtra("nfId", finalId);
                            PendingIntent pIntent = PendingIntent.getActivity(TakePic2Activity.this, 100, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            boolean isStop = false;
                            int counts = 0;
                            while (!isStop) {
                                remoteName = UploadUtils.getChukuRemoteName(pid);
                                notifyName = remoteName.substring(remoteName.lastIndexOf("_") + 1);
                                String remotePath ;
                                String msg = "";
                                try {
                                    FileInputStream fis = new FileInputStream(upFile);
                                    boolean upSuccess = false;
                                    FTPUtils ftpUtil =null;
                                    String mUrl ;
                                    if ("101".equals(MyApp.id)) {
                                        mUrl = FtpManager.mainAddress;
                                        ftpUtil = new FTPUtils(mUrl, 21,  FtpManager.mainName,FtpManager.mainPwd);
//                                       mUrl= "192.168.10.65";
//                                        ftpUtil=  new ftpUtil(mUrl, 21, "zjy", "123456");
                                        remotePath = UploadUtils.KF_DIR + remoteName + ".jpg";
                                    } else {
                                        mUrl = MyApp.ftpUrl;
                                        ftpUtil = new FTPUtils(mUrl, 21,FtpManager.ftpName,
                                                FtpManager.ftpPassword);
                                        remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
                                    }
                                    ftpUtil.login(30);
                                    insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                                    Log.e("zjy", "TakePic2Activity->run(): InsertPath==" + insertPath);
                                    upSuccess = ftpUtil.upload(fis, remotePath);
                                    ftpUtil.exitServer();
                                    Log.e("zjy", "TakePic2Activity->run(): ftpUtil upSuccesss==" + upSuccess);
                                    if (upSuccess) {
                                        while (true) {
                                            //更新服务器信息
                                            if ("101".equals(MyApp.id)) {
                                                isStop = true;
                                                notificationManager.cancel(finalId);
                                                MyApp.totoalTask.remove(this);
                                                map.remove(finalId);
                                                mHandler.obtainMessage(PICUPLOAD_SUCCESS).sendToTarget();
                                                break;
                                            }
                                            try {
                                                String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer.parseInt(MyApp.id), pid, remoteName + ".jpg", insertPath, "CKTZ");
                                                Log.e("zjy", "TakePic2Activity.java-> setInsertPicInfo==" + res);
                                                if (res.equals("操作成功")) {
                                                    isStop = true;
                                                    notificationManager.cancel(finalId);
                                                    MyApp.totoalTask.remove(this);
                                                    map.remove(finalId);
                                                    MyApp.myLogger.writeInfo("background upload success：" + pid + "\t" + remoteName);
                                                    mHandler.obtainMessage(PICUPLOAD_SUCCESS).sendToTarget();
                                                    break;
                                                } else {
                                                    msg = "插入图片信息失败,多次出现请联系后台";
                                                }
                                            } catch (IOException e) {
                                                MyApp.myLogger.writeError("takepic2 upload Exception:" +pid+"\t"+ remoteName+"-"+e.getMessage());
                                                msg = "连接服务器失败,正在重试";
                                                //                                mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                                                e.printStackTrace();
                                            } catch (XmlPullParserException e) {
                                                e.printStackTrace();
                                            }
                                            changeNotificationMsg(builder, finalId, notifyName +msg, 0, pIntent);
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        MyApp.myLogger.writeError("takepic2 upload false:" +pid+"\t"+ remoteName);
                                        Log.e("zjy", "TakePic2Activity->run(): storeFileFalse==" + remoteName);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    MyApp.myLogger.writeError("takepic2 upload Exception:" +pid+"\t"+ remoteName+"-"+e.getMessage());
                                    changeNotificationMsg(builder, finalId, notifyName + "上传失败，正在重新上传", 0, pIntent);
                                }
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                counts++;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            MyApp.totoalTask.remove(this);
                            mHandler.sendEmptyMessage(4);
                        } catch (IOException e) {
                            e.printStackTrace();
                            MyApp.totoalTask.remove(this);
                            mHandler.sendEmptyMessage(4);
                        }
                    }
                };
                tempThread.start();
                MyApp.totoalTask.add(tempThread);
                //                ThreadPool.getThreadPool(10).execute(tempThread);
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
            if (intent.getAction().equals("deleteNotification"))
                if (id != 0) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(id);
                }
        }
    }

    private void changeNotificationMsg(NotificationCompat.Builder builder, int finalId, String msg, int progress, PendingIntent pIntent) {
        if (pIntent != null) {
            builder.setContentIntent(pIntent);
        }
        builder.setProgress(100, progress, false).setContentText(msg);
        notificationManager.notify(finalId, builder.build());
    }

    /**
     获取相机预览的画面旋转角度
     @param activity 当前Activity
     @return
     */
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该旋转的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    /**
     此方法配合OrientationEventListener使用
     @param rot 传感器的角度
     @return 成像图片应该旋转的角度
     */
    public static int getProperRotation(int rot) {
        int degree = 0;
        //根据传感器的方向获取拍照成像的方向
        if (rot > 240 && rot < 300) {
            degree = 270;
        } else if (rot > 60 && rot < 120) {
            degree = 90;
        }
        return degree;
    }
    //name="checkWord" type="string"
    //name="cid" type="int"   分公司id
    //name="did" type="int"    部门id
    //name="uid" type="int"   用户id

    /**
     使用同步代码，涉及数据库操作，多线程中有线程安全。
     */
    public static synchronized String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath, String stypeID) throws IOException, XmlPullParserException {
        Log.e("zjy", "TakePic2Activity.java->setInsertPicInfo(): ThreadId==" + Thread.currentThread().getName() + "\t" + filePath);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachToSensor(mOrientationListener);
    }

}
