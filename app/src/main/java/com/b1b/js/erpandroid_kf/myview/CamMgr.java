package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by 张建宇 on 2019/7/13.
 */
public class CamMgr implements Camera.AutoFocusCallback {
    private Camera mCam;
    boolean isStoped = false;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BackTask bgHandler;
    private static Semaphore mCamLock = new Semaphore(1);
    private static AtomicInteger mCounter = new AtomicInteger();

    public CamMgr(Context mContext) {
        this.mContext = mContext;
        bgHandler = new BackTask("camera_bg_task_" + mCounter.getAndIncrement());
        bgHandler.start();
    }

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int camera_Front = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public void openCamera(SurfaceHolder holder, int mCameraId) throws IOException {
        try {
            Log.w("zjy", getClass() + "->openCamera():开启摄像头 ==" + mCameraId);
            this.mCameraId = mCameraId;
            mCam = Camera.open(mCameraId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if ("Fail to connect to camera service".equals(message)) {
                Log.e("zjy", getClass() + "->openCamera(): ==连接摄像头失败");
                throw new IOException("连接相机失败", e);
            }
        }
        if (holder.getSurface() != null) {
            try {
                if (mCam == null) {
                    throw new IOException("打开摄像头失败，openError,ID=" + mCameraId);
                }
                setCamRotation();
                mCam.startPreview();
                safeAutoFocus();
                isStoped = false;
                mCam.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("zjy", getClass() + "->openCamera(): getSurface null==");
        }
    }

    public void openCamera(SurfaceHolder holder) throws IOException {
        openCamera(holder, mCameraId);
    }

    public void testLeak() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                String name = bgHandler.getName();
                while  (bgHandler.isAlive()) {
                    Log.e("zjy", getClass() + "->run():alive ==" +name);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("zjy", getClass() + "->run():deaded ==" + name);
            }
        }.start();
    }

    static class BackTask extends HandlerThread {
        private Handler mHandler;
        LinkedBlockingQueue<Runnable> mq = new LinkedBlockingQueue<>(10);

        public BackTask(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler(getLooper());

            Runnable mRun;
            while ((mRun = mq.poll()) != null) {
                Log.e("zjy", getClass() + "->onLooperPrepared(): ==run" + mRun.toString());
                mRun.run();
            }
        }

        void runTask(Runnable mRun) {
            if (mHandler == null) {
                mq.offer(mRun);
            } else {
                mHandler.post(mRun);
            }
        }
    }

