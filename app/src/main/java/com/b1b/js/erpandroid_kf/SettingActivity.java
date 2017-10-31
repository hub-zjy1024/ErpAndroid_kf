package com.b1b.js.erpandroid_kf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.MyToast;

public class SettingActivity extends AppCompatActivity {

    private Handler zHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    MyToast.showToast(SettingActivity.this, "保存打印机ip地址成功");
                    break;
                case 1:
                    MyToast.showToast(SettingActivity.this, "插入失败");
                    break;
                case 2:
                    MyToast.showToast(SettingActivity.this, "当前网络质量较差，连接超时");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button btnSave = (Button) findViewById(R.id.activity_setting_btnsave);
        final EditText edPrinterIP = (EditText) findViewById(R.id.activity_setting_edip);
        final EditText edPrinterServer = (EditText) findViewById(R.id.activity_setting_ed_printerserver);
        final EditText edDiaohuoAccount = (EditText) findViewById(R.id.activity_setting_ed_diaohuo_account);
        final SharedPreferences sp = getSharedPreferences("UserInfo", 0);
        String localPrinterIP = sp.getString("printerIP", "");
        final String serverIP = sp.getString("serverPrinter", "");
        final String diaohuoAccount = sp.getString("diaohuoAccount", "");
        edPrinterIP.setText(localPrinterIP);
        edPrinterServer.setText(serverIP);
        edDiaohuoAccount.setText(diaohuoAccount);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                final String ip = edPrinterIP.getText().toString().trim();
                final String serverIp = edPrinterServer.getText().toString().trim();
                final String diaohuoAccount = edDiaohuoAccount.getText().toString().trim();
                Matcher matcher = pattern.matcher(ip);
                Matcher serverMatcher = pattern.matcher(serverIp);
                boolean matches = matcher.matches();
                if (!ip.equals("")) {
                    if (!matches) {
                        MyToast.showToast(SettingActivity.this, "请输入的预出库打印机的ip格式");
                        return;
                    } else {
                        sp.edit().putString("printerIP", ip).commit();
                        MyToast.showToast(SettingActivity.this, "保存预出库打印机ip地址成功");
                    }
                }
                if (!serverIp.equals("")) {
                    if (!serverMatcher.matches()) {
                        MyToast.showToast(SettingActivity.this, "请输入正确的ip格式");
                    } else {
                        sp.edit().putString("serverPrinter", serverIp).commit();
                        MyToast.showToast(SettingActivity.this, "保存预出库打印机ip地址成功");
                    }
                }
                if (!diaohuoAccount.equals("")) {
                    sp.edit().putString("diaohuoAccount", diaohuoAccount).commit();
                }
//                new Thread(){
//                    @Override
//                    public void run() {
//                        saveIP(ip, MyApp.id);
//                        sp.edit().putString("printerIP", ip).commit();
//                    }
//                }.start();
            }
        });

    }

    public void saveIP(String ip, String id) {
        try {
            URL url = new URL("http://192.168.10.101:8080/Dyj_server/saveUser?uid=" + id + "&ip=" + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            InputStream in = conn.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            StringBuilder builder = new StringBuilder();
            while ((len = in.read(buf)) != -1) {
                builder.append(new String(buf, 0, len, "UTF-8"));
            }
            if (builder.toString().equals("插入成功")) {
                zHandler.sendEmptyMessage(0);
            } else {
                zHandler.sendEmptyMessage(1);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            zHandler.sendEmptyMessage(2);
            e.printStackTrace();
        }
    }
}