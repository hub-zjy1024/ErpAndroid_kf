package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.entity.SpSettings;
import com.b1b.js.erpandroid_kf.entity.UserInfoPref;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import utils.common.MyFileUtils;
import utils.common.UpdateClient;
import utils.common.UploadUtils;
import utils.handler.NoLeakHandler;
import utils.net.ftp.FTPUtils;
import utils.net.wsdelegate.Login;
import utils.net.wsdelegate.MartService;
import utils.net.wsdelegate.WebserviceUtils;

public class MainActivity extends BaseScanActivity implements View.OnClickListener{

    private EditText edUserName;
    private EditText edPwd;
    private CheckBox cboRemp;
    private CheckBox cboAutol;
    private SharedPreferences sp;
    private ProgressDialog pd;
    private ProgressDialog downPd;
    private ProgressDialog scanDialog;
    private TextView tvVersion;
    private final int SCANCODE_LOGIN_SUCCESS = 4;
    private final int MSG_SCAN_OK = 7;
    private final int NEWWORK_ERROR = 2;
    private final int FTPCONNECTION_ERROR = 5;
    private final int MSG_LOGIN_FAILED = 0;
    private  final int MSG_LOGIN_SUCCESS = 1;
    private final int MSG_FTPcheck_OK = 6;
    private final int MSG_SCAN_INIT = 8;

    public final String key_debugkey = "debugPwd";