    public void releaseAll() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (mCam != null) {
                    try {
                        Thread.sleep(200);
                        Log.e("zjy", getClass() + "->run() try toStop: ==");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("zjy", getClass() + "->run():startToQuit" +
                        " ==");
                mHandler.removeCallbacksAndMessages(null);
                bgHandler.quit();
            }
        }.start();
    }

    public void asyncClose() {
        isStoped = true;
        bgHandler.runTask(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    public void asycnOpen(final SurfaceHolder holder,int camId, final Camera.PreviewCallback mPreCb, final int width,
                          final int height) {
        mCameraId = camId;
        asycnOpen(holder, mPreCb, width, height);
    }

    public void asycnOpen(final SurfaceHolder holder, final Camera.PreviewCallback mPreCb, final int width,
                          final int height) {
        bgHandler.runTask(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.e("zjy",
                                getClass() + "->run(): ==get Camera,permits=" + mCamLock.availablePermits());
//                        mCamLock.acquire();
                        openCamera(holder, mCameraId);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            openCamera(holder, mCameraId);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
//                        mCamLock.release();
                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                        mCamLock.release();
//                    }
                }

                initParams(width, height);
                setOneShotPreviewCallback(mPreCb);
            }
        });
    }

    private void setCamRotation() {
        int camOritation = getCamOritation();
        mCam.setDisplayOrientation(camOritation);
    }

    public void openFlashLight() {
        if (mCam != null) {
            Camera.Parameters parameters = mCam.getParameters();
            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        }
    }

    public void closeFlashLight() {
        if (mCam != null) {
            Camera.Parameters parameters = mCam.getParameters();
            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCam.setParameters(parameters);
        }
    }

    private int getCamOritation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int cOri = info.orientation;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int rotatedDeg = 0;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotatedDeg = (info.orientation + degrees) % 360;
            rotatedDeg = (360 - rotatedDeg) % 360;  // compensate the mirror
        } else {  // back-facing
            rotatedDeg = (info.orientation - degrees + 360) % 360;
        }
        return rotatedDeg;
    }

    void scheduleAutoFocus() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCam != null) {
                    if (!isStoped) {
                        safeAutoFocus();
                    }
                }
            }
        }, 1500);
    }

    public void safeAutoFocus() {
        try {
            mCam.autoFocus(this);
        } catch (RuntimeException re) {
            re.printStackTrace();
            Log.e("zjy", getClass() + "->safeAutoFocus(): FocusError==" + re.getMessage());
            // Horrible hack to deal with autofocus errors on Sony devices
            // See https://github.com/dm77/barcodescanner/issues/7 for example
            scheduleAutoFocus(); // wait 1 sec and then do check again
        }
    }

    public int getRotationCount() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        if (mCameraId == -1) {
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        } else {
            Camera.getCameraInfo(mCameraId, info);
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int camOrientation = info.orientation;
        int result;
        //        Log.e("zjy", getClass() + "->getRotationCount():oritaion ==" + camOrientation + ",
        // degree=" + degrees);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camOrientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camOrientation - degrees + 360) % 360;
        }
        return result;

    }

    public void focus() {
        scheduleAutoFocus();
    }

    public void initParams(int cWidth, int cHeight) {
        if (isStoped) {
            Log.e("zjy", getClass() + "->initParams(): 已停止==");
            return;
        }
        if (mCam != null) {
            try {
                Camera.Parameters parameters = mCam.getParameters();
                if (cWidth == -1 || cHeight == -1) {
                    Log.e("zjy", getClass() + "->initParams(): ==不合法宽高");
                } else {
                    //                parameters.setFocusMode(Camera.Parameters
                    //                .FOCUS_MODE_CONTINUOUS_PICTURE);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

                    Log.e("zjy", getClass() + "->initParams():containner w-h ==" + cWidth + "-" + cHeight);
                    Point suitablePreviewSize = getSuitablePreviewSize(parameters, cWidth, cHeight);
                    if ("SUNMI".equals(Build.BRAND) || "L2".equals(Build.MODEL)) {
                        suitablePreviewSize = new Point(960, 720);
                    }
                    Log.e("zjy", getClass() + "->initParams(): suit_w-h==" + suitablePreviewSize.x + "\t" +
                            suitablePreviewSize.y);
                    parameters.setPreviewSize(suitablePreviewSize.x, suitablePreviewSize.y);
                }
                //            mCam.stopPreview();
                mCam.setParameters(parameters);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            //            mCam.setParameters(parameters);
        }
    }

    /**
     * 默认使用最大的预览尺寸，以便于获取最清晰的预览画面(测试发现有些不兼容)
     *
     * @param parameters
     */
    private Point getSuitablePreviewSize(Camera.Parameters parameters, int screenW, int screenH) {

        Camera.Size defSize = parameters.getPreviewSize();
        if (defSize.width == screenH && defSize.height == screenW) {
            return new Point(defSize.width, defSize.height);
        }
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        final double aspect = 0.6;
        double targetRatio = (double) screenH / screenW;
        if (supportedPreviewSizes == null)
            return null;
        Camera.Size optimalSize = null;
        int minDiff = Integer.MAX_VALUE;
        int targetHeight = screenW;
        List<Camera.Size> oList = new ArrayList<>();
        String s = "";
        //筛选出比例最合适的size
        for (Camera.Size size : supportedPreviewSizes) {
            double ratio = (double) size.width / size.height;
            double rate = Math.abs(Math.abs(ratio - targetRatio));
            s += rate + ",w-h=" + size.width + "," + size.height + "\n";

            if (rate < aspect) {
                if (size.width == screenH && size.height == targetHeight) {
                    return new Point(size.width, size.height);
                }
                oList.add(size);
            }
        }
//        Log.e("zjy", getClass() + "->getSuitablePreviewSize(): ==" + s);
        if (oList.size() == 0) {
            Log.e("zjy", getClass() + "->getSuitablePreviewSize():sizeList==0" +
                    " ==");
            // 没有满足的比例，直接找到高度最接近的
            oList = supportedPreviewSizes;
        }
        int mSWidth = 0;
        int mSHeight = 0;

        //筛选出高度最相近
        for (Camera.Size size : oList) {
            if (Math.abs(size.height - targetHeight) <= minDiff) {
                minDiff = Math.abs(size.height - targetHeight);
                if (size.width > mSWidth) {
                    mSHeight = size.height;
                    mSWidth = size.width;
                }
            }
        }
        //        Log.e("zjy", getClass() + "->getSuitablePreviewSize():minDiff ==" + minDiff);
        return new Point(mSWidth, mSHeight);
    }


    public void setOneShotPreviewCallback(Camera.PreviewCallback cb) {
        if (mCam != null) {
            if (!isStoped) {
                try {
                    mCam.setOneShotPreviewCallback(cb);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopPreveiw() {

    }

    public void startPreview() {

    }

    public boolean isStoped() {
        return isStoped;
    }

    public void setStoped(boolean stoped) {
        isStoped = stoped;
    }

    /**
     *
     */
    public void close() {
        long time = System.currentTimeMillis();
        Log.w("zjy", getClass() + "->close():关闭摄像头 ==" + mCameraId);
        if (mCam != null) {
            mCam.setOneShotPreviewCallback(null);
            mCam.stopPreview();
            mCam.release();
//            mCamLock.release();
            mCam = null;
            isStoped = true;

        }
        //        Log.e("zjy", getClass() + "->close(): ==" + (System.currentTimeMillis() - time) / 1000f);
    }


    /**
     * Called when the camera auto focus completes.  If the camera
     * does not support auto-focus and autoFocus is called,
     * onAutoFocus will be called immediately with a fake value of
     * <code>success</code> set to <code>true</code>.
     * <p>
     * The auto-focus routine does not lock auto-exposure and auto-white
     * balance after it completes.
     *
     * @param success true if focus was successful, false if otherwise
     * @param camera  the Camera service object
     * @see Camera.Parameters#setAutoExposureLock(boolean)
     * @see Camera.Parameters#setAutoWhiteBalanceLock(boolean)
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.e("zjy", getClass() + "->onAutoFocus(): result==" + success);
        scheduleAutoFocus();
    }
}
