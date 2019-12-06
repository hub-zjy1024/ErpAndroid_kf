package com.b1b.js.erpandroid_kf.myview.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import utils.framwork.BitmapLruCache;

/**
 * Created by 张建宇 on 2019/11/18.
 */
public class CommonImageView extends AppCompatImageView {
    private int mWidth = 0;
    private int mHeight = 0;
    GestureDetector mGestureDetector;
    public static final int MAX_SIZE = (int) Runtime.getRuntime().maxMemory() / 8;
    public static BitmapLruCache<String> mCaches = new BitmapLruCache<>(MAX_SIZE);
    public static  LruCache<String, byte[]> mBitCaches = new LruCache<>(MAX_SIZE);
    BitmapRegionDecoder bitmapRegionDecoder;
    private byte[] mBytes;

    Paint mPaint = new Paint();
    Rect mVisable = new Rect();
    Rect bitRect = new Rect();
    Drawable mDrawable;

    public CommonImageView(Context context) {
        this(context, null);
    }

    public CommonImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setScaleType(ScaleType.CENTER);
    }

    void init() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                count++;
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (System.currentTimeMillis() - time1 < doubleCLickCheck) {
                    startUpdate();
                } else {
                    time1 = (int) System.currentTimeMillis();
                }
                Log.e("zjy", "CommonImageView->onSingleTapUp(): ==" + e.getX());
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                int x =(int)  e2.getX();
                int y = (int) e2.getY();
                mVisable.set(x, y, x + mWidth, y + mHeight);
                startUpdate();
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        mPaint.setAntiAlias(true);
    }

  /*  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CommonImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if (mDrawable != null) {
//            mDrawable.draw(canvas);
//        }
        if (bitmapRegionDecoder != null) {
            BitmapFactory.Options options1 = new BitmapFactory.Options();
            Bitmap bitmap = bitmapRegionDecoder.decodeRegion(mVisable, options1);
            canvas.drawBitmap(bitmap, 0, 0, mPaint);
        }
        //        canvas.drawBitmap();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    public void loadPic2(String url) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] bufs = new byte[1024 * 8];
        int len = -1;
        try {
            InputStream in = new FileInputStream(url);
            while ((len = in.read(bufs)) != -1) {
                bao.write(bufs, 0, len);
            }
            mBytes = bao.toByteArray();
            mBitCaches.put(url, mBytes);

            ByteArrayInputStream bin = new ByteArrayInputStream(mBytes);
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(bin, false);
//            Bitmap finalBit = byteArrayToBitmap(mBytes);
//            mDrawable = new BitmapDrawable(finalBit);
            post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Bitmap byteArrayToBitmap(byte[] mBytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length, options);
        int rWidth = options.outWidth;
        int rHeight = options.outHeight;
        int tWidth = mWidth;
        int tHeight = mWidth;
        if (tWidth == 0 || tHeight == 0) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            tWidth = metrics.widthPixels;
            tHeight = metrics.heightPixels;
        }
        float r1 = tWidth / 1f / rWidth;
        float r2 = tHeight / 1f / rHeight;
        float finalRate = Math.max(r1, r2);
        int finalWidth = (int) (finalRate * rWidth);
        int finalHeight = (int) (finalRate * rHeight);
        options.inJustDecodeBounds = false;
        //        try {
        //            BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(in, false);
        //            BitmapFactory.Options options1 = new BitmapFactory.Options();
        //            bitmapRegionDecoder.decodeRegion(mVisable,options1);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
        options.inSampleSize = (int) finalRate;
        Bitmap finalBit = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length, options);
        return finalBit;
    }

    public void release() {
        if (bitmapRegionDecoder != null) {
            bitmapRegionDecoder.recycle();
        }
    }

    public void loadPicFromFile(final String url) {
        mBytes = mBitCaches.get(url);
        if (mBytes != null) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(mBytes);
                bitmapRegionDecoder = BitmapRegionDecoder.newInstance(in, false);
                BitmapFactory.Options options1 = new BitmapFactory.Options();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    loadPic2(url);
                }
            }.start();
        }
    }

    public void loadPic(String url) {

    }

    Holder holder = new Holder();
    class Holder{
        private int startX = 0;
        private int startY = 0;

    }

    private int count = 0;
    private int time1 = 0;
    private int doubleCLickCheck = 100;


    void startUpdate() {

    }

}
