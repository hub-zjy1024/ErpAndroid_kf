package printer.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import printer.sfutils.ExtraService;
import printer.sfutils.Md5;
import printer.sfutils.SFWsUtils;
import printer.sfutils.XmlDomUtils;

public class SetYundanActivity extends AppCompatActivity {

    private Map<String, String> map;
    private List<Province> provinces;
    String jAddress;
    String jComapany;
    String jProvince;
    String jCity;
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
    Bitmap reviewBitmap;
    private ArrayAdapter<String> printerAdapter;
    private List<String> spiItems;

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
                    AlertDialog.Builder builder = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("下单失败:" + msg.obj.toString());
                    builder.show();
                    break;
                case 3:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder2.setTitle("提示");
                    builder2.setMessage("网络连接错误");
                    builder2.show();
                    break;
                case 4:
                    AlertDialog.Builder builder3 = new AlertDialog.Builder
                            (SetYundanActivity.this);
                    builder3.setTitle("提示");
                    builder3.setMessage("月结账号获取失败，当前不可用寄付月结");
                    builder3.show();
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

    //    620015
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

        final Button btnCommit = (Button) findViewById(R.id
                .activity_set_yundan_btnCommit);
        final Button btnCommit1 = (Button) findViewById(R.id
                .activity_set_yundan_btnCommit1);
        final Button btnCommit2 = (Button) findViewById(R.id
                .activity_set_yundan_btnCommit2);
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

