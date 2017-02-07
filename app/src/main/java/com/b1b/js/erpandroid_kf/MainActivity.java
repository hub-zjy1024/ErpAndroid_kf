package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
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
    private ProgressDialog downPd;
    String name;
    private boolean canStartIntent = true;
    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                //失败
                case 0:
                    pd.cancel();
                    MyToast.showToast(MainActivity.this, msg.obj.toString());
                    break;
                case 1:
                    //成功
                    HashMap<String, String> infoMap = (HashMap<String, String>) msg.obj;
                    //每次登录检查userInfo是否有变动，以免数据库更新（流量允许）
                    MyApp.id = infoMap.get("name");
                    if (!sp.getString("name", "").equals(MyApp.id)) {
                        sp.edit().clear().apply();
                        //登录成功之后调用，获取相关信息
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("name", infoMap.get("name"));
                        edit.putString("pwd", infoMap.get("pwd"));
                        edit.apply();
                        getUserInfoDetail(MyApp.id);
                    } else {
                        MyApp.ftpUrl = sp.getString("ftp", "");
                    }
                    ifSavePwd();
                    pd.cancel();
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                    canStartIntent = true;
                    break;
                case 2:
                    MyToast.showToast(MainActivity.this, "网络状态不佳,请检查网络状态");
                    if (pd != null) {
                        pd.cancel();
                    }

                    break;
                case 3:
                    MyToast.showToast(MainActivity.this, "用户不合法");
                    break;
                //扫码登录处理
                case 4:
                    try {
                        JSONObject object1 = new JSONObject(msg.obj.toString());
                        JSONArray main = object1.getJSONArray("表");
                        JSONObject obj = main.getJSONObject(0);
                        String url = obj.getString("PhotoFtpIP");
                        Log.e("zjy", "MainActivity.java->handleMessage(): ftpUrl==" + url);
                        String uid = obj.getString("UserID");
                        MyApp.id = uid;
                        String defUid = sp.getString("name", "");
                        //换用户则清除缓冲
                        if (!defUid.equals(uid)) {
                            sp.edit().clear().apply();
                            getUserInfoDetail(MyApp.id);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", uid).commit();
                        }
                        //"|"为特殊字符，需要用"\\"转义
                        Intent intentScan = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(intentScan);
                        finish();
                        if (sp.getString("ftp", "").equals("")) {
                            final String[] urls = url.split("\\|");
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    boolean isOver = false;
                                    FTPClient client = new FTPClient();
                                    int counts = urls.length;
                                    for (int i = 0; i < urls.length && !isOver; i++) {
                                        try {
                                            Log.e("zjy", "MainActivity.java->run(): tryTimes==" + i);
                                            client.connect(urls[i]);
                                            MyApp.ftpUrl = urls[i];
                                            isOver = true;
                                            sp.edit().putString("ftp", MyApp.ftpUrl).commit();
                                            handler.sendEmptyMessage(6);
                                            break;
                                        } catch (SocketException e) {
                                            Log.e("zjy", "MainActivity.java->run(): i==" + i);
                                            if (counts - 1 == i) {
                                                handler.sendEmptyMessage(5);
                                            }
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.start();
                        } else {
                            MyApp.ftpUrl = sp.getString("ftp", "");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                //获取ftp地址
                case 5:
                    //连接ftp失败
                    MyToast.showToast(MainActivity.this, "连接不到Ftp服务器");
                    break;
                case 6:
                    MyToast.showToast(MainActivity.this, "获取ftp地址成功:" + MyApp.ftpUrl);
                    break;
                case 8:
                    int percent = msg.arg1;
                    if (percent < 0) {
                        return;
                    }
                    downPd.setProgress(percent);
                    if (percent == 100) {
                        downPd.dismiss();
                        MyToast.showToast(MainActivity.this, "下载完成");
                    }
                    break;
                case 7:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("当前有新版本可用，是否更新");
                    builder.setCancelable(false);
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downPd = new ProgressDialog(MainActivity.this);
                            //必须设定进图条样式
                            downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            downPd.setTitle("更新");
                            downPd.setMax(100);
                            downPd.setMessage("下载中");
                            downPd.setProgress(0);
                            downPd.show();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        update(MainActivity.this, handler);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    });
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                    break;
            }
        }
    };

    private void getUserInfoDetail(final String uid) {

        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                while (!success) {
                    try {
                        getUserInfo(uid);
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
    }

    private void getUserInfo(String uid) throws IOException, XmlPullParserException, JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("checker", "1");
        map.put("uid", uid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetUserInfoByUID");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.Login);
        Log.e("zjy", "MainActivity.java->run(): info==" + MyApp.id + "\t" + response.toString());
        JSONObject object = new JSONObject(response.toString());
        JSONArray jarr = object.getJSONArray("表");
        JSONObject info = jarr.getJSONObject(0);
        String cid = info.getString("CorpID");
        String did = info.getString("DeptID");
        sp = getSharedPreferences("UserInfo", 0);
        sp.edit().putInt("cid", Integer.parseInt(cid)).putInt("did", Integer.valueOf(did)).apply();
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
        } else {
            sp.edit().clear().commit();
        }
    }

    /**
     获取当前连接的wifi地址
     @return 获取当前连接的wifi地址
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
        edUserName = (EditText) findViewById(R.id.login_username);
        edPwd = (EditText) findViewById(R.id.login_pwd);
        btnLogin = (Button) findViewById(R.id.login_btnlogin);
        btnScancode = (Button) findViewById(R.id.login_scancode);
        cboRemp = (CheckBox) findViewById(R.id.login_rpwd);
        cboAutol = (CheckBox) findViewById(R.id.login_autol);
        sp = getSharedPreferences("UserInfo", 0);
        //        getMyPhoneNumber();
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageManager pm = getPackageManager();
                    PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    boolean ifUpdate = checkVersion(info.versionCode);
                    if (ifUpdate) {
                        handler.sendEmptyMessage(7);
                    }
                } catch (SocketException e) {
                    handler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (IOException e) {
                    handler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            MyToast.showToast(MainActivity.this, "得到扫码结果");
            readCode(data);
        }
    }

    /**
     读取条码信息
     @param data onActivtyResult()回调的data
     */
    private void readCode(final Intent data) {
        new Thread() {
            @Override
            public void run() {
                String s = data.getStringExtra("result");
                if (s != null) {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("checkword", "");
                    map.put("code", s);
                    SoapObject object = WebserviceUtils.getRequest(map, "BarCodeLogin");
                    try {
                        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(object, SoapEnvelope.VER11, WebserviceUtils.MartService);
                        Message msg = handler.obtainMessage(4);
                        msg.obj = response.toString();
                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        handler.sendEmptyMessage(2);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
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
     登录
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
        new Thread() {
            @Override
            public void run() {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                PackageManager pm = getPackageManager();
                String version = "";
                try {
                    PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    version = info.versionName;
                    map.put("checkWord", "sdr454fgtre6e655t5rt4");
                    map.put("userID", name);
                    map.put("passWord", pwd);
                    map.put("DeviceID", WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo);
                    map.put("version", version);
                    SoapPrimitive result = null;
                    SoapObject loginReq = WebserviceUtils.getRequest(map, "AndroidLogin");
                    result = WebserviceUtils.getSoapPrimitiveResponse(loginReq, SoapEnvelope.VER11, WebserviceUtils.MartService);
                    String[] resArray = result.toString().split("-");
                    if (resArray[0].equals("SUCCESS")) {
                        Message msg1 = handler.obtainMessage();
                        HashMap<String, String> infoMap = new HashMap<String, String>();
                        infoMap.put("name", name);
                        infoMap.put("pwd", pwd);
                        msg1.what = 1;
                        msg1.obj = infoMap;
                        handler.sendMessage(msg1);
                    } else {
                        Message msg = handler.obtainMessage(0);
                        msg.obj = result.toString();
                        handler.sendMessage(msg);
                    }
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
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            Log.e("zjy", "MainActivity.java->getMyPhoneNumber(): versionCode==" + info.versionCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean checkVersion(int localVersion) throws SocketTimeoutException, IOException {
        boolean ifUpdate = false;
        String url = "http://192.168.10.127:8080/AppUpdate/download/readme.txt";
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String len = reader.readLine();
            StringBuilder stringBuilder = new StringBuilder();
            while (len != null) {
                stringBuilder.append(len);
                len = reader.readLine();
            }
            //            byte[] bytes = stringBuilder.toString().getBytes();
            //            String s = new String(bytes, 0, bytes.length, "GB2312");
            //            Log.e("zjy", "MainActivity.java->checkVersion(): s==" + s);

            String[] info = stringBuilder.toString().split("&");
            //            String[] info = s.split("&");
            if (info.length > 0) {
                try {
                    if (Integer.parseInt(info[1]) > localVersion) {
                        ifUpdate = true;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            is.close();
            Log.e("zjy", "MainActivity.java->checkVersion(): readme==" + stringBuilder.toString());
        }
        return ifUpdate;
    }

    public static void update(Context context, Handler mHandler) throws IOException {
        String url = "http://192.168.10.127:8080/AppUpdate/download/dyjkf.apk";
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            File sdDir = Environment.getExternalStorageDirectory();
            if (!sdDir.exists()) {
                Log.e("zjy", "MainActivity.java->update(): no sd==");
            }
            Log.e("zjy", "MainActivity.java->update(): online==" + size);
            File file1 = new File(sdDir, "/dyjkfapp.apk");
            FileOutputStream fos = new FileOutputStream(file1);
            int len = 0;
            int hasRead = 0;
            int percent = 0;
            byte[] buf = new byte[1024];

            while ((len = is.read(buf)) > 0) {
                hasRead = hasRead + len;
                percent = (hasRead * 100) / size;
                if (hasRead < 0) {
                    Log.e("zjy", "MainActivity.java->update(): hasRead==" + hasRead);
                }
                if (percent < 0) {
                    //                    Log.e("zjy", "MainActivity.java->update(): percent=="+percent);
                }
                Message msg = new Message();
                msg.what = 8;
                msg.arg1 = percent;
                mHandler.sendMessage(msg);
                //写入时第三个参数使用len
                fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(sdDir, "/dyjkfapp.apk");
            Log.e("zjy", "MainActivity.java->update(): local==" + file.length());
            if (file.exists()) {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                //                intent.setData(Uri.fromFile(file));
                context.startActivity(intent);
            } else {
                Log.e("zjy", "MainActivity.java->update(): download==not exists");
            }
        }
    }
}
