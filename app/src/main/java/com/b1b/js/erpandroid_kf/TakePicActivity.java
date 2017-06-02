package com.b1b.js.erpandroid_kf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.b1b.js.erpandroid_kf.utils.FtpManager;
import com.b1b.js.erpandroid_kf.utils.ImageWaterUtils;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class TakePicActivity extends AppCompatActivity implements View.OnClickListener {

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
    private String remoteName;
    private int commitTimes = 0;
    FtpManager ftp;
    //    FtpManager2 ftp;
    private MaterialDialog resultDialog;
    private final static int FTP_CONNECT_FAIL = 3;
    private final static int PICUPLOAD_SUCCESS = 0;
    private final static int PICUPLOAD_ERROR = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICUPLOAD_ERROR:
                    showFinalDialog("上传图片失败，请检查网络并重新拍摄");
                    btn_takepic.setEnabled(true);
                    toolbar.setVisibility(View.GONE);
                    break;
                case PICUPLOAD_SUCCESS:
                    if (msg.obj.toString().equals("操作成功")) {
                        showFinalDialog("上传成功");
                    } else {
                        showFinalDialog("插入图片信息失败，请重新上传");
                    }
                    btn_takepic.setEnabled(true);
                    toolbar.setVisibility(View.GONE);
                    break;
                case FTP_CONNECT_FAIL:
                    MyToast.showToast(TakePicActivity.this, "连接ftp服务器失败，请检查网络");
                    break;
            }
        }
    };

    private OrientationEventListener mOrientationListener;
    private SharedPreferences sp;
    private int itemPosition;
    private AlertDialog inputDialog;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(TakePicActivity.this);
        builder.setTitle("请输入单据号");
        View v = LayoutInflater.from(TakePicActivity.this).inflate(R.layout.dialog_inputpid, null);
        final EditText dialogPid = (EditText) v.findViewById(R.id.dialog_inputpid_ed);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pid = dialogPid.getText().toString();
                checkPid(TakePicActivity.this, pid);
            }
        });
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
        if ("".equals(MyApp.ftpUrl) || MyApp.ftpUrl == null) {
            if ("101".equals(MyApp.id)) {
                MyApp.ftpUrl = "172.16.6.22";
                ftp = FtpManager.getFtpManager("NEW_DYJ", "GY8Fy2Gx", MyApp.ftpUrl, 21);
            } else {
                MyToast.showToast(getApplicationContext(), "FTP地址获取失败，请重新启动程序");
                return;
            }
        } else {
            ftp = FtpManager.getFtpManager("dyjftp", "dyjftp", MyApp.ftpUrl, 21);
        }
        connFTP(mHandler, FTP_CONNECT_FAIL);
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotation = getProperRotation(orientation);
            }
        };
        //成功或失败的提示框
        resultDialog = new MaterialDialog(TakePicActivity.this);
        resultDialog.setTitle("提示");
        resultDialog.setPositiveButton("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
                finish();
            }
        });
        resultDialog.setCanceledOnTouchOutside(true);
        attachToSensor(mOrientationListener);
        //获取surfaceholder
        mHolder = surfaceView.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //添加SurfaceHolder回调
        if (mHolder != null) {
            mHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    int counts = Camera.getNumberOfCameras();
                    if (counts == 0) {
                        MyToast.showToast(TakePicActivity.this, "设备无摄像头");
                        return;
                    }
                    camera = Camera.open(0); // 打开摄像头
                    if (camera == null) {
                        MyToast.showToast(TakePicActivity.this, "检测不到摄像头");
                        return;
                    }
                    //设置旋转角度
                    camera.setDisplayOrientation(getPreviewDegree(TakePicActivity.this));
                    //设置parameter注意要检查相机是否支持，通过parameters.getSupportXXX()
                    parameters = camera.getParameters();
                    String brand = Build.BRAND;
                    if (brand != null) {
                        if (brand.toUpperCase().equals("HONOR")) {
                            container.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    camera.autoFocus(null);
                                }
                            });
                        } else {
                            setAutoFoucs(parameters);
                        }
                    }
                    sp = getSharedPreferences("cameraInfo", 0);
                    try {
                        // 设置用于显示拍照影像的SurfaceHolder对象
                        camera.setPreviewDisplay(holder);
                        //初始化操作在开始预览之前完成
                        if (sp.getInt("width", -1) != -1) {
                            int width = sp.getInt("width", -1);
                            int height = sp.getInt("height", -1);
                            Log.e("zjy", "TakePicActivity.java->surfaceCreated(): ==readCacheSize width" + width + "\t" + height);
                            parameters.setPictureSize(width, height);
                            camera.setParameters(parameters);
                        } else {
                            showSizeChoiceDialog(parameters);
                        }
                        camera.startPreview();
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
                    releaseCamera();
                }
            });
        }
    }

    private void connFTP(final Handler handler, final int target) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (ftp != null) {
                        ftp.connectAndLogin();
                    } else {
                        handler.sendEmptyMessage(target);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(target);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     默认使用最大的预览尺寸，以便于获取最清晰的预览画面(测试发现有些不兼容)
     @param parameters
     @deprecated
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
            Log.e("zjy", "TakePicActivity.java->showProgressDialog(): size==" + picSizes.get(i).width + "\t" + picSizes.get(i).height);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(TakePicActivity.this);
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
                    Log.e("zjy", "TakePicActivity.java->selectSize: width==" + width + "\t" + height);
                    parameters.setPictureSize(width, height);
                    camera.setParameters(parameters);
                }
            });

            dialog.setPositiveButton("设为默认尺寸", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("zjy", "TakePicActivity.java->onClick(): default size pos==" + itemPosition);
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
            MyToast.showToast(TakePicActivity.this, "没有可选的尺寸");
            return;
        }
    }

    /**
     添加屏幕旋转监听
     @param mOrientationListener
     */
    private void attachToSensor(OrientationEventListener mOrientationListener) {
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            Log.e("zjy", "TakePicActivity->attachToSensor(): 获取相机方向失败==");
            mOrientationListener.disable();
            rotation = 0;
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (ftp != null)
                        ftp.exit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static boolean checkPid(Context mContext, String pid) {
        if ("".equals(pid) || pid == null) {
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
                if (checkPid(TakePicActivity.this, pid))
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
                            MyToast.showToast(TakePicActivity.this, "当前尺寸太大，请选择合适的尺寸");
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
                if (photo != null) {
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
                if (photo == null) {
                    MyToast.showToast(TakePicActivity.this, "请稍等，等图像稳定再上传");
                    return;
                }
                showProgressDialog();
                //载入水印图
                final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                if (!photo.isRecycled()) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                //加水印后的图片
                                Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(TakePicActivity.this, photo, bitmap, 0, 0);
                                Bitmap TextBitmap = ImageWaterUtils.drawTextToRightTop(TakePicActivity.this, waterBitmap, pid, (int) (photo.getWidth() * 0.015), Color.RED, 20, 20);
                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                //图片质量压缩到bao数组
                                MyImageUtls.compressBitmapAtsize(TextBitmap, bao, 0.4f);
                                final ByteArrayInputStream in = new ByteArrayInputStream(bao.toByteArray());
                                remoteName = UploadUtils.getRomoteName(pid);
                                String insertPath = UploadUtils.createInsertPath(MyApp.ftpUrl, UploadUtils.getRemoteDir(), remoteName, "jpg");
                                //上传
                                boolean isConn;
                                if ("101".equals(MyApp.id)) {
                                    isConn = ftp.upload(in, "/ZJy", remoteName + ".jpg");
                                    insertPath = "ftp://172.16.6.22/ZJy/" + remoteName + ".jpg";
                                } else {
                                    isConn = ftp.upload(in, "/" + UploadUtils.getRemoteDir(), remoteName + ".jpg");
                                }
                                if (isConn) {
                                    //更新服务器信息
                                    SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                                    final int cid = sp.getInt("cid", -1);
                                    final int did = sp.getInt("did", -1);
                                    Log.e("zjy", "TakePicActivity.java->run(): insertPath==" + insertPath);
                                    String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer.parseInt(MyApp.id), pid, remoteName + ".jpg", insertPath, "CKTZ");
                                    Log.e("zjy", "TakePicActivity.java->run(): res==" + res);
                                    Message msg = mHandler.obtainMessage(PICUPLOAD_SUCCESS);
                                    msg.obj = res;
                                    mHandler.sendMessage(msg);
                                } else {
                                    mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                                }
                            } catch (IOException e) {
                                mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                mHandler.sendEmptyMessage(PICUPLOAD_ERROR);
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                break;
            //设置照片大小
            case R.id.takepic_btn_setting:
                showSizeChoiceDialog(parameters);
                break;
        }
    }

    private void showProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                photo.recycle();
                camera.startPreview();
            }
        });
        pd.setMessage("正在上传");
        pd.setCancelable(false);
        pd.show();
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

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationListener.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachToSensor(mOrientationListener);
    }

    private void showFinalDialog(String message) {
        if (pd != null && pd.isShowing()) {
            pd.cancel();
        }
        resultDialog.setMessage(message);
        resultDialog.show();
    }
}
