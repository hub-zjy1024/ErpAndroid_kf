package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WcfUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {

    private EditText edUserName;
    private EditText edPwd;
    private Button btnLogin;
    private Button btnScancode;
    private CheckBox cboRemp;
    private CheckBox cboAutol;
    private SharedPreferences sp;
    private ProgressDialog pd;
    String name;
    private boolean canStartIntent = true;
    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //失败
                case 0:
                    pd.cancel();
                    MyToast.showToast(MainActivity.this, msg.obj.toString());
                    break;
                case 1:
                    //成功
                    if (canStartIntent) {
                        ifSavePwd();
                        if (!sp.getString("name", "").equals(msg.obj.toString())) {
                            sp.edit().clear().commit();
                        }
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        pd.cancel();
                        handler.removeCallbacksAndMessages(null);
                        MyApp.id = msg.obj.toString();
                        new Thread() {
                            @Override
                            public void run() {
                                boolean success = false;
                                while (!success) {
                                    try {
                                        getUserInfo();
                                        success = true;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                        startActivity(intent);
                        finish();
                        canStartIntent = true;
                    }
                    break;
                case 2:

                    MyToast.showToast(MainActivity.this, "网络状态不佳,请检查网络状态");
                    pd.cancel();
                    break;
            }
        }
    };

    private void getUserInfo() throws IOException, XmlPullParserException, JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("checker", "1");
        map.put("uid", "101");
        SoapObject request = WcfUtils.getRequest(map, "GetUserInfoByUID");
        SoapPrimitive response = WcfUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WcfUtils.Login);
        Log.e("zjy", "MainActivity.java->run(): info==" + MyApp.id + "\t" + response.toString());
        JSONObject object = new JSONObject(response.toString());
        JSONArray jarr = object.getJSONArray("表");
        JSONObject info = jarr.getJSONObject(0);
        String cid = info.getString("CorpID");
        String did = info.getString("DeptID");
        sp = getSharedPreferences("UserInfo", 0);
        sp.edit().putInt("cid", Integer.parseInt(cid)).putInt("did", Integer.valueOf(did)).commit();
    }

    private void ifSavePwd() {
        if (cboRemp.isChecked()) {
            name = edUserName.getText().toString().trim();
            String pwd = edPwd.getText().toString().trim();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("name", name);
            editor.putString("pwd", pwd);
            editor.putBoolean("remp", true);
            editor.putBoolean("autol", cboAutol.isChecked());
            editor.apply();
        }
    }

    /**
     * 获取当前连接的wifi地址
     *
     * @return 获取当前连接的wifi地址
     */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();

        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("zjy", "MainActivity.java->onCreate(): ip==" + getLocalIpAddress());
        edUserName = (EditText) findViewById(R.id.login_username);
        edPwd = (EditText) findViewById(R.id.login_pwd);
        btnLogin = (Button) findViewById(R.id.login_btnlogin);
        btnScancode = (Button) findViewById(R.id.login_scancode);
        cboRemp = (CheckBox) findViewById(R.id.login_rpwd);
        cboAutol = (CheckBox) findViewById(R.id.login_autol);
        sp = getSharedPreferences("UserInfo", 0);
        readCache();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edUserName.getText().toString().trim();
                String pwd = edPwd.getText().toString().trim();
                if (pwd.equals("") || name.equals("")) {
                    MyToast.showToast(MainActivity.this, "请填写完整信息后再登录");
                } else {
                    login(name, pwd);
                }
            }
        });
        btnScancode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 200);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            MyToast.showToast(MainActivity.this, "得到扫码结果");
        }
    }

    private void readCache() {
        if (sp.getBoolean("remp", false)) {
            edUserName.setText(sp.getString("name", null));
            edPwd.setText(sp.getString("pwd", null));
            cboRemp.setChecked(true);
            if (sp.getBoolean("autol", false)) {
                cboAutol.setChecked(true);
                login(sp.getString("name", null), sp.getString("pwd", null));
            }
        }
    }

    /**
     * 登录
     */
    private void login(final String name, final String pwd) {
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("登陆中");
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                canStartIntent = false;
            }
        });
        pd.show();
        //连接超时提示
        new Thread() {
            @Override
            public void run() {
                SoapObject t1 = WcfUtils.getRequest(null, "GetClientIP");
                try {
                    SoapPrimitive sp = WcfUtils.getSoapPrimitiveResponse(t1, SoapEnvelope.VER11, "MartService.svc");
                    Log.e("zjy", "MainActivity.java->run(): ssss==" + sp.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }


                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                PackageManager pm = getPackageManager();
                String version = "";
                try {
                    PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    version = info.versionName;
                    map.put("checkWord", "sdr454fgtre6e655t5rt4");
                    map.put("userID", name);
                    map.put("passWord", pwd);
                    map.put("DeviceID", WcfUtils.DeviceID + "," + WcfUtils.DeviceNo);
                    map.put("version", version);
                    SoapPrimitive result = null;
                    SoapObject loginReq = WcfUtils.getRequest(map, "AndroidLogin");
                    result = WcfUtils.getSoapPrimitiveResponse(loginReq, SoapEnvelope.VER11, WcfUtils.MartService);
                    String[] resArray = result.toString().split("-");
                    if (resArray[0].equals("SUCCESS")) {
                        Message msg1 = handler.obtainMessage();
                        msg1.what = 1;
                        msg1.obj = name;
                        handler.sendMessage(msg1);
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.what = 0;
                        msg.obj = result.toString();
                        handler.sendMessage(msg);
                    }
                    Log.e("zjy", "MainActivity.java->run(): 1111==" + result.toString());
                } catch (IOException e) {
                    handler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    // 获取设备ID
    private void getMyPhoneNumber() {
        try {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
