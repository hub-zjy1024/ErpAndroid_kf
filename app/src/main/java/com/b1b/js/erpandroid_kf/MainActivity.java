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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.utils.MyFileUtils;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private ProgressDialog scanDialog;
    private String updateLog;
    private String downPath;
    private TextView tvVersion;
    private final int SCANCODE_LOGIN_SUCCESS = 4;
    private final int NEWWORK_ERROR = 2;
    private final int FTPCONNECTION_ERROR = 5;
    private AlertDialog permissionDialog;
    private Handler zHandler = new Handler() {
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
                    //登陆用户名改变，清除缓存
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
                    //是否记住密码
                    //                    ifSavePwd(true, "101", "62105300");
                    pd.cancel();
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case NEWWORK_ERROR:
                    MyToast.showToast(MainActivity.this, "网络状态不佳,请检查网络状态");
                    if (pd != null) {
                        pd.cancel();
                    }
                    if (scanDialog != null && scanDialog.isShowing()) {
                        scanDialog.cancel();
                    }
                    break;
                //扫码登录处理
                case SCANCODE_LOGIN_SUCCESS:
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
                        final String[] urls = url.split("\\|");
                        String localUrl = sp.getString("ftp", "");
                        if (!defUid.equals(uid)) {
                            //第一次登录或者更换手机
                            sp.edit().clear().commit();
                            getUserInfoDetail(MyApp.id);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", uid).apply();
                            //"|"为特殊字符，需要用"\\"转义
                            checkSaveFTP(url);
                        } else {
                            if (localUrl.equals("")) {
                                checkSaveFTP(url);
                            } else {
                                for (int i = 0; i < urls.length; i++) {
                                    if (urls[i].equals(localUrl)) {
                                        MyApp.ftpUrl = localUrl;
                                        if (scanDialog != null && scanDialog.isShowing()) {
                                            scanDialog.cancel();
                                        }
                                        Intent intentScan = new Intent(MainActivity.this, MenuActivity.class);
                                        startActivity(intentScan);
                                        finish();
                                        break;
                                    } else {
                                        if (i == urls.length - 1) {
                                            checkSaveFTP(url);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (scanDialog != null && scanDialog.isShowing()) {
                            scanDialog.cancel();
                        }
                        MyToast.showToast(MainActivity.this, "扫描结果有误");
                    }
                    break;
                //获取ftp地址
                case FTPCONNECTION_ERROR:
                    //连接ftp失败
                    MyToast.showToast(MainActivity.this, "连接不到ftp服务器:" + msg.obj.toString() + ",扫码登录失败");
                    if (scanDialog != null && scanDialog.isShowing()) {
                        scanDialog.cancel();
                    }
                    break;
                case 6:
                    MyToast.showToast(MainActivity.this, "获取ftp地址成功:" + MyApp.ftpUrl);
                    if (pd != null && pd.isShowing()) {
                        pd.cancel();
                    }
                    Intent intentScan = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intentScan);
                    finish();
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
                    startUpdate();
                    //                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //                    builder.setTitle("提示");
                    //                    builder.setMessage("当前有新版本可用，是否更新?\n更新内容：\n" + updateLog);
                    //                    builder.setCancelable(false);
                    //                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    //                        @Override
                    //                        public void onClick(DialogInterface dialog, int which) {
                    //                            startUpdate();
                    //                        }
                    //                    });
                    //                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    //                        @Override
                    //                        public void onClick(DialogInterface dialog, int which) {
                    //
                    //                        }
                    //                    });
                    //                    builder.show();
                    break;
                case 9:
                    tvVersion.setText("当前版本为：" + msg.obj.toString());
                    break;
                case 10:
                    MyToast.showToast(MainActivity.this, "部门号或公司号为空");
                    break;
                case 11:
                    downPd.cancel();
                    MyToast.showToast(MainActivity.this, "下载失败");
                    break;
                case 12:
                    String info = tvVersion.getText().toString().trim();
                    info = info + "，更新说明:" + "\n" + updateLog;
                    tvVersion.setText(info);
                    break;
            }
        }
    };

    /**
     @param savedInstanceState
     */
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
        tvVersion = (TextView) findViewById(R.id.main_version);
        sp = getSharedPreferences("UserInfo", 0);
        final String phoneCode = CaigoudanEditActivity.getPhoneCode(MainActivity.this);
        Log.e("zjy", "MainActivity.java->onCreate(): phoneInfo==" + phoneCode);
        MyFileUtils fileUtils = new MyFileUtils();
        fileUtils.obtainFileDir(MainActivity.this);
        if (MyApp.myLogger != null) {
            MyApp.myLogger.writeInfo("phonecode:" + phoneCode);
        }
        //检查更新
        checkUpdate();
        //        readCache();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneCode.endsWith("868930027847564") || phoneCode.endsWith("358403032322590") || phoneCode.endsWith("864394010742122") || phoneCode.endsWith("A0000043F41515")) {
                    login("101", "62105300");
                }
            }
        });
        btnScancode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                //                } else {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 200);
                //                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e("zjy", "MenuActivity.java->onRequestPermissionsResult(): ok==");
        } else {
            if (permissionDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("建议");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        MainActivity.this.startActivity(intent);
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setMessage("缺少相机权限，是否跳转到权限管理页面开启权限");
                permissionDialog = builder.create();
                permissionDialog.show();
            } else {
                permissionDialog.show();
            }
        }
    }

    private void startUpdate() {
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
                    update(MainActivity.this, zHandler);
                } catch (IOException e) {
                    zHandler.sendEmptyMessage(11);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void checkSaveFTP(final String url) {
        final String[] urls = url.split("\\|");
        new Thread() {
            @Override
            public void run() {
                int counts = urls.length;
                int times = 0;
                for (int i = 0; i < urls.length; i++) {
                    try {
                        Socket socket = new Socket();
                        SocketAddress remoteAddr = new InetSocketAddress(urls[i], 21);
                        socket.connect(remoteAddr, 10 * 1000);
                        MyApp.ftpUrl = urls[i];
                        sp.edit().putString("ftp", MyApp.ftpUrl).apply();
                        zHandler.sendEmptyMessage(6);
                        break;
                    } catch (IOException e) {
                        times++;
                        if (counts == times) {
                            Message msg = zHandler.obtainMessage(FTPCONNECTION_ERROR);
                            msg.obj = url;
                            zHandler.sendMessage(msg);
                        }
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void checkUpdate() {
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageManager pm = getPackageManager();
                    PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    Message lVersionMsg = zHandler.obtainMessage(9);
                    lVersionMsg.obj = info.versionName;
                    lVersionMsg.sendToTarget();
                    boolean ifUpdate = checkVersion(info.versionCode);
                    if (ifUpdate) {
                        zHandler.sendEmptyMessage(7);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getUserInfoDetail(final String uid) {
        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                while (!success) {
                    try {
                        Map<String, Object> result = getUserInfo(uid);
                        sp = getSharedPreferences("UserInfo", 0);
                        sp.edit().putInt("cid", (int) result.get("cid")).putInt("did", (int) result.get("did")).
                                putString("oprName", (String) result.get("oprName")).apply();
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NumberFormatException e) {
                        zHandler.sendEmptyMessage(10);
                        e.printStackTrace();
                    }
                    if (!success) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    private Map<String, Object> getUserInfo(String uid) throws IOException, XmlPullParserException, JSONException {
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
        String name = info.getString("Name");
        HashMap<String, Object> result = new HashMap<>();
        result.put("cid", Integer.parseInt(cid));
        result.put("did", Integer.parseInt(did));
        result.put("oprName", name);
        return result;
    }

    private void ifSavePwd(boolean saveOrNot, String name, String pwd) {
        if (saveOrNot) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("name", name);
            editor.putString("pwd", pwd);
            editor.putBoolean("remp", saveOrNot);
            editor.putBoolean("autol", cboAutol.isChecked());
            editor.apply();
        } else {
            sp.edit().clear().commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            scanDialog = new ProgressDialog(MainActivity.this);
            scanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            scanDialog.setMessage("登录中");
            scanDialog.setCancelable(false);
            scanDialog.show();
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
                String code = data.getStringExtra("result");
                if (code != null) {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("checkword", "");
                    map.put("code", code);
                    SoapObject object = WebserviceUtils.getRequest(map, "BarCodeLogin");
                    try {
                        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(object, SoapEnvelope.VER11, WebserviceUtils.MartService);
                        Message msg = zHandler.obtainMessage(SCANCODE_LOGIN_SUCCESS);
                        msg.obj = response.toString();
                        zHandler.sendMessage(msg);
                    } catch (IOException e) {
                        zHandler.sendEmptyMessage(NEWWORK_ERROR);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        zHandler.sendEmptyMessage(NEWWORK_ERROR);
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
                        Message msg1 = zHandler.obtainMessage();
                        HashMap<String, String> infoMap = new HashMap<>();
                        infoMap.put("name", name);
                        infoMap.put("pwd", pwd);
                        msg1.what = 1;
                        msg1.obj = infoMap;
                        zHandler.sendMessage(msg1);
                    } else {
                        Message msg = zHandler.obtainMessage(0);
                        msg.obj = result.toString();
                        zHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    zHandler.sendEmptyMessage(NEWWORK_ERROR);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    zHandler.sendEmptyMessage(NEWWORK_ERROR);
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     @param localVersion 当前应用的版本号
     @return
     @throws SocketTimeoutException
     @throws IOException             */
    public boolean checkVersion(int localVersion) throws IOException {
        boolean ifUpdate = false;
        //        String url = "http://192.168.10.127:8080/AppUpdate/download/readme.txt";
        String url = "http://172.16.6.160:8006/DownLoad/readme.txt";
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(5 * 1000);
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
            String[] info = stringBuilder.toString().split("&");
            if (info.length > 0) {
                try {
                    if (Integer.parseInt(info[1]) > localVersion) {
                        ifUpdate = true;
                    }
                    updateLog = info[3] + "\n" + info[2];
                    Message msg = zHandler.obtainMessage(12);
                    msg.sendToTarget();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            is.close();
            Log.e("zjy", "MainActivity.java->checkVersion(): readme==" + stringBuilder.toString());
        }
        return ifUpdate;
    }

    public void update(Context context, Handler mHandler) throws IOException {
        //        String url = "http://192.168.10.127:8080/AppUpdate/DownLoad/dyjkfapp.apk";
        String downUrl = "http://172.16.6.160:8006/DownLoad/dyjkfapp.apk";
        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setReadTimeout(60000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            File targetDir = MyFileUtils.getFileParent();
            Log.e("zjy", "MainActivity.java->update(): online==" + size);
            File file1 = new File(targetDir, "dyjkfapp.apk");
            FileOutputStream fos = new FileOutputStream(file1);
            int len = 0;
            int hasRead = 0;
            int percent = 0;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                hasRead = hasRead + len;
                percent = (hasRead * 100) / size;
                if (hasRead < 0) {
                    Log.e("zjy", "MainActivity.java->update(): hasRead==" + hasRead);
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
            File file = new File(targetDir, "dyjkfapp.apk");
            if (file.exists()) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(".apk");
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                throw new FileNotFoundException();
            }
        }
    }
}
