package com.b1b.js.erpandroid_kf.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.b1b.js.erpandroid_kf.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by 张建宇 on 2019/3/25.
 * 需要配置样式
 *   <item name="android:windowBackground">@android:color/transparent</item>
 *   <item name="android:colorBackgroundCacheHint">@null</item>
 *   <item name="android:windowIsTranslucent">true</item>
 */
public class SlideBackActivity extends BaseMActivity {

    private int rate = 300;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_slideback);
        decorView = getWindow().getDecorView();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }

    private int downX;
    private View decorView;
    private int screenWidth;
    private int screenHeight;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveDistanceX = event.getX() - downX;
                if (moveDistanceX > 0) {
                    decorView.setX(moveDistanceX);
                }
                break;
            case MotionEvent.ACTION_UP:
                float moveDistanceX2 = event.getX() - downX;
                //只允许右滑
                if (moveDistanceX2 > 0) {
                    if (moveDistanceX2 > screenWidth / 2) {
                        // 如果滑动的距离超过了手机屏幕的一半, 滑动处屏幕后再结束当前Activity
                        continueMove(moveDistanceX2);
                    } else {
                        // 如果滑动距离没有超过一半, 往回滑动
                        rebackToLeft(moveDistanceX2);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private Animator getBackAnimation(float moveDistanceX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(decorView, "x", moveDistanceX, 0);
        //        animator.setInterpolator(new );
        int time = (int) (rate * moveDistanceX / screenWidth);
        animator.setDuration(time);
        return animator;
    }

    private Animator getFinishAnimation(float moveDistanceX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(decorView, "x", moveDistanceX, screenWidth);
        int time = (int) (rate * (screenWidth - moveDistanceX) / screenWidth);
        animator.setDuration(time);
        return animator;
    }

    void rebackToLeft(float moveDistanceX) {
        Animator backAnimation = getBackAnimation(moveDistanceX);
        backAnimation.start();
    }

    void continueMove(float moveDistanceX) {
        Animator backAnimation = getFinishAnimation(moveDistanceX);
        backAnimation.addListener(new AnimatorEndLisenter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }
        });
        backAnimation.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("zjy", "SlideBackActivity->onPause(): ==");
    }

    abstract class AnimatorEndLisenter implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public abstract void onAnimationEnd(Animator animation);

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("zjy", "SlideBackActivity->onStop(): ==");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("zjy", "SlideBackActivity->onDestroy(): ==");
    }
}
