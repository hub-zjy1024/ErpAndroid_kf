package com.b1b.js.erpandroid_kf.yundan.sf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.PreChukuDetailActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;
import com.b1b.js.erpandroid_kf.activity.base.SunmiScanActivity;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.yundan.sf.entity.Cargo;
import com.b1b.js.erpandroid_kf.yundan.sf.entity.Province;
import com.b1b.js.erpandroid_kf.yundan.sf.entity.SFSender;
import com.b1b.js.erpandroid_kf.yundan.sf.sfutils.SFWsUtils;
import com.b1b.js.erpandroid_kf.yundan.utils.DHInfo;
import com.b1b.js.erpandroid_kf.yundan.utils.SavedYundanInfo;
import com.b1b.js.erpandroid_kf.yundan.utils.YunInfoTool;
import com.b1b.js.erpandroid_kf.yundan.utils.YundanDBData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.common.Myuuid;
import utils.framwork.DialogUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.SF_Server;

/**
 * 顺丰快递下单页
 */
public class SetYundanActivity extends SunmiScanActivity implements NoLeakHandler.NoLeakCallback{
    private List<Province> provinces;
    String jAddress;
    String payByWho;

    String jComapany = "";
    String jProvince = "";
    String jCity = "";
    String jCounty = "";
    String payType;
    String jTel;
    String jName;
    String dAddress;
    String dTel;
    String dName;
    String dCompany;
    String dProvince = "";
    String dCity = "";
    String dCounty = "";
    EditText edJPerson;
    EditText edJTel;
    EditText edJAddress;

    EditText eddPerson;
    EditText eddTel;
    EditText eddAddress;
    private int flag = 0;
    private String account = "";
    private String storageID;
    private String serverIP = "";
    Bitmap reviewBitmap;
    String dgoodInfos;
    String dcardID;
    String dpayPart;
    String dpayType;
    String dserverType;
    double dbaojia;
    String dprintName;
    String dhasE;
    String ddestcode;
    String dyundanType;
    private String desOrderid;
    private ArrayAdapter<String> printerAdapter;
    private List<String> spiItems;
    private ProgressDialog pd;
    private String pid;
    private List<Map<String, String>> addrList = new ArrayList<>();
    private final int MSG_GETINFO = 0;
    private static final int MSG_Print_STATE = 3;
    private static final int MSG_ERROR_ACCOUNT = 4;
    private static final int MSG_PRINT_OK = 5;
    private static final int MSG_RL_ERROR=6;
    private static final int MSG_RL_OK = 9;
    private static final int MSG_Print_ERROR = 10;
    private Spinner spiServerType;
    private final int ShengWaiIndex = 1;
    private final int TongchengIndex = 0;
    private final int ShengNei = 0;
    private SharedPreferences sp;

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
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GETINFO:
                flag = 1;
                tvPayBy.setText(payByWho);
                if (payByWho.equals("寄货方")) {
                    spiPayType.setSelection(0);
                } else if (payByWho.equals("收货方")) {
                    spiPayType.setSelection(1);
                }
                if (!isDiaohuo) {
                    edAccount.setText(account);
                    edJPerson.setText(jName);
                    edJAddress.setText(jAddress);
                    edJTel.setText(jTel);
                    eddTel.setText(dTel);
                    eddAddress.setText(dAddress);
                    eddPerson.setText(dName);
                    tvNote.setText(note);
                } else {
                    String saveAccount = sp.getString("diaohuoAccount", "");
                    edAccount.setText(saveAccount);
                }
//                String configJson = sp.getString(SettingActivity.CONFIG_JSON, "");
//                try {
//                    JSONObject obj = new JSONObject(configJson);
//                    String kfName = obj.getString(SettingActivity.NAME);
//                    if (kfName.equals("深圳")) {
//                        if (dAddress.contains("深圳")) {
//                            spiServerType.setSelection(TongchengIndex);
//                        } else {
//                            String json = readRaw(R.raw.json_gd);
//                            boolean shengNei = isShengNei(json, dAddress);
//                            if (shengNei) {
//                                spiServerType.setSelection(ShengNei);
//                            } else {
//                                spiServerType.setSelection(ShengWaiIndex);
//                            }
//                        }
//                    } else if (kfName.equals("北京中转库")) {
//                        String json = readRaw(R.raw.json_bj);
//                        if (dAddress.contains("北京")) {
//                            spiServerType.setSelection(TongchengIndex);
//                        } else {
//                            boolean shengNei = false;
//                            if (dAddress.contains("天津")) {
//                                shengNei = true;
//                            } else {
//                                shengNei = isShengNei(json, dAddress);
//                            }
//                            if (shengNei) {
//                                spiServerType.setSelection(ShengNei);
//                            } else {
//                                spiServerType.setSelection(ShengWaiIndex);
//                            }
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                break;
            case MSG_Print_STATE:
                dismissDialog(pd);
                AlertDialog.Builder builder2 = new AlertDialog.Builder
                        (mContext);
                builder2.setTitle("提示");
                int arg1 = msg.arg1;
                String alertmsg = "";
                if (arg1 == 0) {
                    alertmsg = "网络连接错误，下单失败！！！";
                    showMsgDialog(alertmsg);
                } else if (arg1 == 1) {
                    alertmsg ="打印出现错误，请重新打印！！！";
                    showMsgDialog(alertmsg);
                } else if (arg1 == 2) {
                    alertmsg = "插入单号信息失败，是否重试";
                    DialogUtils.getDialog(mContext).setMsg(alertmsg).setBtn1("是").setBtn1L(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd.setMessage("正在插入单号信息");
                            pd.show();
                            Runnable reInsertRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    reLatePidNew();
                                }
                            };
                            TaskManager.getInstance().execute(reInsertRunnable);
                        }
                    }).setBtn2("否").setBtn2L(null).create();
