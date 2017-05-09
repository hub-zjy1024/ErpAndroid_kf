package com.b1b.js.erpandroid_kf;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.utils.MyToast;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button btnSave = (Button) findViewById(R.id.activity_setting_btnsave);
        final EditText edPrinterIP = (EditText) findViewById(R.id.activity_setting_edip);
        final SharedPreferences sp = getSharedPreferences("UserInfo", 0);
        String localPrinterIP = sp.getString("printerIP", "");
        edPrinterIP.setText(localPrinterIP);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putString("printerIP", edPrinterIP.getText().toString()).commit();
                MyToast.showToast(SettingActivity.this, "保存打印机ip地址成功");
            }
        });
    }
}