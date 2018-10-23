package com.b1b.js.erpandroid_kf.activity.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.b1b.js.erpandroid_kf.MyApp;

import utils.framwork.DialogUtils;
import utils.framwork.MyToast;

/**
 * Created by 张建宇 on 2019/3/20.
 */
public abstract class BaseMActivity extends AppCompatActivity {
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        MyApp.myLogger.writeInfo("create" + getClass());
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        init();
        setListeners();
    }

    public <T extends View> T getViewInContent(@IdRes int resId) {
        return (T) findViewById(resId);
    }


    public abstract void init();

    public abstract void setListeners();

    public void setOnClickListener(View.OnClickListener listener, @IdRes int id) {
        View v = getViewInContent(id);
        v.setOnClickListener(listener);
    }

    public void showMsgDialog(String msg) {
        showMsgDialog(msg, "提示");
    }

    public void showMsgToast(String msg) {
        final String fMSg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyToast.showToast(mContext, fMSg);
            }
        });
    }

    public void showMsgDialog(final String msg, final String title) {
        final String fMSg = msg;
        //执行在主线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.getSpAlert(mContext, fMSg, title).show();
            }
        });
    }
}
