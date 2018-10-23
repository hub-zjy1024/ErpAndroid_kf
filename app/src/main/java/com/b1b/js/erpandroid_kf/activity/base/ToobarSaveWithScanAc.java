package com.b1b.js.erpandroid_kf.activity.base;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;

import com.b1b.js.erpandroid_kf.R;

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
            mToobar.setOnMenuItemClickListener(this);
        }
        //左边的小箭头
        //        mToobar.setNavigationIcon(android.R.drawable.btn_default);
        // Logo
        //        mToobar.setLogo(R.mipmap.appicon);
        //设置mToobar

    }

    @Override
    public abstract boolean onCreateOptionsMenu(Menu menu);

    public abstract String setTitle();
}
