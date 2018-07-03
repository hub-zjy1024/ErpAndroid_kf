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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.PreChukuDetialAdapter;
import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import printer.entity.XiaopiaoInfo;
import utils.DialogUtils;
import utils.MyPrinter;
import utils.MyToast;
import utils.PrinterStyle;
import utils.SoftKeyboardUtils;
import utils.btprint.MyBluePrinter;
import utils.handler.NoLeakHandler;
import utils.wsdelegate.ChuKuServer;
import utils.wsdelegate.MartService;

public class PreChukuDetailActivity extends SavedLoginInfoActivity implements NoLeakHandler.NoLeakCallback {

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
    private MyBluePrinter btPrinter;
    private Handler zHandler = new NoLeakHandler(this);
    @Override
    public void handleMessage(Message msg) {
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
    private Button btnPrintTag;
    private int reqBtCode = 500;
    private String storageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_chuku_detail);
        lv = (ListView) findViewById(R.id.pre_chuku_detail_lv);
        btnPrint = (Button) findViewById(R.id.pre_chuku_detail_print);
        btnPrintTag = (Button) findViewById(R.id.pre_chuku_detail_printtag);
        tvState = (TextView) findViewById(R.id.pre_chuku_detail_state);
        btnReconnect = (Button) findViewById(R.id.pre_chuku_detail_reconnect);
        Button btnSet = (Button) findViewById(R.id.pre_chuku_detail_set);
        edIP = (EditText) findViewById(R.id.pre_chuku_detail_printerip);
        detailAdapter = new PreChukuDetialAdapter(list, this, R.layout.pre_chuku_lv_detail_items);
        lv.setAdapter(detailAdapter);
        pDialog = new ProgressDialog(PreChukuDetailActivity.this);
        pDialog.setCancelable(false);
        sp = getSharedPreferences(SettingActivity.PREF_KF, Context.MODE_PRIVATE);
        pAddress = sp.getString("printerIP", "");
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreChukuDetailActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        btnPrintTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PreChukuDetailActivity.this, PrintSettingActivity.class), reqBtCode);
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
//                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//                    map.put("ip", result);
//                    SoapObject object1 = WebserviceUtils.getRequest(map, "GetChildStorageIDByIP");
//                    SoapPrimitive res1 = WebserviceUtils.getSoapPrimitiveResponse(object1, WebserviceUtils
//                            .MartService);
//                    String soapRes = WebserviceUtils.getWcfResult(map, "GetChildStorageIDByIP",
//                            WebserviceUtils.MartService);
                    String soapRes = MartService.GetChildStorageIDByIP(result);
                    Log.e("zjy", "PreChukuDetailActivity->run(): res==" + soapRes);
                    localKuqu = soapRes;
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
        storageID = getIntent().getStringExtra("storageID");
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Intent intent = getIntent();
                    String pid = intent.getStringExtra("pid");
                    getPreChukuDetail(Integer.parseInt(pid), Integer.parseInt(loginID));
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
            if (CheckUtils.isAdmin()) {
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
            } else {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == reqBtCode && resultCode == RESULT_OK) {
            btPrinter = PrintSettingActivity.getPrint();
            List<PreChukuDetailInfo> detailInfos = info.getDetailInfos();
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");

            for (int i = 0; i < detailInfos.size(); i++) {
                PreChukuDetailInfo pdinfo = detailInfos.get(i);
                String finalDate = info.getCreateDate();
                try {
                    Date tempDate = sdf1.parse(info.getCreateDate());
                    finalDate = sdf2.format(tempDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                XiaopiaoInfo xInfo = new XiaopiaoInfo(pdinfo.getPartNo(), info.getPartNo(), finalDate, info
                        .getDeptID(), pdinfo
                        .getCounts(), pdinfo.getFactory(), "", pdinfo.getPihao(), pdinfo.getFengzhuang(), pdinfo.getDescription
                        (), pdinfo.getPlace(), info
                        .getMainNotes(), null, pdinfo.getDetailID(), storageID);
                PrinterStyle.printXiaopiao2(this, btPrinter, xInfo);
            }
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
        String soapRes = ChuKuServer.GetOutStorageNotifyPrintView(pid, uid);
        if (soapRes.contains("null")) {
            String errorMsg = String.format(getString(R.string.error_soapobject) + "\tpid=%d uid=%d", pid, uid);
            MyApp.myLogger.writeBug(errorMsg);
        }
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + soapRes);
        JSONObject object = new JSONObject(soapRes);
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
                info.setCreateDate(obj.getString("制单日期").substring(0, 10));
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
            //            String detailID = obj.getString("PDID");
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
        return soapRes;
    }

    public String updatePrintCount(int pid) throws IOException, XmlPullParserException {
        String result = ChuKuServer.UpdatePrintCKTZCount(pid);
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + result);
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrinter != null) {
            mPrinter.close();
        }
        if (btPrinter != null) {
            btPrinter.close();
        }
    }

}
