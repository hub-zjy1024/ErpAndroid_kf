package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.PreChukuDetialAdapter;
import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.utils.MyPrinter;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.PrinterStyle;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
    List<PreChukuDetailInfo> list = new ArrayList<PreChukuDetailInfo>();
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
                    tvState.setTextColor(Color.RED);
                    break;
                case 2:
                    btnPrint.setEnabled(true);
                    tvState.setText("连接成功");
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
        edIP = (EditText) findViewById(R.id.pre_chuku_detail_printerip);
        detailAdapter = new PreChukuDetialAdapter(list, this, R.layout.pre_chuku_lv_detail_items);
        lv.setAdapter(detailAdapter);
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        final String localPrinterIP = sp.getString("printerIP", "");
                        mPrinter = new MyPrinter(localPrinterIP);
                        if (mPrinter.getmOut() != null) {
                            zHandler.sendEmptyMessage(2);
                        } else {
                            zHandler.sendEmptyMessage(1);
                        }
                    }
                }.start();
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info == null) {
                    MyToast.showToast(PreChukuDetailActivity.this, "请稍等，打印数据还未获取完成");
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        boolean isFinish = false;
                        try {
                            mPrinter.initPrinter();
                            isFinish = PrinterStyle.printPreparedChuKu(mPrinter, info);
                            mPrinter.cutPaper();
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
        sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        final String localPrinterIP = sp.getString("printerIP", "");
        if (localPrinterIP.equals("")) {
            if (MyApp.id.equals("101")) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Intent intent = getIntent();
                        String pid = intent.getStringExtra("pid");
                        zHandler.sendEmptyMessage(0);
                        mPrinter = new MyPrinter(null);
                        if (mPrinter.getmOut() != null) {
                            zHandler.sendEmptyMessage(2);
                        } else {
                            zHandler.sendEmptyMessage(1);
                        }
                        try {
                            String root = getPreChukuDetail(Integer.parseInt(pid), Integer.parseInt(MyApp.id));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示").setMessage("请在菜单中的'配置'项中进行配置打印机IP地址").setPositiveButton("前往", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(PreChukuDetailActivity.this, SettingActivity.class);
                    startActivity(intent);
                }
            }).show();
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Intent intent = getIntent();
                    String pid = intent.getStringExtra("pid");
                    zHandler.sendEmptyMessage(0);
                    mPrinter = new MyPrinter(localPrinterIP);
                    if (mPrinter.getmOut() != null) {
                        zHandler.sendEmptyMessage(2);
                    } else {
                        zHandler.sendEmptyMessage(1);
                    }
                    try {
                        String root = getPreChukuDetail(Integer.parseInt(pid), Integer.parseInt(MyApp.id));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edIP.getWindowToken(), 0);
            //            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public String getPreChukuDetail(int pid, int uid) throws IOException, XmlPullParserException, JSONException {
        //        GetOutStorageNotifyPrintView
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        map.put("uid", uid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetOutStorageNotifyPrintView");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + response);
        JSONObject object = new JSONObject(response.toString());
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
                info.setClient(obj.getString("业务员"));
                info.setOutType(obj.getString("出库类型"));
                info.setFahuoType(obj.getString("发货类型"));
                info.setMainNotes(obj.getString("note"));
            }
            String partNo = obj.getString("型号");
            String fengzhuang = obj.getString("封装");
            String pihao = obj.getString("批号");
            String factory = obj.getString("厂家");
            String description = obj.getString("描述");
            String p = obj.getString("位置");
            String notes = obj.getString("备注");
            String counts = obj.getString("数量");
            boolean isXiankuan = obj.getBoolean("IsXianHuoXianJie");
            info.setXiankuan(isXiankuan);
            String leftCounts = String.valueOf(Integer.parseInt(obj.getString("BalanceQ")) - Integer.parseInt(counts));
            PreChukuDetailInfo info = new PreChukuDetailInfo(partNo, fengzhuang, pihao, factory, description, notes, p, counts, leftCounts);
            list.add(info);
        }
        info.setDetailInfos(list);
        zHandler.sendEmptyMessage(3);
        return response.toString();
    }

    public String updatePrintCount(int pid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        SoapObject request = WebserviceUtils.getRequest(map, "UpdatePrintCKTZCount");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + response);
        return response.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrinter.close();
    }
}
