package com.b1b.js.erpandroid_kf;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dev.ScanBaseActivity;
import com.b1b.js.erpandroid_kf.adapter.XiaopiaoAdapter;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;

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

import printer.entity.XiaopiaoInfo;
import utils.MyToast;
import utils.PrinterStyle;
import utils.WebserviceUtils;
import utils.btprint.MyBluePrinter;
import utils.btprint.MyPrinterParent;
import utils.btprint.SPrinter;

public class RukuTagPrintAcitivity extends ScanBaseActivity {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyPrinterParent.STATE_CONNECTED:
                    MyToast.showToast(mContext, "连接成功");
                    tvState.setTextColor(Color.GREEN);
                    tvState.setText("已连接");
                    break;
                case MyBluePrinter.STATE_DISCONNECTED:
                    MyToast.showToast(mContext, "连接失败");
                    tvState.setTextColor(Color.RED);
                    tvState.setText("连接失败");
                    break;
                case 3:
                    xpAdapter.notifyDataSetChanged();
                    break;
                case SPrinter.STATE_OPENED:
                    if (!btAddress.equals("")) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                printer2.connect(btAddress);
                            }
                        }.start();
                    }
                    break;
            }
        }
    };
    private MyBluePrinter printer;
    private SPrinter printer2;
    private Context mContext = this;
    private int reqCode = 500;
    private TextView tvState;
    private Button btnSetting;
    private Button btnScan;
    private EditText edPid;
    private Button btnPrint;
    private List<XiaopiaoInfo> infos;
    private XiaopiaoAdapter xpAdapter;
    private Button btnSearch;
    private ImageView ivTest;
    private String btAddress;
    private TextView tvTitle;
    private boolean isOffline = false;
    public static String extraMode = "mode";
    public static String MODE_OFFLINE = "offline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruku_tag_print_acitivity);
        ListView lv = (ListView) findViewById(R.id.ruku_lv);
        tvState = (TextView) findViewById(R.id.rukutag_activity_tv_state);
        edPid = (EditText) findViewById(R.id.rukutag_activity_ed_pid);
        btnSetting = (Button) findViewById(R.id.rukutag_activity_btn_setting);
        tvTitle = (TextView) findViewById(R.id.rukutag_activity_title);
        btnScan = (Button) findViewById(R.id.rukutag_activity_btn_scancode);
        btnPrint = (Button) findViewById(R.id.rukutag_activity_btn_print);
        btnSearch = (Button) findViewById(R.id.rukutag_activity_btn_search);
        final CheckBox cboOffline = (CheckBox) findViewById(R.id.ruku_cbo_offline);
        ivTest = (ImageView) findViewById(R.id.test_iv);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_CODE);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                ivTest.setImageBitmap(BarcodeCreater.creatBarcode(RukuTagPrintAcitivity.this, "123487523", 40
                // * 8, 50, true, 10));
                final String pid = edPid.getText().toString();
                if (pid.equals("")) {
                    MyToast.showToast(mContext, "请输入单据号");
                    return;
                }
                if (infos.size() > 0) {
                    infos.clear();
                    xpAdapter.notifyDataSetChanged();
                }
                getData(pid);
            }
        });
        final String mode = getIntent().getStringExtra(extraMode);
        if (MODE_OFFLINE.equals(mode)) {
            btnSearch.setVisibility(View.GONE);
            cboOffline.setVisibility(View.GONE);
            tvTitle.setText("条码打印");
            isOffline = true;
        }
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PrintSettingActivity.class);
                startActivityForResult(intent, reqCode);
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        String code = edPid.getText().toString();
                        if (cboOffline.isChecked()) {
                            if (!code.equals("")) {
                                printer2.printBarCode(code, 0, 1, 80);
                                printer2.printText(code);
                                printer2.newLine();
                                printer2.newLine();
                                printer2.newLine();
                            } else {
                                MyToast.showToast(RukuTagPrintAcitivity.this, "请输入明细ID");
                            }
                            return;
                        }
                        if (isOffline && !code.equals("")) {
                            if (!code.equals("")) {
                                printer2.printBarCode(code, 0, 1, 80);
                                printer2.printText(code);
                                printer2.newLine();
                                printer2.newLine();
                                printer2.newLine();
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MyToast.showToast(RukuTagPrintAcitivity.this, "请输入明细ID");
                                    }
                                });
                            }
                            return;
                        }
                        for (int i = 0; i < infos.size(); i++) {
                            XiaopiaoInfo tInfo = infos.get(i);
                            //                            PrinterStyle.printXiaopiao(mContext, printer);
                            //                            PrinterStyle.printXiaopiao2(mContext, printer, tInfo);
                            PrinterStyle.printXiaopiao2(printer2, tInfo);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        });
        infos = new ArrayList<>();
        xpAdapter = new XiaopiaoAdapter(this, infos, R.layout.item_rukutag);
        lv.setAdapter(xpAdapter);
        SharedPreferences userInfo = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        btAddress = userInfo.getString("btPrinterMac", "");
        Log.e("zjy", "RukuTagPrintAcitivity->run(): printerAddress==" + btAddress);
        if (btAddress.equals("")) {
            Intent intent = new Intent(mContext, PrintSettingActivity.class);
            startActivityForResult(intent, reqCode);
        } else {
            printer = new MyBluePrinter(this, mHandler, new MyBluePrinter
                    .OnReceiveDataHandleEvent() {
                @Override
                public void OnReceive(BluetoothDevice var1) {
                }
            });
            printer2 = new SPrinter(mHandler, mContext, new MyBluePrinter.OnReceiveDataHandleEvent() {
                @Override
                public void OnReceive(BluetoothDevice var1) {
                    Log.e("zjy", "RukuTagPrintAcitivity->OnReceive(): Discovery2==");
                }
            });
            new Thread() {
                @Override
                public void run() {
                    printer2.open();
                    if (printer2.isOpen()) {
                        printer2.connect(btAddress);
                    }
                }
            }.start();
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_ruku_tag_print_acitivity;
    }

    @Override
    public void resultBack(String result) {
        boolean isNum = MyToast.checkNumber(result);
        edPid.setText(result);
        if (isNum) {
            infos.clear();
            xpAdapter.notifyDataSetChanged();
            getData(result);
        } else {
            MyToast.showToast(this, getString(R.string.error_numberformate));
        }
    }

    private void getData(final String pid) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String detailInfo = getDetailInfo(pid);
                    Log.e("zjy", "RukuTagPrintAcitivity->run(): detailInfo==" + detailInfo);
                    JSONObject jobj = new JSONObject(detailInfo);
                    JSONArray jarray = jobj.getJSONArray("表");
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject tj = jarray.getJSONObject(i);
                        String parno = tj.getString("型号");
                        String pid = tj.getString("PID");
                        String time = tj.getString("制单日期");
                        String temp[] = time.split(" ");
                        if (temp.length > 1) {
                            time = temp[0];
                        }
                        String deptno = tj.getString("DeptID");
                        String counts = tj.getString("数量");
                        String factory = tj.getString("厂家");
                        String producefrom = "";
                        String pihao = tj.getString("批号");
                        String fengzhuang = tj.getString("封装");
                        String description = tj.getString("描述");
                        String place = "";
                        String storageID = tj.getString("StorageID");
                        String flag = tj.getString("InvoiceType");
                        String company = tj.getString("开票公司");
                        String notes = tj.getString("备注");
                        String detailPID = tj.getString("detailPID");
                        XiaopiaoInfo info = new XiaopiaoInfo(parno, deptno, time, deptno, counts, factory,
                                producefrom, pihao, fengzhuang, description, place, notes, flag, detailPID, storageID,
                                company);
                        info.setPid(pid);
                        infos.add(info);
                    }
                    mHandler.sendEmptyMessage(3);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == reqCode) {
                printer = PrintSettingActivity.getPrint();
                tvState.setTextColor(Color.GREEN);
                tvState.setText("已连接");
                printer2 = (SPrinter) PrintSettingActivity.getSPrint();
            } else if (requestCode == CaptureActivity.REQ_CODE) {
                if (data != null) {
                    String result = data.getStringExtra("result");
                    edPid.setText(result);
                    infos.clear();
                    xpAdapter.notifyDataSetChanged();
                    try {
                        Integer.parseInt(result);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        MyToast.showToast(mContext, "扫码结果不为数字");
                        return;
                    }
                    if (isOffline) {
                        printer2.printBarCode(result, 0, 1, 80);
                        printer2.printText(result);
                        printer2.newLine();
                        printer2.newLine();
                        printer2.newLine();
                    } else {
                        getData(result);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (printer != null) {
            printer.close();
        }
        if (printer2 != null) {
            printer2.close();
        }
    }

    public String getDetailInfo(String pid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetInstorectInfo");
        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11,
                WebserviceUtils.ChuKuServer);
        return res.toString();
    }
}
