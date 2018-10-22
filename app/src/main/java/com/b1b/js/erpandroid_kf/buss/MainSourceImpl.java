package com.b1b.js.erpandroid_kf.buss;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MainActivity;
import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.SettingActivity;
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

import utils.UpdateClient;
import utils.WebserviceUtils;
import utils.wsdelegate.Login;
import utils.wsdelegate.MartService;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public class MainSourceImpl implements IMainDataSource {
    private SharedPreferences spUserinfo;
    private DateChecker checker = new DateChecker();
    private int debugTime = 0;
    private String tempPassword = "62105300";

    private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

    public String getLogin(final String uname, final String pwd, final String version, final DataCallback
            mCall) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                String deviceID = WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo;
                String soapResult = null;
                try {
                    soapResult = MartService.AndroidLogin("sdr454fgtre6e655t5rt4", uname, pwd, deviceID,
                            version);
                } catch (IOException e) {
                    e.printStackTrace();
                    soapResult = e.getMessage();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    soapResult = e.getMessage();
                }
                final String finalSoapResult = soapResult;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCall.result(finalSoapResult);
                    }
                });
            }
        };
        TaskManager.getInstance().execute(run);
        return null;
    }

    @Override
    public void getScanResult(final String code, final DataCallback mCall) {

        Runnable codeLogin = new Runnable() {
            @Override
            public void run() {
                try {
                    final String soapResult = MartService.BarCodeLogin("", code);
                    checkFtpPwd(soapResult, mCall);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(codeLogin);
    }

    public void checkFtpPwd(String result, final DataCallback mCall) {
        try {
            JSONObject object1 = new JSONObject(result);
            JSONArray main = object1.getJSONArray("表");
            JSONObject obj = main.getJSONObject(0);
            String url = obj.getString("PhotoFtpIP");
            com.b1b.js.erpandroid_kf.MyApp.myLogger.writeInfo(MainActivity.class, "FTP:" + url);
            String uid = obj.getString("UserID");
            com.b1b.js.erpandroid_kf.MyApp.id = uid;
            String defUid = spUserinfo.getString("name", "");
            //换用户则清除缓冲
            final String[] urls = url.split("\\|");
            String localUrl = spUserinfo.getString("ftp", "");
            if (!defUid.equals(uid)) {
                //第一次登录或者更换手机
                spUserinfo.edit().clear().commit();
                getUserInfoDetail(uid);
                SharedPreferences.Editor editor = spUserinfo.edit();
                editor.putString("name", uid).apply();
                //"|"为特殊字符，需要用"\\"转义
                if (checkSaveFTP(url)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCall.result("1");
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCall.result("0,连接FTP失败");
                        }
                    });
                }
            } else {
                if (ftpCheck(url, urls, localUrl)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCall.result("1");
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCall.result("0,连接FTP失败2");
                        }
                    });
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCall.result("0，扫码结果json解析异常，" + e.getMessage());
                }
            });
        }
    }

    private boolean ftpCheck(String url, String[] urls,
                             String localUrl) {

        if (localUrl.equals("")) {
            return checkSaveFTP(url);
        } else {
            for (int i = 0; i < urls.length; i++) {
                if (urls[i].equals(localUrl)) {
                    MyApp.ftpUrl = localUrl;
                    return true;
                } else {
                    if (i == urls.length - 1) {
                        return checkSaveFTP(url);
                    }
                }
            }
        }
        return false;
    }

    private void getUserInfoDetail(final String uid) {
        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                while (!success) {
                    try {
                        Map<String, Object> result = getUserInfo(uid);
                        spUserinfo.edit().putInt("cid", (int) result.get("cid")).putInt("did", (int) result
                                .get("did")).
                                putString("oprName", (String) result.get("oprName")).apply();
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NumberFormatException e) {
                        // TODO: 2018/10/22 格式化出错
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
        Log.e("zjy", "MainActivity.java->run(): info==" + uid + "\t" + soapResult);
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

    private boolean checkSaveFTP(final String url) {
        final String[] urls = url.split("\\|");
        for (String url1 : urls) {
            try {
                Socket socket = new Socket();
                SocketAddress remoteAddr = new InetSocketAddress(url1, 21);
                socket.connect(remoteAddr, 10 * 1000);
                MyApp.ftpUrl = url1;
                spUserinfo.edit().putString("ftp", MyApp.ftpUrl).apply();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public interface DataCallback {
        void result(String result);
    }

    public interface UpdateInfoCallback {
        void getUpdateInfo(HashMap<String, String> map);
    }

    public String getDebugPwd() {
        return spUserinfo.getString("debugPwd", "");
    }

    public boolean setDebugPwd() {
        if (debugTime == 5) {
            String pwd = spUserinfo.getString("debugPwd", "");
            if (pwd.equals(tempPassword)) {
                spUserinfo.edit().putString("debugPwd", "621053000").commit();
            } else {
                spUserinfo.edit().putString("debugPwd", tempPassword).commit();
            }
            return true;
        }
        debugTime++;
        return false;
    }

    public void savePwd(boolean isSaved, String name, String pwd, boolean autoLogin) {
        if (isSaved) {
            SharedPreferences.Editor editor = spUserinfo.edit();
            editor.putString("name", name);
            editor.putString("pwd", pwd);
            editor.putBoolean("remp", isSaved);
            editor.putBoolean("autol", autoLogin);
            editor.apply();
        } else {
            spUserinfo.edit().clear().commit();
        }
    }

    @Override
    public void startUpdateCheck(final UpdateInfoCallback updateCallback) {

        UpdateClient client = new UpdateClient(mContext) {
            @Override
            public void getUpdateInfo(HashMap<String, String> map) {
                super.getUpdateInfo(map);
                updateCallback.getUpdateInfo(map);
            }
        };
        client.startUpdate();
    }

    private Context mContext;

    public MainSourceImpl(Context mContext) {
        this.mContext = mContext;
        spUserinfo = mContext.getSharedPreferences(SettingActivity.PREF_USERINFO, Context.MODE_PRIVATE);
        TaskManager taskManger = TaskManager.getInstance(5, 9);
    }

    static class DateChecker {
        public boolean loginOk(String result) {
            return false;
        }

        public boolean readCodeOk(String result) {
            return false;
        }
    }
}
