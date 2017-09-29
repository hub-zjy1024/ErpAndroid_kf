package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import utils.DialogUtils;
import utils.FTPUtils;
import utils.HttpUtils;
import utils.MyToast;
import utils.WebserviceUtils;

public class CaigouDetailActivity extends AppCompatActivity implements OnPageChangeListener {

    private String goodInfos;
    private String provider = "";
    private String address = "";
    private String phone = "";
    private String filePath = "";

    private String receiveMan = "";
    private String date = "";
    private Button btnCommit;
    private boolean flag = false;
    private String path;
    private ProgressDialog reviewDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlertDialog.Builder builder = new AlertDialog.Builder(CaigouDetailActivity.this);
            switch (msg.what) {
                case 0:
                    btnCommit.setEnabled(true);
                    break;
                case 1:
//                    btnCommit.setEnabled(true);
                    MyToast.showToast(CaigouDetailActivity.this, "本单据已存在合同文件");
                    break;
                case 2:
                    DialogUtils.dismissDialog(dialog);
                    break;

                case 3:
                    builder.setTitle("提示");
                    builder.setMessage("生成合同成功");
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 4:
                    builder.setTitle("提示");
                    builder.setMessage("生成合同失败：" + msg.obj.toString());
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 5:
                    pdfView.fromFile(new File(msg.obj.toString())) //设置pdf文件地址
                            .defaultPage(1)  //设置默认显示第1页
                            .onPageChange(CaigouDetailActivity.this) //设置翻页监听
                            //                            .onDraw(this)  //绘图监听
                            .showMinimap(false) //pdf放大的时候，是否在屏幕的右上角生成小地图
                            .swipeVertical(false) //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                            .enableSwipe(true)//是否允许翻页，默认是允许翻页
                            //    .pages() //把 5 过滤掉
                            .load();
                    DialogUtils.dismissDialog(dialog);
                    String path = filePath.substring(filePath.indexOf("dyj") - 1);
                    Log.e("zjy", "CaigouDetailActivity->handleMessage(): path==" + path);
                    tvPath.setText("文件存储路径为：" + path);
                    break;
                case 6:
                    builder.setTitle("提示");
                    builder.setMessage("生成合同成功");
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 7:
                    builder.setTitle("提示");
                    String message = "网络质量较差";
                    if (msg.obj != null) {
                        message = msg.obj.toString();
                    }
                    builder.setMessage("生成合同失败：" + message);
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 8:
                    builder.setTitle("提示");
                    builder.setMessage("当前单据未生成合同");
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 9:
                    builder.setTitle("提示");
                    builder.setMessage("连接服务器失败，请重试");
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 10:
                    builder.setTitle("提示");
                    builder.setMessage("下载合同文件失败");
                    builder.show();
                    DialogUtils.dismissDialog(dialog);
                    break;
                case 11:
                    Dialog alertDialog = DialogUtils.getSpAlert(CaigouDetailActivity.this, "FTP地址有误，请重启程序", "提示");
                    DialogUtils.dismissDialog(dialog);
                    break;

            }
        }
    };
    private ProgressDialog dialog;
    private Button btnReview;
    private PDFView pdfView;
    private TextView tvPath;
    private Button btnTakepic;
    private long cTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigou_detial);
        TextView tvPid = (TextView) findViewById(R.id.activity_caigou_detial_tv_pid);
        btnCommit = (Button) findViewById(R.id.activity_caigou_detial_btn_commit);
        btnReview = (Button) findViewById(R.id.activity_caigou_detial_btn_review);
        btnTakepic = (Button) findViewById(R.id.activity_caigou_detial_btn_takepic);
         tvPath = (TextView) findViewById(R.id.activity_caigou_detial_tv_open);
        pdfView = (PDFView) findViewById(R.id.activity_caigou_detial_pdfview);
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("正在查询单据详细信息");
        dialog.show();
        final Intent intent = getIntent();
        final String corpID = intent.getStringExtra("corpID");
        final String proID = intent.getStringExtra("providerID");
        final String pid = intent.getStringExtra("pid");
        tvPid.setText(pid);
        String dataPath = Environment.getDataDirectory().getAbsolutePath();
        File file = Environment.getDataDirectory();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                Log.e("zjy", "CaigouDetailActivity->onCreate(): f.name==" + f.getAbsolutePath());

            }
        }
        Log.e("zjy", "CaigouDetailActivity->onCreate(): data==" + dataPath);
        btnTakepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CaigouDetailActivity.this);
                builder.setTitle("上传方式选择");
                builder.setItems(new String[]{"拍照", "从手机选择", "连拍"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent1 = new Intent(CaigouDetailActivity.this, TakePicActivity.class);
                                intent1.putExtra("flag", "caigou");
                                intent1.putExtra("pid", pid);
                                startActivity(intent1);
                                MyApp.myLogger.writeInfo("takepic-caigou");
                                break;
                            case 1:
                                Intent intent2 = new Intent(CaigouDetailActivity.this, ObtainPicFromPhone.class);
                                intent2.putExtra("flag", "caigou");
                                intent2.putExtra("pid", pid);
                                startActivity(intent2);
                                MyApp.myLogger.writeInfo("obtain-caigou");
                                break;
                            case 2:
                                Intent intent3 = new Intent(CaigouDetailActivity.this, CaigouTakePic2Activity.class);
                                intent3.putExtra("flag", "caigou");
                                intent3.putExtra("pid", pid);
                                MyApp.myLogger.writeInfo("takepic2-caigou");
                                startActivity(intent3);
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodInfos == null) {
                    MyToast.showToast(CaigouDetailActivity.this, "正在获取数据，请稍后");
                    return;
                } else if (goodInfos.equals("")) {
                    MyToast.showToast(CaigouDetailActivity.this, "无采购列表");
                    return;
                } else if (provider.equals("")) {
                    MyToast.showToast(CaigouDetailActivity.this, "供货方为空，请重新进入当前页面");
                    return;
                }
                if ("".equals(proID) || "0".equals(proID)) {
                    MyToast.showToast(CaigouDetailActivity.this, "供应商ID错误，请联系后台人员");
                    return;
                }
                dialog.setMessage("正在生成合同");
                dialog.show();
                //                http:
                //172.16.6.160:8009/2017/Manage/FileInfo/CreatMarkStockFile.aspx?printType=37&id=826766

                new Thread() {
                    @Override
                    public void run() {
                        //                        WebserviceUtils.getRequest(map, "");
                        String url = "http://172.16.6.160:8009/2017/Manage/FileInfo/CreatMarkStockFile.aspx?";
                        //                        printType=0 表示是送货单，非0表示合同
                        //
                        //                  String url = "http://175.16.6.160:8009/2017/Manage/FileInfo/CreatMarkStockFile.aspx?";
                        BufferedReader reader = null;
                        try {
                            url += "printType=" + URLEncoder.encode("1", "utf-8");
                            url += "&id=" + URLEncoder.encode(pid, "utf-8");
                            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                            conn.setConnectTimeout(15 * 1000);
                            conn.setRequestMethod("GET");
                            InputStream res = conn.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(res, "utf-8"));
                            String result = reader.readLine();
                            Log.e("zjy", "CaigouDetailActivity->run(): result==" + result);
                            if (result.contains("OK")) {
                                mHandler.sendEmptyMessage(6);
                            } else {
                                mHandler.obtainMessage(7, result).sendToTarget();
                            }
                            conn.disconnect();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            mHandler.obtainMessage(7).sendToTarget();
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                }.start();
            }


        });

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File dir = Environment
                        .getExternalStorageDirectory();
