package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.PreChukuDetialAdapter;
import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import utils.DialogUtils;
import utils.MyPrinter;
import utils.MyToast;
import utils.PrinterStyle;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;

public class PreChukuDetailActivity extends AppCompatActivity {

    private ListView lv;
    private Button btnPrint;
    private MyPrinter mPrinter;
    private PreChukuInfo info;
    private int printCount = 0;
    private Button btnReconnect;
    private TextView tvState;
    private PreChukuDetialAdapter detailAdapter;
    private EditText edIP;
    SharedPreferences sp;
    ProgressDialog pDialog;
    List<PreChukuDetailInfo> list = new ArrayList<PreChukuDetailInfo>();
    private String pAddress;
    private String localKuqu = "";
    private Handler zHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tvState.setText("正在连接");
                    break;
                case 1:
                    tvState.setText("连接失败");
                    btnPrint.setEnabled(false);
                    MyToast.showToast(PreChukuDetailActivity.this, "连接打印机失败");
                    if (pDialog != null && pDialog.isShowing()) {
                        pDialog.cancel();
                    }
                    tvState.setTextColor(Color.RED);
                    break;
                case 2:
                    btnPrint.setEnabled(true);
                    tvState.setText("连接成功");
                    if (pDialog != null && pDialog.isShowing()) {
                        pDialog.cancel();
                    }
                    tvState.setTextColor(Color.BLACK);
                    break;
                case 3:
                    if (list.size() != 0) {
                        detailAdapter.notifyDataSetChanged();
                    }
                    break;
                case 5:
                    MyToast.showToast(PreChukuDetailActivity.this, "打印次数插入成功");
                    break;
                case 6:
                    MyToast.showToast(PreChukuDetailActivity.this, "打印次数插入失败");
                    MyApp.myLogger.writeError("插入打印次数失败：" + info.getPid());
                    break;
                case 7:
                    DialogUtils.getSpAlert(PreChukuDetailActivity.this, "打印出现错误，请检查打印机是否正常工作", "提示").show();
                    break;
                case 8:
                    DialogUtils.safeShowDialog(PreChukuDetailActivity.this, DialogUtils.getSpAlert(PreChukuDetailActivity.this,
                            "查询不到相关信息", "提示"));
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_chuku_detail);
        lv = (ListView) findViewById(R.id.pre_chuku_detail_lv);
        btnPrint = (Button) findViewById(R.id.pre_chuku_detail_print);
        tvState = (TextView) findViewById(R.id.pre_chuku_detail_state);
        btnReconnect = (Button) findViewById(R.id.pre_chuku_detail_reconnect);
        Button btnSet = (Button) findViewById(R.id.pre_chuku_detail_set);
        edIP = (EditText) findViewById(R.id.pre_chuku_detail_printerip);
        detailAdapter = new PreChukuDetialAdapter(list, this, R.layout.pre_chuku_lv_detail_items);
        lv.setAdapter(detailAdapter);
        pDialog = new ProgressDialog(PreChukuDetailActivity.this);
        pDialog.setCancelable(false);
        sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        pAddress = sp.getString("printerIP", "");
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreChukuDetailActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.setMessage("请稍等，正在重连打印机");
                if (pDialog != null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                if (pAddress.equals("")) {
                    MyToast.showToast(PreChukuDetailActivity.this, "打印机地址还未设置好");
                    AlertDialog.Builder builder = new AlertDialog.Builder(PreChukuDetailActivity.this);
                    builder.setTitle("提示").setMessage("是否前往配置页配置打印机地址").setPositiveButton("前往", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(PreChukuDetailActivity.this, SettingActivity.class);
                            startActivity(intent);
                        }
                    }).show();
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        //beijing 192.168.9.101  //shenzhen
                        mPrinter = new MyPrinter(pAddress);
                        if (mPrinter.getmOut() != null) {
                            zHandler.sendEmptyMessage(2);
                        } else {
                            zHandler.sendEmptyMessage(1);
                        }
                    }
                }.start();
            }
        });
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL("http://172.16.6.101:802/ErpV5IP.asp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String result = "";
                    String s;
                    while ((s = reader.readLine()) != null) {
                        result = result + s;
                    }
                    Log.e("zjy", "SettingActivity->run():getip==" + result);
                    //                        GetChildStorageIDByIP
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("ip", result);
                    SoapObject object1 = WebserviceUtils.getRequest(map, "GetChildStorageIDByIP");
                    SoapPrimitive res1 = WebserviceUtils.getSoapPrimitiveResponse(object1, SoapEnvelope.VER11, WebserviceUtils
                            .MartService);
                    Log.e("zjy", "PreChukuDetailActivity->run(): res==" + res1);
                    localKuqu = res1.toString();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info == null) {
                    MyToast.showToast(PreChukuDetailActivity.this, "请稍等，打印数据还未获取完成");
                    return;
                }
                if (localKuqu.equals("")) {
                    MyToast.showToast(PreChukuDetailActivity.this, "请稍等，正在获取库区信息");
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        boolean isFinish = false;
                        try {
                            mPrinter.initPrinter();
                            isFinish = PrinterStyle.printPreparedChuKu(mPrinter, info, localKuqu);
                            boolean isOK = mPrinter.cutPaper();
                            if (!isOK) {
                                zHandler.sendEmptyMessage(7);
                                return;
                            }
                            String res = updatePrintCount(Integer.parseInt(info.getPid()));
                            if (res.equals("1")) {
                                zHandler.sendEmptyMessage(5);
                            } else {
                                zHandler.sendEmptyMessage(6);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (isFinish) {
                                zHandler.sendEmptyMessage(0);
                            }
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Intent intent = getIntent();
                    String pid = intent.getStringExtra("pid");
                     getPreChukuDetail(Integer.parseInt(pid), Integer.parseInt(MyApp.id));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    zHandler.sendEmptyMessage(8);
                    e.printStackTrace();
                }
            }
        }.start();
        if (pAddress.equals("")) {
            if ("101".equals(MyApp.id)) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        zHandler.sendEmptyMessage(0);
                        pAddress = "192.168.199.200";
                        sp.edit().putString("printerIP", pAddress).commit();
                        mPrinter = new MyPrinter(pAddress);
                        if (mPrinter.getmOut() != null) {
                            zHandler.sendEmptyMessage(2);
                        } else {
                            zHandler.sendEmptyMessage(1);
                        }
                    }
                }.start();
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示").setMessage("是否前往配置页配置打印机地址").setPositiveButton("前往", new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(PreChukuDetailActivity.this, SettingActivity.class);
                        startActivity(intent);
                    }
                }).show();
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    zHandler.sendEmptyMessage(0);
                    mPrinter = new MyPrinter(pAddress);
                    if (mPrinter.getmOut() != null) {
                        zHandler.sendEmptyMessage(2);
                    } else {
                        zHandler.sendEmptyMessage(1);
                    }
                }
            }.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pAddress = sp.getString("printerIP", "");
        SoftKeyboardUtils.closeInputMethod(edIP, PreChukuDetailActivity.this);
    }

    public String getPreChukuDetail(int pid, int uid) throws IOException, XmlPullParserException, JSONException {
        //        GetOutStorageNotifyPrintView
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        map.put("uid", uid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetOutStorageNotifyPrintView");
        SoapObject response = WebserviceUtils.getSoapObjResponse(request, SoapEnvelope.VER11, WebserviceUtils
                .ChuKuServer, 30 * 1000);
        String result = "";
        if (response != null) {
            Object resResult = response.getProperty("GetOutStorageNotifyPrintViewResult");
            if (resResult != null) {
                result = resResult.toString();
            } else {
                MyApp.myLogger.writeError(PreChukuDetailActivity.class, "getProperty  null！！！" + pid + "\t" + uid);
            }
        } else {
            MyApp.myLogger.writeError(PreChukuDetailActivity.class, "detail response null！！！" + pid + "\t" + uid);
        }
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + result);
        JSONObject object = new JSONObject(result);
        JSONArray array = object.getJSONArray("表");
        info = new PreChukuInfo();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (i == 0) {
                info.setPid(obj.getString("PID"));
                info.setSalesman(obj.getString("业务员"));
                info.setEmployeeID(obj.getString("EmployeeID"));
                info.setDeptID(obj.getString("部门ID"));
                info.setPactID(obj.getString("合同编号"));
                info.setClient(obj.getString("客户"));
                info.setOutType(obj.getString("出库类型"));
                info.setFahuoType(obj.getString("发货类型"));
                info.setKuqu(obj.getString("库区"));
                info.setMainNotes(obj.getString("note"));
                info.setIsVip(obj.getString("IsVIP"));
                boolean isXiankuan = obj.getBoolean("IsXianHuoXianJie");
                info.setXiankuan(isXiankuan);
            }
            String partNo = obj.getString("型号");
            String fengzhuang = obj.getString("封装");
            String pihao = obj.getString("批号");
            String factory = obj.getString("厂家");
            String description = obj.getString("描述");
            String p = obj.getString("位置");
            String notes = obj.getString("备注");
            String counts = obj.getString("数量");
            String detailID = obj.getString("PDID");
            String leftCounts = String.valueOf(Integer.parseInt(obj.getString("BalanceQ")) - Integer.parseInt(counts));
            PreChukuDetailInfo dinfo = new PreChukuDetailInfo(partNo, fengzhuang, pihao, factory, description, notes, p, counts,
                    leftCounts);
            dinfo.setDetailID(detailID);
            dinfo.setProLevel(obj.getString("DengJi"));
            dinfo.setInitialDate(obj.getString("InstorageData"));
            list.add(dinfo);
        }
        info.setDetailInfos(list);
        zHandler.sendEmptyMessage(3);
        return response.toString();
    }

    public String updatePrintCount(int pid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        SoapObject request = WebserviceUtils.getRequest(map, "UpdatePrintCKTZCount");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils
                .ChuKuServer);
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + response);
        return response.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPrinter != null){
            mPrinter.close();
        }
    }

    //    GetOutStoragePrintViewPriviceInfo
    public String getPrintInfo(String pid, String uid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("", "");
        map.put("", "");
        SoapObject req = WebserviceUtils.getRequest(map, "GetOutStoragePrintViewPriviceInfo");
        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        return null;
    }

}
