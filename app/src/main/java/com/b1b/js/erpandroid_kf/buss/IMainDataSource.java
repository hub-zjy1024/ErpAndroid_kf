package com.b1b.js.erpandroid_kf.buss;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public interface IMainDataSource {

    String getLogin(String uname, String pwd, String version, MainSourceImpl.DataCallback mCall);

    void getScanResult(String code, MainSourceImpl.DataCallback mCall);

    String getDebugPwd();

    boolean setDebugPwd();

     void savePwd(boolean isSaved, String name, String pwd, boolean autoLogin);

    void startUpdateCheck(final MainSourceImpl.UpdateInfoCallback updateCallback);

}