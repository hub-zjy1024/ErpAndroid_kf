package com.b1b.js.erpandroid_kf.activity.base;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.b1b.js.erpandroid_kf.mvcontract.callback.IBoolCallback;

import java.util.ArrayList;
import java.util.List;

import utils.dbutils.ActivityRecoderDB;
import utils.framwork.DialogUtils;
import utils.framwork.MyToast;

/**
 * Created by 张建宇 on 2019/3/20.
 */
public abstract class BaseMActivity extends AppCompatActivity {
    protected Context mContext;
    private ProgressDialog proDialog;
    private DialogUtils mdialog;
    public static final int reqPermissions = 321;
    public boolean isStoped=true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mdialog = new DialogUtils(mContext);
        ActivityRecoderDB recoderDB = ActivityRecoderDB.newInstance(this);
        recoderDB.addRecord(getClass());
        isStoped=false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        init();
        setListeners();
    }

    IBoolCallback mCallback;

    public void usePermission(String[] permissions, IBoolCallback tempCallback) {
        List<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String pm = permissions[i];
            int isGranted = ContextCompat.checkSelfPermission(this, pm);
            if (isGranted == PackageManager.PERMISSION_DENIED) {
                notGranted.add(pm);
            }
        }
        if (notGranted.size() > 0) {
            mCallback = tempCallback;
            String[] objects = new String[notGranted.size()];
            notGranted.toArray(objects);
            ActivityCompat.requestPermissions(this, objects, reqPermissions);
        } else {
            mCallback = tempCallback;
            mCallback.callback(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == reqPermissions) {
            List<String> notGranted = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                Log.d("zjy", getClass() + "->onRequestPermissionsResult():state=" +
                        "" + grantResult +
                        ",name=" + permissions[i]);
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    notGranted.add(permissions[i]);
                }
            }
            if (notGranted.size() > 0) {
                showMsgDialog("还有权限未被授予，请重启程序并授权");
            } else {
                if (mCallback != null) {
                    mCallback.callback(true);
                }
            }
        }
    }

    public <T extends View> T getViewInContent(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    public void useFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public abstract void init();

    public abstract void setListeners();

    public void setOnClickListener(View.OnClickListener listener, @IdRes int id) {
        View v = getViewInContent(id);
        v.setOnClickListener(listener);
    }

    public void showMsgDialogWithCallback(final String msg, final Dialog.OnClickListener listener) {
        if(isStoped){
            Log.w(getClass().getName()+" ", "isStop when showMsgDialog ,msg= "+msg );
            return;
        }
        if (mdialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mdialog.showMsgDialogWithCallback(msg, listener);
                }
            });
        }
    }

    public void showMsgDialog(final String msg) {
        if(isStoped){
            Log.w(getClass().getName()+" ", "isStop when showMsgDialog ,msg= "+msg );
            return;
        }
        if (mdialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mdialog.showAlertWithId(msg);
                }
            });
        }
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

    public Dialog getDialogById(int id) {
        return mdialog.getDialogById(id);
    }

    public int showProgressWithID(String msg) {
        if (mdialog == null) {
            return -1;
        }
        return mdialog.showProgressWithID(msg);
    }

    public void cancelAllDialog() {
        if (mdialog == null) {
            return ;
        }
        mdialog.cancelAll();
    }

    public void cancelDialogById(int pdId) {
        if (mdialog == null) {
            return ;
        }
        mdialog.cancelDialogById(pdId);
    }

    public void showProgress(String msg) {
        if (mdialog == null) {
            return;
        }
        mdialog.showProgressWithID(msg);
    }

    public void showProgress(String msg, int progress) {
        proDialog = new ProgressDialog(this);
        proDialog.setTitle("请稍后");
        proDialog.setProgress(progress);
        proDialog.setMessage(msg);
        proDialog.show();
    }

    public void cancelProgress() {
        if (proDialog != null) {
            proDialog.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStoped=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStoped=false;
    }

}
