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
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.SensorManager;
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

import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UpLoadPicRunable;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import utils.DialogUtils;
import utils.FTPUtils;
import utils.FtpManager;
import utils.ImageWaterUtils;
import utils.MyImageUtls;
import utils.MyToast;
import utils.UploadUtils;
import utils.WebserviceUtils;
import utils.camera.AutoFoucusMgr;

public class TakePicActivity extends AppCompatActivity implements View.OnClickListener {

    private int rotation = 0;
    private SurfaceView surfaceView;
    private Button btn_tryagain;
    private Button btn_setting;
    protected Button btn_commit;
    protected Button btn_takepic;
    private SurfaceHolder mHolder;
    protected LinearLayout toolbar;
    private Camera.Parameters parameters;
    protected Camera mCamera;
    private boolean isPreview = false;
    private Bitmap photo;
    private List<Camera.Size> picSizes;
    AutoFoucusMgr auto;
    private ProgressDialog pd;
    protected String pid;
    private String kfFTP = MyApp.ftpUrl;
    protected String userID = MyApp.id;
    private MaterialDialog resultDialog;
    private final static int FTP_CONNECT_FAIL = 3;
    protected final static int PICUPLOAD_SUCCESS = 0;
    protected final static int PICUPLOAD_ERROR = 1;
    private Context mContext = TakePicActivity.this;
    protected int tempRotate = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICUPLOAD_ERROR:
                    String msgReason = "上传图片失败，请检查网络并重新拍摄";
                    String str = msg.obj != null ? msg.obj.toString() : null;
                    if (str != null) {
                        msgReason = str;
                    }
                    showFinalDialog(msgReason+"!!!");
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
    Bitmap waterBitmap = null;

