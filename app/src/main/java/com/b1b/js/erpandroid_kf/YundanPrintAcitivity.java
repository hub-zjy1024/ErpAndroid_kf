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
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kyeexpress.KyExpressUtils;
import kyeexpress.YundanJson;
import printer.activity.SetYundanActivity;
import utils.DialogUtils;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;

public class YundanPrintAcitivity extends AppCompatActivity {

    private Context mContext = this;
    private Spinner spiType;
    private String pid;
    private String jName;
    private String jTel;
    private String jAddress;

    private String dName;
    private String dTel;
    private String dAddress;
    private String jComapany = "";
    private String payByWho;
    private String dCompany = "";
    private String payType;
    private String severType = "";

    private EditText edBags;
    private EditText edJPerson;
    private EditText edJTel;
    private EditText edJAddress;
    private EditText eddTel;
    private EditText eddAddress;
    private TextView tvPayBy;
    private Button btnReInsert;
    private Button btnChukudan;
    private EditText edAccount;
    private AlertDialog alertDg;
    private String yundanID;
    private String ddestcode;
    private ProgressDialog pd;
    private String pidNotes = "";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    edJPerson.setText(jName);
                    edJAddress.setText(jAddress);
                    edJTel.setText(jTel);
                    eddAddress.setText(dAddress);
                    eddTel.setText(dTel);
                    eddPerson.setText(dName);
                    tvPayBy.setText(payByWho);
                    tvNote.setText(pidNotes);
                    flag = 1;
                    break;
                case 1:
                    DialogUtils.dismissDialog(pd);
                    break;

