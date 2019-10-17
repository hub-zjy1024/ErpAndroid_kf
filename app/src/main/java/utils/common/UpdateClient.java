package utils.common;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import utils.framwork.DialogUtils;
import utils.framwork.MyToast;
import utils.net.wsdelegate.WebserviceUtils;

/**
 * Created by 张建宇 on 2018/7/4.
 */
public class UpdateClient {
    private Context mContext;
    private Handler handler = new Handler();
    ProgressDialog downPd = null;
    //修改downPath、downUrl和saveName
    //配置目录
    private static String downPath = "DownLoad/dyj_kf/";
    //特定更新配置文件
    private String specialUrl =
            WebserviceUtils.ROOT_URL + downPath + "debug-update.txt";
    //更新信息xml文件
    private String checkUpdateURL = WebserviceUtils.ROOT_URL + downPath + "updateXml.txt";
    //版本控制配置文件
    private String checkAvailableVersion = WebserviceUtils.ROOT_URL + downPath + "versionControl.txt";
    //apk文件名
    public static final String downUrl = WebserviceUtils.ROOT_URL + downPath + "dyjkfapp.apk";

    private String saveName = "dyjkfapp.apk";
    File updateFile = new File(Environment.getExternalStorageDirectory(), saveName);

    @MainThread
    public UpdateClient(Context mContext) {
        this.mContext = mContext;
        downPd = new ProgressDialog(mContext);
    }

