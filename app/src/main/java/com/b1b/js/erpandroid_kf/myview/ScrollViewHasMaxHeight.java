package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.b1b.js.erpandroid_kf.R;

/**
 * Created by 张建宇 on 2019/8/23.
 */
public class ScrollViewHasMaxHeight extends ScrollView {
    public ScrollViewHasMaxHeight(Context context) {
        this(context, null);
    }

    private int maxHeight;


    public ScrollViewHasMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public  int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }
    public ScrollViewHasMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int heightPixels = getContext().getResources().getDisplayMetrics().heightPixels;

        int tempMax = (int) (heightPixels * 0.6);

        if (attrs != null) {
//            context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScrollViewHasMaxHeight, defStyleAttr, 0);
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollViewHasMaxHeight);
            maxHeight = typedArray.getDimensionPixelSize(R.styleable.ScrollViewHasMaxHeight_mh_height,
                    tempMax);
            typedArray.recycle();
        } else {
            maxHeight = tempMax;
        }


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
