package com.b1b.js.erpandroid_kf.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by 张建宇 on 2019/7/13.
 */
public abstract class ScanViewContainer extends FrameLayout implements Camera.PreviewCallback {

    private ScanSurfaceView scanView;
    private ScanRectView scanRectView;

    public ScanViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public ScanViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {

    }

    Rect getFramingRectInPreview(int width, int height) {
        return scanRectView.getFramingRectInPreview(width, height);
    }

    public final void setupLayout() {
        removeAllViews();
        scanView = new ScanSurfaceView(getContext(), this);
        scanView.setZOrderOnTop(false);
        scanRectView = new ScanRectView(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(scanView, lp);
        addView(scanRectView, lp);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e("zjy", getClass() + "->onInterceptTouchEvent(): mTouch==");
        scanView.foucs();
        return super.onInterceptTouchEvent(ev);
    }

    public void startCamera() {
        setupLayout();
    }

    public void stopCamera() {
        scanView.closeCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            if (scanView.isStoped()) {
                return;
            }
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            int width = size.width;
            int height = size.height;
            int rotationCount = scanView.getRotationCount();
            int flag = rotationCount / 90;
            if (flag == 1 || flag == 3) {
                int temp = width;
                width = height;
                height = temp;
            }
            data = scanView.getRotatedData(data, camera);
            Rect rect = getFramingRectInPreview(width, height);
            if (!debugFlag) {
                int previewFormat = camera.getParameters().getPreviewFormat();
                Log.e("zjy", getClass() + "->onPreviewFrame():" + String.format("datasize=%d,pw-ph=%d-%d," +
                        "preFormat=%d,Thread=%s", data.length, width, height, previewFormat,
                        Thread.currentThread().getName()));
                debugFlag = true;
            }
            DecodeTask task = new DecodeTask(data, width, height, rect, this);
            task.execute();

        } catch (RuntimeException e) {
            Log.e("zjy", getClass() + "->onPreviewFrame(): error==", e);
        }
    }

    private void test(byte[] data, int pWidth, int pHeight, Rect decodePart) {

        YuvImage yuvimage2 = new YuvImage(
                data,
                ImageFormat.NV21,
                pWidth,
                pHeight,
                null);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        yuvimage2.compressToJpeg(decodePart, 100, baos2);
        byte[] decodeImg2 = baos2.toByteArray();
        if (listener != null) {
            Log.e("zjy", getClass() + "->test(): showImgData==" + decodeImg2.length);
            listener.getData(decodeImg2);
        }
    }


    private void onDecodeFinished(String result) {
        Log.e("zjy", getClass() + "->onDecodeFinished(): ==" + result);
        if (listener != null) {
            listener.getCodeStr(result);
        }
    }

    static class DecodeTask extends AsyncTask<Void, Void, String> {
        byte[] data;
        int pWidth;
        int pHeight;
        Rect decodePart;
        WeakReference<ScanViewContainer> weakReference;

        public DecodeTask(byte[] data, int pWidth, int pHeight, Rect decodePart, ScanViewContainer
                mContainer) {
            this.data = data;
            this.pWidth = pWidth;
            this.pHeight = pHeight;
            this.decodePart = decodePart;

            weakReference = new WeakReference<>(mContainer);
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... voids) {
            ScanViewContainer mContainer = weakReference.get();
            if (mContainer == null) {
                return null;
            }
            //            mContainer.test(data, pWidth, pHeight, decodePart);
            return mContainer.startDecode(data, decodePart, pWidth, pHeight);
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            ScanViewContainer mContainer = weakReference.get();
            if (mContainer == null) {
                return;
            }
            mContainer.resumeDecode();
            if (aVoid != null) {
                mContainer.onDecodeFinished(aVoid);
            }
        }
    }

    public void resumeDecode() {
        scanView.setPreviewCallback(this);
    }

    boolean debugFlag = false;

    protected abstract String startDecode(byte[] data, Rect rect, int width, int height);

    ResultListener listener;

    public interface ResultListener {
        void getData(byte[] data);

        void getCodeStr(String code);

    }

    public static abstract class SimpleListener implements ResultListener {
        @Override
        public void getData(byte[] data) {

        }
    }

    public void setResultCallback(ResultListener listener) {
        this.listener = listener;
    }
}
