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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private List<Map<String,String>> addrList;
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
    private SharedPreferences spKF;
    private String kfName = "";
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
        spiDiaohuo = (Spinner) findViewById(R.id.yundanprint_spi_printer);
        LinearLayout llDiaohuo = (LinearLayout) findViewById(R.id.yundanprint_ll_diaohuo);
        final String[] serverTypes = new String[]{"陆运件-普", "同城即日-省内", "同城次日-省内", "隔日达-快（空）", "次日达-很快（空）", "当天达-极快（空）"};
        spiType.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province, R.id.item_province_tv, serverTypes));
        final String[] payTypes = new String[]{"寄付月结", "到付"};
        spiPayType.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province, R.id.item_province_tv, payTypes));
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
                Runnable addMordRunnable= new Runnable() {
                    @Override
                    public void run() {
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
            Log.e("zjy", "YundanPrintAcitivity->onCreate(): configJson==" + configJson);
            JSONObject obj = new JSONObject(configJson);
             kfName = obj.getString(SettingActivity.NAME);
            KyExpressUtils.uuid = obj.getString(SettingActivity.KYUUID);
            KyExpressUtils.key = obj.getString(SettingActivity.KYKEY);
            edAccount.setText(obj.getString(SettingActivity.KYACCOUNT));
        } catch (JSONException e) {
            KyExpressUtils.uuid = "";
            KyExpressUtils.key = KyExpressUtils.uuid = "";
            edAccount.setText("");
            showAlert("请先配置库房信息");
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
                    String dhAddresss = getDHAddresss();
                    List<String> titles = new ArrayList<String>();
                    JSONObject addJObj = new JSONObject(dhAddresss);
                    JSONArray addTable = addJObj.getJSONArray("表");
                    titles.add("请-->选择调货方向");
                    for (int j = 0; j < addTable.length(); j++) {
                        JSONObject obj = addTable.getJSONObject(j);
                        String from = obj.getString("FromStorageID");
                        String to = obj.getString("ToStotageID");
                        if (kfName.equals("")) {
                            titles.add(from + "-->" + to);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("key1", from);
                            map.put("name1", obj.getString("FromName"));
                            map.put("phone1", obj.getString("FromPhone"));
                            map.put("address1", obj.getString("FromAddress"));
                            map.put("account", obj.getString("AccountNo"));
                            map.put("key2", to);
                            map.put("name2", obj.getString("ToName"));
                            map.put("phone2", obj.getString("ToPhone"));
                            map.put("address2", obj.getString("ToAddress"));
                            addrList.add(map);
                        } else if (kfName.equals(from)) {
                            titles.add(from + "-->" + to);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("key1", from);
                            map.put("name1", obj.getString("FromName"));
                            map.put("phone1", obj.getString("FromPhone"));
                            map.put("address1", obj.getString("FromAddress"));
                            map.put("account", obj.getString("AccountNo"));
                            map.put("key2", to);
                            map.put("name2", obj.getString("ToName"));
                            map.put("phone2", obj.getString("ToPhone"));
                            map.put("address2", obj.getString("ToAddress"));
                            addrList.add(map);
                        }
                    }
                    final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, R.layout.item_province,
                            R.id.item_province_tv, titles);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            spiDiaohuo.setAdapter(adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(dhRunnable);
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
                String printer = "";
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
                String goodInfos = reIntent.getStringExtra("goodInfos");
                final String tGoodInfos = goodInfos;
                final String tpayType = payType;
                final String tserverType = serverType;
                final String tCounts = counts;
                if(yundanID!=null){
                    DialogUtils.getSpAlert(mContext, "当前单据已有运单:" + yundanID + "，是否继续", "提示", new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd.setMessage("正在打印中");
                            pd.show();
                            startOrder(tGoodInfos, account, tpayType, tserverType, tCounts, dprintName);
                        }
                    }, "是", null, "否").show();
                }else{
                    pd.setMessage("正在打印中");
                    pd.show();
                    startOrder(goodInfos, account, payType, serverType, counts, dprintName);
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
                 Runnable rePrintRun=new Runnable() {
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
        final ArrayAdapter<String> printerAdapter = new ArrayAdapter<>(this, R.layout.item_province, R.id.item_province_tv,
                spiItems);
//        spiPrinter.setAdapter(printerAdapter);
      Runnable pdRun=  new Runnable() {
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
                    Log.e("zjy", "SetYundan->run():printer: reuslt=="
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
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    showAlert("数据解析出错：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(onLineRun);
        Runnable onlineSavedRunnable=new Runnable() {
            @Override
            public void run() {
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
        };
        TaskManager.getInstance().execute(onlineSavedRunnable);
    }
    private String getDHAddresss() throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        SoapObject req = WebserviceUtils.getRequest(map, "GetBD_DHAddress");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(req, WebserviceUtils.SF_SERVER);
        return response.toString();
    }
    @Override
    protected void onResume() {
        super.onResume();
        printerAddress = spKF.getString(SettingActivity.PRINTERSERVER, "");
    }

    @NonNull
    private boolean printKyYundan(String serverIP, String orderID, String goodInfos, String cardID, String
            payType, String counts, String printName, String destcode, String yundanType)
            throws IOException {
        long time1 = System.currentTimeMillis();
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
        strURL += "&j_company=" + URLEncoder.encode(jComapany,
                "UTF-8");
        strURL += "&d_company=" + URLEncoder.encode(dCompany,
                "UTF-8");
        strURL += "&pid=" + URLEncoder.encode(pid,
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
        double len = (double) (System.currentTimeMillis() - time1) / 1000;
        MyApp.myLogger.writeInfo("KY yundan" + orderID + "\ttime:" + len);
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
            String result = data.getStringExtra("result");
            edMorePid.setText(result);
        }
    }

    public void startOrder(final String goodInfos, final String cardID, final String
            payType, final String serverType, final String counts,
                           final String printName) {

        Runnable orderRun = new Runnable() {
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
                info.col_008 = dTel;
                info.col_009 = dTel;
                info.sjTelQH = "";
                info.sjTelFJH = "";
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
                        Log.e("zjy", "YundanPrintAcitivity->insertYundanInfo(): result==" + insertResult);
                        changeInsertState(insertResult, pid);
                        boolean printOk = printKyYundan(printerAddress, yundanID, dgoodInfos, dcardID, dpayType, counts,
                                dprintName,
                                ddestcode
                                , dserverType);
                        if (printOk) {
                            showAlert("打印成功");
                            if (isDiaohuo) {
                                MyApp.myLogger.writeInfo("ky diaohuo " + pid);
                            }
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
                mHandler.sendEmptyMessage(1);
            }
        };
        TaskManager.getInstance().execute(orderRun);
    }

    public void getDetailInfo(String pid) throws IOException, XmlPullParserException, JSONException {
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
    }
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
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(map,
                "InsertBD_YunDanInfoOfType", WebserviceUtils.SF_SERVER);
        String result = response.toString();
        if (result.equals("")) {
            MyApp.myLogger.writeError(YundanPrintAcitivity.class, getResources().getString(R.string.error_soapobject) + pid +
                    "\t" + MyApp.id);
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
