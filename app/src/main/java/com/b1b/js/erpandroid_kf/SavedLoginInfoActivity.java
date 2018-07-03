package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SavedLoginInfoActivity extends AppCompatActivity {
    protected String loginID = MyApp.id;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putString("uid", loginID);
            outState.putString("ftpUrl", MyApp.ftpUrl);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_login_info);
        if (savedInstanceState != null) {
            loginID = savedInstanceState.getString("uid");
            MyApp.id = loginID;
            MyApp.ftpUrl = savedInstanceState.getString("ftpUrl");
        }
        Log.e("zjy", "SavedLoginInfoActivity->onCreate(): nowID==" + loginID);
    }
}
