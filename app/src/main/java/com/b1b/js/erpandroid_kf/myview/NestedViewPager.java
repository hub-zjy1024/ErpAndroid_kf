package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 张建宇 on 2019/11/18.
 */
public class NestedViewPager extends ViewPager implements NestedScrollingParent2 {
    private NestedScrollingParentHelper mHelper = new NestedScrollingParentHelper(this);
    public NestedViewPager(@NonNull Context context) {
        super(context);
    }

    public NestedViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public int mHeaderHeight = 100;

    @Override
    public boolean onStartNestedScroll(@NonNull View view, @NonNull View view1, int i, int i1) {
        return view1 instanceof NestedScrollingChild;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View view, @NonNull View view1, int i, int i1) {
        mHelper.onNestedScrollAccepted(view, view1, i, i1);
    }

    @Override
    public void onStopNestedScroll(@NonNull View view, int i) {
        mHelper.onStopNestedScroll(view, i);
    }

    @Override
    public void onNestedScroll(@NonNull View view, int i, int i1, int i2, int i3, int i4) {

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
//        super.onNestedPreScroll(target, dx, dy, consumed);
//
//        boolean headerScrollUp = dy > 0 && getScrollY() < mHeaderHeight;
//        boolean headerScrollDown = dy < 0 && getScrollY() > 0 && !target.canScrollVertically(-1);
//        if (headerScrollUp || headerScrollDown) {
//            scrollBy(0, dy);
//            consumed[1] = dy;
//        }
    }
    @Override
    public void onNestedPreScroll(@NonNull View view, int i, int i1, @NonNull int[] ints, int i2) {

    }
}
