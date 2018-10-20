package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.buss.MainSourceImpl;
import com.b1b.js.erpandroid_kf.presenter.IMainTaskPresenter;
import com.b1b.js.erpandroid_kf.presenter.MainPresenter;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.viewinterface.MainAcView;

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
    private SharedPreferences sp;
    private ProgressDialog pd;
    private ProgressDialog downPd;
    private ProgressDialog scanDialog;
    private TextView tvVersion;
    private final int SCANCODE_LOGIN_SUCCESS = 4;
    private final int NEWWORK_ERROR = 2;
    private final int FTPCONNECTION_ERROR = 5;
    private String versionName = "1";
    private String tempPassword = "62105300";
    private int time = 0;
    final Context mContext = MainActivityMvp.this;

    TaskManager taskManger = TaskManager.getInstance(5, 9);
    private AlertDialog permissionDialog;
    private Handler zHandler = new NoLeakHandler(this);

    @Override
    public void initView() {
        mV = new MainPresenter(this, new MainSourceImpl(this));
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_debug:
                break;
            case R.id.activity_main_btn_code:
                break;
            case R.id.login_btnlogin:
                break;
            case R.id.login_scancode:


                break;
        }

    }

    @Override
    public void getCameraScanResult(String result) {
        mV.readCode(result);
    }

    @Override
    public int getId() {
        return R.id.activity_main;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void login() {

    }

    @Override
    public void loginBefore() {

    }

    @Override
    public void loginFinish(String code, String msg) {
        if (code.equals("1")) {
            openAc(new Intent(this, MenuActivity.class));
        }else{
            loginFail(msg);
        }
    }

    @Override
    public void openAc(Intent intent) {
        startActivity(intent);
    }

    public void loginFail(String msg) {
        showToast(msg);
    }

    @Override
    public void showProgressDialog(String msg) {

    }

    @Override
    public boolean getSavedPwd() {
        return cboRemp.isChecked();
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void setPresenter(MainPresenter presenter) {
    }


}
