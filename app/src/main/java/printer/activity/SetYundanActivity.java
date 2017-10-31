package printer.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.PreChukuDetailActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import printer.entity.Cargo;
import printer.entity.Province;
import printer.entity.SFSender;
import printer.sfutils.Md5;
import printer.sfutils.SFWsUtils;
import printer.sfutils.XmlDomUtils;
import utils.DialogUtils;
import utils.MyToast;
import utils.Myuuid;
import utils.WebserviceUtils;
import utils.dbutils.MyDbManger;

public class SetYundanActivity extends AppCompatActivity {
    private List<Province> provinces;
    String jAddress;
    String payByWho;

    String jComapany = "";
    String jProvince = "";
    String jCity = "";
    String jCounty = "";
    MyDbManger manger = new MyDbManger(SetYundanActivity.this, "newyundan");
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
    private List<Map<String, String>> addrList = new ArrayList<>();

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
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
                    } else {
                        final SharedPreferences sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
                        String saveAccount = sp.getString("diaohuoAccount", "");
                        edAccount.setText(saveAccount);
                    }
                    break;
                case 1:
                    DialogUtils.dismissDialog(pd);
                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("下单失败:" + msg.obj.toString());
                    DialogUtils.safeShowDialog(SetYundanActivity.this, builder.create());
                    break;
                case 3:
                    DialogUtils.dismissDialog(pd);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder2.setTitle("提示");
                    int arg1 = msg.arg1;
                    if (arg1 == 0) {
                        builder2.setMessage("网络连接错误，下单失败！！！");
                        builder2.show();
                    } else if (arg1 == 1) {
                        builder2.setMessage("打印出现错误，请重新打印！！！");
                        builder2.show();
                    } else if (arg1 == 2) {
                        builder2.setMessage("插入单号信息失败，是否重试");
                        builder2.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pd.setMessage("正在插入单号信息");
                                pd.show();
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            String result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                                            result = insertYundanInfo(tvPid.getText().toString(), desOrderid, ddestcode);
                                            if (result.equals("成功")) {
                                                mhandler.sendEmptyMessage(9);
                                            } else {
                                                mhandler.sendEmptyMessage(6);
                                            }
                                        } catch (IOException e) {
                                            mhandler.sendEmptyMessage(6);
                                            e.printStackTrace();
                                        } catch (XmlPullParserException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });
                        builder2.setNegativeButton("否", null);
                        DialogUtils.safeShowDialog(SetYundanActivity.this, builder2.create());
                    }
                    break;
                case 4:
                    DialogUtils.dismissDialog(pd);
                    AlertDialog.Builder builder3 = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder3.setTitle("提示");
                    builder3.setMessage("月结账号获取失败，当前不可用寄付月结！！！");
                    DialogUtils.safeShowDialog(SetYundanActivity.this, builder3.create());
                    break;
                case 5:
                    DialogUtils.dismissDialog(pd);
                    Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                            "操作成功", "提示");
                    DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert1);
                    break;
                case 6:
                    DialogUtils.dismissDialog(pd);
                    Dialog spAlert = DialogUtils.getSpAlert(SetYundanActivity.this,
                            "插入单号信息失败,请重新插入！！！", "提示");
                    tvState.setText("关联运单号失败！！！");
                    tvState.setTextColor(Color.RED);
                    DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert);
                    break;
                case 7:
                    DialogUtils.dismissDialog(pd);
                    DialogUtils.safeShowDialog(SetYundanActivity.this, DialogUtils.getSpAlert(SetYundanActivity.this,
                            "网络质量较差，请重新尝试！！！", "提示"));
                    break;
                case 8:
                    DialogUtils.dismissDialog(pd);
                    DialogUtils.safeShowDialog(SetYundanActivity.this, DialogUtils.getSpAlert(SetYundanActivity.this,
                            "连接打印服务器失败，请重新尝试！！！", "提示"));
                    break;
                case 9:
                    DialogUtils.dismissDialog(pd);
                    btnReInsert.setBackgroundColor(Color.GRAY);
                    tvState.setText("关联运单号成功：" + desOrderid);
                    tvState.setTextColor(Color.GREEN);
                    break;
            }
        }
    };
    private TextView tvPid;
    private String corpID;
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

    //    6200151
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
        final Spinner spiServerType = (Spinner) findViewById(R.id
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
        tvPayBy = (TextView) findViewById(R.id
                .activity_set_yundan_tv_payby);
        btnReInsert = (Button) findViewById(R.id
                .activity_set_yundan_btnReInsert);
        btnChukudan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetYundanActivity.this, PreChukuDetailActivity.class);
                Intent lastIntent = getIntent();
                String pid = lastIntent.getStringExtra("pid");
                intent.putExtra("pid", pid);
                startActivity(intent);
            }
        });
        btnAddmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String targetPid = edMorePid.getText().toString().trim();
                if (targetPid.equals("")) {
                    MyToast.showToast(SetYundanActivity.this, "请输入单据号");
                } else {
                    if (desOrderid == null) {
                        MyToast.showToast(SetYundanActivity.this, "请先下单");
                        return;
                    }
                    pd.setMessage("正在关联其他单据号");
                    pd.show();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                String ok = insertYundanInfo(targetPid, desOrderid, ddestcode);
                                if ("成功".equals(ok)) {
                                    mhandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DialogUtils.dismissDialog(pd);
                                            Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                                                    "关联其他单号成功", "提示");
                                            DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert1);
                                        }
                                    });
                                } else {
                                    mhandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DialogUtils.dismissDialog(pd);
                                            Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                                                    "关联其他单号失败！！！", "提示");
                                            DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert1);
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                mhandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.dismissDialog(pd);
                                        Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                                                "连接服务器超时", "提示");
                                        DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert1);
                                    }
                                });
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
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
                Intent intent = new Intent(SetYundanActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_CODE);
            }
        });
        final Intent intent = getIntent();
        String sendFlag = intent.getStringExtra("type");
        if ("2".equals(sendFlag)) {
            isDiaohuo = true;
        }
        if ("101".equals(MyApp.id)) {
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

        diaohuoList.add("深圳市福田区-->北京中转");
        diaohuoList.add("深圳市福田区-->香港");
        diaohuoList.add("深圳市福田区-->上海");
        diaohuoList.add("深圳市福田区-->深圳市龙岗区");

        diaohuoList.add("深圳市龙岗区-->北京中转");
        diaohuoList.add("深圳市龙岗区-->香港");
        diaohuoList.add("深圳市龙岗区-->深圳市福田区");
        diaohuoList.add("深圳市龙岗区-->上海");

        diaohuoList.add("香港-->北京中转");
        diaohuoList.add("香港-->深圳市龙岗区");
        diaohuoList.add("香港-->深圳市福田区");
        diaohuoList.add("香港-->上海");

        diaohuoList.add("上海-->北京中转");
        diaohuoList.add("上海-->深圳市龙岗区");
        diaohuoList.add("上海-->深圳市福田区");
        diaohuoList.add("上海-->香港");

//        spiDiaohuo.setAdapter(new ArrayAdapter<String>(SetYundanActivity.this, R.layout.item_province, R.id
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
//                for (HashMap<String, String> s : list) {
//                    if (from.equals(s.get("key"))) {
//                        edJPerson.setText(s.get("name"));
//                        edJTel.setText(s.get("phone"));
//                        edJAddress.setText(s.get("address"));
//                    }
//                    if (to.equals(s.get("key"))) {
//                        eddPerson.setText(s.get("name"));
//                        eddTel.setText(s.get("phone"));
//                        eddAddress.setText(s.get("address"));
//                    }
//                }
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
        Log.e("zjy", "SetYundanActivity->onCreate(): isdiaohuo==" + isDiaohuo);
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
                    MyToast.showToast(SetYundanActivity.this, "还未下单");
                    return;
                }
                pd.setMessage("正在重新插入单号信息");
                pd.show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                            result = insertYundanInfo(tvPid.getText().toString(), desOrderid, ddestcode);
                            if (result.equals("成功")) {
                                mhandler.sendEmptyMessage(9);
                            } else {
                                mhandler.sendEmptyMessage(6);
                            }
                        } catch (IOException e) {
                            mhandler.sendEmptyMessage(6);
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在打印中");
        spiBags.setAdapter(new ArrayAdapter<String>(this, R.layout.lv_item_printer, R
                .id.spinner_item_tv, new String[]{"1", "2", "3", "4",}));
        spiBags.setVisibility(View.GONE);
        final SharedPreferences sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
        serverIP = sp.getString("serverPrinter", "");
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

        new Thread() {
            @Override
            public void run() {
                super.run();
                Intent intent = getIntent();
                String pid = intent.getStringExtra("pid");
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
                String detail = null;
                try {
                    detail = searchByPid(pid);
                    String addressJson = getDHAddresss();
                    JSONObject addJObj = new JSONObject(addressJson);
                    JSONArray addTable = addJObj.getJSONArray("表");
//                    {"objid":"5","FromStorageID":"云岗库房","ToStotageID":"北京中转库","FromName":"商庆房",
//                            "FromPhone":"0755-83764658","FromAddress":""
//                            "深圳市龙岗区吉华路393号英达丰科技园","ToName":"王鹏",
//                            "ToPhone":"010-62105503","ToAddress":"北京市海淀区知春路108号豪景大厦C座1503",
//                            "AccountNo":""},
                    List<String> titles = new ArrayList<String>();
                    titles.add("请-->选择调货方向");
                    for (int j = 0; j < addTable.length(); j++) {
                        JSONObject obj = addTable.getJSONObject(j);
                        String from = obj.getString("FromStorageID");
                        String to = obj.getString("ToStotageID");
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
                        titles.add(from + "-->" + to);
                    }
                    final ArrayAdapter adapter = new ArrayAdapter<String>(SetYundanActivity.this,R.layout.item_province,
                            R.id.item_province_tv, titles);
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            spiDiaohuo.setAdapter(adapter);
                        }
                    });
                    JSONObject root = new JSONObject(detail);
                    JSONArray table = root.getJSONArray("表");
                    for (int i = 0; i < table.length(); i++) {
                        JSONObject obj = table.getJSONObject(i);
                        jAddress = obj.getString("寄件地址1");
                        jComapany = obj.getString("寄件公司");
                        payByWho = obj.getString("谁付运费");
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
                        jTel = obj.getString("寄件电话");
                        jName = obj.getString("业务员");
                        dAddress = obj.getString("收件地址");
                        dTel = obj.getString("收件电话");
                        dName = obj.getString("收件人");
                        dCompany = obj.getString("收件公司");
                        payType = obj.getString("谁付运费");
                        corpID = obj.getString("InvoiceCorp");
                        storageID = obj.getString("StorageID");
                    }
                    Log.e("zjy", "SetYundanActivity->run(): corpID==" + corpID);
                    account = getAccoutByCorpID(corpID);
                    //                    account = "9999999999";
                    mhandler.sendEmptyMessage(0);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
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
                    Log.e("zjy", "SetYundan->run():printer: reuslt=="
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
                            DialogUtils.dismissDialog(pd);
                            Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                                    "打印机地址有误，请重新配置", "提示");
                            DialogUtils.safeShowDialog(SetYundanActivity.this, spAlert1);
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
        Log.e("zjy", "SetYundanActivity->onCreate(): goodInfos==" + intent
                .getStringExtra("goodInfos"));
        String pid = intent.getStringExtra("pid");
        tvPid.setText(pid);
        //        HashMap<String, String> maps = new HashMap<>();
        //       maps = manger.serachYundan(pid);
        //        final String orderid = maps.get("orderid");
        //        final String destcode = maps.get("destcode");
        //        if (orderid != null) {
        //            tvOrderID.setText("当前单据已有单号：" + orderid);
        //            btnReview.setEnabled(true);
        //            desOrderid = orderid;
        //            ddestcode = destcode;
        //            dgoodInfos = maps.get("goodInfos");
        //            dserverType = maps.get("serverType");
        //            dbaojia = Double.parseDouble(maps.get("baojia"));
        //            dpayPart = maps.get("payPart");
        //            dpayType = maps.get("payType");
        //            dcardID = maps.get("cardID");
        //            dprintName = maps.get("printName");
        //            dhasE = maps.get("hasE");
        //            dyundanType = maps.get("yundanType");
        //            btnRePrint.setEnabled(true);
        //        }
        btn150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        btnRePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desOrderid != null && dyundanType == null) {
                    if (flag != 1) {
                        Toast.makeText(SetYundanActivity.this, "获取寄送信息失败，请稍等或返回重新进入", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }

                    if (boxBaojia.isChecked()) {
                        String strBaojia = edBaojia.getText().toString();
                        if (strBaojia.equals("")) {
                            Toast.makeText(SetYundanActivity.this, "必须输入保价金额", Toast
                                    .LENGTH_SHORT).show();
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
                        Toast.makeText(SetYundanActivity.this, "请输入包裹数", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (jTel.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入寄件人电话", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (jAddress.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入寄件人地址", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (jName.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入寄件人姓名", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (dTel.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入收件人电话", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (dAddress.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入收件人地址", Toast
                                .LENGTH_SHORT).show();
                        return;
                    }
                    if (dName.equals("")) {
                        Toast.makeText(SetYundanActivity.this, "必须输入收件人姓名", Toast
                                .LENGTH_SHORT).show();
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
                            mhandler.sendEmptyMessage(4);
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
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            boolean ok = startPrint(desOrderid, dgoodInfos, dcardID, dpayPart, dpayType, dserverType,
                                    dbaojia, dprintName,
                                    dhasE, ddestcode, dyundanType);
                            if (!ok) {
                                Message message = mhandler.obtainMessage(3);
                                message.arg1 = 1;
                                mhandler.sendMessage(message);
                            } else {
                                mhandler.obtainMessage(5).sendToTarget();
                            }
                        } catch (IOException e) {
                            mhandler.obtainMessage(8).sendToTarget();
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
        btn210.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ddestcode != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (SetYundanActivity.this);
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                //                GetBD_YunDanInfoByID
                String pid = tvPid.getText().toString();
                try {
                    String result = getOnlineSavedYdInfo(pid);
                    Log.e("zjy", "SetYundanActivity->run(): onlineYundan==" + result);
                    //                    "objid":"613","parentid":"0","objname":"1176338","objvalue":"616606640489",
                    // "objtype":"顺丰","objexpress":"010",
                    JSONObject obj = new JSONObject(result);
                    JSONArray root = obj.getJSONArray("表");
                    if (root.length() > 0) {
                        JSONObject t = root.getJSONObject(0);
                        String orderID = t.getString("objvalue");
                        String destcode = t.getString("objexpress");
                        ddestcode = destcode;
                        desOrderid = orderID;
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvOrderID.setText("当前单据已有单号：" + desOrderid);
                                btnRePrint.setEnabled(true);
                            }
                        });
                    }
                } catch (IOException e) {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvOrderID.setText("查询关联单号失败，请重新进入");
                        }
                    });
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvOrderID.setText("还未下单，请下单");
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
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
        //                                    (SetYundanActivity.this,
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
        InputStream in = getResources().openRawResource(R.raw.province);
        provinces = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,
                    "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String temp = "";
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
            JSONArray object = new JSONArray(builder.toString());
            for (int i = 0; i < object.length(); i++) {
                JSONObject tempObj = object.getJSONObject(i);
                String code = tempObj.getString("code");
                String name = tempObj.getString("name");
                Province p1 = new Province();
                p1.name = name;
                p1.code = code;
                provinces.add(p1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //        spiProvince.setAdapter(new ArrayAdapter<>(this, R.layout.item_province,
        //                R.id.item_province_tv, provinces));

    }
    //    GetBD_YunDanInfoByID

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String result = data.getStringExtra("result");
                edMorePid.setText(result);
            }
        }
    }

    public String getOnlineSavedYdInfo(String pid) throws IOException, XmlPullParserException {
        //        GetBD_YunDanInfoByID;
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pid", pid);
        SoapObject req = WebserviceUtils.getRequest(map, "GetBD_YunDanInfoByID");
        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req, SoapEnvelope.VER11, WebserviceUtils.SF_SERVER);
        return res.toString();
    }

    private void checkPrintCouts(String times, final CheckBox boxBaojia, final EditText edBaojia, final Intent intent, final
    Spinner spiPayType, final Spinner spiServerType, final CheckBox boxDuanxin, final CheckBox boxEsign, final CheckBox
                                         boxTakepic) {

        if (times.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder
                    (SetYundanActivity.this);
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
            Toast.makeText(SetYundanActivity.this, "获取寄送信息失败，请稍等或返回重新进入", Toast
                    .LENGTH_SHORT).show();
            return;
        }

        if (boxBaojia.isChecked()) {
            String strBaojia = edBaojia.getText().toString();
            if (strBaojia.equals("")) {
                Toast.makeText(SetYundanActivity.this, "必须输入保价金额", Toast
                        .LENGTH_SHORT).show();
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
            Toast.makeText(SetYundanActivity.this, "请输入包裹数", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (jTel.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入寄件人电话", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (jAddress.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入寄件人地址", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (jName.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入寄件人姓名", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (dTel.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入收件人电话", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (dAddress.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入收件人地址", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (dName.equals("")) {
            Toast.makeText(SetYundanActivity.this, "必须输入收件人姓名", Toast
                    .LENGTH_SHORT).show();
            return;
        }
        pd.setMessage("正在打印中");
        pd.show();
        new Thread() {
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
                try {

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
                            mhandler.sendEmptyMessage(4);
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
                    for (int i = 0; i < infos.length; i++) {
                        String[] s = infos[i].split("&");
                        if (s.length > 0) {
                            Cargo cargo = new Cargo();
                            cargo.setCount(s[1]);
                            cargo.setName(s[0]);
                            cargos.add(cargo);
                        }
                    }
                    //                    GetBD_YunDanInfoByID
                    String xml = SFWsUtils.createOrderXml(SFWsUtils.ORDER_SERVICE,
                            order, cargos, null);
                    String destcode = "110";
                    boolean test = cboTest.isChecked();
                    if (!test) {
                        Map<String, String> resMap = getOrderResponse(xml);
                        String head = resMap.get("head");
                        if (head.equals("ERR")) {
                            Message msg = mhandler.obtainMessage(1);
                            msg.obj = resMap.get("error");
                            MyApp.myLogger.writeError(SetYundanActivity.class, resMap.get("errorCode") + "\t" + msg.obj + "\t" +
                                    dProvince + "\t" + dCity + "\t" + dCounty +
                                    "\t" + dAddress + "\t" + tvPid.getText().toString());
                            mhandler.sendMessage(msg);
                            return;
                        }
                        destcode = resMap.get("destcode");
                        orderID = resMap.get("orderID");
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
                    //                    InsertBD_YunDanInfo
                    //                    manger.insertYundan(tvPid.getText().toString(), orderID, destcode, goodInfos, cardID,
                    // payPart, payType,
                    //                            serverType, baojia, printName, hasE, yundanType);
                    //                    startPostPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
                    //                            printName, hasE, destcode, yundanType);
                    try {
                        String result = "";
                        if (test) {
                            result = "成功";
                        } else {
                            String pid = tvPid.getText().toString();
                            result = updatePrintCount(pid, desOrderid);
                            result = insertYundanInfo(pid, orderID, destcode);
                        }
                        if (!result.equals("成功")) {
                            mhandler.sendEmptyMessage(6);
                        } else {
                            mhandler.sendEmptyMessage(9);
                        }
                    } catch (IOException e) {
                        Message upMsg = mhandler.obtainMessage(3);
                        upMsg.arg1 = 2;
                        mhandler.sendMessage(upMsg);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                    try {
                        boolean printOk = startPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
                                printName, hasE, destcode, yundanType);
                        if (!printOk) {
                            Message message = mhandler.obtainMessage(3);
                            message.arg1 = 1;
                            mhandler.sendMessage(message);
                            throw new IOException("print error");
                        } else {
                            mhandler.sendEmptyMessage(5);
                        }
                    } catch (IOException e) {
                        String exMsg = e.getMessage();
                        if ("print error".equals(exMsg)) {
                            MyApp.myLogger.writeError("print error" + tvPid.getText().toString());
                        } else {
                            mhandler.obtainMessage(8).sendToTarget();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Message msg = mhandler.obtainMessage(3);
                    msg.arg1 = 0;
                    mhandler.sendMessage(msg);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String insertYundanInfo(String pid, String orderID, String destcode) throws IOException, XmlPullParserException {

        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("objname", pid);
        map.put("objvalue", orderID);
        map.put("express", destcode);
        SoapObject parm = WebserviceUtils.getRequest(map, "InsertBD_YunDanInfo");
        SoapPrimitive insertResult = WebserviceUtils.getSoapPrimitiveResponse(parm, SoapEnvelope
                .VER11, WebserviceUtils.SF_SERVER);
        Log.e("zjy", "SetYundanActivity->run(): insert Res==" + insertResult);
        if (insertResult.toString().equals("成功")) {
            MyApp.myLogger.writeInfo("SFprint--insertYundanInfo:OK" + pid);
        }
        return insertResult.toString();
    }

    @NonNull
    private boolean startPrint(String orderID, String goodInfos, String cardID, String payPart, String payType, String
            serverType, double baojia, String printName, String hasE, String destcode, String yundanType) throws IOException {
        String ip = "http://" + serverIP + ":8080";
        String strURL = ip + "/PrinterServer/SFPrintServlet?";
        strURL += "orderID=" + URLEncoder.encode(orderID,
                "UTF-8");
        strURL += "&hasE=" + URLEncoder.encode(hasE,
                "UTF-8");
        strURL += "&yundanType=" + URLEncoder.encode(yundanType,
                "UTF-8");
        strURL += "&goodinfos=" + URLEncoder.encode(goodInfos,
                "UTF-8");
        strURL += "&printer=" + URLEncoder.encode(printName,
                "UTF-8");

        strURL += "&cardID=" + URLEncoder.encode(cardID,
                "UTF-8");
        strURL += "&baojiaprice=" + URLEncoder.encode(String
                        .valueOf(baojia),
                "UTF-8");
        strURL += "&payPerson=" + URLEncoder.encode(payPart,
                "UTF-8");
        strURL += "&payType=" + URLEncoder.encode(payType,
                "UTF-8");
        strURL += "&serverType=" + URLEncoder.encode(serverType,
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
        Log.e("zjy", "SetYundanActivity->startPrint(): StrUrl==" + strURL);
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

    @NonNull
    private boolean startPostPrint(String orderID, String goodInfos, String cardID, String payPart, String payType, String
            serverType, double baojia, String printName, String hasE, String destcode, String yundanType) throws IOException {
        String ip = "http://" + serverIP + ":8080";
        String strURL = ip + "/PrinterServer/SFPrintServlet?";
        Log.e("zjy", "SetYundanActivity->startPrint(): StrUrl==" + strURL);
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
        writer.write("printer=" + printName + "\n");
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
        Log.e("zjy", "SetYundanActivity->run(): print_result==" + builder
                .toString());
        if (res.equals("ok")) {
            return true;
        } else {
            return false;
        }
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

    private Map<String, String> getOrderResponse(String xml) throws IOException,
            XmlPullParserException {
        SoapObject object = new SoapObject(SFWsUtils.NAMESPACE,
                "sfexpressService");
        byte[] byteCode = Base64.encode((Md5.getMD5Bytes(xml + SFWsUtils
                .verifyCode)), Base64.NO_WRAP);
        String verifyCode = new String(byteCode, "UTF-8");
        object.addProperty("arg0", xml);
        object.addProperty("arg1", verifyCode);
        SoapSerializationEnvelope envelope =
                SFWsUtils.getEnvelope(object,
                        SoapEnvelope.VER11, null, SFWsUtils
                                .ROOT_URL, false);
        SoapPrimitive
                soapPrimitive = (SoapPrimitive)
                envelope.getResponse();
        Log.e("zjy",
                "SFActivity->getOrderResponse(): result==" +
                        soapPrimitive.toString());
        XmlDomUtils xmlUtils = new XmlDomUtils();
        return xmlUtils.readXML(soapPrimitive.toString());
    }

    public String getSFClientInfo(String clientID) throws IOException,
            XmlPullParserException {
        //        GetClientPCCInfo
        SoapObject request = new SoapObject("http://tempuri.org/",
                "GetClientPCCInfo");
        request.addProperty("id", clientID);
        //设置版本号，ver11，和ver12比较常见
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/ISF_Server/GetClientPCCInfo";
        trans.call(action, envelope1);
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        Log.e("zjy", "SFActivity->getSFClientInfo(): clientDetail==" + sp.toString());
        return sp.toString();
    }

    private String searchByPid(String pid) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject("http://tempuri.org/",
                "GetYunDanInfos");
        request.addProperty("pid", pid);
        //设置版本号，ver11，和ver12比较常见
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/ISF_Server/GetYunDanInfos";
        trans.call(action, envelope1);
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        Log.e("zjy", "SFActivity->searchByPid(): yundanInfo==" + sp.toString());
        return sp.toString();
    }

    private String getDHAddresss() throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject("http://tempuri.org/",
                "GetBD_DHAddress");
        //设置版本号，ver11，和ver12比较常见
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/ISF_Server/GetBD_DHAddress";
        trans.call(action, envelope1);
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        Log.e("zjy", "SFActivity->getDHAddresss(): daohouAddress==" + sp.toString());
        return sp.toString();
    }


    public String updatePrintCount(String pid, String orderID) throws IOException,
            XmlPullParserException {
        if (isDiaohuo) {
            return "成功";
        }
        String newOrder = orderID.replace(",", "/");
        //       UpdateYunDanInfoByPrintCountResult
        SoapObject request = new SoapObject("http://tempuri.org/",
                "UpdateYunDanInfoByPrintCount");
        request.addProperty("pid", pid);
        request.addProperty("yundanID", newOrder);
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/ISF_Server/UpdateYunDanInfoByPrintCount";
        trans.call(action, envelope1);
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        Log.e("zjy", "SFActivity->updatePrintCount(): updateCount==" + sp.toString());
        if ("成功".equals(sp.toString())) {
            MyApp.myLogger.writeInfo("SFprint--updatePrintCount:OK" + pid);
        }
        return sp.toString();
    }

    public String getAccoutByCorpID(String corpID) throws IOException,
            XmlPullParserException {
        //        expressName定为：顺风
        //                corpID是开票公司ID
        SoapObject request = new SoapObject("http://tempuri.org/",
                "GetCorpExpressAccountNo");
        request.addProperty("expressName", "顺丰");
        request.addProperty("corpID", corpID);
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/ISF_Server/GetCorpExpressAccountNo";
        trans.call(action, envelope1);
        SoapObject sobj = (SoapObject) envelope1.bodyIn;
        String item = sobj.getPropertyAsString("GetCorpExpressAccountNoResult");
        Log.e("zjy", "SetYundanActivity->getAccoutByCorpID(): account==" + item);
        if (item.equals("anyType{}")) {
            return null;
        } else {
            return item;
        }
    }
}
