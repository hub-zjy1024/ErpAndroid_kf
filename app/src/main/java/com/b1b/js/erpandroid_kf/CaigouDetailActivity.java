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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import utils.framwork.DialogUtils;
import utils.handler.NoLeakHandler;
import utils.net.ftp.FTPUtils;
import utils.net.wsdelegate.MartService;

public class CaigouDetailActivity extends BaseMActivity implements OnPageChangeListener, NoLeakHandler
        .NoLeakCallback {

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

    private static final int MSG_CANCEL_DIALOG = 2;
    private static final int MSG_OK = 6;
    private static final int MSG_ERROR1 = 7;
    private static final int MSG_GET_PDF = 5;
    private static final int FILE_PDF_MAX = 300;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                btnCommit.setEnabled(true);
                break;
            case 1:
                //                    btnCommit.setEnabled(true);
                showMsgToast("本单据已存在合同文件");
                break;
            case MSG_CANCEL_DIALOG:
                DialogUtils.dismissDialog(dialog);
                break;
            case MSG_GET_PDF:
                File file = new File(msg.obj.toString());
                pdfView.fromFile(file) //设置pdf文件地址
                        //设置翻页监听
                        .showMinimap(true) //pdf放大的时候，是否在屏幕的右上角生成小地图
                        // pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                        .enableSwipe(true)//是否允许翻页，默认是允许翻页
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                int pageCount = pdfView.getPageCount();
                                String msg = String.format("正在加载%s/%s", nbPages, pageCount);
                                showMsgToast(msg);
                            }
                        })//是否允许翻页，默认是允许翻页
                        .load();
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                String path = filePath.substring(filePath.indexOf("dyj") - 1);
                Log.e("zjy", "CaigouDetailActivity->handleMessage(): path==" + path);
                tvPath.setText("文件存储路径为：" + path);
                break;
            case MSG_OK:
                String okMsg = msg.obj.toString();
                showMsgDialog(okMsg);
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                break;
            case MSG_ERROR1:
                String message = "网络质量较差";
                if (msg.obj != null) {
                    message = msg.obj.toString();
                }
                String finalMsg = "生成合同失败" + message;
                showMsgDialog(finalMsg);
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                break;
            case 8:
                String msg8 = "当前单据未生成合同";
                mHandler.obtainMessage(MSG_ERROR1, msg8).sendToTarget();
                break;
            case 9:
                String msg9 ="连接服务器失败，请重试";
                mHandler.obtainMessage(MSG_ERROR1, msg9).sendToTarget();
                break;
            case 10:
                String msg10 ="下载合同文件失败";
                mHandler.obtainMessage(MSG_ERROR1, msg10).sendToTarget();
                break;
            case 11:
                Dialog alertDialog = DialogUtils.getSpAlert(CaigouDetailActivity.this, "FTP地址有误，请重启程序", "提示");
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                break;

        }
    }

    private Handler mHandler = new NoLeakHandler(this);
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
        btnTakepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CaigouDetailActivity.this);
                builder.setTitle("上传方式选择");
                final Intent uploadIntent = new Intent();
                uploadIntent.putExtra("flag", "caigou");
                uploadIntent.putExtra("pid", pid);
                builder.setItems(getResources().getStringArray(R.array.upload_type), new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                uploadIntent.setClass(mContext, TakePicActivity.class);
                                MyApp.myLogger.writeInfo("takepic-caigou");
                                break;
                            case 1:
                                uploadIntent.setClass(mContext, ObtainPicFromPhone.class);
                                MyApp.myLogger.writeInfo("obtain-caigou");
                                break;
                            case 2:
                                uploadIntent.setClass(mContext, CaigouTakePic2Activity
                                        .class);
                                MyApp.myLogger.writeInfo("takepic2-caigou");
                                break;
                        }
                        startActivity(uploadIntent);
                    }
                });
                builder.show();
            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodInfos == null) {
                    showMsgToast("正在获取数据，请稍后");
                    return;
                } else if (goodInfos.equals("")) {
                    showMsgToast("无采购列表");
                    return;
                } else if (provider.equals("")) {
                    showMsgToast("供货方为空，请重新进入当前页面");
                    return;
                }
                if ("".equals(proID) || "0".equals(proID)) {
                    showMsgToast("供应商ID错误，请联系后台人员");
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
                        //                  String url =
                        // "http://175.16.6.160:8009/2017/Manage/FileInfo/CreatMarkStockFile.aspx?";
                        BufferedReader reader = null;
                        String msg = "";
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
                                msg = "生成合同成功";
                                Message message = mHandler.obtainMessage(MSG_OK, msg);
                                mHandler.sendMessage(message);
                            } else {
                                throw new Exception("返回异常" + result);
                            }
                            conn.disconnect();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            msg = "io," + e.getMessage();
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                            msg = "其他," + e.getMessage();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        mHandler.obtainMessage(MSG_ERROR1, msg).sendToTarget();
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
                String name = pid + ".pdf";
                final File file = new File(dir, "dyj_ht/" + name);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                final File[] listFiles = file.getParentFile().listFiles();
                if (listFiles.length > FILE_PDF_MAX) {
                    DialogUtils.getDialog(mContext).setMsg("缓存的pdf文件达到" + FILE_PDF_MAX + "，是否清理").setBtn1(
                            "是").setBtn1L(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < listFiles.length; i++) {
                                listFiles[i].delete();
                            }
                            showMsgToast("删除成功");
                        }
                    }).setBtn2("否").create();
                }

                if (file.exists()) {
                    if (System.currentTimeMillis() - cTime < 5 * 1000) {
                        showMsgToast("请不要点击过快");
                        return;
                    }
                    filePath = file.getAbsolutePath();
                    String path = filePath.substring(filePath.indexOf("dyj") - 1);
                    tvPath.setText("文件存储路径为：" + path);
                    pdfView.fromFile(file) //设置pdf文件地址
                            .showMinimap(true) //pdf放大的时候，是否在屏幕的右上角生成小地图
                            .enableSwipe(true)
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    int pageCount = pdfView.getPageCount();
                                    String msg = String.format("正在加载%s/%s", nbPages, pageCount);
                                    showMsgToast(msg);
                                }
                            })//是否允许翻页，默认是允许翻页
                            .load();
                    cTime = System.currentTimeMillis();
                } else {
                    dialog.setMessage("正在下载合同");
                    dialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            String msg10 ="下载合同文件失败";
                            String result = null;
                            try {
                                result = getHetongInfo(pid);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                            if (result == null) {
                                msg10 =    "连接服务器失败，请重试" ;
                            } else if (result.equals("")) {
                                msg10 = "当前单据未生成合同" ;
                            } else if (result.contains("ftp")) {
                                FTPUtils ftpUtil = FTPUtils.getGlobalFTP();
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
                                    mHandler.obtainMessage(MSG_GET_PDF, file.getAbsolutePath()).sendToTarget();
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    msg10 = "下载合同文件失败," + e.getMessage();
                                }
                            } else {
                                msg10 = "当前单据未生成合同" ;
                            }
                            mHandler.obtainMessage(MSG_ERROR1, msg10).sendToTarget();
                        }
                    }.start();
                }
            }
        });
        String createDate = intent.getStringExtra("date");
        date = createDate.split(" ")[0];
        Runnable getFileRun = new Runnable() {
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
                        mHandler.sendEmptyMessage(1);
                    } else {
                        mHandler.sendEmptyMessage(0);
                    }
                } else {
                    mHandler.sendEmptyMessage(0);
                }
                try {
                    getData(corpID, proID);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String infos = getData2(pid);
                    JSONObject object = new JSONObject(infos);
                    JSONArray array = object.getJSONArray("表");
                    goodInfos = array.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    goodInfos = "";
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
            }
        };
        TaskManager.getInstance().execute(getFileRun);
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

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
        String host = "http://192.168.10.65:8080/";
        String strUrl = host + "PrinterServer/HetongServlet?";
        String errMsg = "";
        BufferedReader reader = null;
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
             reader = new BufferedReader(new InputStreamReader(lin, "UTF-8"));
            String htRes = reader.readLine();
            Log.e("zjy", "CaigouDetailActivity->run(): hetong_response==" + htRes);
            if (htRes.startsWith("ok")) {
                mHandler.obtainMessage(MSG_OK, "生成合同成功").sendToTarget();
                return;
            } else {
                errMsg = "返回异常," + htRes.substring(htRes.indexOf(":") + 1);
                throw new Exception(errMsg);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            errMsg = "io," + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            errMsg = "其他," + e.getMessage();
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Message msg = mHandler.obtainMessage(MSG_ERROR1);
        msg.obj = errMsg;
        msg.sendToTarget();
    }

    public String getHetongInfo(String pid) throws IOException, XmlPullParserException {
        //        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        //        properties.put("pid", pid);
        //        SoapObject req = WebserviceUtils.getRequest(properties, "GetHeTongFileInfo");
        //        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req, WebserviceUtils
        // .MartService);
        String result = MartService.GetHeTongFileInfo(Integer.parseInt(pid));
        Log.e("zjy", "CaigouDetailActivity->getHetongInfo(): result==" + result);
        if (result.equals("anyType{}")) {
            return "";
        }
        return result;
    }

    public void getData(String corpID, String proDetialID) throws IOException, XmlPullParserException,
            JSONException {
        //        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        //        map1.put("id", corpID);
        //        SoapObject req = WebserviceUtils.getRequest(map1, "GetInvoiceCorpInfo");
        //        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req,  WebserviceUtils
        // .MartService);
        String soapRes = MartService.GetInvoiceCorpInfo(Integer.parseInt(corpID));
        JSONObject obj = new JSONObject(soapRes);
        JSONArray table = obj.getJSONArray("表");
        for (int i = 0; i < table.length(); i++) {
            JSONObject temp = table.getJSONObject(i);
            //            phone = temp.getString("Phone");
            provider = temp.getString("Name");
            //            address = temp.getString("Address");
        }
        Log.e("zjy", "CaigouDetailActivity->getData(): invoice==" + soapRes);
        //        map1.put("id", proDetialID);
        //        SoapObject req1 = WebserviceUtils.getRequest(map1, "GetPriviteInfo");
        //        SoapPrimitive res1 = WebserviceUtils.getSoapPrimitiveResponse(req1,  WebserviceUtils
        // .MartService);
        String soapRes2 = MartService.GetPriviteInfo(Integer.parseInt(proDetialID));
        try {
            JSONObject root = new JSONObject(soapRes2);
            JSONArray providerArray = root.getJSONArray("表");
            for (int i = 0; i < table.length(); i++) {
                JSONObject temp = providerArray.getJSONObject(i);
                receiveMan = temp.getString("ReceiveMan");
            }
            Log.e("zjy", "CaigouDetailActivity->getData(): provider==" + soapRes2);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public String getData2(String pid) throws IOException, XmlPullParserException {
        return MartService.GetOLDMartStockView_mx(pid);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

}
