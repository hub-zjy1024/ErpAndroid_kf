package com.b1b.js.erpandroid_kf.viewinterface;

import android.content.Intent;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public interface BaseView<T> {
    void openAc(Intent intent);

    void showProgressDialog(String msg);

    void showToast(String msg);

    void setPresenter(T presenter);

    void loginFail(String msg);
}
