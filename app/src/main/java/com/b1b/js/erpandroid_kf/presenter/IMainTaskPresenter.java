package com.b1b.js.erpandroid_kf.presenter;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public interface IMainTaskPresenter extends BasePresenter {
    void readCode(String code);

    void login(String uname, String password, String version);

    String getDebugPwd();

    void changePwd();

    void startUpdate();
}