//                String name = pid + "_" + (int) (Math.random() * 1000) + ".pdf";
                String name = pid  + ".pdf";
                final File file = new File(dir, "dyj_ht/" + name);
                final String[] files = file.list();
                final File[] listFiles = file.getParentFile().listFiles();
                if (listFiles.length > 300) {
                    AlertDialog alertDilog = (AlertDialog) DialogUtils.getSpAlert(CaigouDetailActivity.this,
                            "缓存的pdf文件达到40M，是否清理", "提示", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for(int i=0;i<listFiles.length;i++) {
                                        listFiles[i].delete();
                                    }
                                    MyToast.showToast(CaigouDetailActivity.this, "删除成功");
                                }
                            }, "是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }, "否");
                    alertDilog.show();
                }

                if (file.exists()) {
                    if (System.currentTimeMillis() - cTime < 5 * 1000) {
                        MyToast.showToast(CaigouDetailActivity.this, "请不要点击过快");
                        return;
                    }
                    filePath = file.getAbsolutePath();
                    String path = filePath.substring(filePath.indexOf("dyj") - 1);
                    tvPath.setText("文件存储路径为：" +path);
                    pdfView.fromFile(file) //设置pdf文件地址
                            .defaultPage(1)  //设置默认显示第1页
                            .onPageChange(CaigouDetailActivity.this) //设置翻页监听
                            .showMinimap(false) //pdf放大的时候，是否在屏幕的右上角生成小地图
                            .swipeVertical(false) //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                            .enableSwipe(true)//是否允许翻页，默认是允许翻页
                            //    .pages() //把 5 过滤掉
                            .load();
                    cTime = System.currentTimeMillis();
                } else {
                    dialog.setMessage("正在下载合同");
                    dialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            String result = null;
                            try {
                                result = getHetongInfo(pid);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                            if (result != null) {
                                if (!result.equals("")) {
                                    if (result.contains("ftp")) {
                                        FTPUtils ftpUtil = new FTPUtils( CaigoudanTakePicActivity.ftpAddress, 21,
                                                CaigoudanTakePicActivity.username, CaigoudanTakePicActivity.password);
                                        try {
                                            ftpUtil.login();
                                            for (int i = 0; true; i++) {
                                                int index = result.indexOf("/") + 1;
                                                result = result.substring(index);
                                                Log.e("zjy", "CaigouDetailActivity->run(): result==" + result);
                                                if (i == 2) {
                                                    break;
                                                }
                                            }
                                            String remotePath = "/" + result;

                                            File parentFile = file.getParentFile();
                                            if (!parentFile.exists()) {
                                                parentFile.mkdirs();
                                            }
                                            FileOutputStream fio = new FileOutputStream
                                                    (file);
                                            ftpUtil.download(fio, remotePath);
                                            filePath = file.getAbsolutePath();
                                            fio.close();
                                            mHandler.obtainMessage(5, file.getAbsolutePath()).sendToTarget();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            mHandler.sendEmptyMessage(10);
                                        }
                                    } else {
                                        mHandler.sendEmptyMessage(8);
                                    }

                                } else {
                                    mHandler.sendEmptyMessage(8);
                                }
                            } else {
                                mHandler.sendEmptyMessage(9);
                            }
                        }
                    }.start();
                }
            }
        });
        String createDate = intent.getStringExtra("date");
        date = createDate.split(" ")[0];
        Log.e("zjy", "CaigouDetailActivity->onCreate(): date==" + date);

        new Thread() {
            @Override
            public void run() {
                super.run();
                String result = null;
                try {
                    result = getHetongInfo(pid);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    if (!result.equals("")) {
                        mHandler.sendEmptyMessage(1);
                    } else {
                        mHandler.sendEmptyMessage(0);
                    }
                } else {
                    mHandler.sendEmptyMessage(0);
                }
                try {
                    getData(corpID, proID);
                    String infos = getData2(pid);
                    try {
                        JSONObject object = new JSONObject(infos);
                        JSONArray array = object.getJSONArray("表");
                        goodInfos = array.toString();
                        mHandler.sendEmptyMessage(2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        goodInfos = "";
                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                }
            }
        }.start();


    }

    private void javaHttp(String pid, String corpID) {
        String fullName = provider;
        String proShortName = provider;
        String ginfo = goodInfos;
        String proPhone = phone;
        String proAddress = address;
        String proReceiveMan = receiveMan;
        String createDate = date;
        String hetongID = pid;
        String fileID = corpID;
        String strUrl = HttpUtils.host + "PrinterServer/HetongServlet?";
        // pid=1024521?proFullName=北京市供货商&hetongID=101110&proShortName=bjgh&goodInfos=name1,name2,name3,
        // &proPhone=13452525623&proAddress=北京市海淀区中关村&proReceiveMan=晨晨&createDate=2017-07-13
        try {
            strUrl += "proFullName=" + URLEncoder.encode(fullName, "UTF-8");
            strUrl += "&proShortName=" + URLEncoder.encode(proShortName, "UTF-8");
            strUrl += "&goodInfos=" + URLEncoder.encode(ginfo, "UTF-8");
            strUrl += "&proPhone=" + URLEncoder.encode(proPhone, "UTF-8");
            strUrl += "&proAddress=" + URLEncoder.encode(proAddress, "UTF-8");
            strUrl += "&proReceiveMan=" + URLEncoder.encode(proReceiveMan, "UTF-8");
            strUrl += "&createDate=" + URLEncoder.encode(createDate, "UTF-8");
            strUrl += "&hetongID=" + URLEncoder.encode(hetongID, "UTF-8");
            strUrl += "&fileID=" + URLEncoder.encode(fileID, "UTF-8");
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            InputStream lin = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(lin, "UTF-8"));
            String htRes = reader.readLine();
            if (htRes.startsWith("ok")) {
                mHandler.sendEmptyMessage(3);
            } else {
                Message msg = mHandler.obtainMessage(4);
                msg.obj = htRes.substring(htRes.indexOf(":") + 1);
                msg.sendToTarget();
            }
            Log.e("zjy", "CaigouDetailActivity->run(): hetong_response==" + htRes);
            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHetongInfo(String pid) throws IOException, XmlPullParserException {
        //        http://172.16.6.160:8006/
        //        GetHeTongFileInfo
        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        properties.put("pid", pid);
        SoapObject req = WebserviceUtils.getRequest(properties, "GetHeTongFileInfo");
        SoapObject res = WebserviceUtils.getSoapObjResponse(req, SoapEnvelope.VER11, WebserviceUtils.MartService,  WebserviceUtils.DEF_TIMEOUT);
        String result = res.getPropertyAsString("GetHeTongFileInfoResult");
        Log.e("zjy", "CaigouDetailActivity->getHetongInfo(): result==" + result);
        if (result.equals("anyType{}")) {
            return "";
        }
        return res.getPropertyAsString("GetHeTongFileInfoResult");
    }

    public void getData(String corpID, String proDetialID) throws IOException, XmlPullParserException, JSONException {
        //GetPriviteInfo
        //        GetInvoiceCorpInfo
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("id", corpID);
        SoapObject req = WebserviceUtils.getRequest(map1, "GetInvoiceCorpInfo");
        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req, SoapEnvelope.VER11, WebserviceUtils.MartService);
        JSONObject obj = new JSONObject(res.toString());
        JSONArray table = obj.getJSONArray("表");
        for (int i = 0; i < table.length(); i++) {
            JSONObject temp = table.getJSONObject(i);
            //            phone = temp.getString("Phone");
            provider = temp.getString("Name");
            //            address = temp.getString("Address");
        }
        Log.e("zjy", "CaigouDetailActivity->getData(): invoice==" + res.toString());
        map1.put("id", proDetialID);
        SoapObject req1 = WebserviceUtils.getRequest(map1, "GetPriviteInfo");
        SoapPrimitive res1 = WebserviceUtils.getSoapPrimitiveResponse(req1, SoapEnvelope.VER11, WebserviceUtils.MartService);
        try {
            JSONObject root = new JSONObject(res1.toString());
            JSONArray providerArray = root.getJSONArray("表");
            for (int i = 0; i < table.length(); i++) {
                JSONObject temp = providerArray.getJSONObject(i);
                receiveMan = temp.getString("ReceiveMan");
            }
            Log.e("zjy", "CaigouDetailActivity->getData(): provider==" + res1.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public String getData2(String pid) throws IOException, XmlPullParserException {
        //        GetOLDMartStockView_mx
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("pid", pid);
        SoapObject req = WebserviceUtils.getRequest(map1, "GetOLDMartStockView_mx");
        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req, SoapEnvelope.VER11, WebserviceUtils.MartService);
        Log.e("zjy", "CaigouDetailActivity->getData(): caigouDetial==" + res.toString());
        return res.toString();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }
}