//                    builder2.setMessage("插入单号信息失败，是否重试");
//                    builder2.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            pd.setMessage("正在插入单号信息");
//                            pd.show();
//                            Runnable reInsertRunnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    reLatePidNew();
//                                }
//                            };
//                            TaskManager.getInstance().execute(reInsertRunnable);
//                        }
//                    });
//                    builder2.setNegativeButton("否", null);
//                    DialogUtils.safeShowDialog(mContext, builder2.create());
                }
                break;
            case MSG_Print_ERROR:
                dismissDialog(pd);
                String errMsg = msg.obj.toString();
                showMsgDialog(errMsg);
                break;
            case MSG_ERROR_ACCOUNT:
                dismissDialog(pd);
                String ecMsg = "月结账号获取失败，当前不可用寄付月结！！！";
                showMsgDialog(ecMsg);
                break;
            case MSG_PRINT_OK:
                dismissDialog(pd);
                Dialog spAlert1 = DialogUtils.getSpAlert(mContext,
                        "操作成功", "提示");
                DialogUtils.safeShowDialog(mContext, spAlert1);
                break;
            case MSG_RL_ERROR:
                dismissDialog(pd);
                Dialog spAlert = DialogUtils.getSpAlert(mContext,
                        "插入单号信息失败,请重新插入！！！", "提示");
                tvState.setText("关联运单号失败！！！");
                tvState.setTextColor(Color.RED);
                DialogUtils.safeShowDialog(mContext, spAlert);
                break;
            case MSG_RL_OK:
                dismissDialog(pd);
                btnReInsert.setBackgroundColor(Color.GRAY);
                tvState.setText("关联运单号成功：" + desOrderid);
                tvState.setTextColor(Color.GREEN);
                break;
        }
    }
    private Context mContext = this;
    Handler mhandler = new NoLeakHandler(this);
    private TextView tvPid;
    private String TYPE_210 = "210";
    private String TYPE_150 = "150";
    private Spinner spiPrinter;
    private EditText edBags;
    private TextView tvOrderID;
    private Button btnRePrint;
    private Button btnReInsert;
    private CheckBox cboTest;
    private RadioButton rdo210;
    private String mYundanType;
    private boolean isDiaohuo = false;
    private Spinner spiDiaohuo;
    private TextView tvState;
    private TextView tvPayBy;
    private Spinner spiPayType;
    private EditText edAccount;
    private EditText edMorePid;
    private String note;
    private TextView tvNote;

    public void dismissDialog(Dialog mDiaog) {
        if (mDiaog != null) {
            mDiaog.dismiss();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoftKeyboardUtils.hideKeyBoard(this);
        setContentView(R.layout.activity_set_yundan);
        //        Spinner spiProvince = (Spinner) findViewById(R.id
        //                .activity_set_yundan_spi_province);
        //        final Spinner spiCity = (Spinner) findViewById(R.id
        //                .activity_set_yundan_spi_city);
        //        final Spinner spiPart = (Spinner) findViewById(R.id
        //                .activity_set_yundan_spi_district);
        //        新疆维吾尔自治区伊犁哈萨克自治州察布查尔锡伯自治县
        final Button btn210 = (Button) findViewById(R.id
                .activity_set_yundan_btnCommit);
        final Spinner spiBags = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_bags);
        final Button btn150 = (Button) findViewById(R.id
                .activity_set_yundan_btnCommit1);
        btnRePrint = (Button) findViewById(R.id
                .activity_set_yundan_btnReprint);
        final Button btnReview = (Button) findViewById(R.id
                .activity_set_yundan_btnReview);
        final ImageView iv = (ImageView) findViewById(R.id
                .activity_set_yundan_iv);
        spiPayType = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_paytype);
        spiServerType = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_servetype);
        spiPrinter = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_printer);
        final CheckBox boxBaojia = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_baojia);
        final CheckBox boxDuanxin = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_duanxin);
        final CheckBox boxEsign = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_esign);
        final Button btnChukudan = (Button) findViewById(R.id
                .activity_set_yundan_btn_printchukudan);
        final Button btnScan = (Button) findViewById(R.id
                .activity_set_yundan_btn_scan);
        final Button btnAddmore = (Button) findViewById(R.id
                .activity_set_yundan_btn_addmore);
        edMorePid = (EditText) findViewById(R.id
                .activity_set_yundan_ed_morepid);

        cboTest = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbotest);
        spiDiaohuo = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_diaohuo);
        final LinearLayout diaohuoContainer = (LinearLayout) findViewById(R.id
                .activity_set_yundan_container_diaohuo);

        tvOrderID = (TextView) findViewById(R.id
                .activity_set_yundan_tv_orderid);
        tvState = (TextView) findViewById(R.id
                .activity_set_yundan_tv_insertinfo_state);
        rdo210 = (RadioButton) findViewById(R.id
                .activity_set_yundan_rdo_210);

        final CheckBox boxTakepic = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_takepic);
        final CheckBox cboAddMore = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_addmore);

        final LinearLayout moreContainer = (LinearLayout) findViewById(R.id
                .activity_set_yundan_container_addmore);

        final EditText edBaojia = (EditText) findViewById(R.id
                .activity_set_yundan_ed_baojia);
        edJPerson = (EditText) findViewById(R.id
                .activity_set_yundan_ed_j_person);
        edAccount = (EditText) findViewById(R.id
                .activity_set_yundan_ed_account);
        edJTel = (EditText) findViewById(R.id
                .activity_set_yundan_ed_j_tel);
        edJAddress = (EditText) findViewById(R.id
                .activity_set_yundan_ed_j_addresss);
        eddPerson = (EditText) findViewById(R.id
                .activity_set_yundan_ed_d_person);
        eddTel = (EditText) findViewById(R.id
                .activity_set_yundan_ed_d_tel);
        eddAddress = (EditText) findViewById(R.id
                .activity_set_yundan_ed_d_address);
        edBags = (EditText) findViewById(R.id
                .activity_set_yundan_ed_bags);

        tvPid = (TextView) findViewById(R.id
                .activity_set_yundan_tv_pid);
        tvNote = (TextView) findViewById(R.id
                .activity_set_yundan_note);
        tvPayBy = (TextView) findViewById(R.id
                .activity_set_yundan_tv_payby);
        btnReInsert = (Button) findViewById(R.id
                .activity_set_yundan_btnReInsert);
        btnChukudan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PreChukuDetailActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
            }
        });
        btnAddmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String targetPid = edMorePid.getText().toString().trim();
                if (targetPid.equals("")) {
                   showMsgToast( "请输入单据号");
                } else {
                    if (desOrderid == null) {
                       showMsgToast( "请先下单");
                        return;
                    }
                    pd.setMessage("正在关联其他单据号");
                    pd.show();
                    Runnable insertMoreRunnable=new Runnable() {
                        @Override
                        public void run() {
                            String msg = "";
                            try {
                                String ok = insertYundanInfo(targetPid, desOrderid, ddestcode);
                                if ("成功".equals(ok)) {
                                    msg = "关联单号到 " +
                                            "" + targetPid +
                                            "成功";
                                } else {
                                    throw new Exception("关联其他单号失败,返回="+ok);
                                }
                            } catch (IOException e) {
                                final String message = e.getMessage();
                                msg = "io异常，" + message;
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                msg = "xml异常，" + e.getMessage();
                                e.printStackTrace();
                            } catch (Exception e) {
                                msg = "其他异常，" + e.getMessage();
                                e.printStackTrace();
                            }
                            final String finalMsg = msg;
                            mhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (pd != null) {
                                        pd.cancel();
                                    }
                                    showMsgDialog(finalMsg);
                                }
                            });
                        }
                    };
                    TaskManager.getInstance().execute(insertMoreRunnable);
                }
            }
        });
        cboAddMore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    moreContainer.setVisibility(View.VISIBLE);
                } else {
                    moreContainer.setVisibility(View.GONE);
                }
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        final Intent intent = getIntent();
        pid = intent.getStringExtra(SettingActivity.extra_PID );
        tvPid.setText(pid);
        String sendFlag = intent.getStringExtra("type");
        if ("2".equals(sendFlag)) {
            isDiaohuo = true;
        }
        if (CheckUtils.isAdmin()) {
            cboTest.setVisibility(View.VISIBLE);
        } else {
            cboTest.setVisibility(View.GONE);
        }
        ArrayList<String> diaohuoList = new ArrayList<String>();
        diaohuoList.add("请-->选择调货方向");
        diaohuoList.add("北京中转-->深圳市福田区");
        diaohuoList.add("北京中转-->深圳市龙岗区");
        diaohuoList.add("北京中转-->上海");
        diaohuoList.add("北京中转-->香港");
