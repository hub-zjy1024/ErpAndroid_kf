package com.b1b.js.erpandroid_kf.activity.base;

import android.os.Bundle;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.config.ExtraParams;
import com.b1b.js.erpandroid_kf.config.SpSettings;

public class SavedLoginInfoActivity extends BaseMActivity {
    protected String loginID = MyApp.id;

    public String getLoginID() {
        return loginID;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            SpSettings settings;
            outState.putString(ExtraParams.NM_LOGIN_ID, loginID);
            outState.putString(ExtraParams.NM_ftpUrl, MyApp.ftpUrl);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_login_info);
        if (savedInstanceState != null) {
            loginID = savedInstanceState.getString(ExtraParams.NM_LOGIN_ID);
            MyApp.id = loginID;
            MyApp.ftpUrl = savedInstanceState.getString(ExtraParams.NM_ftpUrl);
        }
//        Log.e("zjy", getClass().getName() + "->supper->onCreate(): nowID==" + loginID);
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }
}
