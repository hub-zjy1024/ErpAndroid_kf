package com.b1b.js.erpandroid_kf.myview.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by 张建宇 on 2019/8/31.
 */
public class Cam2TextureView extends TextureView {

    private Context context;

    /**
     * Creates a new TextureView.
     *
     * @param context The context to associate this view with.
     */
    public Cam2TextureView(Context context) {
        this(context, null);
    }

    /**
     * Creates a new TextureView.
     *
     * @param context The context to associate this view with.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public Cam2TextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new TextureView.
     *
     * @param context      The context to associate this view with.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     */
    public Cam2TextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = getContext();
    }

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    private Handler childHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    void onSurfaceCreated(Surface mSurface) {

    }

    private Size mPreviewSize;

    public Size getSpecific(int reqW, int reqH, Size[] prevSize) {
        for (int i = 0; i < prevSize.length; i++) {

        }
        return prevSize[prevSize.length - 1];
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startPreview() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager  = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIdList = cameraManager.getCameraIdList();
                String camStr = cameraIdList[0];

                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(camStr);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSizes = map.getOutputSizes(Cam2TextureView.class);
                mPreviewSize = getSpecific(0, 0, outputSizes);

                CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {


                    @Override
                    public void onOpened(CameraDevice camera) {//打开摄像头
                        mCameraDevice = camera;
                        //开启预览
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {//关闭摄像头
                        if (null != mCameraDevice) {
                            mCameraDevice.close();
                        }
                    }

                    @Override
                    public void onError(CameraDevice camera, int error) {//发生错误
                        Toast.makeText(context, "摄像头开启失败", Toast.LENGTH_SHORT).show();
                    }
                };
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraManager.openCamera(camStr, stateCallback, childHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }

        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                try {
                    //设置TextureView的缓冲区大小
                    surface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

                    Surface mSurface = new Surface(surface);
                    // 创建预览需要的CaptureRequest.Builder
                    final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    // 将SurfaceView的surface作为CaptureRequest.Builder的目标
                    previewRequestBuilder.addTarget(mSurface);
                    // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
                    mCameraDevice.createCaptureSession(Arrays.asList(mSurface), new CameraCaptureSession.StateCallback() // ③
                    {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) return;
                            // 当摄像头已经准备好时，开始显示预览
                            mCameraCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 打开闪光灯
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                // 显示预览
                                CaptureRequest previewRequest = previewRequestBuilder.build();
                                mCameraCaptureSession.setRepeatingRequest(previewRequest, new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                        Log.e("zjy", "这里接受到数据" + result.toString());
                                    }

                                    @Override
                                    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult){

                                    }
                                }, childHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(context, "配置失败", Toast.LENGTH_SHORT).show();
                        }
                    }, childHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        }
    }

    public void stopPreview() {

    }

    public void takePicture() {

    }
}
