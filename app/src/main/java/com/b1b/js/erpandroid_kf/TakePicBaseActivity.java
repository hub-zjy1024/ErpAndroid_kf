package com.b1b.js.erpandroid_kf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import utils.MyToast;
import utils.camera.AutoFoucusMgr;

public class TakePicBaseActivity extends AppCompatActivity {

    SharedPreferences sp;
    protected Context mContext = this;
    protected Camera mCamera;
    protected Camera.Parameters parameters;
    protected AutoFoucusMgr auto;
    protected String pid;
    private List<Camera.Size> picSizes;
    public int itemPosition;
    private Dialog inputDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pic_base);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.takepic_base_surfaceview);
        final LinearLayout container = (LinearLayout) findViewById(R.id.takepic_base_containner);
        SurfaceHolder holder = surfaceView.getHolder();
        sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
        pid = getIntent().getStringExtra("pid");
        holder.addCallback(new SurfaceHolder.Callback() {
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
                //设置旋转角度
                mCamera.setDisplayOrientation(getPreviewDegree((Activity) mContext));
                parameters = mCamera.getParameters();
                try {
                    // 设置用于显示拍照影像的SurfaceHolder对象
                    mCamera.setPreviewDisplay(holder);
                    int sw = getWindowManager().getDefaultDisplay().getWidth();
                    int sh = getWindowManager().getDefaultDisplay().getHeight();
                    Point finalSize = getSuitablePreviewSize(parameters, sw, sh);
                    if (finalSize != null) {
                        parameters.setPreviewSize(finalSize.x, finalSize.y);
                    }
                    //初始化操作在开始预览之前完成
                    if (sp.getInt("width", -1) != -1) {
                        int width = sp.getInt("width", -1);
                        int height = sp.getInt("height", -1);
                        Log.e("zjy", "TakePicActivity.java->surfaceCreated(): ==readCacheSize width" + width + "\t" + height);
                        parameters.setPictureSize(width, height);
                        mCamera.setParameters(parameters);
                    } else {
                        showSizeChoiceDialog(parameters);
                    }
                    mCamera.startPreview();
                    container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mCamera != null) {
                                auto.start();
                            }
                        }
                    });
                    auto = new AutoFoucusMgr(mCamera);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onSurfaceCreate();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                onSurfaceDestory();
            }
        });
    }

    public void init() {

    }

    public void onSurfaceCreate() {

    }

    public void onSurfaceDestory() {

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
                    SharedPreferences.Editor editor = sp.edit();
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

    public void initInputDialog() {
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
    }

    private static void checkPid(Context mContext, String pid, int len) {
        if ("".equals(pid) || pid == null) {
            MyToast.showToast(mContext, "请输入单据号");
        } else {
            if (pid.length() < len) {
                MyToast.showToast(mContext, "请输入" + len + "位单据号");
            }
        }
    }
}
