package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.picupload.TomcatTransferUploader;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import utils.common.MyFileUtils;
import utils.common.UpdateClient;
import utils.common.UploadUtils;
import utils.common.log.LogUploader;
import utils.net.wsdelegate.WebserviceUtils;

/**
 * 关于页面
 */
public class AboutActivity extends ToolbarHasSunmiActivity implements View.OnClickListener{

    private Handler mHandler = new Handler();
    private Handler logHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LogUploader.LOG_UPLOAD_FAIL:
                    showMsgToast("上传失败");
                    break;
                case LogUploader.LOG_UPLOAD_SUCCESS:
                    showMsgToast("上传成功");
                    break;

            }
        }
    };
    private ProgressDialog downPd;
    private TextView tvNewVersion;
    final String updateUrl = UpdateClient.downUrl;
    private ImageView updateIv;
    UpdateClient mClient;
    File targetDir = MyFileUtils.getFileParent();
    final File file1 = new File(targetDir, "dyjkfapp.apk");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tvNewVersion = (TextView) findViewById(R.id.activity_about_tv_newversion);
        TextView tvVersion = (TextView) findViewById(R.id.activity_about_tv_version);
        Button btnDonloadNew = (Button) findViewById(R.id.activity_about_btn_downloadnew);
         updateIv = (ImageView) findViewById(R.id.iv_about_update_qr);

        mClient = new UpdateClient(mContext);
        Button btnCheck = (Button) findViewById(R.id.activity_about_btn_check);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downPd.show();
                Runnable checkRun=new Runnable() {
                    @Override
                    public void run() {
                        getNewVersion();
                    }
                };
                TaskManager.getInstance().execute(checkRun);
            }
        });
        downPd = new ProgressDialog(this);
        downPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downPd.setTitle("更新");
        downPd.setMax(100);
        downPd.setMessage("下载中");
        downPd.setProgress(0);
        downPd.show();
        Runnable getUpdateInfo = new Runnable() {
            @Override
            public void run() {
                getNewVersion();
            }
        };
        TaskManager.getInstance().execute(getUpdateInfo);
        btnDonloadNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //必须设定进图条样式
                downPd.show();
                Runnable downloadRun = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateAPK(mContext, mHandler, updateUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                TaskManager.getInstance().execute(downloadRun);
            }
        });
        PackageManager pm = getPackageManager();

        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            if (info != null) {
                tvVersion.setText("版本:v" + info.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String deviceId = UploadUtils.getDeviceID(mContext);
        TextView tvDevice = getViewInContent(R.id.activity_about_tv_deviceId);
        tvDevice.setText(deviceId);
    }

    public String setTitle() {
        return getResString(R.string.title_about);
    }
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void setListeners() {
        setOnClickListener(this, R.id.activity_about_btn_clear_sp);
        setOnClickListener(this, R.id.iv_about_contact);
        setOnClickListener(this, R.id.activity_about_btn_upload);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIv.measure(0, 0);
        int w = updateIv.getMeasuredWidth();
        int h = updateIv.getMeasuredHeight();
        if (w == 0 || h == 0) {
            showMsgToast("获取二维码大小失败");
            return;
        }
        Runnable mkQr = new Runnable() {
            @Override
            public void run() {
                createQRcodeImage(updateUrl, updateIv);
//                testPicDownLoad();
            }
        };
        TaskManager.getInstance().execute(mkQr);
    }

    public void testPicDownLoad(){
        //                sig = "868591030278039";
        String picUrl = "ftp://172.16.6.22/ZJy/kf/and_1206447_20180103140515_5932.jpg";
        String local = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"test.jpeg";
        Log.e("zjy", getClass() + "->run(): ==" + local);
        String sig = UploadUtils.getDeviceID(mContext);
        TomcatTransferUploader mUploader = new TomcatTransferUploader(sig);
        //                FtpUploader mUploader2 = new TomcatTransferUploader(finalHost);
        try {
            mUploader.download(picUrl, local);
            final Bitmap bitmap = BitmapFactory.decodeFile(local);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateIv.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createQRcodeImage(String url, final ImageView im1) {
        int w = im1.getMeasuredWidth();
        int h = im1.getMeasuredHeight();
        try {
            //判断URL合法性
            if (url == null || "".equals(url)) {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * w + x] = 0xff000000;
                    } else {
                        pixels[y * w + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            //显示到我们的ImageView上面
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    im1.setImageBitmap(bitmap);
                }
            });

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void updateAPK(final Context context, Handler mHandler, String downUrl) throws IOException {
        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setReadTimeout(30000);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            if(!file1.getParentFile().exists()){
                file1.getParentFile().mkdirs();
            }
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
                            showMsgToast( "下载完成");
                        }
                    }
                });
                fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
            MyApp.myLogger.writeInfo("update download");
            logHandler.post(new Runnable() {
                @Override
                public void run() {
                    mClient.installApp28(file1);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == UpdateClient.INSTALL_PERMISS_CODE) {
            Log.e("zjy",
                    getClass() + "->onRequestPermissionsResult(): onInstall callback==,grantResults=" + Arrays.toString(grantResults));
            if (grantResults.length > 0 && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED) {
                mClient.installApp28(file1);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mClient.toInstallPermissionSettingIntent();
                }
            }
        }
    }

    public void getNewVersion() {
        HashMap<String, String> updateInfo = null;
        try {
            updateInfo = getUpdateXml(WebserviceUtils.ROOT_URL+"/DownLoad/dyj_kf/updateXml.txt");
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
                    tvNewVersion.setText("v"+sCode);
                    downPd.cancel();
                    showMsgToast( "已获取最新版本信息");
                }
            });
        }
    }

    public static HashMap<String, String> getUpdateXml(String url) throws IOException {
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_about_btn_clear_sp:
                SpSettings.clearAllSp(mContext);
//                showMsgDialog("测试animation");
                showMsgToast("应用数据已清空");
                break;

            case R.id.activity_about_btn_upload:
                LogUploader logUploader = new LogUploader(mContext);
                logUploader.upload(logHandler);
                break;

            case R.id.iv_about_contact:
                if (isQQInstall(this)) {
                    String mqq="123";
                    final String qqUrl =
                            "mqqwpa://im/chat?chat_type=wpa&uin=" + getResString(R.string.about_contact_qq);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl)));
                } else {
                    showMsgToast("请安装QQ客户端,再进行跳转");
                }
                break;
        }
    }

    public static boolean isQQInstall(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                //通过遍历应用所有包名进行判断
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }
}
