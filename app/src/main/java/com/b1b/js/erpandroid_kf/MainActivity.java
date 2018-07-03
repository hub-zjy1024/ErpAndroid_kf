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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import utils.DialogUtils;
import utils.HttpUtils;
import utils.MyFileUtils;
import utils.MyToast;
import utils.UploadUtils;
import utils.WebserviceUtils;
import utils.handler.NoLeakHandler;
import utils.wsdelegate.Login;
import utils.wsdelegate.MartService;

public class MainActivity extends BaseScanActivity  {

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
    final MainActivity mContext = MainActivity.this;
    TaskManager taskManger = TaskManager.getInstance(5, 9);
    private AlertDialog permissionDialog;
    private Handler zHandler = new NoLeakHandler(this);
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
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
                com.b1b.js.erpandroid_kf.MyApp.id = infoMap.get("name");
                //登陆用户名改变，清除缓存
                if (!sp.getString("name", "").equals(com.b1b.js.erpandroid_kf.MyApp.id)) {
                    sp.edit().clear().apply();
                    //登录成功之后调用，获取相关信息
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("name", infoMap.get("name"));
                    edit.putString("pwd", infoMap.get("pwd"));
                    edit.apply();
                    getUserInfoDetail(com.b1b.js.erpandroid_kf.MyApp.id);
                } else {
                    //                        MyApp.ftpUrl = sp.getString("ftp", "");
                }
                //是否记住密码
                //                    ifSavePwd(true, "101", "62105300");
                pd.cancel();
                Intent intent = new Intent(MainActivity.this, com.b1b.js.erpandroid_kf.MenuActivity.class);
                startActivity(intent);
                finish();
                break;
            case NEWWORK_ERROR:
                com.b1b.js.erpandroid_kf.MyApp.myLogger.writeError("bad network");
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
                    com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo(MainActivity.class, "FTP:" + url);
                    String uid = obj.getString("UserID");
                    com.b1b.js.erpandroid_kf.MyApp.id = uid;
                    String defUid = sp.getString("name", "");
                    //换用户则清除缓冲
                    final String[] urls = url.split("\\|");
                    String localUrl = sp.getString("ftp", "");
                    if (!defUid.equals(uid)) {
                        //第一次登录或者更换手机
                        sp.edit().clear().commit();
                        getUserInfoDetail(com.b1b.js.erpandroid_kf.MyApp.id);
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
                                    com.b1b.js.erpandroid_kf.MyApp.ftpUrl = localUrl;
                                    if (scanDialog != null && scanDialog.isShowing()) {
                                        scanDialog.cancel();
                                    }
                                    Intent intentScan = new Intent(MainActivity.this, com.b1b.js.erpandroid_kf.MenuActivity
                                            .class);
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
                MyToast.showToast(MainActivity.this, "获取ftp地址成功:" + com.b1b.js.erpandroid_kf.MyApp.ftpUrl);
                if (pd != null && pd.isShowing()) {
                    pd.cancel();
                }
                Intent intentScan = new Intent(MainActivity.this, com.b1b.js.erpandroid_kf.MenuActivity.class);
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
                    downPd.cancel();
                    MyToast.showToast(MainActivity.this, "下载完成");
                }
                break;
            case 10:
                MyToast.showToast(MainActivity.this, "部门号或公司号为空");
                break;
            case 11:
                downPd.cancel();
                MyToast.showToast(MainActivity.this, "下载失败");
                break;
        }
    }
    /**
     @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edUserName = (EditText) findViewById(R.id.login_username);
        ImageView ivDebug = (ImageView) findViewById(R.id.main_debug);
        ivDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (time == 5) {
                    MyToast.showToast(MainActivity.this, "进入debug模式");
                    String pwd = sp.getString("debugPwd", "");
                    if (pwd.equals(tempPassword)) {
                        sp.edit().putString("debugPwd", "621053000").commit();
                    } else {
                        sp.edit().putString("debugPwd", tempPassword).commit();
                    }
                    return;
                }
                time++;
            }
        });
        edPwd = (EditText) findViewById(R.id.login_pwd);
        btnLogin = (Button) findViewById(R.id.login_btnlogin);
        btnScancode = (Button) findViewById(R.id.login_scancode);
        cboRemp = (CheckBox) findViewById(R.id.login_rpwd);
        cboAutol = (CheckBox) findViewById(R.id.login_autol);
        tvVersion = (TextView) findViewById(R.id.main_version);
        final Button btnPrintCode = (Button) findViewById(R.id.activity_main_btn_code);
        btnPrintCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.b1b.js.erpandroid_kf.RukuTagPrintAcitivity.class);
                intent.putExtra(com.b1b.js.erpandroid_kf.RukuTagPrintAcitivity.extraMode, com.b1b.js.erpandroid_kf
                        .RukuTagPrintAcitivity.MODE_OFFLINE);
                startActivity(intent);
            }
        });
        sp = getSharedPreferences(com.b1b.js.erpandroid_kf.SettingActivity.PREF_USERINFO, 0);
        final String phoneCode = UploadUtils.getPhoneCode(MainActivity.this);
        Log.e("zjy", "MainActivity.java->onCreate(): phoneInfo==" + phoneCode);
        MyFileUtils.obtainFileDir(MainActivity.this);
        //检查更新
        PackageManager pm = getPackageManager();
        PackageInfo info = null;
        int code = 0;
        try {
            info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            code = info.versionCode;
            versionName = info.versionName;
            tvVersion.setText("当前版本为：" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final SharedPreferences logSp = getSharedPreferences("uploadlog", MODE_PRIVATE);
        String saveDate = logSp.getString("date", "");
        final String current = UploadUtils.getCurrentDate();
        if (com.b1b.js.erpandroid_kf.MyApp.myLogger != null) {
            if (!saveDate.equals(current)) {
                com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo("phonecode:" + phoneCode);
                com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo("dyj-version:" + code);
                com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo("API_CODE:" + Build.VERSION.SDK_INT);
            }
        }

        checkUpdate(code);
        //        readCache();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String debugPwd = sp.getString("debugPwd", "");
                Log.e("zjy", "MainActivity->onClick(): password==" + tempPassword);
                if (phoneCode.endsWith("868930027847564") || phoneCode.endsWith("358403032322590") || phoneCode.endsWith
                        ("864394010742122") || phoneCode.endsWith("A0000043F41515") || phoneCode.endsWith("86511114021521")
                        || phoneCode.endsWith("866462026203849") || phoneCode.endsWith("869552022575930")) {
                    login("101", debugPwd);
                    //                    disbleScanService(MainActivity.this);
                } else {
                    MyToast.showToast(MainActivity.this, "请使用扫码登录");
                }
            }
        });
        btnScancode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
    }


    @Override
    public void resultBack(String result) {
        readCode(result);
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


    private void checkSaveFTP(final String url) {
        final String[] urls = url.split("\\|");
        new Thread() {
            @Override
            public void run() {
                int counts = urls.length;
                int times = 0;
                for (String url1 : urls) {
                    try {
                        Socket socket = new Socket();
                        SocketAddress remoteAddr = new InetSocketAddress(url1, 21);
                        socket.connect(remoteAddr, 10 * 1000);
                        com.b1b.js.erpandroid_kf.MyApp.ftpUrl = url1;
                        sp.edit().putString("ftp", com.b1b.js.erpandroid_kf.MyApp.ftpUrl).apply();
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

    private void checkUpdate(final int nowCode) {
        if (nowCode == 0) {
            com.b1b.js.erpandroid_kf.MyApp.myLogger.writeError("apk versioncode==0");
            return;
        }
        downPd = new ProgressDialog(mContext);
        downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downPd.setTitle("更新");
        downPd.setMax(100);
        downPd.setMessage("下载中");
        downPd.setProgress(0);
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                boolean ifUpdate = false;
                String saveName = "dyjkfapp.apk";
                String downUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/dyjkfapp.apk";
                String specialUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/debug-update.txt";
                String checkUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/updateXml.txt";
                HashMap<String, String> updateInfo = null;
                File targetDir = MyFileUtils.getFileParent();
                final File apkFile = new File(targetDir, saveName);
                String code = "code";
                String content = "content";
                String date = "date";
                HttpUtils.Builder builder = HttpUtils.create(checkUrl);
                InputStream is = null;
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    is = builder.getInputStream();
                    updateInfo = new HashMap<>();
                    DocumentBuilder docBuilder = factory.newDocumentBuilder();
                    Document xmlDoc = docBuilder.parse(is);
                    NodeList newVersion = xmlDoc.getElementsByTagName("latest-version");
                    Node item = newVersion.item(0);
                    NodeList childNodes = item.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node n = childNodes.item(i);
                        String nName = n.getNodeName();
                        if (nName.equals(code)) {
                            updateInfo.put(code, n.getTextContent());
                        } else if (nName.equals(content)) {
                            updateInfo.put(content, n.getTextContent());
                        } else if (nName.equals(date)) {
                            updateInfo.put(date, n.getTextContent());
                        }
                    }
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (updateInfo != null) {
                    String sCode = updateInfo.get(code);
                    final String sContent = updateInfo.get(content);
                    final String sDate = updateInfo.get(date);
                    zHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String info = tvVersion.getText().toString().trim();
                            info = info + "，更新说明:\n";
                            info += "更新时间:" + sDate + "\n";
                            info += "更新内容:" + sContent;
                            tvVersion.setText(info);
                        }
                    });
                    int savedCode = sp.getInt("lastCode", -1);
                    int sIntCode = Integer.parseInt(sCode);
                    if (sIntCode > nowCode) {
                        sp.edit().putInt("lastCode", sIntCode).apply();
                        if (apkFile.exists()) {
                            if (sIntCode > savedCode) {
                                apkFile.delete();
                            } else {
                                zHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.getSpAlert(mContext, "当前有未安装的更新，是否安装", "提示", new
                                                DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        installAPK(apkFile);
                                                    }
                                                }, "是", null, "否").show();
                                    }
                                });
                                return;
                            }
                        }
                        ifUpdate = true;
                    }
                }
                if (ifUpdate) {
                    zHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downPd.show();
                        }
                    });
                    try {
                        updateAPK(mContext, zHandler, downUrl, saveName);
                    } catch (IOException e) {
                        zHandler.sendEmptyMessage(11);
                        e.printStackTrace();
                    }
                } else {
                    HashMap<String, String> map = null;
                    try {
                        map = specialUpdate(specialUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (map == null) {
                        return;
                    }
                    SharedPreferences speUpdate = getSharedPreferences("speUpdate", Context.MODE_PRIVATE);
                    String localCheckID = speUpdate.getString("checkid", "");
                    String deviceCode = UploadUtils.getDeviceID(mContext);
                    String onlineCode = map.get("deviceID");
                    String apkUrl = map.get("url");
                    String onlineCheckID = map.get("checkid");
                    if (apkUrl != null) {
                        if (localCheckID.equals("")) {
                            speUpdate.edit().putString("checkid", onlineCheckID).commit();
                            return;
                        }
                        if (!localCheckID.equals(onlineCheckID)) {
                            if ("all".equals(onlineCheckID)) {
                                zHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //必须设定进图条样式
                                        downPd.show();
                                    }
                                });
                                try {
                                    updateAPK(mContext, zHandler, downUrl, saveName);
                                    speUpdate.edit().putString("checkid", onlineCheckID).commit();
                                } catch (IOException e) {
                                    zHandler.sendEmptyMessage(11);
                                    e.printStackTrace();
                                }
                            } else if (onlineCheckID != null && onlineCode.equals(deviceCode)) {
                                zHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downPd.show();
                                    }
                                });
                                try {
                                    updateAPK(mContext, zHandler, downUrl, saveName);
                                    speUpdate.edit().putString("checkid", onlineCheckID).commit();
                                } catch (IOException e) {
                                    zHandler.sendEmptyMessage(11);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        };
        taskManger.execute(updateRun);
    }

    private void getUserInfoDetail(final String uid) {
        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                while (!success) {
                    try {
                        Map<String, Object> result = getUserInfo(uid);
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
//        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//        map.put("checker", "1");
//        map.put("uid", uid);
//        String soapResult = WebserviceUtils.getWcfResult(map, "GetUserInfoByUID", WebserviceUtils.Login);
        String soapResult = Login.GetUserInfoByUID("1", uid);
        Log.e("zjy", "MainActivity.java->run(): info==" + MyApp.id + "\t" + soapResult);
        JSONObject object = new JSONObject(soapResult);
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

    /**
     读取条码信息
     @param code onActivtyResult()回调的data
     */
    private void readCode(final String code) {
        scanDialog = new ProgressDialog(MainActivity.this);
        scanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        scanDialog.setMessage("登录中");
        scanDialog.setCancelable(false);
        scanDialog.show();
        Log.e("zjy", "MainActivity->resultBack(): codeResult==" + code);
        Runnable codeLogin = new Runnable() {
            @Override
            public void run() {
                if (code != null) {
                    try {
//                        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//                        map.put("checkword", "");
//                        map.put("code", code);
//                        String soapResult = WebserviceUtils.getWcfResult(map, "BarCodeLogin", WebserviceUtils.MartService);
                        String soapResult = MartService.BarCodeLogin("", code);
                        Message msg = zHandler.obtainMessage(SCANCODE_LOGIN_SUCCESS);
                        msg.obj = soapResult;
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
        };
        taskManger.execute(codeLogin);
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
        Runnable normalLoginRun = new Runnable() {
            @Override
            public void run() {
                String version = "";
                try {
                    String deviceID = WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo;
                    version = versionName;
//                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//                    map.put("checkWord", "sdr454fgtre6e655t5rt4");
//                    map.put("userID", name);
//                    map.put("passWord", pwd);
//                    map.put("DeviceID", WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo);
//                    map.put("version", version);
//                    String soapResult = WebserviceUtils.getWcfResult(map, "AndroidLogin", WebserviceUtils.MartService);
                    String soapResult = MartService.AndroidLogin("sdr454fgtre6e655t5rt4",name,pwd,deviceID,version);
                    String[] resArray = soapResult.split("-");
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
                        msg.obj =soapResult;
                        zHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    zHandler.sendEmptyMessage(NEWWORK_ERROR);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    zHandler.sendEmptyMessage(NEWWORK_ERROR);
                    e.printStackTrace();
                }
            }
        };
        taskManger.execute(normalLoginRun);
    }


    /**
     @return
     @throws SocketTimeoutException
     @throws IOException             */
    public HashMap<String, String> specialUpdate(String url) throws IOException {
        boolean ifUpdate = false;
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String len = reader.readLine();
            StringBuilder stringBuilder = new StringBuilder();
            HashMap<String, String> map = new HashMap<>();
            while (len != null) {
                String[] parm = len.split("=");
                map.put(parm[0], parm[1]);
                stringBuilder.append(len);
                len = reader.readLine();
            }
            Log.e("zjy", "MainActivity->specialUpdate(): result==" + stringBuilder.toString());
            return map;
        }
        return null;
    }


    public void updateAPK(Context context, Handler mHandler, String downUrl, String saveName) throws IOException {

        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setReadTimeout(60000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            File targetDir = MyFileUtils.getFileParent();
            File file1 = new File(targetDir, saveName);
            FileOutputStream fos = new FileOutputStream(file1);
            int len = 0;
            int hasRead = 0;
            int percent = 0;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                hasRead = hasRead + len;
                percent = (hasRead * 100) / size;
                final int tempPercent = percent;
                if (hasRead < 0) {
                    Log.e("zjy", "MainActivity.java->updateAPK(): hasRead==" + hasRead);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int percent = tempPercent;
                        if (percent < 0) {
                            return;
                        }
                        downPd.setProgress(percent);
                        if (percent == 100) {
                            downPd.cancel();
                            MyToast.showToast(MainActivity.this, "下载完成");
                        }
                    }
                });
                fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
            com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo("update download");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(targetDir, saveName);
            if (file.exists()) {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                throw new FileNotFoundException();
            }
        }
    }

    public void installAPK(File apkFile) {
        if (apkFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void getCameraScanResult(String result) {
        readCode(result);
    }
}
