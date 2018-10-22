package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.buss.MainSourceImpl;
import com.b1b.js.erpandroid_kf.presenter.IMainTaskPresenter;
import com.b1b.js.erpandroid_kf.presenter.MainPresenter;
import com.b1b.js.erpandroid_kf.viewinterface.MainAcView;

import utils.MyToast;
import utils.UploadUtils;
import utils.handler.NoLeakHandler;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public class MainActivityMvp extends MvpBaseAc<IMainTaskPresenter> implements MainAcView, View
        .OnClickListener {

    private EditText edUserName;
    private EditText edPwd;
    private Button btnLogin;
    private Button btnScancode;
    private CheckBox cboRemp;
    private CheckBox cboAutol;
    private ProgressDialog pd;
    private ProgressDialog scanDialog;
    private TextView tvVersion;
    private String versionName = "1";
    private String tempPassword = "62105300";
    private int time = 0;
    final Context mContext = MainActivityMvp.this;


    private AlertDialog permissionDialog;
    private Handler zHandler = new NoLeakHandler(this);

    @Override
    public void initView() {
        edUserName = (EditText) findViewById(R.id.login_username);
        edPwd = (EditText) findViewById(R.id.login_pwd);
        cboRemp = (CheckBox) findViewById(R.id.login_rpwd);
        cboAutol = (CheckBox) findViewById(R.id.login_autol);
        tvVersion = (TextView) findViewById(R.id.main_version);

        ImageView ivDebug = (ImageView) findViewById(R.id.main_debug);
        btnLogin = (Button) findViewById(R.id.login_btnlogin);
        btnScancode = (Button) findViewById(R.id.login_scancode);
        final Button btnPrintCode = (Button) findViewById(R.id.activity_main_btn_code);
        ivDebug.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnScancode.setOnClickListener(this);
        btnPrintCode.setOnClickListener(this);
        mPresenter = new MainPresenter(this, new MainSourceImpl(this));
        mPresenter.startUpdate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_debug:
                break;
            case R.id.activity_main_btn_code:
                startScanActivity();
                break;
            case R.id.login_btnlogin:
                final String phoneCode = UploadUtils.getPhoneCode(mContext);
                String debugPwd = mPresenter.getDebugPwd();
                PackageManager pm = getPackageManager();
                PackageInfo info = null;
                try {
                    info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    int code = info.versionCode;
                    versionName = info.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Log.e("zjy", "MainActivity->onClick(): password==" + tempPassword);
                if (phoneCode.endsWith("868930027847564") || phoneCode.endsWith("358403032322590") ||
                        phoneCode.endsWith
                                ("864394010742122") || phoneCode.endsWith("A0000043F41515")
                        || phoneCode.endsWith("866462026203849") || phoneCode.endsWith("869552022575930")) {
                    mPresenter.login("101", debugPwd, versionName);
                } else {
                    showToast("请使用扫码登录");
                }
                break;
            case R.id.login_scancode:


                break;
        }

    }

    @Override
    public void getCameraScanResult(String result) {
        mPresenter.readCode(result);
    }

    @Override
    public int getId() {
        return R.id.activity_main;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void loginBefore() {
        showProgressDialog("登陆中");
    }

    @Override
    public void loginFinish(String code, String msg) {
        pd.cancel();
        if (code.equals("1")) {
            logSuccess();
        } else {
            loginFail(msg);
        }
    }

    public void scanResult(String code, String msg) {
        pd.cancel();
        if (code.equals("1")) {
            logSuccess();
        } else {
            loginFail(msg);
        }
    }
    public void logSuccess() {
        openAc(new Intent(this, MenuActivity.class));
        zHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    @Override
    public void setUpdateInfo(String updateInfo) {
        tvVersion.setText(tvVersion.getText().toString() + updateInfo);
    }

    @Override
    public void openAc(Intent intent) {
        startActivity(intent);
    }

    public void loginFail(String msg) {
        pd.cancel();
        showToast(msg);
    }

    @Override
    public void showProgressDialog(String msg) {
        pd = new ProgressDialog(mContext);
        pd.setMessage(msg);
        pd.show();
    }

    @Override
    public boolean isSavedPwd() {
        return cboRemp.isChecked();
    }

    @Override
    public void showToast(String msg) {
        MyToast.showToast(mContext, msg);
    }

    @Override
    public void setPresenter(MainPresenter presenter) {
    }


}
