package com.b1b.js.erpandroid_kf;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.b1b.js.erpandroid_kf.utils.FtpUpFile;
import com.b1b.js.erpandroid_kf.utils.ImageWaterUtils;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class TakePicActivity extends AppCompatActivity implements View.OnClickListener {

    private int rotation;
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
    private List<Camera.Size> picSize;
    private ProgressDialog pd;
    private String pid;
    private boolean canInsert = true;
    private long timeOut = 10000;
    private String remoteName;
    private int commitTimes = 0;
    FtpUpFile ftp;
    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    btn_takepic.setEnabled(true);
                    //更新服务器信息
                    SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                    final int cid = sp.getInt("cid", -1);
                    final int did = sp.getInt("did", -1);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                String insertPath = "ftp://" + MyApp.ftpUrl +  "/" + getRemoteDir() + "/" + remoteName + ".jpg";
                                Log.e("zjy", "TakePicActivity.java->run(): ==" + cid + "\t" + did + "\t" + pid + "\t" + MyApp.id);
                                Log.e("zjy", "TakePicActivity.java->run(): insertPath==" + insertPath);
                                Log.e("zjy", "TakePicActivity.java->run(): fileName==" + remoteName + ".jpg");
                                String res = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer.parseInt(MyApp.id), pid, remoteName + ".jpg", insertPath, "CKTZ");
                                Message msg = mHandler.obtainMessage();
                                msg.what = 6;
                                msg.obj = res;
                                mHandler.sendMessage(msg);
                            } catch (IOException e) {
                                mHandler.sendEmptyMessage(9);
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
                //内存溢出了
                case 1:
                    MyToast.showToast(TakePicActivity.this, "当前尺寸太大，请选择合适的尺寸");
                    photo.recycle();
                    camera.startPreview();
                    isPreview = true;
                    showDialog(parameters);
                    toolbar.setVisibility(View.GONE);
                    break;
                case 2:
                    MyToast.showToast(TakePicActivity.this, "上传图片失败，请重新拍摄或检查网络");
                    pd.cancel();
                    toolbar.setVisibility(View.GONE);
                    break;
                //处理上传信息
                case 3:
                    if (msg.obj.toString().equals("操作成功")) {
                        mHandler.sendEmptyMessage(6);
                    } else {
                        MyToast.showToast(TakePicActivity.this, "上传信息出错");
                        pd.cancel();
                        toolbar.setVisibility(View.GONE);
                    }
                    break;

                case 6:
                    pd.cancel();
                    MyToast.showToast(TakePicActivity.this, "上传完成");
                    toolbar.setVisibility(View.GONE);
                    break;
                case 9:
                    MyToast.showToast(TakePicActivity.this, "当前网络质量较差,操作失败");
                    toolbar.setVisibility(View.GONE);
                    pd.cancel();
                    break;
            }
        }
    };


    public static String getRomoteName(String id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "android_" + id + "_" + sdf.format(new Date());
    }

    public static String getRemoteDir() {
        Calendar calendar = Calendar.getInstance();
        String str = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH);
        return str;
    }

    private OrientationEventListener mOrientationListener;
    private SharedPreferences sp;
    private int itemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepic_main);
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
        pid = getIntent().getStringExtra("pid");
        if (MyApp.ftpUrl == null || MyApp.ftpUrl.equals("")) {
            MyApp.ftpUrl = "172.16.6.22";
            ftp = FtpUpFile.getFtpUpFile("NEW_DYJ", "GY8Fy2Gx", MyApp.ftpUrl, 21);
        } else {
            ftp = FtpUpFile.getFtpUpFile("dyjftp", "dyjftp", MyApp.ftpUrl, 21);
        }
        if (pid == null) {
            pid = MyApp.id;
        }
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotation = getProperRotation(orientation);
            }
        };

        attachToSensor(mOrientationListener);
        //获取surfaceholder
        mHolder = surfaceView.getHolder();
        mHolder.setKeepScreenOn(true);
        //添加SurfaceHolder回调
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(TakePicActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("zjy", "TakePicActivity.java->surfaceCreated(): no permission==");
                    ActivityCompat.requestPermissions(TakePicActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                } else {
                    Log.e("zjy", "TakePicActivity.java->surfaceCreated(): has permission");
                    int counts = Camera.getNumberOfCameras();
                    Log.e("zjy", "TakePicActivity-cameraCounts ==" + counts);
                    if (counts == 0) {
                        MyToast.showToast(TakePicActivity.this, "设备无摄像头");
                    }
                    try {
                        camera = Camera.open(0); // 打开摄像头
                        if (camera == null) {
                            MyToast.showToast(TakePicActivity.this, "检测不到摄像头");
                            return;
                        }
                        // 设置用于显示拍照影像的SurfaceHolder对象
                        camera.setPreviewDisplay(holder);
                        //设置旋转角度
                        camera.setDisplayOrientation(getPreviewDegree(TakePicActivity.this));
                        //设置parameter注意要检查相机是否支持，通过parameters.getSupportXXX()
                        parameters = camera.getParameters();
                        setAutoFoucs(parameters);
                        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                        int len = supportedPreviewSizes.size();
                        if (supportedPreviewSizes.get(0).width > supportedPreviewSizes.get(len - 1).width) {
                            parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);
                        } else {
                            parameters.setPreviewSize(supportedPreviewSizes.get(len - 1).width, supportedPreviewSizes.get(len - 1).height);
                        }
                        sp = getSharedPreferences("cameraInfo", 0);
                        if (sp.getInt("width", -1) != -1) {
                            int width = sp.getInt("width", -1);
                            int height = sp.getInt("height", -1);
                            parameters.setPictureSize(width, height);
                            camera.setParameters(parameters);
                        } else {
                            showDialog(parameters);
                        }
                        camera.startPreview(); //初始化操作在开始预览之前完成
                        isPreview = true;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 100) {

        } else {
            MyToast.showToast(TakePicActivity.this, "建议允许相机权限");
        }
    }

    /**
     如果相机支持设置自动聚焦
     @param parameters
     */
    private void setAutoFoucs(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for (int i = 0; i < supportedFocusModes.size(); i++) {
            Log.e("zjy", "TakePicActivity.java->setAutoFoucs(): mods==" + supportedFocusModes.get(i));
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
    private void showDialog(final Camera.Parameters parameters) {
        picSize = parameters.getSupportedPictureSizes();
        //剔除出尺寸太小的，和尺寸太大的，宽度（1280-2048)
        for (int i = picSize.size() - 1; i >= 0; i--) {
            int width = picSize.get(i).width;
            Log.e("zjy", "TakePicActivity.java->showDialog(): size==" + picSize.get(i).width + "\t" + picSize.get(i).height);
            if (width < 1280 || width > 2048) {
                picSize.remove(i);
            }
        }
        if (picSize.size() > 0) {
            String[] strs = new String[picSize.size()];
            for (int i = 0; i < picSize.size(); i++) {
                Camera.Size size = picSize.get(i);
                String item = size.width + "X" + size.height;
                strs[i] = item;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(TakePicActivity.this);
            dialog.setTitle("选择照片大小");//窗口名
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
                    int width = picSize.get(itemPosition).width;
                    int height = picSize.get(itemPosition).height;
                    Log.e("zjy", "TakePicActivity.java->selectSize: width==" + width + "\t" + height);
                    parameters.setPictureSize(width, height);
                    camera.setParameters(parameters);
                }
            });

            dialog.setPositiveButton("设为默认尺寸", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("zjy", "TakePicActivity.java->onClick(): default==" + itemPosition);
                    SharedPreferences.Editor editor = sp.edit();
                    int width = picSize.get(itemPosition).width;
                    int height = picSize.get(itemPosition).height;
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
     * @param mOrientationListener
     */
    private void attachToSensor(OrientationEventListener mOrientationListener) {
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //拍照
            case R.id.btn_takepic:
                //禁止点击拍照按钮
                btn_takepic.setEnabled(false);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrixs = new Matrix();
                        matrixs.setRotate(90 + rotation);
                        try {
                            photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);
                            //显示工具栏
                            toolbar.setVisibility(View.VISIBLE);
                            commitTimes = 0;
                        } catch (OutOfMemoryError error) {
                            error.printStackTrace();
                            mHandler.sendEmptyMessage(1);
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
                if (commitTimes > 1) {
                    showProgressDialog();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                //上传
                                remoteName = getRomoteName(pid);
                                FileInputStream fis = openFileInput("compress.jpg");
                                ftp.upload(fis, "/" + getRemoteDir(), remoteName + ".jpg");
                                mHandler.sendEmptyMessage(0);
                            } catch (IOException e) {
                                mHandler.sendEmptyMessage(9);
                                e.printStackTrace();
                            } catch (FtpUpFile.RemoteDeleteException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    if (photo == null) {
                        MyToast.showToast(TakePicActivity.this, "请稍等，等图像稳定再上传");
                        return;
                    }
                    showProgressDialog();
                    //载入水印图
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                    if (!photo.isRecycled()) {
                        //加水印后的图片
                        Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(TakePicActivity.this, photo, bitmap, 0, 0);
                        if (pid != null) {
                            Bitmap TextBitmap = ImageWaterUtils.drawTextToRightTop(TakePicActivity.this, waterBitmap, pid, 30, Color.RED, 20, 20);
                            //储存加水印的图片
                            MyImageUtls.saveBitmapToInternal(TakePicActivity.this, "o_temp.jpg", TextBitmap);
                        } else {
                            MyImageUtls.saveBitmapToInternal(TakePicActivity.this, "o_temp.jpg", waterBitmap);
                        }
                        //压缩处理过的图片，并存储
                        try {
                            MyImageUtls.compressBitmapAtsize(getFilesDir().getAbsolutePath() + "/o_temp.jpg", openFileOutput("compress.jpg", 0), 0.5f);

                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        //上传
                                        remoteName = getRomoteName(pid);
                                        FileInputStream fis = openFileInput("compress.jpg");
                                        ftp.upload(fis, "/" + getRemoteDir(), remoteName + ".jpg");
                                        mHandler.sendEmptyMessage(0);
                                    } catch (IOException e) {
                                        mHandler.sendEmptyMessage(9);
                                        e.printStackTrace();
                                    } catch (FtpUpFile.RemoteDeleteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
                break;
            //设置照片大小
            case R.id.takepic_btn_setting:
                showDialog(parameters);
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
        pd.show();
        pd.setCancelable(false);
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
    //    <xs:element minOccurs="0" name="checkWord" nillable="true" type="xs:string" />
    //    <xs:element minOccurs="0" name="cid" type="xs:int" />  分公司id
    //    <xs:element minOccurs="0" name="did" type="xs:int" />   部门id
    //    <xs:element minOccurs="0" name="uid" type="xs:int" />  用户id

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
}