//        spiDiaohuo.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province, R.id
//                .item_province_tv, diaohuoList));
        final List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> addressMap = new HashMap<String, String>();
        addressMap.put("name", "王朋");
        addressMap.put("key", "北京中转");
        addressMap.put("phone", "010-62105503");
        addressMap.put("address", "北京市海淀区知春路108号豪景大厦C座1503");
        list.add(addressMap);
        addressMap = new HashMap<String, String>();
        addressMap.put("name", "商庆房");
        addressMap.put("key", "深圳市龙岗区");
        addressMap.put("phone", "0755-83764658");
        addressMap.put("address", "深圳市龙岗区吉华路393号英达丰科技园");
        list.add(addressMap);
        addressMap = new HashMap<String, String>();
        addressMap.put("name", "李娜");
        addressMap.put("key", "深圳市福田区");
        addressMap.put("phone", "0755-83764658");
        addressMap.put("address", "深圳市福田区中航路鼎城大厦1920室");
        list.add(addressMap);
        addressMap = new HashMap<String, String>();
        addressMap.put("name", "王永松");
        addressMap.put("key", "上海");
        addressMap.put("phone", "021-61170776");
        addressMap.put("address", "上海市宝山区城银路518号A座2楼201室");
        list.add(addressMap);
        addressMap = new HashMap<String, String>();
        addressMap.put("name", "盧少欣");
        addressMap.put("key", "香港");
        addressMap.put("phone", "852-34264941");
        addressMap.put("address", "香港九龙官塘鸿图道76号联运工业大厦2楼B室");
        list.add(addressMap);
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
        if (isDiaohuo) {
            flag = 1;
            diaohuoContainer.setVisibility(View.VISIBLE);
        } else {
            diaohuoContainer.setVisibility(View.GONE);
        }
        btnReInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desOrderid == null) {
                   showMsgToast( "还未下单");
                    return;
                }
                pd.setMessage("正在重新插入单号信息");
                pd.show();
                Runnable reInsertRunnable=new Runnable() {
                    @Override
                    public void run() {
                        reLatePidNew();
                    }
                };
                TaskManager.getInstance().execute(reInsertRunnable);
            }
        });
        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在打印中");
        spiBags.setAdapter(new ArrayAdapter<String>(this, R.layout.lv_item_printer, R
                .id.spinner_item_tv, new String[]{"1", "2", "3", "4",}));
        spiBags.setVisibility(View.GONE);
        sp = getSharedPreferences(SettingActivity.PREF_KF, MODE_PRIVATE);
        serverIP = sp.getString(SettingActivity.PRINTERSERVER, "");
        boxBaojia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener
                () {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edBaojia.setVisibility(View.VISIBLE);
                } else {
                    edBaojia.setVisibility(View.GONE);
                }
            }
        });
        spiItems = new ArrayList<>();
        printerAdapter = new ArrayAdapter<String>(this, R.layout.item_province, R.id
                .item_province_tv, spiItems);
        spiPrinter.setAdapter(printerAdapter);

        final String[] pay = new String[]{"寄付月结", "到方付", "第三方付"};

        spiPayType.setAdapter(new ArrayAdapter<>(this, R.layout.item_province, R.id
                .item_province_tv, pay));

        String[] serverTypes = new String[]{"2-顺丰隔日(陆)", "1-顺丰次日(空)", "5-顺丰次晨",
                "6-顺丰即日", "7-物流普运", "18-重货快运"};
        spiServerType.setAdapter(new ArrayAdapter<>(this, R.layout.item_province,
                R.id.item_province_tv, serverTypes));

        Runnable getSFinfoRunnable=new Runnable() {
            @Override
            public void run() {
                long time1 = System.currentTimeMillis();
                String client = intent.getStringExtra("client");
                try {
                    String result = getSFClientInfo(client);
                    JSONObject address = new JSONObject(result);
                    JSONArray aJarray = address.getJSONArray("表");
                    for (int i = 0; i < aJarray.length(); i++) {
                        JSONObject obj = aJarray.getJSONObject(i);
                        dProvince = obj.getString("Province");
                        dCity = obj.getString("City");
                        dCounty = obj.getString("County");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                long time2= System.currentTimeMillis();

//                Log.e("zjy", "SetYundanActivity->run(): Times_getSFClientInfo==" + (time2 - time1) / 1000f);

                String pid = tvPid.getText().toString();
                String msg = "";
                try {
                    SavedYundanInfo onlineSavedYdInfo = YunInfoTool.getSaveYundanInfo(pid);
                    if (onlineSavedYdInfo == null) {
                        throw new JSONException("下单信息为空");
                    }
                    ddestcode = onlineSavedYdInfo.getDestcode();
                    desOrderid = onlineSavedYdInfo.getOrderID();
                    final String  exName = onlineSavedYdInfo.getExName();
                    msg = "当前单据已有单号：" +exName+ desOrderid;
                    //                    String result = YunInfoTool.getOnlineSavedYdInfo(pid);
                    //                    Log.e("zjy", "SetYundanActivity->run(): onlineYundan==" + result);
                    //                    //                    "objid":"613","parentid":"0","objname":"1176338","objvalue":"616606640489",
                    //                    // "objtype":"顺丰","objexpress":"010",
                    //                    JSONObject obj = new JSONObject(result);
                    //                    JSONArray root = obj.getJSONArray("表");
                    //                    if (root.length() > 0) {
                    //                        JSONObject t = root.getJSONObject(0);
                    //                        String orderID = t.getString("objvalue");
                    //                        String destcode = t.getString("objexpress");
                    //                        final String exName = t.getString("objtype");
                    //                        ddestcode = destcode;
                    //                        desOrderid = orderID;
                    //                        mhandler.post(new Runnable() {
                    //                            @Override
                    //                            public void run() {
                    //                                tvOrderID.setText("当前单据已有单号：" +exName+ desOrderid);
                    //                                btnRePrint.setEnabled(true);
                    //                            }
                    //                        });
                    //                    }
                } catch (IOException e) {
                    msg = "查询关联单号失败，请重新进入";
                    e.printStackTrace();
                } catch (JSONException e) {
                    msg = "还未下单，请下单";
                    e.printStackTrace();
                }
                final String finalMsg = msg;
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (desOrderid != null && !desOrderid.equals("")) {
                            btnRePrint.setEnabled(true);
                        }
                        tvOrderID.setText(finalMsg);
                    }
                });
                long time3= System.currentTimeMillis();
//                Log.e("zjy", "SetYundanActivity->run(): Times_getSaveYundanInfo==" + (time3 - time2) / 1000f);

                try {
                    List<YundanDBData> yundanDBData = YunInfoTool.searchYundanDataByPID(pid);
                    YundanDBData yundanDBData1 = yundanDBData.get(0);
                    jTel = yundanDBData1.getjTel();
                    jName =yundanDBData1.getjName();
                    jAddress = yundanDBData1.getjAddress();
                    jComapany =yundanDBData1.getjComapany();
                    payByWho = yundanDBData1.getPayByWho();

                    dAddress =yundanDBData1.getdAddress();
                    dTel = yundanDBData1.getdTel();
                    dName =yundanDBData1.getdName();
                    note = yundanDBData1.getPidNotes();
                    dCompany =yundanDBData1.getdCompany();
                    payType =yundanDBData1.getPayByWho();
                    String corpID = yundanDBData1.getCorpID();
                    storageID = yundanDBData1.getStorageID();
                    if (jAddress.contains("北京市")) {
                        jProvince = "北京市";
                        jCity = "北京市";
                        jCounty = "海淀区";
                    } else if (jAddress.contains("深圳")) {
                        jProvince = "广东省";
                        jCity = "深圳市";
                        if (jAddress.contains("福田")) {
                            jCounty = "福田区";
                        } else if (jAddress.contains("龙岗")) {
                            jCounty = "龙岗区";
                        }
                    } else {
                        jProvince = "";
                        jCity = "";
                        jCounty = "";
                    }
                    account = getAccoutByCorpID(corpID);
                    if (account.equals("")) {
                        MyApp.myLogger.writeBug("SF account unknow");
                    }
                    //                    account = "9999999999";
                    mhandler.sendEmptyMessage(MSG_GETINFO);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long time4= System.currentTimeMillis();
//                Log.e("zjy", "SetYundanActivity->run(): addressTimes==" + (time4 - time3) / 1000f);

                if (isDiaohuo) {
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
                            titles.add(from + "-->" + to);
                            addrList.add(map);
                        }
                        final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, R.layout
                                .item_province,
                                R.id.item_province_tv, titles);
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                spiDiaohuo.setAdapter(adapter);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    long time5= System.currentTimeMillis();
//                    Log.e("zjy", "SetYundanActivity->run(): dhTimes==" + (time5 - time4) / 1000f);
                }
            }
        };
        TaskManager.getInstance().execute(getSFinfoRunnable);
        Runnable getPrintRunnable = new Runnable() {
            @Override
            public void run() {
                String ip = "http://" + serverIP + ":8080";
                String urlPrinter = ip + "/PrinterServer/GetPrinterInfoServlet";
                try {
                    URL url = new URL(urlPrinter);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    InputStream in = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                    BufferedReader bis = new BufferedReader(reader);
                    String s = "";
                    String result = "";
                    while ((s = bis.readLine()) != null) {
                        result += s;
                    }
                    Log.d("zjy", "SetYundan->run():com.b1b.js.erpandroid_kf.printer: reuslt=="
                            + result);
                    if (!result.equals("")) {
                        String[] printers = result.split(",");
                        for (String p : printers) {
                            if (p.equals("SF_Printer")) {
                                spiItems.add(0, p);
                            } else {
                                spiItems.add(p);
                            }
                        }
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                printerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog(pd);
                            Dialog spAlert1 = DialogUtils.getSpAlert(mContext,
                                    "打印机地址有误，请重新配置", "提示");
                            DialogUtils.safeShowDialog(mContext, spAlert1);
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(getPrintRunnable);
//        Log.e("zjy", "SetYundanActivity->onCreate(): goodInfos==" + intent
//                .getStringExtra("goodInfos"));
        btnRePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desOrderid != null && dyundanType == null) {
                    if (flag != 1) {
                          showMsgToast("获取寄送信息失败，请稍等或返回重新进入");
                        return;
                    }

                    if (boxBaojia.isChecked()) {
                        String strBaojia = edBaojia.getText().toString();
                        if (strBaojia.equals("")) {
                              showMsgToast("必须输入保价金额");
                            return;
                        }
                    }
                    jName = edJPerson.getText().toString();
                    jAddress = edJAddress.getText().toString();
                    jTel = edJTel.getText().toString();
                    dName = eddPerson.getText().toString();
                    dAddress = eddAddress.getText().toString();
                    dTel = eddTel.getText().toString();
                    account = edAccount.getText().toString().trim();
                    final String bags = edBags.getText().toString().trim();
                    if (bags.equals("")) {
                        showMsgToast("请输入包裹数");
                        return;
                    }
                    if (jTel.equals("")) {
                          showMsgToast("必须输入寄件人电话");
                        return;
                    }
                    if (jAddress.equals("")) {
                          showMsgToast("必须输入寄件人地址");
                        return;
                    }
                    if (jName.equals("")) {
                          showMsgToast("必须输入寄件人姓名");
                        return;
                    }
                    if (dTel.equals("")) {
                          showMsgToast("必须输入收件人电话");
                        return;
                    }
                    if (dAddress.equals("")) {
                          showMsgToast("必须输入收件人地址");
                        return;
                    }
                    if (dName.equals("")) {
                          showMsgToast("必须输入收件人姓名");
                        return;
                    }
                    String printType = TYPE_210;
                    if (!rdo210.isChecked()) {
                        printType = TYPE_150;
                    }
                    String goodInfos = "url1-500,url2-6000,url3-700";
                    goodInfos = intent.getStringExtra("goodInfos");
                    String cardID = "";
                    String payPart = "";
                    String payType = spiPayType.getSelectedItem().toString();
                    String serverType = "标准快递";
                    String stype = spiServerType.getSelectedItem().toString();
                    serverType = stype.split("-")[1];
                    String serverID = stype.split("-")[0];
                    double baojia = 12050;
                    String strBaojia = edBaojia.getText().toString();
                    Object selectP = spiPrinter.getSelectedItem();
                    String printName = "";
                    String hasE = "0";
                    if (serverID.equals("2")) {
                        hasE = "1";
                        serverType = "顺丰隔日";
                    } else if (serverID.equals("1")) {
                        serverType = "顺丰次日";
                    }
                    if (selectP != null) {
                        printName = selectP.toString();
                    }
                    if (strBaojia.equals("")) {
                        baojia = -1;
                    } else {
                        baojia = Double.valueOf(strBaojia);
                    }
                    if (payType.equals("寄付月结")) {
                        if (account == null || "".equals(account)) {
                            mhandler.sendEmptyMessage(MSG_ERROR_ACCOUNT);
                            return;
                        }
                        if (!storageID.equals("102")) {
                            payPart = "0755BL";
                        } else {
                            cardID = account;
                        }
                    } else if (payType.equals("到方付")) {
                    } else if (payType.equals("第三方付")) {
                        payPart = "0755BL";
                    }
                    dgoodInfos = goodInfos;
                    dcardID = cardID;
                    dpayPart = payPart;
                    dpayType = payType;
                    dserverType = serverType;
                    dbaojia = baojia;
                    dprintName = printName;
                    dyundanType = printType;
                    dhasE = hasE;
                }
                pd.setMessage("正在打印中");
                pd.show();
                Runnable rePrintThread=new Runnable() {
                    @Override
                    public void run() {
                        Message message = mhandler.obtainMessage(MSG_Print_ERROR);
                        try {
                            boolean ok = startPrint(desOrderid, dgoodInfos, dcardID, dpayPart, dpayType, dserverType,
                                    dbaojia, dprintName,
                                    dhasE, ddestcode, dyundanType);
                            if (!ok) {
                                throw new IOException("打印返回异常false");
                            } else {
                                message.what = MSG_PRINT_OK;
                                message.sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            MyApp.myLogger.writeError(e, "SF_StartPrint Error:" + e.getMessage());
                            message.obj = e.getMessage();
                            mhandler.sendMessage(message);
                        }
                    }
                };
                TaskManager.getInstance().execute(rePrintThread, mContext);
            }
        });
        btn210.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ddestcode != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (mContext);
                    builder.setMessage("已经下单过了,是否重新下单");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String printType = TYPE_210;
                            if (!rdo210.isChecked()) {
                                printType = TYPE_150;
                            }
                            orderAndPrint(boxBaojia, edBaojia, intent, spiPayType,
                                    spiServerType,
                                    boxDuanxin, boxEsign, boxTakepic, edJAddress,
                                    edJPerson, edJTel, eddAddress, eddPerson, eddTel,
                                    printType);
                        }
                    });
                    builder.setNegativeButton("否", null);
                    builder.show();
                } else {
                    String printType = TYPE_210;
                    if (!rdo210.isChecked()) {
                        printType = TYPE_150;
                    }
                    orderAndPrint(boxBaojia, edBaojia, intent, spiPayType, spiServerType,
                            boxDuanxin, boxEsign, boxTakepic, edJAddress,
                            edJPerson, edJTel, eddAddress, eddPerson, eddTel, printType);
                }
                //                String times = intent.getStringExtra("times");
                //                checkPrintCouts(times, boxBaojia, edBaojia, intent, spiPayType, spiServerType, boxDuanxin,
                // boxEsign, boxTakepic);
            }
        });
        Runnable getOnlineInfo=new Runnable() {
            @Override
            public void run() {

            }
        };
        TaskManager.getInstance().execute(getOnlineInfo);
        //        spiProvince.setOnItemSelectedListener(new AdapterView
        // .OnItemSelectedListener() {
        //            @Override
        //            public void onItemSelected(AdapterView<?> parent, View view, int
        // position,
        //                                       long id) {
        //                Province item = (Province) parent.getItemAtPosition(position);
        //                List<City> list = new ArrayList<City>();
        //                try {
        //                    StringBuilder builder = getJsonFromFile(R.raw.city);
        //                    JSONArray object = new JSONArray(builder.toString());
        //                    for (int i = 0; i < object.length(); i++) {
        //                        JSONObject tempObj = object.getJSONObject(i);
        //                        String parentCode = tempObj.getString("parent_code");
        //                        String code = tempObj.getString("code");
        //                        String name = tempObj.getString("name");
        //                        if (parentCode.equals(item.code)) {
        //                            City tempCity = new City();
        //                            if (name.equals("市辖区")) {
        //                                name = item.name;
        //                            }
        //                            tempCity.name = name;
        //                            tempCity.code = code;
        //                            list.add(tempCity);
        //                        }
        //                        if (list.size() == 0) {
        //                            List<String> partList = new ArrayList<String>();
        //                            spiPart.setAdapter(new ArrayAdapter<String>
        //                                    (mContext,
        //                                            R.layout.item_province, R.id
        // .item_province_tv,
        //                                            partList));
        //                        }
        //                    }
        //                } catch (UnsupportedEncodingException e) {
        //                    e.printStackTrace();
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                } catch (JSONException e) {
        //                    e.printStackTrace();
        //                }
        //                spiCity.setAdapter(new ArrayAdapter<City>(SetYundanActivity
        // .this,
        //                        R.layout.item_province, R.id.item_province_tv, list));
        //            }
        //
        //            @Override
        //            public void onNothingSelected(AdapterView<?> parent) {
        //
        //            }
        //        });
        //        spiCity.setOnItemSelectedListener(new AdapterView
        // .OnItemSelectedListener() {
        //            @Override
        //            public void onItemSelected(AdapterView<?> parent, View view, int
        // position,
        //                                       long id) {
        //                City item = (City) parent.getItemAtPosition(position);
        //                List<String> list = new ArrayList<String>();
        //                try {
        //                    StringBuilder builder = getJsonFromFile(R.raw.part);
        //                    JSONArray object = new JSONArray(builder.toString());
        //                    for (int i = 0; i < object.length(); i++) {
        //                        JSONObject tempObj = object.getJSONObject(i);
        //                        String parentCode = tempObj.getString("parent_code");
        //                        String code = tempObj.getString("code");
        //                        String name = tempObj.getString("name");
        //                        if (parentCode.equals(item.code)) {
        //                            list.add(name);
        //                        }
        //                    }
        //                } catch (UnsupportedEncodingException e) {
        //                    e.printStackTrace();
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                } catch (JSONException e) {
        //                    e.printStackTrace();
        //                }
        //                spiPart.setAdapter(new ArrayAdapter<String>(SetYundanActivity
        // .this,
        //                        R.layout.item_province, R.id.item_province_tv, list));
        //            }
        //
        //            @Override
        //            public void onNothingSelected(AdapterView<?> parent) {
        //
        //            }
        //        });
//        InputStream in = getResources().openRawResource(R.raw.province);
//        provinces = new ArrayList<>();
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in,
//                    "UTF-8"));
//            StringBuilder builder = new StringBuilder();
//            String temp = "";
//            while ((temp = reader.readLine()) != null) {
//                builder.append(temp);
//            }
//            JSONArray object = new JSONArray(builder.toString());
//            for (int i = 0; i < object.length(); i++) {
//                JSONObject tempObj = object.getJSONObject(i);
//                String code = tempObj.getString("code");
//                String name = tempObj.getString("name");
//                Province p1 = new Province();
//                p1.name = name;
//                p1.code = code;
//                provinces.add(p1);
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        //        spiProvince.setAdapter(new ArrayAdapter<>(this, R.layout.item_province,
        //                R.id.item_province_tv, provinces));

    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void resultBack(String result) {
        getCameraScanResult(result);
    }

    @Override
    public void setListeners() {

    }
    //    GetBD_YunDanInfoByID

    @Override
    public void getCameraScanResult(String result, int code) {
        edMorePid.setText(result);
    }


    private void checkPrintCouts(String times, final CheckBox boxBaojia, final EditText edBaojia, final Intent intent, final
    Spinner spiPayType, final Spinner spiServerType, final CheckBox boxDuanxin, final CheckBox boxEsign, final CheckBox
                                         boxTakepic) {

        if (times.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("已经打印过了,是否继续打印");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String printType = TYPE_210;
                    if (!rdo210.isChecked()) {
                        printType = TYPE_150;
                    }
                    orderAndPrint(boxBaojia, edBaojia, intent, spiPayType,
                            spiServerType,
                            boxDuanxin, boxEsign, boxTakepic, edJAddress,
                            edJPerson, edJTel, eddAddress, eddPerson, eddTel,
                            printType);
                }
            });
            builder.setNegativeButton("否", null);
            builder.show();
        } else {
            String printType = TYPE_210;
            if (!rdo210.isChecked()) {
                printType = TYPE_150;
            }
            orderAndPrint(boxBaojia, edBaojia, intent, spiPayType, spiServerType,
                    boxDuanxin, boxEsign, boxTakepic, edJAddress,
                    edJPerson, edJTel, eddAddress, eddPerson, eddTel, printType);
        }
    }

    private void orderAndPrint(final CheckBox boxBaojia, final EditText edBaojia, final
    Intent intent, final Spinner spiPayType, final Spinner spiServerType, final
                               CheckBox boxDuanxin, final CheckBox boxEsign, final
                               CheckBox boxTakepic, final
                               EditText edJAddress, final
                               EditText edJPerson, final
                               EditText edJTel, final
                               EditText edDAddress, final
                               EditText edDPerson, final
                               EditText edDTel, final String yundanType) {

        if (flag != 1) {
            showMsgToast( "获取寄送信息失败，请稍等或返回重新进入");
            return;
        }

        if (boxBaojia.isChecked()) {
            String strBaojia = edBaojia.getText().toString();
            if (strBaojia.equals("")) {
                showMsgToast( "必须输入保价金额");
                return;
            }
        }
        jName = edJPerson.getText().toString();
        jAddress = edJAddress.getText().toString();
        jTel = edJTel.getText().toString();
        dName = edDPerson.getText().toString();
        dAddress = edDAddress.getText().toString();
        dTel = edDTel.getText().toString();
        final String bags = edBags.getText().toString().trim();
        if (bags.equals("")) {
            showMsgToast( "请输入包裹数");
            return;
        }
        if (jTel.equals("")) {
            showMsgToast( "必须输入寄件人电话");
            return;
        }
        if (jAddress.equals("")) {
            showMsgToast( "必须输入寄件人地址");
            return;
        }
        if (jName.equals("")) {
            showMsgToast( "必须输入寄件人姓名");
            return;
        }
        if (dTel.equals("")) {
            showMsgToast( "必须输入收件人电话");
            return;
        }
        if (dAddress.equals("")) {
            showMsgToast( "必须输入收件人地址");
            return;
        }
        if (dName.equals("")) {
            showMsgToast( "必须输入收件人姓名");
            return;
        }
        pd.setMessage("正在打印中");
        pd.show();
        Runnable orderRunnable=new Runnable() {
            @Override
            public void run() {
                String orderID = "666655554444";
                String goodInfos = "url1-500,url2-6000,url3-700";
                goodInfos = intent.getStringExtra("goodInfos");
                String cardID = "";
                String payPart = "";
                int okCounts = 0;
                String payType = spiPayType.getSelectedItem().toString();
                String serverType = "标准快递";
                String stype = spiServerType.getSelectedItem().toString();
                serverType = stype.split("-")[1];
                String serverID = stype.split("-")[0];
                double baojia = 12050;
                String strBaojia = edBaojia.getText().toString();
                Object selectP = spiPrinter.getSelectedItem();
                String printName = "";
                String hasE = "0";
                account = edAccount.getText().toString().trim();
                if (serverID.equals("2")) {
                    hasE = "1";
                    serverType = "顺丰隔日";
                } else if (serverID.equals("1")) {
                    serverType = "顺丰次日";
                }
                if (selectP != null) {
                    printName = selectP.toString();
                }
                if (strBaojia.equals("")) {
                    baojia = -1;
                } else {
                    baojia = Double.valueOf(strBaojia);
                }


                    SFSender order = new SFSender();
                    order.orderID = tvPid.getText().toString() + Myuuid.create2(5);
                    order.j_name = jName;
                    order.j_tel = jTel;
                    order.j_address = jAddress;
                    order.j_company = jComapany;
                    order.j_province = jProvince;
                    order.j_city = jCity;
                    order.j_district = jCounty;

                    order.d_name = dName;
                    order.d_tel = dTel;
                    order.d_address = dAddress;
                    order.d_company = dCompany;

                    order.d_province = dProvince;
                    order.d_city = dCity;
                    order.d_district = dCounty;

                    order.expressType = serverID;
                    order.bagCounts = bags;
                    if (payType.equals("寄付月结")) {
                        if (account == null || "".equals(account)) {
                            mhandler.sendEmptyMessage(MSG_ERROR_ACCOUNT);
                            return;
                        }
                        order.payType = "1";
                        order.custid = account;
                        if (!storageID.equals("102")) {
                            order.payType = "3";
                            payPart = "0755BL";
                        } else {
                            cardID = account;
                            order.payType = "1";
                        }
                    } else if (payType.equals("到方付")) {
                        order.payType = "2";
                    } else if (payType.equals("第三方付")) {
                        order.payType = "3";
                        payPart = "0755BL";
                    }
                    String[] infos = goodInfos.split("\\$");
                    List<Cargo> cargos = new ArrayList<>();
                    for (String info : infos) {
                        String[] s = info.split("&");
                        if (s.length > 0) {
                            Cargo cargo = new Cargo();
                            cargo.setCount(s[1]);
                            cargo.setName(s[0]);
                            cargos.add(cargo);
                        }
                    }

                    //                    InsertBD_YunDanInfo
                    //                    manger.insertYundan(tvPid.getText().toString(), orderID, destcode, goodInfos, cardID,
                    // payPart, payType,
                    //                            serverType, baojia, printName, hasE, yundanType);
                    //                    startPostPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
                    //                            printName, hasE, destcode, yundanType);
                boolean test = cboTest.isChecked();
                String destcode = "110";
                Message message = mhandler.obtainMessage(MSG_Print_STATE);
                    try {
                        try{
                            if (!test) {
//                                String xml = SFWsUtils.createOrderXml(SFWsUtils.ORDER_SERVICE,
//                                        order, cargos, null);
//                                Map<String, String> resMap = getOrderResponse(xml);
//                                String head = resMap.get("head");
//                                if (head.equals("ERR")) {
//                                    String errmsg = resMap.get("error");
//                                    Message msg = mhandler.obtainMessage(1);
//                                    msg.obj = errmsg;
//                                    MyApp.myLogger.writeError(SetYundanActivity.class, resMap.get("errorCode") + "\t" + msg.obj + "\t" +
//                                            dProvince + "\t" + dCity + "\t" + dCounty +
//                                            "\t" + dAddress + "\t" + tvPid.getText().toString());
////                                    mhandler.sendMessage(msg);
//                                    throw new Exception("下单失败," + errmsg);
//                                }
//                                destcode = resMap.get("destcode");
//                                orderID = resMap.get("orderID");
                                SFWsUtils.OrderResponse orderResponse = SFWsUtils.getOrderResponse(order,
                                        cargos, null);
                                destcode = orderResponse.destcode;
                                orderID = orderResponse.yundanId;
                            }
                            final String finalOrderID = orderID;
                            desOrderid = orderID;
                            dgoodInfos = goodInfos;
                            dcardID = cardID;
                            dpayPart = payPart;
                            dpayType = payType;
                            dserverType = serverType;
                            dbaojia = baojia;
                            dprintName = printName;
                            dyundanType = yundanType;
                            dhasE = hasE;
                            ddestcode = destcode;
                            mhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvOrderID.setText("下单成功，返回单号：" + finalOrderID);
                                    btnRePrint.setEnabled(true);
                                }
                            });
                        }catch (IOException e){
                            throw new Exception("下单失败IO," + e.getMessage());
                        }
                        try {
                            String result = "";
                            if (test) {
                                result = "成功";
                            } else {
                                String pid = tvPid.getText().toString();
                                result = updatePrintCount(pid, desOrderid);
                                result = insertYundanInfo(pid, orderID, destcode);
                            }
                            if (isDiaohuo) {
                                MyApp.myLogger.writeInfo("SF diaohuo result" + result + ",pid=" + pid);
                            }
                            if (!result.equals("成功")) {
                                throw new Exception("关联失败," + result);
                            } else {
                                mhandler.sendEmptyMessage(MSG_RL_OK);
                            }
                        } catch (IOException e) {
                            throw new Exception("关联失败，re_io," + e.getMessage());
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        try {
                            boolean printOk = startPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
                                    printName, hasE, destcode, yundanType);
                            if (!printOk) {
                                MyApp.myLogger.writeError("print error" + tvPid.getText().toString());
                                throw new Exception("打印失败,ret=false");
                            } else {
                                message.what = MSG_PRINT_OK;
                                mhandler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            String exMsg = e.getMessage();
                            MyApp.myLogger.writeError(e, "SF_StartPrint Error:");
                            throw new Exception("打印失败io," +exMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        message.what = MSG_Print_ERROR;
                        message.obj = "错误," + e.getMessage();
                        mhandler.sendMessage(message);
                    }
            }
        };
        TaskManager.getInstance().execute(orderRunnable);
    }

    private String insertYundanInfo(String pid, String orderID, String destcode) throws IOException, XmlPullParserException {
        if (CheckUtils.isAdmin()) {
            return "成功";
        }
        String result = SF_Server.InsertBD_YunDanInfo(pid, orderID, destcode);
        Log.d("zjy", "SetYundanActivity->run(): insert Res==" + result);
        if (result.equals("成功")) {
            MyApp.myLogger.writeInfo("SFprint--insertYundanInfo:OK" + pid);
        }
        return result;
    }

    @NonNull
    private boolean startPrint(String orderID, String goodInfos, String cardID, String payPart, String payType, String
            serverType, double baojia, String printName, String hasE, String destcode, String yundanType) throws IOException {
        long time1 = System.currentTimeMillis();
        String ip = "http://" + serverIP + ":8080";
        String urlCoding = "UTF-8";
        String strURL = ip + "/PrinterServer/SFPrintServlet?";
        strURL += "orderID=" + URLEncoder.encode(orderID,
                urlCoding);
        strURL += "&hasE=" + URLEncoder.encode(hasE,
                urlCoding);
        strURL += "&yundanType=" + URLEncoder.encode(yundanType,
                urlCoding);
        strURL += "&goodinfos=" + URLEncoder.encode(goodInfos,
                urlCoding);
        strURL += "&printer=" + URLEncoder.encode(printName,
                urlCoding);

        strURL += "&cardID=" + URLEncoder.encode(cardID,
                urlCoding);
        strURL += "&baojiaprice=" + URLEncoder.encode(String
                        .valueOf(baojia),
                urlCoding);
        strURL += "&payPerson=" + URLEncoder.encode(payPart,
                urlCoding);
        strURL += "&payType=" + URLEncoder.encode(payType,
                urlCoding);
        strURL += "&serverType=" + URLEncoder.encode(serverType,
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
        Log.d("zjy", "SetYundanActivity->startPrint(): StrUrl==" + strURL);
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
        Log.d("zjy", "SetYundanActivity->run(): print_result==" + builder
                .toString());
        double len = (double) (System.currentTimeMillis() - time1) / 1000;
        MyApp.myLogger.writeInfo("SF yundan" + orderID + "\ttime:" + len);
        if (res.equals("ok")) {
            return true;
        } else {
            String[] errors = res.split(":");
            if (errors.length == 2) {
                String errMs = errors[1];
                throw new IOException(errMs);
            }
            return false;
        }
    }

    @NonNull
    private boolean startPostPrint(String orderID, String goodInfos, String cardID, String payPart, String payType, String
            serverType, double baojia, String printName, String hasE, String destcode, String yundanType) throws IOException {
        String ip = "http://" + serverIP + ":8080";
        String strURL = ip + "/PrinterServer/SFPrintServlet?";
        Log.d("zjy", "SetYundanActivity->startPrint(): StrUrl==" + strURL);
        URL url = new URL(strURL);
        HttpURLConnection conn = (HttpURLConnection) url
                .openConnection();
        String post = "POST";
        conn.setRequestMethod(post);
        conn.setDoOutput(true);
        conn.setConnectTimeout(20 * 1000);
        OutputStream out = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
        writer.write("orderID=" + orderID + "\n");
        writer.write("hasE=" + hasE + "\n");
        writer.write("yundanType=" + yundanType + "\n");
        writer.write("goodinfos=" + goodInfos + "\n");
        writer.write("cardID=" + cardID + "\n");
        writer.write("baojiaprice=" + baojia + "\n");
        writer.write("serverType=" + serverType + "\n");
        writer.write("payPerson=" + payPart + "\n");
        writer.write("payType=" + payType + "\n");
        writer.write("j_name=" + jName + "\n");
        writer.write("j_phone=" + jTel + "\n");
        writer.write("j_address=" + jAddress + "\n");
        writer.write("d_name=" + dName + "\n");
        writer.write("d_phone=" + dTel + "\n");
        writer.write("d_address=" + dAddress + "\n");
        writer.write("destcode=" + destcode + "\n");
        writer.write("com.b1b.js.erpandroid_kf.printer=" + printName + "\n");
        writer.flush();
        writer.close();
        StringBuilder builder = new StringBuilder();
        String s = "";
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(in, "UTF-8"));
        while ((s = reader.readLine()) != null) {
            builder.append(s);
        }
        String res = builder.toString();
        Log.d("zjy", "SetYundanActivity->run(): print_result==" + builder
                .toString());
        return res.equals("ok");
    }

    @NonNull
    private StringBuilder getJsonFromFile(int rawId) throws IOException {
        InputStream in = getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,
                "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String temp = "";
        while ((temp = reader.readLine()) != null) {
            builder.append(temp);
        }
        return builder;
    }


    public String getSFClientInfo(String clientID) throws IOException,
            XmlPullParserException {
        String detail = SF_Server.GetClientPCCInfo(clientID);
   //     Log.d("zjy", "SFActivity->getSFClientInfo(): clientDetail==" + detail);
        return detail;
    }

    public void reLatePidNew() {
        try {
            String pid = tvPid.getText().toString();
            String result = updatePrintCount(pid, desOrderid);
            result = insertYundanInfo(pid, desOrderid, ddestcode);
            if (result.equals("成功")) {
                mhandler.sendEmptyMessage(MSG_RL_OK);
            } else {
                throw new IOException("更新关联信息失败，" + result);
            }
        } catch (IOException e) {
            mhandler.sendEmptyMessage(MSG_RL_ERROR);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
    public String updatePrintCount(String pid, String orderID) throws IOException,
            XmlPullParserException {
        if (isDiaohuo) {
            MyApp.myLogger.writeInfo(SetYundanActivity.class, "下单调货：" + pid);
            return "成功";
        }
        String newOrder = orderID.replace(",", "/");
        String result = SF_Server.UpdateYunDanInfoByPrintCount(pid, newOrder);
        if ("成功".equals(result)) {
            MyApp.myLogger.writeInfo("SFprint--updatePrintCount:OK" + pid);
        }
        return result;
    }

    public String getAccoutByCorpID(String corpID) throws IOException,
            XmlPullParserException {
        //        expressName定为：顺风
        //                corpID是开票公司ID
        int id = 0;
        try {
            id = Integer.parseInt(corpID);
        } catch (Exception e) {
            Log.d("zjy", "SetYundanActivity->getAccoutByCorpID() Error-- corpId==" + corpID);
            throw new IOException("corpId不为数字", e);
        }
        String result = SF_Server.GetCorpExpressAccountNo("顺丰", id);
        //Log.d("zjy", "SetYundanActivity->getAccoutByCorpID(): account==" + result);
        return result;
    }
}
