package com.b1b.js.erpandroid_kf.yundan.kyeexpress;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.PreChukuDetailActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;
import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoWithScanActivity;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.BillOrder;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.BillUserInfo;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.OrderInfo;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.OrderRetInfo;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.YundanJson;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.YundanShixiao;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.util.KyExpressUtils;
import com.b1b.js.erpandroid_kf.yundan.utils.DHInfo;
import com.b1b.js.erpandroid_kf.yundan.utils.SavedYundanInfo;
import com.b1b.js.erpandroid_kf.yundan.utils.YunInfoTool;
import com.b1b.js.erpandroid_kf.yundan.utils.YundanDBData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.common.UploadUtils;
import utils.framwork.DialogUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.MartService;
import utils.net.wsdelegate.SF_Server;

/**
 * 跨越快递下单页
 */
public class KyPrintAcitivity extends SavedLoginInfoWithScanActivity implements NoLeakHandler.NoLeakCallback {

    private Spinner spiType;
    private String pid;
    private String jName;
    private String jTel;
    private String jAddress;
    private String jComapany = "";

    private String dName;
    private String dTel;
    private String dAddress;
    private String dCompany = "";

    private String payByWho;
    private String payType;
    private String severType = "";

    private YundanDBData CPidData;
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
    public String smsNum = "";
    private List<YundanShixiao> shixiaoList;
    private final int MSG_SHIXIAO = 2;
    private final int MSG_GETINFO = 0;
    private final int MSG_CANCEL_DIALOG = 1;
    private final int ShengWaiIndex = 6;
    private final int TongchengIndex = 2;
    private final int ShengNei = 0;
    private CheckBox cboSign;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GETINFO:
                edJPerson.setText(jName);
                edJAddress.setText(jAddress);
                edJTel.setText(jTel);
                eddAddress.setText(dAddress);
                eddTel.setText(dTel);
                eddPerson.setText(dName);
                tvPayBy.setText(payByWho);
                tvNote.setText(pidNotes);
                flag = 1;
                if (kfName.equals("深圳")) {
                    if (dAddress.contains("深圳")) {
                        spiType.setSelection(TongchengIndex);
                    } else {
                        String json = readRaw(R.raw.json_gd);
                        boolean shengNei = isShengNei(json, dAddress);
                        if (shengNei) {
                            spiType.setSelection(ShengNei);
                        } else {
                            spiType.setSelection(ShengWaiIndex);
                        }
                    }
                } else if (kfName.equals("北京中转库")) {
                    String json = readRaw(R.raw.json_bj);
                    if (dAddress.contains("北京")) {
                        spiType.setSelection(TongchengIndex);
                    } else {
                        boolean shengNei = false;
                        if (dAddress.contains("天津")) {
                            shengNei = true;
                        } else {
                            shengNei = isShengNei(json, dAddress);
                        }
                        if (shengNei) {
                            spiType.setSelection(ShengNei);
                        } else {
                            spiType.setSelection(ShengWaiIndex);
                        }
                    }
                }
                break;
            case MSG_SHIXIAO:
                Object obj = msg.obj;
                if (obj != null) {
                    shixiaoList = (List<YundanShixiao>) obj;
                    spiType.setAdapter(new ArrayAdapter<YundanShixiao>(mContext, R.layout.item_province, R
                            .id.item_province_tv, shixiaoList));
                }
                break;
            case MSG_CANCEL_DIALOG:
                DialogUtils.dismissDialog(pd);
                break;
        }
    }
    private List<Map<String, String>> addrList;
    private Handler mHandler = new NoLeakHandler(this);
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
    private SharedPreferences spKF;
    private String kfName = "";
    private String expressName = "跨越";
    private Intent reIntent;
    private boolean isDiaohuo = false;
    private Spinner spiDiaohuo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoftKeyboardUtils.hideKeyBoard(this);
        setContentView(R.layout.activity_yundan_print_acitivity);
        spiType = (Spinner) findViewById(R.id.yundanprint_spi_type);
        spiPayType = (Spinner) findViewById(R.id.yundanprint_spi_paytype);
        cboSign = (CheckBox) findViewById(R.id.yundanprint_cbo_sign);
        spiDiaohuo = (Spinner) findViewById(R.id.yundanprint_spi_printer);
        LinearLayout llDiaohuo = (LinearLayout) findViewById(R.id.yundanprint_ll_diaohuo);
        final String[] serverTypes = new String[]{"省内次日-省内", "省内即日-省内",
                "同城次日-同城", "同城即日-同城", "陆运件-货多(慢)", "隔日达-快（空）", "次日达-很快（空）",
                "当天达-极快（空）"};