        String[] serverTypes = new String[]{"1-顺丰次日", "2-顺丰隔日","5-顺丰次晨",
                "6-顺丰即日", "7-物流普运", "18-重货快运"};
        spiServerType.setAdapter(new ArrayAdapter<>(this, R.layout.item_province,
                R.id .item_province_tv, serverTypes));

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
                    account = "9999999999";
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
                String ip = "http://192.168.10.65:8080";
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
                            spiItems.add(p);
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
        //下单并打印
        final Intent intent = getIntent();
        Log.e("zjy", "SetYundanActivity->onCreate(): goodInfos==" + intent
                .getStringExtra("goodInfos"));
        tvPid.setText(intent.getStringExtra("pid"));
        btnCommit1.setOnClickListener(new View.OnClickListener() {
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
                            orderAndPrint(boxBaojia, edBaojia, intent, spiPayType,
                                    spiServerType,
                                    boxDuanxin, boxEsign, boxTakepic, edJAddress,
                                    edJPerson, edJTel, eddAddress, eddPerson, eddTel,
                                    TYPE_150);
                        }
                    });
                    builder.setNegativeButton("否", null);
                    builder.show();
                } else {
                    orderAndPrint(boxBaojia, edBaojia, intent, spiPayType, spiServerType,
                            boxDuanxin, boxEsign, boxTakepic, edJAddress,
                            edJPerson, edJTel, eddAddress, eddPerson, eddTel, TYPE_150);
                }
            }
        });
        btnCommit2.setOnClickListener(new View.OnClickListener() {
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
                            orderAndPrint(boxBaojia, edBaojia, intent, spiPayType,
                                    spiServerType,
                                    boxDuanxin, boxEsign, boxTakepic, edJAddress,
                                    edJPerson, edJTel, eddAddress, eddPerson, eddTel,
                                    TYPE_210);
                        }
                    });
                    builder.setNegativeButton("否", null);
                    builder.show();
                } else {
                    orderAndPrint(boxBaojia, edBaojia, intent, spiPayType, spiServerType,
                            boxDuanxin, boxEsign, boxTakepic, edJAddress,
                            edJPerson, edJTel, eddAddress, eddPerson, eddTel, TYPE_210);
                }
            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
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
                            orderAndPrint(boxBaojia, edBaojia, intent, spiPayType,
                                    spiServerType,
                                    boxDuanxin, boxEsign, boxTakepic, edJAddress,
                                    edJPerson, edJTel, eddAddress, eddPerson, eddTel,
                                    TYPE_210);
                        }
                    });
                    builder.setNegativeButton("否", null);
                    builder.show();
                } else {
                    orderAndPrint(boxBaojia, edBaojia, intent, spiPayType, spiServerType,
                            boxDuanxin, boxEsign, boxTakepic, edJAddress,
                            edJPerson, edJTel, eddAddress, eddPerson, eddTel, TYPE_210);
                }

            }
        });
        //预览
        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag != 1) {
                    Toast.makeText(SetYundanActivity.this, "获取寄送信息失败，请稍等或重新重新进入", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                iv.setImageBitmap(null);
                if (reviewBitmap != null && !reviewBitmap.isRecycled())
                    reviewBitmap.recycle();

                new Thread() {
                    @Override
                    public void run() {
                        String ip = "http://192.168.10.65:8080";
                        String orderID = "123456789123";
                        String goodInfos = "url1-500,url2-6000,url3-700";
                        goodInfos = intent.getStringExtra("goodInfos");
                        String strURL = ip + "/PrinterServer/ReviewServlet?";
                        //                        String jName = "张三";
                        //                        String jTel = "102012012";
                        //                        String jAddress = "湖北省";
                        //                        String dName = "李四";
                        //                        String dTel = "1541646546";
                        //                        String dAddress = "北京市";
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

        new Thread() {
            @Override
            public void run() {
                String ip = "http://192.168.10.65:8080";
                String orderID = "";
                String goodInfos = "url1-500,url2-6000,url3-700";
                goodInfos = intent.getStringExtra("goodInfos");
                String strURL = ip + "/PrinterServer/SFPrintServlet?";
                //                        String jName = "张三";
                //                        String jTel = "102012012";
                //                        String jAddress = "湖北省荆州市";
                //                        String dName = "李四";
                //                        String dTel = "1541646546";
                //                        String dAddress = dName;
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
                if (serverType.equals("电商特惠")) {
                    hasE = "1";
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
                    order.orderID = String.valueOf(System.currentTimeMillis());

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
//                     TODO: 2017/7/10 支付方式还原

//                    if (payType.equals("寄方付")) {
//                        order.custid = account;
//                        if (!storageID.equals("102")) {
//                            order.payType = "3";
//                        } else {
//                            order.payType = "1";
//                        }
//                    } else {
//                        order.payType = "2";
//                    }
                    if (payType.equals("寄付月结")) {
                        if (account == null || "".equals(account)) {
                            mhandler.sendEmptyMessage(4);
                            return;
                        }
                        order.custid = account;
                        order.payType = "1";
//                        if (!storageID.equals("102")) {
                            //                            order.payType = "3";
//                        payPart = "北京";
                            //                        } else {
                            //                            order.payType = "1";
                            //                        }
                    } else if (payType.equals("到方付")) {
                        order.payType = "2";
//                        "寄付月结", "到方付", "第三方付"
                    } else if (payType.equals("第三方付")) {
                        order.payType = "3";
                        order.custid = account;
//                        payPart = "0755BL";
                        payPart = "755A";
                    }
                    if (!payType.equals("到方付")) {
                        cardID = account;
                    }
                    String[] infos = goodInfos.split(",");
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

                    List<ExtraService> services = new ArrayList<>();
//                    if (boxBaojia.isChecked()) {
//                        services.add(ExtraService.addBaojia(baojia));
//                    }
//                    if (boxDuanxin.isChecked()) {
//                        services.add(ExtraService.addDuanxin(jTel));
//                    }
//                    if (boxEsign.isChecked()) {
//                        services.add(ExtraService.addEsign(1, 1));
//                    }
//                    if (boxTakepic.isChecked()) {
//                        services.add(ExtraService.addPsign(1, 1));
//                    }
                    String xml = SFWsUtils.createOrderXml(SFWsUtils.ORDER_SERVICE,
                            order, cargos, services);
                    Map<String, String> resMap = getOrderResponse(xml);
                    for (Map.Entry<String, String> entry : resMap.entrySet()) {
                        Log.e("zjy", "SetYundanActivity->run(): entry==" + entry.getKey
                                () + "-value:" + entry.getValue());
                    }
                    orderID = resMap.get("orderID");
                    String destcode = "110";
                    destcode = resMap.get("destcode");
                    String head = resMap.get("head");
                    if (head.equals("ERR")) {
                        Message msg = mhandler.obtainMessage(1);
                        msg.obj = resMap.get("error");
                        mhandler.sendMessage(msg);
                        return;
                    }
                    okCounts++;
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
                    Log.e("zjy", "SetYundanActivity->run(): print_result==" + builder
                            .toString());
                    okCounts++;
                    // TODO: 2017/7/11 更新打印次数
//                    updatePrintCount(tvPid.getText().toString(), orderID);
                    okCounts++;
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
        Log.e("zjy", "SFActivity->updatePrintCount(): updateCount==" + sp.toString());
        return sp.toString();
    }

    public String updatePrintCount(String pid, String orderID) throws IOException,
            XmlPullParserException {

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
        SoapPrimitive sp = (SoapPrimitive) envelope1.getResponse();
        //        SoapObject sp = (SoapObject) envelope1.bodyIn;
        Log.e("zjy", "SFActivity->getAccoutByCorpID(): account==" + sp.toString());
        return sp.toString();
    }
}
