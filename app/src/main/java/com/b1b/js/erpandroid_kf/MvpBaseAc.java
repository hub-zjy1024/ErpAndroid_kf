package com.b1b.js.erpandroid_kf;

import android.os.Bundle;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.presenter.BasePresenter;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public abstract class MvpBaseAc<T extends BasePresenter> extends BaseScanActivity {


    protected T mPresenter;

    public abstract void initView();

    public abstract int getId();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getId());
        initView();
    }

    public abstract void initPresenter();

}