    private OrientationEventListener mOrientationListener;
    protected SharedPreferences cameraSp;
    private int itemPosition;
    private AlertDialog inputDialog;
    private String flag;
    protected byte[] tempBytes;
    protected SharedPreferences userInfo;

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
        userInfo = getSharedPreferences("UserInfo", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("请输入单据号");
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_inputpid, null);
        final EditText dialogPid = (EditText) v.findViewById(R.id.dialog_inputpid_ed);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pid = dialogPid.getText().toString();
                checkPid(mContext, pid, 3);
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
        flag = getIntent().getStringExtra("flag");
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
        resultDialog = new MaterialDialog(mContext);
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
        //添加SurfaceHolder回调
        if (mHolder != null) {
            mHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    int counts = Camera.getNumberOfCameras();
                    if (counts == 0) {
                        MyToast.showToast(mContext, "设备无摄像头");
                        return;
                    }
                    mCamera = Camera.open(0); // 打开摄像头
                    if (mCamera == null) {
                        MyToast.showToast(mContext, "检测不到摄像头");
                        return;
                    }
                    cameraSp = getSharedPreferences("cameraInfo", 0);
                    //设置旋转角度
                    mCamera.setDisplayOrientation(getPreviewDegree((TakePicActivity) mContext));
                    //设置parameter注意要检查相机是否支持，通过parameters.getSupportXXX()
                    parameters = mCamera.getParameters();
                    try {
                        // 设置用于显示拍照影像的SurfaceHolder对象
                        mCamera.setPreviewDisplay(holder);
                        Camera.Size previewSize = parameters.getPreviewSize();
                        int width1 = previewSize.width;
                        int height1 = previewSize.height;
                        int sw = getWindowManager().getDefaultDisplay().getWidth();
                        int sh = getWindowManager().getDefaultDisplay().getHeight();
                        MyApp.myLogger.writeInfo("mCamera screen:" + sw + "\t" + sh);
                        MyApp.myLogger.writeInfo("mCamera def:" + width1 + "\t" + height1);
                        Log.e("zjy", "TakePicActivity->surfaceCreated(): mCamera.preview==" + mCamera.getParameters()
                                .getPreviewSize().width + "\t" + mCamera.getParameters().getPreviewSize().height
                        );
                        Point finalSize = getSuitablePreviewSize(parameters, sw, sh);
                        if (finalSize != null) {
                            parameters.setPreviewSize(finalSize.x, finalSize.y);
                        }
                        //初始化操作在开始预览之前完成
                        if (cameraSp.getInt("width", -1) != -1) {
                            int width = cameraSp.getInt("width", -1);
                            int height = cameraSp.getInt("height", -1);
                            Log.e("zjy", "TakePicActivity.java->surfaceCreated(): ==readCacheSize width" + width + "\t" + height);
                            parameters.setPictureSize(width, height);
                            mCamera.setParameters(parameters);
                        } else {
                            showSizeChoiceDialog(parameters);
                        }
                        mCamera.startPreview();
                        isPreview = true;
                        container.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                auto.stop();
                                if (mCamera != null && isPreview) {
                                    mCamera.autoFocus(null);
                                }
                            }
                        });
                        auto = new AutoFoucusMgr(mCamera);
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
     默认使用最大的预览尺寸，以便于获取最清晰的预览画面(测试发现有些不兼容)
     @param parameters
     */
    public static Point getSuitablePreviewSize(Camera.Parameters parameters, int screenW, int screenH) {
        Camera.Size defSize = parameters.getPreviewSize();
        if (defSize.width == screenH && defSize.height == screenW) {
            return null;
        }
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int px1 = lhs.width * lhs.height;
                int px2 = rhs.width * rhs.height;
                if (px1 > px2) {
                    return -1;
                } else if (px1 == px2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        int tWidth = 0;
        int tHeight = 0;
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Camera.Size tSize = supportedPreviewSizes.get(i);
            if (screenH == tSize.width && tSize.height == screenW) {
                tWidth = tSize.width;
                tHeight = tSize.height;
                return new Point(tWidth, tHeight);
            } else if (screenH > tSize.width) {
                float rate = tSize.width / (float) tSize.height;
                float screenRate = screenH / (float) screenW;
                float res = Math.abs(rate - screenRate);
                if (res < 0.23) {
                    tWidth = tSize.width;
                    tHeight = tSize.height;
                    break;
                }
            }
        }
        if (tWidth == 0 && tHeight == 0) {
            return null;
        }
        return new Point(tWidth, tHeight);
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
            Log.e("zjy", "TakePicActivity.java->showProgressDialog(): size==" + picSizes.get(i).width + "\t" + picSizes.get(i)
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
                    Log.e("zjy", "TakePicActivity.java->selectSize: width==" + width + "\t" + height);
                    parameters.setPictureSize(width, height);
                    mCamera.setParameters(parameters);
                }
            });

            dialog.setPositiveButton("设为默认尺寸", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("zjy", "TakePicActivity.java->onClick(): default size pos==" + itemPosition);
                    SharedPreferences.Editor editor = cameraSp.edit();
                    int width = picSizes.get(itemPosition).width;
                    int height = picSizes.get(itemPosition).height;
                    editor.putInt("width", width);
                    editor.putInt("height", height);
                    editor.apply();
                    parameters.setPictureSize(width, height);
                    mCamera.setParameters(parameters);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        } else {
            MyToast.showToast(mContext, "没有可选的尺寸");
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
                MyApp.myLogger.writeError(TakePicActivity.class, "获取相机方向失败,Detect fail");
                mOrientationListener.disable();
                rotation = 0;
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        MyImageUtls.releaseBitmap(waterBitmap);
    }


    public static boolean checkPid(Context mContext, String pid, int len) {

        if ("".equals(pid) || pid == null) {
            MyToast.showToast(mContext, "请输入单据号");
            return true;
        } else {
            if (pid.length() < len) {
                MyToast.showToast(mContext, "请输入" + len + "位单据号");
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
                if (checkPid(mContext, pid, 3))
                    return;
                btn_takepic.setEnabled(false);
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            camera.stopPreview();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            MyApp.myLogger.writeError(throwable);
                        }
                        tempRotate = rotation;
                        isPreview = false;
                        if (data == null || data.length == 0) {
                            MyToast.showToast(mContext, "拍照出现错误，请重启程序");
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
                if (photo != null) {
                    photo.recycle();
                    photo = null;
                }
                mCamera.startPreview();
                btn_takepic.setEnabled(true);
                toolbar.setVisibility(View.GONE);
                break;
            //提交
            case R.id.main_commit:
                if (kfFTP == null || "".equals(kfFTP)) {
                    if (!"101".equals(userID)) {
                        MyToast.showToast(mContext, "读取上传地址失败，请重启程序");
                        return;
                    }
                }
                showProgressDialog();
                //载入水印图
                if (waterBitmap == null||waterBitmap.isRecycled()) {
                    waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                }
                final Bitmap bitmap = waterBitmap;
                String insertPath = "";
                FTPUtils ftpUtil;
                String remoteName;
                String remotePath;
                String mUrl;
                if (flag != null && flag.equals("caigou")) {
                    remoteName = UploadUtils.createSCCGRemoteName(pid);
                    mUrl = CaigouActivity.ftpAddress;
                    ftpUtil = new FTPUtils(mUrl, CaigouActivity
                            .username,
                            CaigouActivity.password);
                     remotePath = UploadUtils.getCaigouRemoteDir(remoteName + ".jpg");
                } else {
                    remoteName = UploadUtils.getChukuRemoteName(pid);
                    mUrl = kfFTP;
                    ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName, FtpManager.ftpPassword);
                    remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName + ".jpg";
                }
                if ("101".equals(userID)) {
                    mUrl = FtpManager.mainAddress;
                    ftpUtil = new FTPUtils(mUrl, FtpManager.mainName, FtpManager
                            .mainPwd);
                    remotePath = UploadUtils.CG_DIR + remoteName + ".jpg";
                }
                insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
                final byte[] picData = Arrays.copyOf(tempBytes, tempBytes.length);
                final int cRotate = tempRotate;
                UpLoadPicRunable runable = new UpLoadPicRunable(remotePath, insertPath, ftpUtil, mHandler) {

                    @Override
                    public InputStream getInputStream() throws Exception {
                        Bitmap bmp = BitmapFactory.decodeByteArray(picData, 0, picData.length);
                        Matrix matrixs = new Matrix();
                        matrixs.setRotate(90 + cRotate);
                        Bitmap photo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);
                        //加水印后的图片
                        Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext, photo,
                                bitmap, 0, 0);
                        Bitmap TextBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap, pid,
                                (int) (photo.getWidth() * 0.015), Color.RED, 20, 20);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        //图片质量压缩到bao数组
                        MyImageUtls.compressBitmapAtsize(TextBitmap, bao, 0.4f);
                        MyImageUtls.releaseBitmap(bmp);
                        MyImageUtls.releaseBitmap(photo);
                        MyImageUtls.releaseBitmap(TextBitmap);
                        MyImageUtls.releaseBitmap(waterBitmap);
                        return new ByteArrayInputStream(bao.toByteArray());
                    }

                    @Override
                    public boolean getInsertResult() throws Exception {
                        String result;
                        String remoteName = getRemoteName();
                        String insertPath = getInsertpath();
                        final int cid = userInfo.getInt("cid", -1);
                        final int did = userInfo.getInt("did", -1);
                        if ("101".equals(userID)) {
                             return true;
                        }
                        if ("caigou".equals(flag)) {
                            result = ObtainPicFromPhone.setSSCGPicInfo(WebserviceUtils.WebServiceCheckWord, cid,
                                    did, Integer
                                            .parseInt(userID), pid, remoteName, insertPath, "SCCG");
                        } else {
                            result = setInsertPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer
                                    .parseInt(userID), pid, remoteName, insertPath, "CKTZ");
                        }
                        Log.e("zjy", "TakePicActivity->getInsertResult():insertPath ==" + insertPath);
                        MyApp.myLogger.writeInfo("takepic insert:" + pid + "\t" + remoteName + "=" + result);
                        return "操作成功".equals(result);
                    }
                };
                TaskManager.getInstance().execute(runable);
                break;
            //设置照片大小
            case R.id.takepic_btn_setting:
                showSizeChoiceDialog(parameters);
                break;
        }
    }

    protected void showProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (photo != null)
                    photo.recycle();
                if (mCamera != null) {
                    mCamera.startPreview();
                    isPreview = true;
                }
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

    public String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath,
                                   String stypeID) throws IOException, XmlPullParserException {
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
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .ChuKuServer);
        str = response.toString();
        return str;
    }

    public String setSSCGPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath,
                                 String stypeID) throws IOException, XmlPullParserException {
        String str = "";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("cid", cid);
        map.put("did", did);
        map.put("uid", uid);
        map.put("pid", pid);
        map.put("filename", fileName);
        map.put("filepath", filePath);
        map.put("stypeID", stypeID);//标记，固定为"SCCG"
        SoapObject request = WebserviceUtils.getRequest(map, "InsertSSCGPicInfo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .MartStock);
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

    protected void showFinalDialog(String message) {
        DialogUtils.cancelDialog(pd);
        resultDialog.setMessage(message);
        resultDialog.show();
    }
}
