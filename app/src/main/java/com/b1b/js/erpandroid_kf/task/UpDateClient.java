package com.b1b.js.erpandroid_kf.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import utils.MyFileUtils;
import utils.MyToast;
import utils.UploadUtils;
import utils.WebserviceUtils;

/**
 Created by 张建宇 on 2018/4/20. */
public class UpDateClient {
    private Context mContext;
    private Handler zHandler;

    public UpDateClient(Context mContext, Handler zHandler) {
        this.mContext = mContext;
        this.zHandler = zHandler;
    }

    private UpdateListner listner;

    public void setListner(UpdateListner listner) {
        this.listner = listner;
    }

    interface UpdateListner {
        void getPersent(int p);

        void getUpdateInfo(String info);

    }

    private String saveName = "dyjkfapp.apk";
    private File saveDir = Environment.getExternalStorageDirectory();
    private String downUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/dyjkfapp.apk";
    private String sDownUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/app-release.apk";
    private String specialUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/debug-update.txt";
    private String checkUrl = WebserviceUtils.ROOT_URL + "DownLoad/dyj_kf/updateXml.txt";
    private ProgressDialog downPd = new ProgressDialog(mContext);

    public void checkUpDate(final int nowCode) {
        downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downPd.setTitle("更新");
        downPd.setMax(100);
        downPd.setMessage("下载中");
        downPd.setProgress(0);
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                boolean ifUpdate = false;
                //                    boolean ifUpdate = checkVersion(nowCode);
                HashMap<String, String> updateInfo = null;
                String code = "code";
                String content = "content";
                String date = "date";
                //                HttpUtils.Builder builder = HttpUtils.create(checkUrl);
                //                    is = builder.getInputStream();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    updateInfo = new HashMap<>();
                    DocumentBuilder docBuilder = factory.newDocumentBuilder();
                    Document xmlDoc = docBuilder.parse(checkUrl);
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
                    String info = "，更新说明:\n";
                    info += "更新时间:" + sDate + "\n";
                    info += "更新内容:" + sContent;
                    final String tInfo = info;
                    int sIntCode = Integer.parseInt(sCode);
                    if (sIntCode > nowCode) {
                        ifUpdate = true;
                    }
                    final boolean needUpdate = ifUpdate;
                    zHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listner == null) {
                                return;
                            }
                            if (needUpdate) {
                                downPd.show();
                            }
                            listner.getUpdateInfo(tInfo);
                        }
                    });
                }
                try {
                    if (ifUpdate) {
                        zHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                downPd.show();
                            }
                        });
                        downLoadApk(false);
                        InstallApk();
                    } else {
                        HashMap<String, String> map = null;
                        try {
                            map = getDebugUpdateInfo(specialUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (map == null) {
                            return;
                        }
                        SharedPreferences speUpdate = mContext.getSharedPreferences("speUpdate", Context.MODE_PRIVATE);
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
                                            downPd.show();
                                        }
                                    });
                                    downLoadApk(true);
                                    InstallApk();
                                    speUpdate.edit().putString("checkid", onlineCheckID).commit();
                                } else if (onlineCheckID != null && onlineCode.equals(deviceCode)) {
                                    zHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            downPd.show();
                                        }
                                    });
                                    try {
                                        //                                    updateAPK(mContext, zHandler, downUrl, saveName);
                                        downLoadApk(true);
                                        InstallApk();
                                        speUpdate.edit().putString("checkid", onlineCheckID).commit();
                                    } catch (IOException e) {
                                        zHandler.sendEmptyMessage(11);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyApp.myLogger.writeError(e, "install update Error");
                }
            }
        };
        TaskManager.getInstance().execute(updateRun);
    }

    public void downLoadApk(boolean isSpecial) throws IOException {
        URL url = new URL(downUrl);
        if (isSpecial) {
            url = new URL(sDownUrl);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10 * 1000);
        conn.setReadTimeout(60 * 1000);
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
                    MyApp.myLogger.writeInfo("update download read:" + hasRead);
                }
                zHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tempPercent < 0) {
                            return;
                        }
                        if (listner == null) {
                            return;
                        }
                        listner.getPersent(tempPercent);
                        downPd.setProgress(tempPercent);
                        if (tempPercent == 100) {
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
        }
    }

    public void InstallApk() throws FileNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(saveDir, saveName);
        if (file.exists()) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     @return
     @throws IOException
     */
    public HashMap<String, String> getDebugUpdateInfo(String url) throws IOException {
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(10 * 1000);
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
            Log.e("zjy", "MainActivity->getDebugUpdateInfo(): result==" + stringBuilder.toString());
            return map;
        }
        return null;
    }
}
