package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 Created by 张建宇 on 2017/5/17. */

public class MyHorizontalScroll extends HorizontalScrollView{
    public MyHorizontalScroll(Context context) {
        super(context);
    }

    float y;
    float x;
    public MyHorizontalScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyHorizontalScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x =getX();
                y = getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(getX() - x) < Math.abs(getY() - y)) {
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

}
