package com.b1b.js.erpandroid_kf.presenter;

import com.b1b.js.erpandroid_kf.buss.IMainDataSource;
import com.b1b.js.erpandroid_kf.buss.MainSourceImpl;
import com.b1b.js.erpandroid_kf.viewinterface.MainAcView;

import java.util.HashMap;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public class MainPresenter implements IMainTaskPresenter {

    private MainAcView mView;
    private IMainDataSource dataSource;


    public MainPresenter(MainAcView mView, IMainDataSource dataSource) {
        this.mView = mView;
        this.dataSource = dataSource;
        mView.setPresenter(this);
    }

    //登录
    //检查uid是否改变，1：清理缓存，获取UserInfo
    //是否保存密码
    //跳转
    public void login(final String uname, final String password, String version) {
        mView.loginBefore();
        dataSource.getLogin(uname, password, version, new MainSourceImpl.DataCallback() {
            @Override
            public void result(String result) {
                if (result.startsWith("SUCCESS")) {
                    mView.loginFinish("1", "");
                    savePwd(uname, password);
                } else {
                    mView.loginFinish("0", result);
                }
            }
        });
    }

    public void clearCache() {
    }

    public void getUserInfo() {

    }

    public String getDebugPwd() {
        return dataSource.getDebugPwd();
    }

    public void savePwd(String uname, String password) {
        dataSource.savePwd(true, uname, password, false);
    }

    public void changePwd() {
        if (dataSource.setDebugPwd()) {
            mView.showToast("进入debug模式");
        }
    }

    @Override
    public void start() {

    }

    public void readCode(String code) {
        mView.loginBefore();
        dataSource.getScanResult(code, new MainSourceImpl.DataCallback() {
            @Override
            public void result(String result) {
                if (result.startsWith("1")) {
                    mView.scanResult("1", result);
                }else{
                    mView.scanResult("0", result);
                }
            }
        });
    }
    public void startUpdate(){
        dataSource.startUpdateCheck(new MainSourceImpl.UpdateInfoCallback() {
            @Override
            public void getUpdateInfo(HashMap<String, String> map) {
                String sCode = map.get("code");
                final String sContent = map.get("content");
                final String sDate = map.get("date");
                String updateInfo = "，更新说明:\n";
                updateInfo += "更新时间:" + sDate + "\n";
                updateInfo += "更新内容:" + sContent;
                mView.setUpdateInfo(updateInfo);
            }
        });
    }
}
