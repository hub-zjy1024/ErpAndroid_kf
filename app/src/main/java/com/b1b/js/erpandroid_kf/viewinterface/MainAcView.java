package com.b1b.js.erpandroid_kf.viewinterface;

import com.b1b.js.erpandroid_kf.presenter.MainPresenter;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public interface MainAcView extends BaseView<MainPresenter> {


    void loginBefore();

    void loginFinish(String code, String msg);

    boolean isSavedPwd();

    void scanResult(String code, String msg);

    void logSuccess();

    void setUpdateInfo(String updateInfo);
}