    private String versionName = "1";
    private String tempPassword;
    private int time = 0;
    TaskManager taskManger = TaskManager.getInstance(5, 9);
    private AlertDialog permissionDialog;
    private Handler zHandler = new NoLeakHandler(this);
    private String phoneCode;
    private String storID;
    UserInfoPref pref;

    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            //失败
            case MSG_LOGIN_FAILED:
                pd.cancel();
                showMsgToast( msg.obj.toString());
                break;
            case MSG_LOGIN_SUCCESS:
                //成功
                HashMap<String, String> infoMap = (HashMap<String, String>) msg.obj;
                //每次登录检查userInfo是否有变动，以免数据库更新（流量允许）
                String uid = infoMap.get("name");
                com.b1b.js.erpandroid_kf.MyApp.id = uid;
                //登陆用户名改变，清除缓存
                if (!sp.getString("name", "").equals(com.b1b.js.erpandroid_kf.MyApp.id)) {
                    sp.edit().clear().apply();
                    //登录成功之后调用，获取相关信息
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("name", infoMap.get("name"));
                    edit.putString("pwd", infoMap.get("pwd"));
                    edit.apply();
                    getUserInfoDetail(uid);
                } else {
                    //                        MyApp.ftpUrl = sp.getString("ftp", "");
                }
                //是否记住密码
                //                    ifSavePwd(true, "101", "62105300");
                pd.cancel();
                zHandler.removeMessages(MSG_LOGIN_SUCCESS);
                gotoMenu();
                break;
            case NEWWORK_ERROR:
                Object mobj = msg.obj;
                String errMsg = "网络状态不佳,请检查网络状态";
                if (mobj != null) {
                    errMsg = mobj.toString();
                }
                com.b1b.js.erpandroid_kf.MyApp.myLogger.writeError("bad network");
                showMsgToast(errMsg);
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
                    String uid2 = obj.getString("UserID");
                    com.b1b.js.erpandroid_kf.MyApp.id = uid2;
                    String defUid = sp.getString("name", "");
                    //换用户则清除缓冲
                    final String[] urls = url.split("\\|");
                    String localUrl = sp.getString("ftp", "");
                    if (localUrl.equals("")) {
                        if (!ftpCheckNeed()) {
                            return;
                        }
                        checkSaveFTP(url);
                    }else if (!defUid.equals(uid2)) {
                        //第一次登录或者更换手机
                        sp.edit().clear().commit();
                        getUserInfoDetail(com.b1b.js.erpandroid_kf.MyApp.id);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("name", uid2).apply();
                        //"|"为特殊字符，需要用"\\"转义
                        if (!ftpCheckNeed()) {
                            return;
                        }
                        checkSaveFTP(url);
                    } else {
                        if (!ftpCheckNeed()) {
                            return;
                        }
                        for (int i = 0; i < urls.length; i++) {
                            if (urls[i].equals(localUrl)) {
                                com.b1b.js.erpandroid_kf.MyApp.ftpUrl = localUrl;
                                if (scanDialog != null && scanDialog.isShowing()) {
                                    scanDialog.cancel();
                                }
                                gotoMenu();
                                break;
                            } else {
                                if (i == urls.length - 1) {
                                    checkSaveFTP(url);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showMsgToast("扫描结果有误");
                    if (scanDialog != null && scanDialog.isShowing()) {
                        scanDialog.cancel();
                    }
                } catch (IOException e) {
                    showMsgToast(e.getMessage());
                        if (scanDialog != null && scanDialog.isShowing()) {
                            scanDialog.cancel();

                        }
                } finally {
//                    if (scanDialog != null && scanDialog.isShowing()) {
//                        scanDialog.cancel();
//                    }
                }
                break;
            //获取ftp地址
            case FTPCONNECTION_ERROR:
                //连接ftp失败
                showMsgToast( "连接不到ftp服务器:" + msg.obj.toString());
                break;
            case MSG_FTPcheck_OK:
                showMsgToast( "获取ftp地址成功:" + com.b1b.js.erpandroid_kf.MyApp.ftpUrl);
                if (pd != null && pd.isShowing()) {
                    pd.cancel();
                }
                gotoMenu();
                break;
            case 10:
                showMsgToast( "部门号或公司号为空");
                break;
            case MSG_SCAN_OK:
                if (scanDialog != null) {
                    scanDialog.cancel();
                }
                gotoMenu();
                break;
        }
    }

    void gotoMenu(){
        Intent intentScan = new Intent(mContext, com.b1b.js
                .erpandroid_kf.MenuActivity
                .class);
        startActivity(intentScan);
        finish();
    }
    boolean ftpCheckNeed() throws IOException {
        if (CheckUtils.isAdmin()) {
            return false;
        }
        if("110".equals(storID)){
            return false;
        } else if ("" .equals(storID) || storID == null){
            throw new IOException("获取库房信息失败，请检查网络");
        }
        return true;
    }
    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void init() {
        edUserName = (EditText) findViewById(R.id.login_username);
        edPwd = (EditText) findViewById(R.id.login_pwd);
        cboRemp = (CheckBox) findViewById(R.id.login_rpwd);
        cboAutol = (CheckBox) findViewById(R.id.login_autol);
        tvVersion = (TextView) findViewById(R.id.main_version);
        sp = getSharedPreferences(SettingActivity.PREF_USERINFO, 0);
        pref = new UserInfoPref(sp);
//        pref.cid = sp.getInt("cid", -1);
//        pref.did = sp.getInt("did", -1);
//        pref.ftp = sp.getString("ftp", "");
//        pref.uname = sp.getString("name", "");
//        pref.debugPwd = sp.getString("pwdDebug", "");

        phoneCode = UploadUtils.getPhoneCode(mContext);
        Log.e("zjy", "MainActivity.java->onCreate(): phoneInfo==" + phoneCode);
        MyFileUtils.obtainFileDir(mContext);
        int code = 0;
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            code = info.versionCode;
            versionName = info.versionName;
            String devId = UploadUtils.getDeviceID(mContext);

            tvVersion.setText("当前版本为：" + versionName + "\t设备ID=" + devId);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final SharedPreferences spLog = getSharedPreferences(SpSettings.PREF_LOGUPLOAD, MODE_PRIVATE);
        String saveDate = spLog.getString("codeDate", "");
        int lastCode = spLog.getInt("lastCode", -1);
        final String current = UploadUtils.getCurrentDate();
        if (MyApp.myLogger != null) {
            SharedPreferences.Editor edit = spLog.edit();
            if (!saveDate.equals(current)) {
                MyApp.myLogger.writeInfo("phonecode:" + phoneCode);
                MyApp.myLogger.writeInfo("ApiVersion:" + Build.VERSION.SDK_INT);
                MyApp.myLogger.writeInfo("dyj-version:" + code);
                edit.putString("codeDate", current).apply();
            } else if (code != lastCode && lastCode != -1) {
                MyApp.myLogger.writeInfo("dyj-dated-version:" + code);
                edit.putInt("lastCode", code).apply();
            }
        }
        //检查更新
        final UpdateClient client = new UpdateClient(this) {
            @Override
            public void getUpdateInfo(HashMap<String, String> map) {
                String sCode = map.get("code");
                final String sContent = map.get("content");
                final String sDate = map.get("date");
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
            }
        };
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences spKf = getSharedPreferences(SpSettings.PREF_KF, MODE_PRIVATE);
                    String storageInfo = spKf.getString(SpSettings.storageKey, "");
                    if ("".equals(storageInfo)) {
                        storageInfo = StorageUtils.getStorageByIp();
                    }
                    storID = StorageUtils.getStorageIDFromJson(storageInfo);
                    MyApp.myLogger.writeInfo("nowStorInfo=" + storageInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                    showMsgDialog("获取库房信息失败," + e.getMessage());
                }
                client.startUpdate();
            }
        };
        TaskManager.getInstance().execute(updateRun);
        //登录
        tempPassword = sp.getString(key_debugkey, "");
        if ("".equals(tempPassword)) {
            tempPassword = "62105300";
        }
    }

    private String getDebugPwd(String mPwd) {
        String newPwd = "62105300";
        if (mPwd.equals("621053000")) {
            newPwd = "62105300";
        } else if (mPwd.equals("62105300")) {
            newPwd = "621053000";
        }
        return newPwd;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_btn_code:
                Intent intent = new Intent(mContext, com.b1b.js.erpandroid_kf
                        .RukuTagPrintAcitivity.class);
                intent.putExtra(com.b1b.js.erpandroid_kf.RukuTagPrintAcitivity.extraMode, com.b1b.js
                        .erpandroid_kf
                        .RukuTagPrintAcitivity.MODE_OFFLINE);
                startActivity(intent);
                break;
            case R.id.main_debug:
                if (time == 5) {
                    showMsgToast( "进入debug模式");
                    WebserviceUtils.ROOT_URL = WebserviceUtils.COMMON_URL;
                    String tempPwd= sp.getString(key_debugkey, "");
                    tempPassword = getDebugPwd(tempPwd);
                    return;
                }

                time++;
                break;
            case R.id.login_btnlogin:
                String tempPhone = phoneCode;
                Log.e("zjy", "MainActivity->onClick(): password==" + tempPassword);
                if (tempPhone.endsWith("868930027847564") || tempPhone.endsWith("358403032322590") ||
                        tempPhone.endsWith
                                ("864394010742122") || tempPhone.endsWith("A0000043F41515")
                        || tempPhone.endsWith("866462026203849") || tempPhone.endsWith("869552022575930")
                        || tempPhone.endsWith("460011060601459")   || tempPhone.endsWith("868591030284169") ) {
                    login("101", tempPassword);
                } else {
                    showMsgToast( "请使用扫码登录");
                }
                break;
            case R.id.login_scancode:
                if ("".equals(storID) || storID == null) {
                    showMsgDialog("正在获取库房信息，请稍后");
                    return;
                }
                startScanActivity();
                break;
        }
    }
    @Override
    public void setListeners() {
        //离线条码打印
        setOnClickListener(this, R.id.activity_main_btn_code);
        //调试模式
        setOnClickListener(this, R.id.main_debug);
        //用户名密码登录
        setOnClickListener(this, R.id.login_btnlogin);
        //扫码登录
        setOnClickListener(this, R.id.login_scancode);
    }


    @Override
    public void resultBack(String result) {
        MyApp.myLogger.writeInfo("use RedLineScan");
        readCode(result);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && permissions.length > 0 && grantResults[0] == PackageManager
                .PERMISSION_GRANTED) {
            Log.e("zjy", "MenuActivity.java->onRequestPermissionsResult(): ok==");
        } else {
            if (permissionDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("建议");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        mContext.startActivity(intent);
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
                        zHandler.sendEmptyMessage(MSG_FTPcheck_OK);
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

    void checkSaveFTP2(String url) {
        try {
            if (!ftpCheckNeed()) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String[] urls = url.split("\\|");
        int counts = urls.length;
        int times = 0;
        for (String url1 : urls) {
            try {
                Socket socket = new Socket();
                SocketAddress remoteAddr = new InetSocketAddress(url1, 21);
                socket.connect(remoteAddr, 10 * 1000);
                com.b1b.js.erpandroid_kf.MyApp.ftpUrl = url1;
                sp.edit().putString("ftp", com.b1b.js.erpandroid_kf.MyApp.ftpUrl).apply();
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

    private void getUserInfoDetail2(final String uid, SharedPreferences.Editor mSp) throws IOException,
            JSONException,
            XmlPullParserException {
        String cid = "";
        String did = "";
        try {
            String soapResult = Login.GetUserInfoByUID("1", uid);
            JSONObject object = new JSONObject(soapResult);
            JSONArray jarr = object.getJSONArray("表");
            JSONObject info = jarr.getJSONObject(0);
            cid = info.getString("CorpID");
            did = info.getString("DeptID");
            String name = info.getString("Name");
            HashMap<String, Object> result = new HashMap<>();
            result.put("cid", Integer.parseInt(cid));
            result.put("did", Integer.parseInt(did));
            result.put("oprName", name);
            mSp.putInt("cid", (int) result.get("cid")).putInt("did", (int) result.get
                    ("did")).
                    putString("oprName", (String) result.get("oprName")).apply();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new XmlPullParserException("获取用户详情接口异常");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException("获取用户详情失败");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("cid，did不合法，cid=" + cid + ",did=" + did);
        }
    }
    private void getUserInfoDetail(final String uid) {
        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                while (!success) {
                    try {
                        Map<String, Object> result = getUserInfo(uid);
                        sp.edit().putInt("cid", (int) result.get("cid")).putInt("did", (int) result.get
                                ("did")).
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

    private Map<String, Object> getUserInfo(String uid) throws IOException, XmlPullParserException,
            JSONException {
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
     * 读取条码信息
     *
     * @param code onActivtyResult()回调的data
     */
    private void readCode(final String code) {
        scanDialog = new ProgressDialog(mContext);
        scanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        scanDialog.setMessage("登录中");
        scanDialog.setCancelable(false);
        scanDialog.show();
        Runnable codeLogin = new Runnable() {
            @Override
            public void run() {
                String errMsg = "";
                try {
                    String soapResult = MartService.BarCodeLogin("", code);
                    JSONObject object1 = new JSONObject(soapResult);
                    JSONArray main = object1.getJSONArray("表");
                    JSONObject obj = main.getJSONObject(0);
                    String url = obj.getString("PhotoFtpIP");
                    String uid = obj.getString("UserID");
                    String defUid = sp.getString("name", "");
                    String localUrl = sp.getString("ftp", "");
                    com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo(MainActivity.class, "FTP:" + url);
                    com.b1b.js.erpandroid_kf.MyApp.id = uid;
                    //换用户则清除缓冲
                    final String[] urls = url.split("\\|");
                    if (!ftpCheckNeed()) {
                        MyApp.ftpUrl = FTPUtils.mainAddress;
                    }
                    SharedPreferences.Editor edit = sp.edit();
                    if (!defUid.equals(uid)) {
                        //切换用户或者新用户
                        edit.clear().commit();
                        getUserInfoDetail2(uid, edit);
                        edit.putString("name", uid).apply();
                        checkSaveFTP2(url);
                    }else{
                        //用户不变
                        boolean isChanged = true;
                        for (int i = 0; i < urls.length; i++) {
                            if (urls[i].equals(localUrl)) {
                                isChanged = false;
                                MyApp.ftpUrl = localUrl;
                                break;
                            }
                        }
                        if (isChanged) {
                            checkSaveFTP2(url);
                        }
                    }
                    Message msg = zHandler.obtainMessage(MSG_SCAN_OK);
                    msg.obj = soapResult;
                    zHandler.sendMessage(msg);
                } catch (IOException e) {
                    errMsg = "网络异常，" + e.getMessage();
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    errMsg = "接口解析异常，" + e.getMessage();
                }  catch (JSONException e) {
                    e.printStackTrace();
                    errMsg = "查询不到条码信息," + e.getMessage();
                }
                if (!"".equals(errMsg)) {
                    zHandler.obtainMessage(NEWWORK_ERROR, errMsg);
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
     * 登录
     */
    private void login(final String name, final String pwd) {
        pd = new ProgressDialog(mContext);
        pd.setMessage("登陆中");
        pd.show();
        Runnable normalLoginRun = new Runnable() {
            @Override
            public void run() {
                String errMsg = "";
                String version = "";
                try {
                    String deviceID = WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo;
                    version = versionName;
                    String soapResult = MartService.AndroidLogin("sdr454fgtre6e655t5rt4", name, pwd,
                            deviceID, version);
                    String[] resArray = soapResult.split("-");
                    if (resArray[0].equals("SUCCESS")) {
                        Message msg1 = zHandler.obtainMessage();
                        HashMap<String, String> infoMap = new HashMap<>();
                        infoMap.put("name", name);
                        infoMap.put("pwd", pwd);
                        msg1.what = MSG_LOGIN_SUCCESS;
                        msg1.obj = infoMap;
                        zHandler.sendMessage(msg1);
                        sp.edit().putString(key_debugkey, tempPassword).commit();
                    } else {
                        tempPassword = getDebugPwd(tempPassword);
                        Message msg = zHandler.obtainMessage(MSG_LOGIN_FAILED);
                        msg.obj = soapResult;
                        zHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    errMsg = "网络异常," + e.getMessage();
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    errMsg = "接口解析异常," + e.getMessage();
                    e.printStackTrace();
                }
                if (!"".equals(errMsg)) {
                    zHandler.obtainMessage(NEWWORK_ERROR, errMsg);
                }
            }
        };
        taskManger.execute(normalLoginRun);
    }

    @Override
    public void getCameraScanResult(String result) {
        readCode(result);
    }
}