                case 2:
                    break;

            }
        }
    };
    private EditText eddPerson;
    private TextView tvYundanID;
    private TextView tvInsertState;
    private Button btnPrint;
    private Button btnRePrint;
    private String printerAddress = "";
    private Spinner spiPayType;
    private int flag = 0;
    private String account = "";
    private Spinner spiPrinter;
    private CheckBox cboAddMore;
    private LinearLayout addmoreContainer;
    private String dgoodInfos;
    private String dcardID;
    private String dpayType;
    private String dserverType;
    private String dprintName = "";
    private EditText edMorePid;
    private TextView tvNote;
    private String kdName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoftKeyboardUtils.hideKeyBoard(this);
        setContentView(R.layout.activity_yundan_print_acitivity);
        spiType = (Spinner) findViewById(R.id.yundanprint_spi_type);
        spiPayType = (Spinner) findViewById(R.id.yundanprint_spi_paytype);
        spiPrinter = (Spinner) findViewById(R.id.yundanprint_spi_printer);
        final String[] serverTypes = new String[]{"陆运件-普", "同城即日-省内", "同城次日-省内", "隔日达-快（空）", "次日达-很快（空）", "当天达-极快（空）"};
        spiType.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province, R.id.item_province_tv, serverTypes));
        final String[] payTypes = new String[]{"寄付月结", "到付"};
        spiPayType.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province, R.id.item_province_tv, payTypes));
        final List<String> printerItems = new ArrayList<>();
        printerItems.add("请选择打印机");
        pid = getIntent().getStringExtra("pid");
        edJPerson = (EditText) findViewById(R.id.yundanprint_ed_j_person);
        cboAddMore = (CheckBox) findViewById(R.id.yundanprint_cbo_addmore);
        TextView tvPID = (TextView) findViewById(R.id.yundanprint_tv_pid);
        tvPID.setText(pid);
        edAccount = (EditText) findViewById(R.id.yundanprint_ed_account);
        tvNote = (TextView) findViewById(R.id.yundanprint_tv_note);
        edJTel = (EditText) findViewById(R.id.yundanprint_ed_j_tel);
        edJAddress = (EditText) findViewById(R.id.yundanprint_ed_j_addresss);
        eddTel = (EditText) findViewById(R.id.yundanprint_ed_d_tel);
        eddAddress = (EditText) findViewById(R.id.yundanprint_ed_d_address);
        eddPerson = (EditText) findViewById(R.id.yundanprint_ed_d_person);
        edBags = (EditText) findViewById(R.id.yundanprint_ed_counts);
        btnReInsert = (Button) findViewById(R.id.yundanprint_btnReInsert);
        tvYundanID = (TextView) findViewById(R.id.yundanprint_tv_orderid);
        tvInsertState = (TextView) findViewById(R.id.yundanprint_tv_insertinfo_state);
        tvPayBy = (TextView) findViewById(R.id.yundanprint_tv_payby);
        btnChukudan = (Button) findViewById(R.id.yundanprint_btn_printchukudan);
        btnPrint = (Button) findViewById(R.id.yundanprint_btn_print);
        btnRePrint = (Button) findViewById(R.id.yundanprint_btnReprint);
        edMorePid = (EditText) findViewById(R.id.yundanprint_ed_morepid);

        Button btnMoreScan = (Button) findViewById(R.id.yundanprint_btn_scan);
        btnMoreScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanIntent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(scanIntent, CaptureActivity.REQ_CODE);
            }
        });
        final Button btnMoreCommit = (Button) findViewById(R.id.yundanprint_btn_addmore);
        btnMoreCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tempPID = edMorePid.getText().toString();
                if (tempPID.equals("")) {
                    MyToast.showToast(mContext, "请输入需要关联的单据号");
                    return;
                }
                if (yundanID == null) {
                    MyToast.showToast(mContext, "当前还未下单");
                    return;
                }
                pd.setMessage("正在关联");
                pd.show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            String ok = insertYundanInfo(tempPID, yundanID, ddestcode, "跨越");
                            changeInsertState(ok, tempPID);
                        } catch (IOException e) {
                            showAlert("关联失败：" +getString(R.string.bad_connection));
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(1);
                    }
                }.start();

            }
        });
        SharedPreferences userInfo = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String configJson = userInfo.getString(SettingActivity.CONFIG_JSON, "");
        try {
            Log.e("zjy", "YundanPrintAcitivity->onCreate(): configJson==" + configJson);
            JSONObject obj = new JSONObject(configJson);
            KyExpressUtils.uuid = obj.getString(SettingActivity.KYUUID);
            KyExpressUtils.key = obj.getString(SettingActivity.KYKEY);
            edAccount.setText(obj.getString(SettingActivity.KYACCOUNT));
        } catch (JSONException e) {
            KyExpressUtils.uuid = "";
            KyExpressUtils.key = KyExpressUtils.uuid = "";
            edAccount.setText("");
            MyApp.myLogger.writeError("KYprint:" + e.toString());
            e.printStackTrace();
        }
        btnRePrint = (Button) findViewById(R.id.yundanprint_btnReprint);
        addmoreContainer = (LinearLayout) findViewById(R.id.yundanprint_container_addmore);
        cboAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cboAddMore.isChecked()) {
                    addmoreContainer.setVisibility(View.VISIBLE);
                } else {
                    addmoreContainer.setVisibility(View.GONE);
                }
            }
        });
        btnChukudan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PreChukuDetailActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = edAccount.getText().toString().trim();
                String counts = edBags.getText().toString();
                if (printerAddress.equals("")) {
                    DialogUtils.getSpAlert(mContext, "当前未配置打印服务器地址，是否前往配置?", "提示", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(mContext, SettingActivity.class);
                            startActivity(intent);
                        }
                    }, "是", null, "否").show();
                    return;
                }
                jName = edJPerson.getText().toString();
                jAddress = edJAddress.getText().toString();
                jTel = edJTel.getText().toString();
                dName = eddPerson.getText().toString();
                dAddress = eddAddress.getText().toString();
                dTel = eddTel.getText().toString();
                String payType = spiPayType.getSelectedItem().toString();
                Object seletPrinter = spiPrinter.getSelectedItem();
                String printer = "";
                if (seletPrinter != null) {
                    printer = seletPrinter.toString();
                }
                String serverType = spiType.getSelectedItem().toString();
                serverType = serverType.substring(0, serverType.indexOf("-"));
                final String bags = edBags.getText().toString().trim();
                if (bags.equals("")) {
                    Toast.makeText(mContext, "请输入包裹数", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jTel.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人电话", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jAddress.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人地址", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jName.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人姓名", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dTel.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人电话", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dAddress.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人地址", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dName.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人姓名", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                String goodInfos = getIntent().getStringExtra("goodInfos");
                pd.setMessage("正在打印中");
                pd.show();
                startOrder(goodInfos, account, payType, serverType, counts, dprintName);
            }
        });
        btnRePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jName = edJPerson.getText().toString();
                jAddress = edJAddress.getText().toString();
                jTel = edJTel.getText().toString();
                dName = eddPerson.getText().toString();
                dAddress = eddAddress.getText().toString();
                dTel = eddTel.getText().toString();
                account = edAccount.getText().toString().trim();
                final String bags = edBags.getText().toString().trim();
                if (bags.equals("")) {
                    Toast.makeText(mContext, "请输入包裹数", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jTel.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人电话", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jAddress.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人地址", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (jName.equals("")) {
                    Toast.makeText(mContext, "必须输入寄件人姓名", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dTel.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人电话", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dAddress.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人地址", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                if (dName.equals("")) {
                    Toast.makeText(mContext, "必须输入收件人姓名", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                Object selectP = spiPrinter.getSelectedItem();
                String printer = "";
                if (selectP != null) {
                    printer = selectP.toString();
                }
                String goodInfos = "url1-500,url2-6000,url3-700";
                goodInfos = getIntent().getStringExtra("goodInfos");
                String cardID = "";
                String payType = spiPayType.getSelectedItem().toString();
                String serverType = spiType.getSelectedItem().toString();
                serverType = serverType.substring(0, serverType.indexOf("-"));
                dgoodInfos = goodInfos;
                dcardID = account;
                dpayType = payType;
                dserverType = serverType;
                pd.setMessage("正在重新打印");
                pd.show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            boolean ok = printKyYundan(printerAddress, yundanID, dgoodInfos, dcardID, dpayType, bags,
                                    dprintName, ddestcode
                                    , dserverType);
                            if (ok) {
                                showAlert("打印成功");
                            } else {
                                showAlert("打印失败，打印过程出错");
                            }
                        } catch (IOException e) {
                            showAlert("打印失败，连接服务器失败");
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(1);
                    }
                }.start();
            }
        });
        btnReInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("正在重新关联运单号");
                pd.show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        String insertResult = null;
                        try {
                            insertResult = insertYundanInfo(pid, yundanID, ddestcode, "跨越");
                            changeInsertState(insertResult, pid);
                        } catch (IOException e) {
                            showAlert("关联运单号失败，网络连接失败");
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(1);
                    }
                }.start();
            }
        });
        printerAddress = getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("serverPrinter", "");
        alertDg = (AlertDialog) DialogUtils.getSpAlert(this, "提示", "提示");
        pd = new ProgressDialog(this);
        pd.setTitle("请稍等");
        pd.setMessage("正在打印中。。。");
        final List<String> spiItems = new ArrayList<>();
        final ArrayAdapter<String> printerAdapter = new ArrayAdapter<>(this, R.layout.item_province, R.id.item_province_tv,
                spiItems);
        spiPrinter.setAdapter(printerAdapter);
        new Thread() {
            @Override
            public void run() {
                String ip = "http://" + printerAddress + ":8080";
                String urlPrinter = ip + "/PrinterServer/GetPrinterInfoServlet";
                try {
                    URL url = new URL(urlPrinter);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    InputStream in = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                    BufferedReader bis = new BufferedReader(reader);
                    String s = "";
                    String result = "";
                    while ((s = bis.readLine()) != null) {
                        result += s;
                    }
                    Log.e("zjy", "SetYundan->run():printer: reuslt=="
                            + result);
                    if (!result.equals("")) {
                        String[] printers = result.split(",");
                        for (String p : printers) {
                            if (p.equals("Ky_Printer")) {
                                spiItems.add(0, p);
                            } else {
                                spiItems.add(p);
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                printerAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    showAlert("打印机地址有误，请重新配置");
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                getDetailInfo(pid);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                //                GetBD_YunDanInfoByID
                try {
                    String result = SetYundanActivity.getOnlineSavedYdInfo(pid);
                    Log.e("zjy", "SetYundanActivity->run(): onlineYundan==" + result);
                    //                    "objid":"613","parentid":"0","objname":"1176338","objvalue":"616606640489",
                    // "objtype":"顺丰","objexpress":"010",
                    JSONObject obj = new JSONObject(result);
                    JSONArray root = obj.getJSONArray("表");
                    if (root.length() > 0) {
                        JSONObject t = root.getJSONObject(0);
                        String orderID = t.getString("objvalue");
                        String destcode = t.getString("objexpress");
                        final String exName = t.getString("objtype");
                        ddestcode = destcode;
                        yundanID = orderID;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvYundanID.setText("当前单据已有单号：" + exName + yundanID);
                                btnRePrint.setEnabled(true);
                            }
                        });
                    }
                } catch (IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvYundanID.setText("查询关联单号失败，请重新进入");
                        }
                    });
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvYundanID.setText("还未下单，请下单");
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        printerAddress = getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("serverPrinter", "");
    }

    @NonNull
    private boolean printKyYundan(String serverIP, String orderID, String goodInfos, String cardID, String
            payType, String counts, String printName, String destcode, String yundanType)
            throws IOException {

        String ip = "http://" + serverIP + ":8080";
        String strURL = ip + "/PrinterServer/KyPrintServlet?";
        strURL += "orderID=" + URLEncoder.encode(orderID,
                "UTF-8");
        strURL += "&yundanType=" + URLEncoder.encode(yundanType,
                "UTF-8");
        strURL += "&counts=" + URLEncoder.encode(counts,
                "UTF-8");
        strURL += "&goodinfos=" + URLEncoder.encode(goodInfos,
                "UTF-8");
        strURL += "&printer=" + URLEncoder.encode(printName,
                "UTF-8");
        strURL += "&cardID=" + URLEncoder.encode(cardID,
                "UTF-8");
        strURL += "&payType=" + URLEncoder.encode(payType,
                "UTF-8");
        strURL += "&j_name=" + URLEncoder.encode(jName,
                "UTF-8");
        strURL += "&j_phone=" + URLEncoder.encode(jTel,
                "UTF-8");
        strURL += "&j_address=" + URLEncoder.encode(jAddress,
                "UTF-8");
        strURL += "&destcode=" + URLEncoder.encode(destcode,
                "UTF-8");
        strURL += "&d_name=" + URLEncoder.encode(dName,
                "UTF-8");
        strURL += "&d_phone=" + URLEncoder.encode(dTel,
                "UTF-8");
        strURL += "&d_address=" + URLEncoder.encode(dAddress,
                "UTF-8");
        Log.e("zjy", "SetYundanActivity->printKyYundan(): StrUrl==" + strURL);
        URL url = new URL(strURL);
        HttpURLConnection conn = (HttpURLConnection) url
                .openConnection();
        conn.setConnectTimeout(20 * 1000);
        InputStream in = conn.getInputStream();
        StringBuilder builder = new StringBuilder();
        String s = "";
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(in, "UTF-8"));
        while ((s = reader.readLine()) != null) {
            builder.append(s);
        }
        String res = builder.toString();
        Log.e("zjy", "SetYundanActivity->run(): print_result==" + builder
                .toString());
        if (res.equals("ok")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQ_CODE) {
            String result = "";
            if (data != null) {
                result = data.getStringExtra("result");
            }
            edMorePid.setText(result);
        }
    }

    public void startOrder(final String goodInfos, final String cardID, final String
            payType, final String serverType, final String counts,
                           final String printName) {

        new Thread() {
            @Override
            public void run() {
                //                20112320120
                YundanJson info = new YundanJson();
                info.col_018 = serverType;
                info.col_001 = jComapany;
                info.col_004 = jName;
                info.col_005 = jTel;
                info.jjTelQH = "";
                info.col_003 = jTel;
                info.col_002 = jAddress;
                info.col_011 = "";
                info.col_006 = dCompany;
                info.col_010 = dName;
                info.col_007 = dAddress;
                info.col_009 = dTel;
                info.sjTelQH = "";
                info.sjTelFJH = "";
                info.col_008 = dTel;
                info.col_037 = cardID;
                info.col_013 = payType;
                info.col_019 = "托寄";
                info.col_028 = "0";
                info.col_021 = counts;
                //                info.col_033 = "";
                info.col_027 = "";
                info.payCardNo = account;
                info.bfAmount = "";
                info.vipshopCode = "";
                String receiveID = "";
                String destcode = "";
                try {
                    String ret = KyExpressUtils.sendPostRequest(info);
                    JSONObject jobj = new JSONObject(ret);
                    String code = jobj.getString("errCode");
                    String msg = "";
                    JSONObject detailObj = jobj.getJSONObject("result");
                    if ("0000".equals(code)) {
                        receiveID = detailObj.getString("YDCode");
                        destcode = detailObj.getString("destAreaCode");
                        yundanID = receiveID;
                        ddestcode = destcode;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvYundanID.setText("下单成功：" + yundanID);
                                btnRePrint.setEnabled(true);
                            }
                        });
                    } else {
                        msg = jobj.getString("errMsg");
                        showAlert("下单错误:" + msg);
                    }
                    dgoodInfos = goodInfos;
                    dcardID = cardID;
                    dpayType = payType;
                    dserverType = serverType;
                } catch (IOException e) {
                    showAlert("下单失败，请检查网络");
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!receiveID.equals("")) {
                    try {
                        String insertResult = insertYundanInfo(pid, receiveID, destcode, "跨越");
                        changeInsertState(insertResult, pid);
                        //                        printKyYundan(serverIP, yundanID, goodInfos, cardID, payType, counts,
                        // printName, ddestcode, yundanType);
                        boolean printOk = printKyYundan(printerAddress, yundanID, dgoodInfos, dcardID, dpayType, counts,
                                dprintName,
                                ddestcode
                                , dserverType);
                        if (printOk) {
                            showAlert("打印成功");
                        } else {
                            showAlert("打印失败，打印出错！！");
                        }
                    } catch (IOException e) {
                        showAlert("打印出现错误" +getString(R.string.bad_connection));
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    public void getDetailInfo(String pid) {
        try {
            String detail = SetYundanActivity.searchByPid(pid);
            JSONObject root = new JSONObject(detail);
            JSONArray table = root.getJSONArray("表");
            for (int i = 0; i < table.length(); i++) {
                JSONObject obj = table.getJSONObject(i);
                jName = obj.getString("业务员");
                jTel = obj.getString("寄件电话");
                jAddress = obj.getString("寄件地址1");
                jComapany = obj.getString("寄件公司");
                payByWho = obj.getString("谁付运费");
                pidNotes = obj.getString("Note");
                dAddress = obj.getString("收件地址");
                dTel = obj.getString("收件电话");
                dName = obj.getString("收件人");
                dCompany = obj.getString("收件公司");
                //                corpID = obj.getString("InvoiceCorp");
                //                storageID = obj.getString("StorageID");
            }
            mHandler.sendEmptyMessage(0);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    {
    //        "retMsg":"提交成功", "YDCode":"20112325362", "DestinationAirport":
    //        "朝阳四季青点部", "DeliveryPoint":"", "destAreaCode":"010"
    //    }
    public void showAlert(final String msg) {
        DialogUtils.dismissDialog(alertDg);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            alertDg.setMessage(msg);
            DialogUtils.safeShowDialog(mContext, alertDg);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    alertDg.setMessage(msg);
                    DialogUtils.safeShowDialog(mContext, alertDg);
                }
            });
        }
    }

    //    string objname, string objvalue, string express, string objtype  四个参数：随后一个是类型，跨越或者
    // 顺丰
    public String insertYundanInfo(String pid, String orderID, String destcode, String objtype) throws IOException,
            XmlPullParserException {
        if ("101".equals(MyApp.id)) {
            return "成功";
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("objname", pid);
        map.put("objvalue", orderID);
        map.put("express", destcode);
        map.put("objtype", objtype);
        SoapObject req = WebserviceUtils.getRequest(map, "InsertBD_YunDanInfoOfType");
//        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(req, SoapEnvelope.VER11, WebserviceUtils.SF_SERVER);
        SoapObject obj = WebserviceUtils.getSoapObjResponse(req, SoapEnvelope.VER11, WebserviceUtils.SF_SERVER, WebserviceUtils
                .DEF_TIMEOUT);
        String result = "";
        if (obj != null) {
            Object resResult = obj.getProperty("InsertBD_YunDanInfoOfTypeResult");
            if (resResult != null) {
                result = resResult.toString();
            } else {
                MyApp.myLogger.writeError(YundanPrintAcitivity.class, "getProperty  null！！！" + pid + "\t" + MyApp.id);
            }
        }
        return result;
    }

    public void changeInsertState(String result, final String pid) {
        if ("成功".equals(result)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvInsertState.setTextColor(Color.GREEN);
                    tvInsertState.setText("关联单号到" + pid + "成功");
                }
            });
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvInsertState.setTextColor(Color.RED);
                    tvInsertState.setText("关联单号到" + pid + "失败！！！");
                }
            });
        }
    }
}
