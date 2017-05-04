package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.utils.MyInetConn;
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
    private MyInetConn mPrinter;
    private PreChukuInfo info;
    private int printCount = 0;
    private Button btnReconnect;
    private TextView tvState;
    private Handler zHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    tvState.setText("连接失败");
                    break;
                case 2:
                    btnPrint.setEnabled(true);
                    tvState.setText("连接成功");
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
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrinter == null) {
                    tvState.setText("正在重连。。");
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            mPrinter = new MyInetConn();
                        }
                    }.start();
                }
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrinter == null) {
                    MyToast.showToast(PreChukuDetailActivity.this, "请先连接打印机");
                    return;
                }
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
//                            mPrinter.printTextLn("这是font1");
//                            mPrinter.getResponse();
                            isFinish = PrinterStyle.printPreparedChuKu(mPrinter, info);
                            mPrinter.cutPaper();
                            String res = updatePrintCount(Integer.parseInt(info.getPid()));
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                Intent intent = getIntent();
                String pid = intent.getStringExtra("pid");
                mPrinter = new MyInetConn();

                if (mPrinter != null) {
                    zHandler.sendEmptyMessage(2);
                } else {
                    zHandler.sendEmptyMessage(1);
                }
                try {
                    String root = getPreChukuDetail(Integer.parseInt(pid), Integer.parseInt(MyApp.id));
                    List<PreChukuDetailInfo> list = new ArrayList<PreChukuDetailInfo>();
                    JSONObject object = new JSONObject(root);
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
                        String leftCounts = String.valueOf(Integer.parseInt(obj.getString("BalanceQ")) - Integer.parseInt(counts));
                        PreChukuDetailInfo info = new PreChukuDetailInfo(partNo, fengzhuang, pihao, factory, description, notes, p, counts, leftCounts);
                        list.add(info);
                    }
                    info.setDetailInfos(list);
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

    public String getPreChukuDetail(int pid, int uid) throws IOException, XmlPullParserException {
        //        GetOutStorageNotifyPrintView
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        map.put("uid", uid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetOutStorageNotifyPrintView");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        Log.e("zjy", "PreChukuActivity->getPreChukuCallback(): response==" + response);
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
