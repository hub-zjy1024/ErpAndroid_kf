package com.b1b.js.erpandroid_kf.activity.base;

import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.b1b.js.erpandroid_kf.R;

import java.lang.reflect.Method;

/**
 * Created by 张建宇 on 2019/5/25.
 */
public abstract class ToobarSaveWithScanAc extends SavedLoginInfoWithScanActivity  implements Toolbar.OnMenuItemClickListener {
    protected android.support.v7.widget.Toolbar mToobar;

    boolean isShow = true;
    public void disabledToolbar(){
        isShow = false;
    }
    @Override
    public void init() {
        if (isShow) {
            ViewGroup rootView = getViewInContent(android.R.id.content);
            ViewGroup layoutView = (ViewGroup) rootView.getChildAt(0);
            mToobar = (android.support.v7.widget.Toolbar) LayoutInflater.from(this).inflate(R.layout
                            .title_normal_toobar,
                    layoutView, false);
            layoutView.addView(mToobar, 0);
            setSupportActionBar(mToobar);
            // 主标题
            mToobar.setTitle(setTitle());
            // 副标题
            mToobar.setSubtitle("");
            //设置点击事件
//            mToobar.setOnMenuItemClickListener(this);

            mToobar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        //左边的小箭头
        //        mToobar.setNavigationIcon(android.R.drawable.btn_default);
        // Logo
        //        mToobar.setLogo(R.mipmap.appicon);
        //设置mToobar

    }

    public void addViewToToolBar(View mView) {
        int marginRight = getResDimen(R.dimen.activity_horizontal_margin);
        android.support.v7.widget.Toolbar.LayoutParams mParams =
                new android.support.v7.widget.Toolbar.LayoutParams(android.widget.Toolbar.LayoutParams.WRAP_CONTENT,
                        android.widget.Toolbar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
        mParams.rightMargin = marginRight;
        addViewToToolBar(mView, mParams);
    }

    public void addViewToToolBar(View mView, android.support.v7.widget.Toolbar.LayoutParams mParams) {
        mToobar.addView(mView, mParams);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public abstract boolean onCreateOptionsMenu(Menu menu);

    public abstract String setTitle();
}
