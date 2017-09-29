package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

public class AboutActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private ProgressDialog downPd;
    private TextView tvNewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tvNewVersion = (TextView) findViewById(R.id.activity_about_tv_newversion);
        TextView tvVersion = (TextView) findViewById(R.id.activity_about_tv_version);
        Button btnDonloadNew = (Button) findViewById(R.id.activity_about_btn_downloadnew);
        Button btnCheck = (Button) findViewById(R.id.activity_about_btn_check);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downPd.show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getNewVersion();
                    }
                }.start();
            }
        });
        downPd = new ProgressDialog(this);
        downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downPd.setTitle("更新");
        downPd.setMax(100);
        downPd.setMessage("下载中");
        downPd.setProgress(0);
        btnDonloadNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String downUrl = "http://172.16.6.160:8006/DownLoad/dyj_kf/dyjkfapp.apk";
                //必须设定进图条样式
                downPd.show();
                try {
                    updateAPK(AboutActivity.this, mHandler, downUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        PackageManager pm = getPackageManager();

        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            if (info != null) {
                tvVersion.setText(info.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void updateAPK(final Context context, Handler mHandler, String downUrl) throws IOException {
        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setReadTimeout(60000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            File targetDir = MyFileUtils.getFileParent();
            File file1 = new File(targetDir, "dyjkfapp.apk");
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
                            MyToast.showToast(context, "下载完成");
                        }
                    }
                });
                //                Message msg = mHandler.obtainMessage(8);
                //                msg.arg1 = percent;
                //                mHandler.sendMessage(msg);
                //写入时第三个参数使用len
                fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
            MyApp.myLogger.writeInfo("update download");
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

    public void getNewVersion() {
        ;
        HashMap<String, String> updateInfo = null;
        try {
            updateInfo = getUpdateXml("http://172.16.6.160:8006/DownLoad/dyj_kf/updateXml.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (updateInfo != null) {
            final String sCode = updateInfo.get("code");
            final String sContent = updateInfo.get("content");
            final String sDate = updateInfo.get("date");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvNewVersion.setText(sCode);
                }
            });
        }
    }
    public static HashMap<String, String>  getUpdateXml(String url) throws IOException {
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30*1000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String len = reader.readLine();
            StringBuilder stringBuilder = new StringBuilder();
            while (len != null) {
                stringBuilder.append(len);
                len = reader.readLine();
            }
            String res = stringBuilder.toString();
            HashMap<String, String> result = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                ByteArrayInputStream bin = new ByteArrayInputStream(res.getBytes("utf-8"));
                Document xmlDoc = docBuilder.parse(bin);
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
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }
}
