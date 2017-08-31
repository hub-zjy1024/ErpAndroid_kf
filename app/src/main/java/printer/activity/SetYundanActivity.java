package printer.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;

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

public class SetYundanActivity extends AppCompatActivity {

    private Map<String, String> map;
    private List<Province> provinces;
    String jAddress;
    String jComapany = "";
    String jProvince = "";
    String jCity = "";
    String jCounty;

    String payType;
    String jTel;
    String jName;
    String dAddress;
    String dTel;
    String dName;
    String dCompany;
    String dProvince;
    String dCity;
    String dCounty;
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
    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    flag = 1;
                    edJAddress.setText(jAddress);
                    edJTel.setText(jTel);
                    edJPerson.setText(jName);

                    eddTel.setText(dTel);
                    eddAddress.setText(dAddress);
                    eddPerson.setText(dName);
                    break;
                case 1:
                    DialogUtils.dismissDialog(pd);
                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("下单失败:" + msg.obj.toString());
                    builder.show();
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
                                new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            String result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                                            if (result.equals("成功")) {
                                                mhandler.sendEmptyMessage(5);
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
                        builder2.show();
                    }
                    break;
                case 4:
                    DialogUtils.dismissDialog(pd);
                    AlertDialog.Builder builder3 = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder3.setTitle("提示");
                    builder3.setMessage("月结账号获取失败，当前不可用寄付月结！！！");
                    builder3.show();
                    break;
                case 5:
                    DialogUtils.dismissDialog(pd);
                    Dialog spAlert1 = DialogUtils.getSpAlert(SetYundanActivity.this,
                            "操作成功", "提示");
                    spAlert1.show();
                    break;
                case 6:
                    DialogUtils.dismissDialog(pd);
                    Dialog spAlert = DialogUtils.getSpAlert(SetYundanActivity.this, "提示",
                            "插入单号信息失败,请重新插入！！！");
                    spAlert.show();
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
    private Button rePrint;
    private Button btnReInsert;
    private CheckBox cboTest;
    private RadioButton rdo210;
    private String mYundanType;
    private TextView cboDiaohuo;
    private boolean isDiaohuo=false;
    private Spinner spiDiaohuo;

    //    6200151
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rePrint = (Button) findViewById(R.id
                .activity_set_yundan_btnReprint);
        final Button btnReview = (Button) findViewById(R.id
                .activity_set_yundan_btnReview);
        final ImageView iv = (ImageView) findViewById(R.id
                .activity_set_yundan_iv);
        final Spinner spiPayType = (Spinner) findViewById(R.id
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
       cboTest = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbotest);
       spiDiaohuo = (Spinner) findViewById(R.id
                .activity_set_yundan_spi_diaohuo);
        tvOrderID = (TextView) findViewById(R.id
                .activity_set_yundan_tv_orderid);
        rdo210 = (RadioButton) findViewById(R.id
                .activity_set_yundan_rdo_210);
        cboDiaohuo = (TextView) findViewById(R.id.activity_set_yundan_cbo_diaohuo);

        final CheckBox boxTakepic = (CheckBox) findViewById(R.id
                .activity_set_yundan_cbo_takepic);
        final EditText edBaojia = (EditText) findViewById(R.id
                .activity_set_yundan_ed_baojia);
        edJPerson = (EditText) findViewById(R.id
                .activity_set_yundan_ed_j_person);
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
        btnReInsert = (Button) findViewById(R.id
                .activity_set_yundan_btnReInsert);
        final Intent intent = getIntent();
        String sendFlag = intent.getStringExtra("flag");
        if (sendFlag != null) {
            isDiaohuo = true;
        }
        if ("101".equals(MyApp.id)) {
            cboTest.setVisibility(View.VISIBLE);
        }else{
        cboTest.setVisibility(View.GONE);
         }
        ArrayList<String> diaohuoList = new ArrayList<String>();
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

