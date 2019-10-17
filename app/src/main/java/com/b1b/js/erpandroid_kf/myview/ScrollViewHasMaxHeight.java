package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by 张建宇 on 2019/8/23.
 */
public class ScrollViewHasMaxHeight extends ScrollView {
    public ScrollViewHasMaxHeight(Context context) {
        super(context);
    }

    private int maxHeight;


    public ScrollViewHasMaxHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewHasMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredHeight() >= maxHeight) {
            setMeasuredDimension(getMeasuredWidth(), maxHeight);
        }
    }
    public int getMaxHeight() {
        return maxHeight;
    }
    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }
}