//        final String[] serverTypes2 = new String[]{"省内次日-160", "同城次日-50", "同城即日-70", "陆运件-40", "隔日达-30",
//                "次日达-20",
//                "当天达-10", "次晨达-60", "航空件-80", "早班件-90", "中班件-100", "晚班件-110"};
        spiType.setAdapter(new ArrayAdapter<>(mContext, R.layout.item_province, R.id
                .item_province_tv, serverTypes));
        final String[] payTypes = new String[]{"寄付月结", "到付", "转第三方付款"};
        spiPayType.setAdapter(new ArrayAdapter<>(mContext, R.layout.item_province, R.id.item_province_tv,
                payTypes));
        final List<String> printerItems = new ArrayList<>();
        printerItems.add("请选择打印机");
        reIntent = getIntent();
        addrList = new ArrayList<>();
        pid = reIntent.getStringExtra("pid");
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
                startScanActivity();
            }
        });
        final Button btnMoreCommit = (Button) findViewById(R.id.yundanprint_btn_addmore);
        btnMoreCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tempPID = edMorePid.getText().toString();
                if (tempPID.equals("")) {
                    showMsgToast("请输入需要关联的单据号");
                    return;
                }
                if (yundanID == null) {
                    showMsgToast("当前还未下单");
                    return;
                }
                pd.setMessage("正在关联");
                pd.show();
                Runnable addMordRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            relateYdToDB(tempPID, yundanID, ddestcode, expressName);
                        } catch (IOException e) {
                            showAlert("关联失败：" + getString(R.string.bad_connection) + "," + e.getMessage());
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                    }
                };
                TaskManager.getInstance().execute(addMordRunnable);
            }
        });
        alertDg = (AlertDialog) DialogUtils.getSpAlert(this, "提示", "提示");
        SharedPreferences userInfo = getSharedPreferences(SettingActivity.PREF_KF, MODE_PRIVATE);
        String configJson = userInfo.getString(SettingActivity.CONFIG_JSON, "");
        spiDiaohuo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemAtPosition = (String) parent.getItemAtPosition(position);
                String[] detail = itemAtPosition.split("-->");
                String from = detail[0];
                String to = detail[1];
                for (Map<String, String> map : addrList) {
                    if (from.equals(map.get("key1")) && to.equals(map.get("key2"))) {
                        edJPerson.setText(map.get("name1"));
                        edJTel.setText(map.get("phone1"));
                        edJAddress.setText(map.get("address1"));
                        eddPerson.setText(map.get("name2"));
                        eddTel.setText(map.get("phone2"));
                        eddAddress.setText(map.get("address2"));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String sendFlag = reIntent.getStringExtra("type");
        if ("2".equals(sendFlag)) {
            isDiaohuo = true;
            llDiaohuo.setVisibility(View.VISIBLE);
        }

        try {
            Log.e("zjy", "KyPrintAcitivity->onCreate(): configJson==" + configJson);
            JSONObject obj = new JSONObject(configJson);
            kfName = obj.getString(SettingActivity.NAME);
            KyExpressUtils.uuid = obj.getString(SettingActivity.KYUUID);
            KyExpressUtils.key = obj.getString(SettingActivity.KYKEY);
            smsNum = obj.getString(SettingActivity.KY_SMSNUM);
            edAccount.setText(obj.getString(SettingActivity.KYACCOUNT));
            if (kfName.equals("深圳")) {
                if (spiPayType.getCount() > 2) {
                    spiPayType.setSelection(2);
                }
            }
        } catch (JSONException e) {
            KyExpressUtils.uuid = "";
            KyExpressUtils.key = KyExpressUtils.uuid = "";
            edAccount.setText("");
            showAlert("请在设置中配置库房信息");
            MyApp.myLogger.writeError("KYprint:no config" + e.toString());
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
        Runnable dhRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> titles = new ArrayList<>();
                    titles.add("请-->选择调货方向");
                    List<DHInfo> dhInfos = YunInfoTool.getDHInfos();
                    for (int j = 0; j < dhInfos.size(); j++) {
                        DHInfo mInfo = dhInfos.get(j);
                        String from = mInfo.getFrom();
                        String to = mInfo.getTo();
                        String name1 = mInfo.getName1();
                        String phone1 = mInfo.getPhone1();
                        String address1 = mInfo.getAddress1();
                        String name2 = mInfo.getName2();
                        String phone2 = mInfo.getPhone2();
                        String address2 = mInfo.getAddress2();
                        String account = mInfo.getAccount();
                        if (kfName.equals("")) {
                            titles.add(from + "-->" + to);
                            HashMap<String, String> map = new HashMap<>();
                            map.put("key1", from);
                            map.put("name1", name1);
                            map.put("phone1", phone1);
                            map.put("address1", address1);
                            map.put("account", account);
                            map.put("key2", to);
                            map.put("name2", name2);
                            map.put("phone2", phone2);
                            map.put("address2", address2);
                            addrList.add(map);
                        } else if (kfName.equals(from) || (kfName.contains("深圳"))) {
                            titles.add(from + "-->" + to);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("key1", from);
                            map.put("name1", name1);
                            map.put("phone1", phone1);
                            map.put("address1", address1);
                            map.put("account", account);
                            map.put("key2", to);
                            map.put("name2", name2);
                            map.put("phone2", phone2);
                            map.put("address2", address2);
                            addrList.add(map);
                        }
                    }
                    final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, R.layout
                            .item_province,
                            R.id.item_province_tv, titles);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            spiDiaohuo.setAdapter(adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        if (isDiaohuo) {
            TaskManager.getInstance().execute(dhRunnable);
        }
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
                    DialogUtils.getSpAlert(mContext, "当前未配置打印服务器地址，是否前往配置?", "提示", new DialogInterface
                            .OnClickListener() {
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
                String printer = "";
                String serverType = spiType.getSelectedItem().toString();
                serverType = serverType.substring(0, serverType.indexOf("-"));
                final String bags = edBags.getText().toString().trim();
               if(!checkNeedInfo(bags)){
                   return;
               }
                String goodInfos = reIntent.getStringExtra("goodInfos");
                final String tGoodInfos = goodInfos;
                final String tpayType = payType;
                final String tserverType = serverType;
                final String tCounts = counts;
                if (yundanID != null) {
                    DialogUtils.getSpAlert(mContext, "当前单据已有运单:" + yundanID + "，是否继续", "提示", new
                            DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd.setMessage("正在打印中");
                            pd.show();
                            //startOrder
                            startOrderNew(tGoodInfos, account, tpayType, tserverType, tCounts, dprintName);
                        }
                    }, "是", null, "否").show();
                } else {
                    pd.setMessage("正在打印中");
                    pd.show();
                    startOrderNew(goodInfos, account, payType, serverType, counts, dprintName);
                }
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
                if(!checkNeedInfo(bags)){
                    return;
                }
                String printer = "";
                String goodInfos = "url1-500,url2-6000,url3-700";
                goodInfos = reIntent.getStringExtra("goodInfos");
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
                Runnable rePrintRun = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String isSigned = cboSign.isChecked() ? "1" : "0";
                            boolean ok = printKyYundan(printerAddress, yundanID, dgoodInfos, dcardID,
                                    dpayType, bags,
                                    dprintName, ddestcode
                                    , dserverType, isSigned);
                            String msg = "打印成功";
                            if (!ok) {
                                throw new IOException("打印失败，打印过程出错");
                            }
                            showAlert(msg);
                        } catch (IOException e) {
                            showAlert("打印失败，连接服务器失败," + e.getMessage());
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                    }
                };
                TaskManager.getInstance().execute(rePrintRun);
            }
        });
        btnReInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("正在重新关联运单号");
                pd.show();
                Runnable reInsertRun = new Runnable() {
                    @Override
                    public void run() {
                        String insertResult = null;
                        try {
                            relateYdToDB(pid, yundanID, ddestcode, expressName);
                        } catch (IOException e) {
                            showAlert("关联运单号失败，网络连接失败" +e.getMessage());
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                    }
                };
                TaskManager.getInstance().execute(reInsertRun);
            }
        });
        spKF = getSharedPreferences(SettingActivity.PREF_KF, Context.MODE_PRIVATE);
        printerAddress = spKF.getString(SettingActivity.PRINTERSERVER, "");
        pd = new ProgressDialog(this);
        pd.setTitle("请稍等");
        pd.setMessage("正在打印中。。。");
        final List<String> spiItems = new ArrayList<>();
        final ArrayAdapter<String> printerAdapter = new ArrayAdapter<>(this, R.layout.item_province, R.id
                .item_province_tv,
                spiItems);
        //        spiPrinter.setAdapter(printerAdapter);
        Runnable pdRun = new Runnable() {
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
                    String s;
                    StringBuilder bd = new StringBuilder();
                    while ((s = bis.readLine()) != null) {
                        bd.append(s);
                    }
                    String result = bd.toString();
                    Log.e("zjy", "SetYundan->run():com.b1b.js.erpandroid_kf.printer: reuslt=="
                            + result);
                    if (!result.equals("")) {
                        String[] printers = result.split(",");
                        for (String p : printers) {
                            if (p.equals("KY_Printer")) {
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
                    showAlert("连接打印服务器失败，请检查网络或重新配置IP地址");
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(pdRun);
        Runnable onLineRun = new Runnable() {
            @Override
            public void run() {
                try {
                    getDetailInfo(pid);
                } catch (IOException e) {
                    showAlert("错误：" + getString(R.string.bad_connection));
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(onLineRun);
        Runnable onlineSavedRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SavedYundanInfo onlineSavedYdInfo = YunInfoTool.getSaveYundanInfo(pid);
                    if (onlineSavedYdInfo == null) {
                        throw new JSONException("下单信息为空");
                    }
                    ddestcode = onlineSavedYdInfo.getDestcode();
                    yundanID = onlineSavedYdInfo.getOrderID();
                    final String exName = onlineSavedYdInfo.getExName();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvYundanID.setText("当前单据已有单号：" + exName + yundanID);
                            btnRePrint.setEnabled(true);
                        }
                    });
                } catch (IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvYundanID.setText("查询关联单号失败，请重新进入");
                        }
                    });
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
        };
        TaskManager.getInstance().execute(onlineSavedRunnable);
        //        getYundanShixiao();
    }

    public boolean checkNeedInfo(String bags) {
        if (bags.equals("")) {
            showMsgToast("请输入包裹数");
            return false;
        }
        if (jTel.equals("")) {
            showMsgToast("必须输入寄件人电话");
            return false;
        }
        if (jAddress.equals("")) {
            showMsgToast("必须输入寄件人地址");
            return false;
        }
        if (jName.equals("")) {
            showMsgToast("必须输入寄件人姓名");
            return false;
        }
        if (dTel.equals("")) {
            showMsgToast("必须输入收件人电话");
            return false;
        }
        if (dAddress.equals("")) {
            showMsgToast("必须输入收件人地址");
            return false;
        }
        if (dName.equals("")) {
            showMsgToast("必须输入收件人姓名");
            return false;
        }
        return true;
    }

    private String readRaw(int rawId) {
        String content = "";
        InputStream is = getResources().openRawResource(rawId);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String temp = null;
            BufferedReader breader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            while ((temp = breader.readLine()) != null) {
                stringBuilder.append(temp);
            }
            content = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public boolean isShengNei(String json, String address) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String name = object.getString("name");
                JSONArray aray = object.getJSONArray("area");
                if (address.contains(name)) {
                    return true;
                }
                for (int j = 0; j < aray.length(); j++) {
                    String name2 = aray.getString(j);
                    if (name2.contains("市")) {
                        if (address.contains(name2)) {
                            return true;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        printerAddress = spKF.getString(SettingActivity.PRINTERSERVER, "");
    }

    private boolean printKyYundan(String serverIP, String orderID, String goodInfos, String cardID, String
            payType, String counts, String printName, String destcode, String yundanType, String ifSign)
            throws IOException {
        long time1 = System.currentTimeMillis();
        String ip = "http://" + serverIP + ":8080";
        String urlCoding = "UTF-8";
        String strURL = ip + "/PrinterServer/KyPrintServlet?";
        strURL += "orderID=" + URLEncoder.encode(orderID,
                urlCoding);
        strURL += "&yundanType=" + URLEncoder.encode(yundanType,
                urlCoding);
        strURL += "&counts=" + URLEncoder.encode(counts,
                urlCoding);
        strURL += "&goodinfos=" + URLEncoder.encode(goodInfos,
                urlCoding);
        strURL += "&printer=" + URLEncoder.encode(printName,
                urlCoding);
        strURL += "&cardID=" + URLEncoder.encode(cardID,
                urlCoding);
        strURL += "&payType=" + URLEncoder.encode(payType,
                urlCoding);
        strURL += "&j_name=" + URLEncoder.encode(jName,
                urlCoding);
        strURL += "&j_phone=" + URLEncoder.encode(jTel,
                urlCoding);
        strURL += "&j_address=" + URLEncoder.encode(jAddress,
                urlCoding);
        strURL += "&destcode=" + URLEncoder.encode(destcode,
                urlCoding);
        strURL += "&d_name=" + URLEncoder.encode(dName,
                urlCoding);
        strURL += "&d_phone=" + URLEncoder.encode(dTel,
                urlCoding);
        strURL += "&d_address=" + URLEncoder.encode(dAddress,
                urlCoding);
        strURL += "&j_company=" + URLEncoder.encode(jComapany,
                urlCoding);
        strURL += "&d_company=" + URLEncoder.encode(dCompany,
                urlCoding);
        strURL += "&pid=" + URLEncoder.encode(pid,
                urlCoding);
        strURL += "&signreturn=" + URLEncoder.encode(ifSign,
                urlCoding);
        Log.e("zjy", "SetYundanActivity->printKyYundan(): StrUrl==" + strURL);
        try {
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
            double len = (double) (System.currentTimeMillis() - time1) / 1000;
            MyApp.myLogger.writeInfo("KY yundan" + orderID + "\ttime:" + len);
            if (!res.equals("ok")) {
                throw new IOException("打印返回异常,res=" + res);
            }
            return true;
        } catch (IOException e) {
            throw new IOException("连接打印服务器异常," + e.getMessage());
        }
    }

    @Override
    public void resultBack(String result) {
        getCameraScanResult(result);
    }

    @Override
    public void getCameraScanResult(String result, int code) {
        edMorePid.setText(result);
    }

    OrderRetInfo getOrderRetInfo(OrderInfo orderInfo) throws IOException {
        String errMsg = "";
        try {
            OrderRetInfo mInfo = new OrderRetInfo();
            com.alibaba.fastjson.JSONObject mObj = (com.alibaba.fastjson.JSONObject) com
                    .alibaba.fastjson.JSONObject.toJSON
                            (orderInfo);
            String newOrderJson = mObj.toString();
            Log.e("zjy", getClass() + "->run():newApi json ==" + newOrderJson);
            String newApiRes = SF_Server.PostDataOpenApiInfo(newOrderJson);
            Log.e("zjy", getClass() + "->run():newApi Res ==" + newApiRes);
            com.alibaba.fastjson.JSONObject mresJobj = com.alibaba.fastjson.JSONObject.parseObject
                    (newApiRes);
            int code = mresJobj.getIntValue("code");
            String msg = mresJobj.getString("msg");
            if (code == 10000) {
                if (mresJobj.containsKey("data")) {
                    com.alibaba.fastjson.JSONObject dataObj = mresJobj.getJSONArray("data")
                            .getJSONObject(0);
                    mInfo = com.alibaba.fastjson.JSONObject.parseObject(dataObj.toJSONString(),
                            OrderRetInfo.class);
//                    JSONObject mob = new JSONObject.wrap(mInfo);
                    return mInfo;
                } else {
                    throw new IOException("接口错误,返回数据为空");
                }
            } else {
                throw new IOException("接口错误,"+msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            errMsg = "io异常," + e.getMessage();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            errMsg = "xml," + e.getMessage();
        } catch (com.alibaba.fastjson.JSONException e) {
            e.printStackTrace();
            errMsg = "json异常," + e.getMessage();
        }
        throw new IOException(errMsg);
    }

    public void startOrderNew(final String goodInfos, final String cardID, final String
            payType, final String serverType, final String counts,
                              final String printName) {
        String ifSing = "0";
        if (cboSign.isChecked()) {
            ifSing = "1";
        }
        final String finalIfSing = ifSing;
        Runnable orderRun = new Runnable() {
            @Override
            public void run() {
                OrderInfo orderInfo = new OrderInfo();
                List<BillOrder> mBills = new ArrayList<>();
                BillOrder realBill = new BillOrder();
                //寄件人信息
                BillUserInfo jUser = new BillUserInfo();
                jUser.setAddress(jAddress);
                jUser.setPerson(jName);
                jUser.setMobile(jTel);
                jUser.setCompanyName(jComapany);
                //收件人信息
                BillUserInfo dUser = new BillUserInfo();
                dUser.setAddress(dAddress);
                dUser.setPerson(dName);
                dUser.setMobile(dTel);
                dUser.setCompanyName(dCompany);

                realBill.setPreWaybillDelivery(jUser);
                realBill.setPreWaybillPickup(dUser);

                //其他参数
                realBill.setCount(Integer.parseInt(counts));
                //                serverType
                //10-当天达
                int serviceMode = 20;
                switch (serverType) {
                    case "当天达":
                        serviceMode = 10;
                        break;
                    case "次日达":
                        serviceMode = 20;
                        break;
                    case "隔日达":
                        serviceMode = 30;
                        break;
                    case "陆运件":
                        serviceMode = 40;
                        break;
                    case "同城次日":
                        serviceMode = 50;
                        break;
                    case "次晨达":
                        serviceMode = 50;
                        break;
                    case "同城即日":
                        serviceMode = 70;
                        break;
                    case "航空件":
                        serviceMode = 80;
                        break;
                    case "早班件":
                        serviceMode = 90;
                        break;
                    case "中班件":
                        serviceMode = 100;
                        break;
                    case "晚班件":
                        serviceMode = 110;
                        break;
                    case "省内次日":
                        serviceMode = 160;
                        break;
                    case "省内即日":
                        serviceMode = 170;
                        break;
                    case "空运":
                        serviceMode = 210;
                        break;
                    case "专运":
                        serviceMode = 220;
                        break;
                    default:
                        break;
                }
                realBill.setServiceMode(serviceMode);
                int payMode = 10;
                if ("转第三方付款".equals(payType)) {
                    payMode = 30;
                } else if ("到付".equals(payType)) {
                    payMode = 20;
                }
                realBill.setPayMode(payMode);
                realBill.setGoodsType(goodInfos);
                String tempOrderId = pid + "_" + UploadUtils.getRandomNumber(6);

                realBill.setOrderId(tempOrderId);
                realBill.setPaymentCustomer(cardID);
                int setReceiptFlag = 20;
                if (finalIfSing.equals("1")) {
                    setReceiptFlag = 10;
                }
                realBill.setReceiptFlag(setReceiptFlag);
                realBill.setWaybillRemark("");
                mBills.add(realBill);

                orderInfo.setCustomerCode(KyExpressUtils.uuid);
                orderInfo.setPlatformFlag(KyExpressUtils.platformFlag);
                orderInfo.setOrderInfos(mBills);
                OrderRetInfo retInfo = null;

                String errMsg = "未知错误";
                int code = 1;
                try {
                     retInfo = getOrderRetInfo(orderInfo);
                } catch (IOException e) {
                    errMsg = e.getMessage();
                    e.printStackTrace();
                }
                if (retInfo != null) {
                    yundanID = retInfo.waybillNumber;
                    ddestcode = retInfo.areaCode;
                    dgoodInfos = goodInfos;
                    dcardID = cardID;
                    dpayType = payType;
                    dserverType = serverType;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvYundanID.setText("下单成功：" + yundanID);
                            btnRePrint.setEnabled(true);
                        }
                    });

                    boolean printOk = false;
                    try {
                        relateYdToDB(pid, yundanID, ddestcode, expressName);
                        printKyYundan(printerAddress, yundanID, dgoodInfos,
                                dcardID,
                                dpayType, counts,
                                dprintName,
                                ddestcode
                                , dserverType, finalIfSing);
                        code = 0;
                        errMsg = "打印成功";
                        if (isDiaohuo) {
                            MyApp.myLogger.writeInfo("ky diaohuo result" + printOk + ",pid=" +
                                    pid);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                showAlert(errMsg);
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
            }
        };
        TaskManager.getInstance().execute(orderRun);
    }

    public void startOrder(final String goodInfos, final String cardID, final String
            payType, final String serverType, final String counts,
                           final String printName) {
        String ifSing = "0";
        if (cboSign.isChecked()) {
            ifSing = "1";
        }
        final String finalIfSing = ifSing;
        Runnable orderRun = new Runnable() {
            @Override
            public void run() {
                //                20112320120
                YundanJson info = new YundanJson();
                info.col_018 = serverType;
                info.col_001 = jComapany;
                info.col_004 = jName;
                info.col_005 = smsNum;
                info.jjTelQH = "";
                info.col_003 = jTel;
                info.col_002 = jAddress;
                info.col_011 = "";
                info.col_006 = dCompany;
                info.col_010 = dName;
                info.col_007 = dAddress;
                info.col_008 = dTel;
                info.col_009 = dTel;
                info.sjTelQH = "";
                info.sjTelFJH = "";
                info.col_037 = cardID;
                info.col_013 = payType;
                info.col_019 = "托寄";
                info.col_028 = finalIfSing;
                info.col_021 = counts;
                //                info.col_033 = "";
                info.col_027 = "";
                info.payCardNo = cardID;
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
                        throw new IOException("下单错误:" + msg);
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
//                        String insertResult = insertYundanInfo(pid, receiveID, destcode, expressName);
//                        changeInsertState(insertResult, pid);
                        relateYdToDB(pid, yundanID, ddestcode, expressName);
                        boolean printOk = printKyYundan(printerAddress, yundanID, dgoodInfos, dcardID,
                                dpayType, counts,
                                dprintName,
                                ddestcode
                                , dserverType, finalIfSing);
                        if (isDiaohuo) {
                            MyApp.myLogger.writeInfo("ky diaohuo result" + printOk + ",pid=" + pid);
                        }
                        if (printOk) {
                            showAlert("打印成功");
                        } else {
                            showAlert("打印失败，打印出错！！");
                        }
                    } catch (IOException e) {
                        showAlert("打印出现错误" + getString(R.string.bad_connection));
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
            }
        };
        TaskManager.getInstance().execute(orderRun);
    }

    public void getDetailInfo(String pid) throws IOException {
        List<YundanDBData> yundanDBData = YunInfoTool.searchYundanDataByPID(pid);
        YundanDBData yundanDBData1 = yundanDBData.get(0);
        CPidData = yundanDBData1;
        jTel = yundanDBData1.getjTel();
        jName = yundanDBData1.getjName();
        jAddress = yundanDBData1.getjAddress();
        jComapany = yundanDBData1.getjComapany();
        payByWho = yundanDBData1.getPayByWho();
        dAddress = yundanDBData1.getdAddress();
        dTel = yundanDBData1.getdTel();
        dName = yundanDBData1.getdName();
        dCompany = yundanDBData1.getdCompany();
        pidNotes = yundanDBData1.getPidNotes();
        mHandler.sendEmptyMessage(MSG_GETINFO);
    }

    public void showAlert(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDg.setMessage(msg);
                DialogUtils.safeShowDialog(mContext, alertDg);
            }
        });
    }


    public void relateYdToDB(final String pid, String orderID, String destcode, String objtype) throws
            IOException, XmlPullParserException {
        String result = "";
        if (CheckUtils.isAdmin()) {
            result = "成功";
            Log.e("zjy", getClass() + "->relateYdToDB(): isAdmin res==" + result);
        }else{
            try {
                result = SF_Server.InsertBD_YunDanInfoOfType(pid, orderID, destcode, objtype);
            } catch (IOException e) {
                throw new IOException("关联接口异常，" + e.getMessage());
            }
            if (result.equals("")) {
                MyApp.myLogger.writeError(KyPrintAcitivity.class, getResources().getString(R.string
                        .error_soapobject) + pid +
                        "\t" + loginID);
            }
            Log.e("zjy", getClass() + "->relateYdToDB(): res==" + result);
        }
        final String finalResult = result;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if ("成功".equals(finalResult)) {
                    tvInsertState.setTextColor(Color.BLUE);
                    tvInsertState.setText("关联单号到" + pid + "成功");
                } else {
                    tvInsertState.setTextColor(Color.RED);
                    tvInsertState.setText("关联单号到" + pid + "失败！！！");
                }
            }
        });
        if (!"成功".equals(finalResult)) {
            throw new IOException("关联失败,ret=" + result);
        }
    }

    public void getYundanShixiao() {
        //获取数据库中跨越的时效类型
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                try {
                    String info = MartService.GetHYTypeInfo("4");
                    JSONObject rootObj = new JSONObject(info);
                    JSONArray array = rootObj.getJSONArray("表");
                    List<YundanShixiao> tempList = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject tempJson = array.getJSONObject(i);
                        String objName = tempJson.getString("objname");
                        String description = tempJson.getString("objexpress");
                        YundanShixiao shixiao = new YundanShixiao(objName, description);
                        tempList.add(shixiao);
                    }
                    mHandler.obtainMessage(MSG_SHIXIAO, tempList).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        TaskManager.getInstance().execute(mRun);
    }
}