    private int getLocalVersion() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean startUpdate() {
        downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downPd.setTitle("更新");
        downPd.setMax(100);
        downPd.setMessage("下载中");
        downPd.setProgress(0);
        SharedPreferences spUpdate = mContext.getSharedPreferences("updatechecker", Context.MODE_PRIVATE);
        boolean needDownload = false;
        int lastCode = spUpdate.getInt("lastcode", -1);

        String errMsg = "";
        try {
            HashMap<String, String> updateInfo = getUpdateXml(checkUpdateURL);
            if (updateInfo != null) {
                getUpdateInfo(updateInfo);
                String sCode = updateInfo.get("code");
                int nowCode = getLocalVersion();
                int sIntCode = Integer.parseInt(sCode);
                if (sIntCode > nowCode) {
                    if (updateFile.exists()) {
                        if (sIntCode > lastCode) {
                            updateFile.delete();
                            needDownload = true;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtils.getSpAlert(mContext, "当前存在更新文件，是否安装", "提示", new
                                            DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    setInstallPermission();
                                                }
                                            }, "是", null, "否").show();
                                }
                            });
                        }
                    } else {
                        needDownload = true;
                    }
                    spUpdate.edit().putInt("lastcode", sIntCode).apply();
                }
                if (needDownload) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downPd.show();
                        }
                    });
                    downloadApk();
                } else {
                    HashMap<String, String> map = specialUpdate(specialUrl);
                    if (map == null) {
                        return false;
                    }
                    SharedPreferences speUpdate = mContext.getSharedPreferences("speUpdate", Context
                            .MODE_PRIVATE);
                    String localCheckID = speUpdate.getString("checkid", "");
                    String deviceCode = UploadUtils.getDeviceID(mContext);
                    String onlineCode = map.get("deviceID");
                    String apkUrl = map.get("url");
                    String onlineCheckID = map.get("checkid");
                    if (apkUrl != null) {
                        if (localCheckID.equals("")) {
                            speUpdate.edit().putString("checkid", onlineCheckID).commit();
                            return false;
                        }
                        if (!localCheckID.equals(onlineCheckID)) {
                            boolean startDown = false;
                            if ("all".equals(onlineCheckID)) {
                                startDown = true;
                            } else if (onlineCheckID != null && onlineCode.equals(deviceCode)) {
                                startDown = true;
                            }
                            if (startDown) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downPd.show();
                                    }
                                });
                                downloadApk(apkUrl);
                                speUpdate.edit().putString("checkid", onlineCheckID).commit();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            String exMsg = e.getMessage();
            if (e instanceof SocketException) {
                errMsg = "检查更新失败，连接服务器失败" ;
            } else {
                errMsg = "检查更新失败，其他错误：" + exMsg;
            }
            e.printStackTrace();
        } catch (SAXException e) {
            String exMsg = e.getMessage();
            errMsg = "检查更新失败，文件格式异常," + exMsg;
            e.printStackTrace();
        }
        if (!"".equals(errMsg)) {
            MyApp.myLogger.writeError("自动更新出现问题，" + errMsg);
            final String finalErrMsg = errMsg;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    downPd.cancel();
                    MyToast.showToast(mContext, finalErrMsg);
                }
            });
        }
        return needDownload;
    }

    public void getUpdateInfo(HashMap<String, String> map) {

    }

    /**
     * @return
     * @throws IOException
     */
    private HashMap<String, String> specialUpdate(String url) throws IOException {
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
                if (parm.length == 2) {
                    map.put(parm[0], parm[1]);
                    stringBuilder.append(len);
                    stringBuilder.append("\n");
                }
                len = reader.readLine();
            }
            return map;
        }
        return null;
    }

    private void downloadApk() throws
            IOException {
        downloadApk(downUrl);
    }

    private void downloadApk(String downUrl) throws
            IOException {
        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            FileOutputStream fos = new FileOutputStream(updateFile);
            int len = 0;
            int hasRead = 0;
            int percent = 0;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                hasRead = hasRead + len;
                percent = (hasRead * 100) / size;
                final int tempPercent = percent;
                if (hasRead < 0) {
                    Log.e("zjy", "UpdateClient.java->updateAPK(): hasRead==" + hasRead);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int percent = tempPercent;
                        if (percent < 0) {
                            return;
                        }
                        downPd.setProgress(percent);
                        if (percent == 100) {
                            downPd.cancel();
                            MyToast.showToast(mContext, "下载完成");
                        }
                    }
                });
                fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
            MyApp.myLogger.writeInfo("update download");
            setInstallPermission();
        } else {
            InputStream errorStream = conn.getErrorStream();
            int len = 0;
            byte[] buf = new byte[1024];
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "gb2312"));
            String s = "";
            String tempStr = "";
            while ((tempStr = reader.readLine()) != null) {
                s += tempStr + "\n";
            }
            String startTag = "<body>";
            int index1 = s.indexOf(startTag);
            int index2 = s.indexOf("</body>");
            String substring = s;
            if (index1 != -1) {
                substring = s.substring(index1 + startTag.length(), index2);
            }
            substring = "返回码:" + conn.getResponseCode() + "\n" + substring;
            throw new IOException(substring);
        }
    }

    /**
     * 8.0以上系统设置安装未知来源权限
     */
    public void installApp28(File apkFile) {
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = mContext.getPackageManager().canRequestPackageInstalls();
            Log.e("zjy", getClass() + "->setInstallPermission(): ==" + haveInstallPermission );
            if (!haveInstallPermission) {
                requestPermission();
            } else {
                installApk(apkFile);
            }
        } else {
            Log.e("zjy", getClass() + "->setInstallPermission(): no need==");
            installApk(apkFile);
        }
    }
    /**
     * 8.0以上系统设置安装未知来源权限
     */
    public void setInstallPermission(){
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = mContext.getPackageManager().canRequestPackageInstalls();
            Log.e("zjy", getClass() + "->setInstallPermission(): ==" + haveInstallPermission );
            if (!haveInstallPermission) {
                requestPermission();
            } else {
                installApk(updateFile);
            }
        } else {
            Log.e("zjy", getClass() + "->setInstallPermission(): no need==");
            installApk(updateFile);
        }
    }

    private void requestPermission() {
                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                                INSTALL_PERMISS_CODE);
    }

    public static int INSTALL_PERMISS_CODE = 211;

    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void realToSetting() {
        Log.e("zjy", getClass() + "->realToSetting(): ==open unknow source");
        Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        ( (Activity)(mContext)).startActivityForResult(intent, INSTALL_PERMISS_CODE);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void toInstallPermissionSettingIntent() {
        DialogUtils.getDialog(mContext).setMsg("需要打开允许安装来自此来源的程序，是否前往设置开启").setBtn1("前往").setBtn1L(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                realToSetting();
            }
        }).create();
    }

    public void installApk() {
        installApk(updateFile);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void installApk(File file) {
        if (file.exists()) {
            Log.e("zjy", getClass() + "->installApk(): ==start install");
//            Intent intent = new Intent(Intent.ACTION_VIEW);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            Uri apkUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //判断版本是否在7.0以上
                apkUri =
                        FileProvider.getUriForFile(mContext,
                                mContext.getPackageName() + ".fileprovider",
                                file);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
//                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.e("zjy", getClass() + "->installApk():7.0 ==" + apkUri);
            } else {
                apkUri = Uri.fromFile(file);
            }
            intent.setDataAndType(apkUri, "application/vnd.android" +
                    ".package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                List<ResolveInfo> resolveLists = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                // 然后全部授权
//                for (ResolveInfo resolveInfo : resolveLists){
//                    String packageName = resolveInfo.activityInfo.packageName;
//                    mContext.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//            }
            mContext.startActivity(intent);
        }
    }

    private HashMap<String, String> getUpdateXml(String url) throws IOException, SAXException {
        HashMap<String, String> result = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(url);
            Document xmlDoc = docBuilder.parse(inputSource);
            NodeList newVersion = xmlDoc.getElementsByTagName("latest-version");
            Node item = newVersion.item(0);
            NodeList childNodes = item.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node n = childNodes.item(i);
                String nName = n.getNodeName();
                if (nName.equals("code")) {
                    result.put("code", n.getTextContent());
                } else if (nName.equals("content")) {
                    result.put("content", n.getTextContent());
                } else if (nName.equals("date")) {
                    result.put("date", n.getTextContent());
                }
            }
            return result;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
            throw new SAXException("解析失败," + e.getMessage());
        }
        return null;
    }

    public boolean checkVersionAvailable() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), PackageManager
                    .GET_ACTIVITIES);
            String vName = info.versionName;
            HttpURLConnection conn = (HttpURLConnection) new URL(checkAvailableVersion).openConnection();
            conn.setConnectTimeout(30 * 1000);
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String s = null;
            StringBuilder sb = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }
            String versions = sb.toString();
            Log.e("zjy", "UpdateClient->checkVersionAvailable(): OnlineVersoins==" + versions);
            JSONArray jsonArray = new JSONArray(versions);
            for (int i = 0; i < jsonArray.length(); i++) {
                String tempV = jsonArray.getString(i);
                if (tempV.equals(vName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
