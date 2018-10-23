package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by 张建宇 on 2019/7/13.
 */
public class ScanSurfaceView extends SurfaceView {
    private ScanViewContainer parentContainer;
    private CamMgr mgr;
    private CameraDataConverter dataConverter = new CameraDataConverter();

    public ScanSurfaceView(Context context, ScanViewContainer parentContainer) {
        this(context, (AttributeSet) null);
        this.parentContainer = parentContainer;
    }

    public ScanSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void foucs() {
        mgr.safeAutoFocus();
    }

    public int getRotationCount() {
        return mgr.getRotationCount();
    }

    private void init() {

        SurfaceHolder holder = getHolder();
        mgr = new CamMgr(getContext());

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                ViewParent parent = getParent();
                int sWidth = -1;
                int sHeight = -1;
                if (parent != null) {
                    sWidth = ((ViewGroup) parent).getWidth();
                    sHeight = ((ViewGroup) parent).getHeight();
                }
                final int finalSWidth = sWidth;
                final int finalSHeight = sHeight;
                long time = System.currentTimeMillis();
                mgr.asycnOpen(holder, parentContainer, finalSWidth, finalSHeight);
                Log.e("zjy", getClass() + "->testTime(): useTime ==" + (System.currentTimeMillis() - time) / 1000);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mgr.stopPreveiw();
                mgr.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e("zjy", getClass() + "->surfaceDestroyed(): ==");
                mgr.asyncClose();
            }
        });
    }

    public void closeCamera() {
        mgr.releaseAll();
        mgr.testLeak();
//        mgr.asyncClose();
    }

    byte[] getRotatedData(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;
        int rotationCount = getRotationCount();
        int flag = rotationCount / 90;
        int previewFormat = parameters.getPreviewFormat();
        if (previewFormat == ImageFormat.YV12) {
            data = dataConverter.YV12toNV21(data, width, height);
        }
        if (flag == 2) {
            data = dataConverter.rotateYUV420Degree180(data, width, height);
        } else if (flag == 1) {
            data = dataConverter.rotateYUV420Degree90(data, width, height);
        } else if (flag == 3) {
            data = dataConverter.YUV420spRotate270(data, width, height);
        }
        return data;
    }

    public boolean isStoped() {
        return mgr.isStoped;
    }

    public void setPreviewCallback(Camera.PreviewCallback cb) {
        mgr.setOneShotPreviewCallback(cb);
    }
}