        spiDiaohuo.setAdapter(new ArrayAdapter<String>(SetYundanActivity.this, R.layout.item_province, R.id
                .item_province_tv, diaohuoList));
       final List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();    HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "王朋");
        map.put("key", "北京中转");
        map.put("phone", "010-62105503");
        map.put("address", "北京市海淀区知春路108号豪景大厦C座1503");
        list.add(map);
        map = new HashMap<String, String>();
        map.put("name", "商庆房");
        map.put("key", "深圳市龙岗区");
        map.put("phone", "0755-83764658");
        map.put("address", "深圳市龙岗区吉华路393号英达丰科技园");
        list.add(map);
        map = new HashMap<String, String>();
        map.put("name", "李娜");
        map.put("key", "深圳市福田区");
        map.put("phone", "0755-83764658");
        map.put("address", "深圳市福田区中航路鼎城大厦1920室");
        list.add(map);
        map = new HashMap<String, String>();
        map.put("name", "王永松");
        map.put("key", "上海");
        map.put("phone", "021-61170776");
        map.put("address", "上海市宝山区城银路518号A座2楼201室");
        list.add(map);
        map = new HashMap<String, String>();
        map.put("name", "盧少欣");
        map.put("key", "香港");
        map.put("phone", "852-34264941");
        map.put("address", "香港九龙官塘鸿图道76号联运工业大厦2楼B室");
        list.add(map);
        spiDiaohuo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemAtPosition = (String) parent.getItemAtPosition(position);
                String[] detail = itemAtPosition.split("-->");
                String from = detail[0];
                String to = detail[1];
                for(HashMap<String,String> s:list){
                    if (from.equals(s.get("key"))) {
                        edJPerson.setText(s.get("name"));
                        edJTel.setText(s.get("phone"));
                        edJAddress.setText(s.get("address"));
                    }
                    if (to.equals(s.get("key"))) {
                        eddPerson.setText(s.get("name"));
                        eddTel.setText(s.get("phone"));
                        eddAddress.setText(s.get("address"));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (isDiaohuo) {
            flag = 1;
            spiDiaohuo.setVisibility(View.VISIBLE);
            cboDiaohuo.setVisibility(View.VISIBLE);
        } else {
            cboDiaohuo.setVisibility(View.GONE);
            spiDiaohuo.setVisibility(View.GONE);
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
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            String result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                            if (result.equals("成功")) {
                                mhandler.sendEmptyMessage(5);
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
        btn210.setFocusable(true);
        btn210.setFocusableInTouchMode(true);
        btn210.requestFocus();
        spiBags.setAdapter(new ArrayAdapter<String>(this, R.layout.lv_item_printer, R
                .id.spinner_item_tv, new String[]{"1", "2", "3", "4",}));
        spiBags.setVisibility(View.GONE);
        SharedPreferences sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
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

        String[] serverTypes = new String[]{"2-顺丰隔日(陆)","1-顺丰次日(空)" , "5-顺丰次晨",
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
                Log.e("zjy", "SetYundanActivity->run(): pid==" + pid);
                try {
                    String result = getSFClientInfo(client);
                    Log.e("zjy", "address->run(): result==" + result);
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
                    JSONObject root = new JSONObject(detail);
                    JSONArray table = root.getJSONArray("表");
                    for (int i = 0; i < table.length(); i++) {
                        JSONObject obj = table.getJSONObject(i);
                        jAddress = obj.getString("寄件地址1");
                        jComapany = obj.getString("寄件公司");
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
                        //                        edJPerson.setText(obj.getString("业务员"));
                        //                        edJTel.setText(obj.getString("寄件电话"));
                        //                        edJAddress.setText(obj.getString
                        // ("寄件地址1"));
                        //                        eddPerson.setText(obj.getString("收件人"));
                        //                        eddAddress.setText(obj.getString
                        // ("收件地址"));
                        //                        eddTel.setText(obj.getString("收件电话"));
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
                    e.printStackTrace();
                }
            }
        }.start();
        Log.e("zjy", "SetYundanActivity->onCreate(): goodInfos==" + intent
                .getStringExtra("goodInfos"));
        tvPid.setText(intent.getStringExtra("pid"));
        btn150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        rePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("正在打印中");
                pd.show();
                new Thread() {
                    @Override
                    public void run() {
                        String result = "";
                        try {
                            startPrint(desOrderid, dgoodInfos, dcardID, dpayPart, dpayType, dserverType,
                                    dbaojia, dprintName,
                                    dhasE, ddestcode, dyundanType);
                            result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                            if (result.equals("成功")) {
                                Message errorMesg = mhandler.obtainMessage(5);
                                mhandler.sendMessage(errorMesg);
                            } else {
                                mhandler.sendEmptyMessage(6);
                            }
                        } catch (IOException e) {
                            if (result.equals("")) {
                                mhandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.dismissDialog(pd);
                                        Dialog spAlert = DialogUtils.getSpAlert(SetYundanActivity.this, "提示",
                                                "打印出现错误！！！");
                                        spAlert.show();
                                    }
                                });
                            } else {
                                mhandler.sendEmptyMessage(6);
                            }
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
        btn210.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String times = intent.getStringExtra("times");
                //检测打印次数
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
        });
        //预览
        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag != 1) {
                    Toast.makeText(SetYundanActivity.this, "获取寄送信息失败，请检查网络，重新进入", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                iv.setImageBitmap(null);
                if (reviewBitmap != null && !reviewBitmap.isRecycled())
                    reviewBitmap.recycle();

                new Thread() {
                    @Override
                    public void run() {
                        String ip = "http://" + serverIP + ":8080";
                        String orderID = "123456789123";
                        String goodInfos = "url1-500,url2-6000,url3-700";
                        goodInfos = intent.getStringExtra("goodInfos");
                        String strURL = ip + "/PrinterServer/ReviewServlet?";
                        double baojia = 12050;
                        String strBaojia = edBaojia.getText().toString();
                        if (strBaojia.equals("")) {
                            baojia = -1;
                        } else {
                            baojia = Double.valueOf(strBaojia);
                        }
                        String cardID = "";

                        String payMan = "";
                        String payType = spiPayType.getSelectedItem().toString();
                        String serverType = "标准快递";
                        String destcode = "1234";
                        String stype = spiServerType.getSelectedItem().toString();
                        String hasE = "0";
                        if (serverType.equals("电商特惠")) {
                            hasE = "1";
                        }
                        if (payType.equals("寄付月结")) {
                            if (account == null || "".equals(account)) {
                                mhandler.sendEmptyMessage(4);
                                return;
                            }
                        } else if (payType.equals("第三方付")) {
                            payMan = "北京";


                        }
                        if (!payType.equals("到方付")) {
                            cardID = account;
                        }
                        serverType = stype.split("-")[1];
                        try {
                            strURL += "orderID=" + URLEncoder.encode(orderID,
                                    "UTF-8");
                            strURL += "&yundanType=" + URLEncoder.encode(TYPE_210,
                                    "UTF-8");
                            strURL += "&hasE=" + URLEncoder.encode(hasE,
                                    "UTF-8");
                            strURL += "&goodinfos=" + URLEncoder.encode(goodInfos,
                                    "UTF-8");
                            strURL += "&cardID=" + URLEncoder.encode(cardID,
                                    "UTF-8");
                            strURL += "&baojiaprice=" + URLEncoder.encode(String
                                            .valueOf(baojia),
                                    "UTF-8");
                            strURL += "&payPerson=" + URLEncoder.encode(payMan,
                                    "UTF-8");
                            strURL += "&payType=" + URLEncoder.encode(payType,
                                    "UTF-8");
                            strURL += "&serverType=" + URLEncoder.encode(serverType,
                                    "UTF-8");
                            strURL += "&destcode=" + URLEncoder.encode(destcode,
                                    "UTF-8");
                            // &baojiaprice=100&cardID=12345678&payType=寄方&payPerson
                            // =&serverType=标准快递&j_name=张
                            // 三&j_phone=1234567&j_address=北京市海淀区&d_name=李四&d_phone
                            // =18865240122&d_address=湖北省荆州市
                            strURL += "&j_name=" + URLEncoder.encode(jName,
                                    "UTF-8");
                            strURL += "&j_phone=" + URLEncoder.encode(jTel,
                                    "UTF-8");
                            strURL += "&j_address=" + URLEncoder.encode(jAddress,
                                    "UTF-8");
                            strURL += "&d_name=" + URLEncoder.encode(dName,
                                    "UTF-8");
                            strURL += "&d_phone=" + URLEncoder.encode(dTel,
                                    "UTF-8");
                            strURL += "&d_address=" + URLEncoder.encode(dAddress,
                                    "UTF-8");
                            URL url = new URL(strURL);
                            HttpURLConnection conn = (HttpURLConnection) url
                                    .openConnection();
                            conn.setConnectTimeout(20 * 1000);
                            final InputStream in = conn.getInputStream();
                            try {
                                reviewBitmap = BitmapFactory.decodeStream(in);
                                mhandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!reviewBitmap.isRecycled())
                                            iv.setImageBitmap(reviewBitmap);
                                    }
                                });
                            } catch (OutOfMemoryError outOfMemoryError) {
                                outOfMemoryError.printStackTrace();
                            }

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
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
        map = new HashMap<>();
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
            Toast.makeText(SetYundanActivity.this, "获取寄送信息失败，请返回重新进入", Toast
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
                    String xml = SFWsUtils.createOrderXml(SFWsUtils.ORDER_SERVICE,
                            order, cargos, null);
                    String destcode = "110";
                    boolean test = cboTest.isChecked();
                    if (!test) {
                        Map<String, String> resMap = getOrderResponse(xml);
                        for (Map.Entry<String, String> entry : resMap.entrySet()) {
                            Log.e("zjy", "SetYundanActivity->run(): entry==" + entry.getKey
                                    () + "-value:" + entry.getValue());
                        }
                        String head = resMap.get("head");
                        if (head.equals("ERR")) {
                            Message msg = mhandler.obtainMessage(1);
                            msg.obj = resMap.get("error");
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
                            rePrint.setEnabled(true);
                        }
                    });
                    okCounts++;
                    startPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
                            printName, hasE, destcode, yundanType);
//                    startPostPrint(orderID, goodInfos, cardID, payPart, payType, serverType, baojia,
//                            printName, hasE, destcode, yundanType);

                    okCounts++;
                    try {
                        if (test) {
                            mhandler.sendEmptyMessage(5);
                            return;
                        }
                        String result = updatePrintCount(tvPid.getText().toString(), desOrderid);
                        okCounts++;
                        if (result.equals("成功")) {
                            mhandler.sendEmptyMessage(5);
                        } else {
                            mhandler.sendEmptyMessage(6);
                        }
                    } catch (IOException e) {
                        mhandler.sendEmptyMessage(6);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Message msg = mhandler.obtainMessage(3);
                    msg.arg1 = okCounts;
                    mhandler.sendEmptyMessage(3);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
        // &baojiaprice=100&cardID=12345678&payType=寄方&payPerson
        // =&serverType=标准快递&j_name=张
        // 三&j_phone=1234567&j_address=北京市海淀区&d_name=李四&d_phone
        // =18865240122&d_address=湖北省荆州市
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
        map = new HashMap<>();
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
        Log.e("zjy", "SFActivity->getOrderResponse(): check==" + verifyCode);
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

    public String getHetongInfo(String pid) throws IOException, XmlPullParserException {
        //        http://172.16.6.160:8006/
        //        GetHeTongFileInfo
        SoapObject request = new SoapObject("http://tempuri.org/",
                "GetHeTongFileInfo");
        request.addProperty("pid", pid);
        SoapSerializationEnvelope envelope1 = new
                SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelope1.bodyOut = request;
        //.net开发的webservice必须加入
        envelope1.dotNet = true;
        HttpTransportSE trans = new HttpTransportSE("http://172.16.6" +
                ".160:8006/MartService.svc?wsdl", 15 * 1000);
        String action = "http://tempuri.org/IMartService/GetHeTongFileInfo";
        trans.call(action, envelope1);
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        Log.e("zjy", "SFActivity->GetHeTongFileInfo(): updateCount==" + sp.toString());
        return sp.toString();
    }

    public String updatePrintCount(String pid, String orderID) throws IOException,
            XmlPullParserException {
        if (isDiaohuo) {
            return "成功";
        }
        //       UpdateYunDanInfoByPrintCountResult
        SoapObject request = new SoapObject("http://tempuri.org/",
                "UpdateYunDanInfoByPrintCount");
        request.addProperty("pid", pid);
        request.addProperty("yundanID", orderID);
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
