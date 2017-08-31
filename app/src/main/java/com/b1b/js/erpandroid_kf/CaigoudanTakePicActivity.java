package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.entity.Caigoudan;

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

import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;

public class CaigoudanTakePicActivity extends AppCompatActivity {

    private ListView lv;
    private EditText edPid;
    private EditText edPartNo;
    private ArrayList<Caigoudan> caigoudans;
    private ArrayAdapter<Caigoudan> caigouAdapter;
    public static String username = "mingming";
    public static String password = "ryDl42QF";
    public static String ftpAddress = "172.16.6.22";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int counts = caigoudans.size();
                    if (counts != 0) {
                        caigouAdapter.notifyDataSetChanged();
                        MyToast.showToast(CaigoudanTakePicActivity.this, "查询到" + counts + "条数据");
                        SoftKeyboardUtils.closeInputMethod(edPartNo, CaigoudanTakePicActivity
                                .this);
                    }
                    break;
                case 1:
                    MyToast.showToast(CaigoudanTakePicActivity.this, "当前网络状态不佳");
                    break;
                case 2:
                    MyToast.showToast(CaigoudanTakePicActivity.this, "条件有误，请重新输入");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigoudan_take_pic);
        lv = (ListView) findViewById(R.id.activity_caigoudan_take_pic_lv);
        edPid = (EditText) findViewById(R.id.activity_caigoudan_take_pic_ed_pid);
        edPartNo = (EditText) findViewById(R.id.activity_caigoudan_take_pic_ed_partno);
        Button btnSearch = (Button) findViewById(R.id.activity_caigoudan_take_pic_btn_search);
        Button btnSaoma = (Button) findViewById(R.id.activity_caigoudan_take_pic_btn_saoma);
        btnSaoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaigoudanTakePicActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > caigoudans.size() - 1) {
                    return;
                }
                final Caigoudan item = (Caigoudan) parent.getItemAtPosition(position);
                Intent temp = new Intent(CaigoudanTakePicActivity.this, CaigouDetailActivity.class);
                temp.putExtra("corpID", item.getCorpID());
                temp.putExtra("providerID", item.getProviderID());
                temp.putExtra("pid", item.getPid());
                temp.putExtra("date", item.getCreatedDate());
                startActivity(temp);
                if (true) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CaigoudanTakePicActivity.this);
                builder.setTitle("上传方式选择");
                builder.setItems(new String[]{"拍照", "从手机选择", "连拍"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent1 = new Intent(CaigoudanTakePicActivity.this, TakePicActivity.class);
                                intent1.putExtra("flag", "caigou");
                                intent1.putExtra("pid", item.getPid());
                                startActivity(intent1);
                                MyApp.myLogger.writeInfo("takepic-caigou");
                                break;
                            case 1:
                                Intent intent2 = new Intent(CaigoudanTakePicActivity.this, ObtainPicFromPhone.class);
                                intent2.putExtra("flag", "caigou");
                                intent2.putExtra("pid", item.getPid());
                                startActivity(intent2);
                                MyApp.myLogger.writeInfo("obtain-caigou");
                                break;
                            case 2:
                                Intent intent3 = new Intent(CaigoudanTakePicActivity.this, CaigouTakePic2Activity.class);
                                intent3.putExtra("flag", "caigou");
                                intent3.putExtra("pid", item.getPid());
                                MyApp.myLogger.writeInfo("takepic2-caigou");
                                startActivity(intent3);
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caigoudans.clear();
                SoftKeyboardUtils.closeInputMethod(edPid, CaigoudanTakePicActivity.this);
                final String partNo = edPartNo.getText().toString();
                final String pid = edPid.getText().toString();
                if (MyApp.id == null) {
                    MyToast.showToast(CaigoudanTakePicActivity.this, "当前登录人为空，请重新登录");
                    return;
                }
                getData(partNo, pid);
            }
        });
        caigoudans = new ArrayList<>();
        caigouAdapter = new ArrayAdapter<Caigoudan>(this, R.layout.spinner_simple_item, R.id.spinner_item_tv, caigoudans);
        lv.setAdapter(caigouAdapter);
    }


    private void getData(final String partNo, final String pid) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String res = getCaigoudanByPidAndPartNo("", Integer.parseInt(MyApp.id), partNo, pid);
                    Log.e("zjy", "CaigoudanTakePicActivity->run(): json==" + res);
                    JSONObject object = new JSONObject(res);
                    JSONArray array = object.getJSONArray("表");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String pid = obj.getString("PID");
                        String createdDate = obj.getString("制单日期");
                        String state = obj.getString("单据状态");
                        String ywName = obj.getString("业务员");
                        String caigouName = obj.getString("采购员");
                        String partNo1 = obj.getString("型号");
                        String inPrice = obj.getString("进价");
                        String counts = obj.getString("数量");

                        String salePrice = obj.getString("售价");
                        String note = obj.getString("备注");
                        String clientID = obj.getString("客户编码");
                        String provder = obj.getString("供应商");
                        String askPriceMan = obj.getString("询价员");
                        String caigouPlace = obj.getString("采购地");
                        boolean isForeignClient = obj.getBoolean("IsForeignClient");
                        String corpID = obj.getString("InvoiceCorp");
                        String providerID = obj.getString("ProviderID");
                        Caigoudan caigoudan = new Caigoudan(state, pid, createdDate, ywName, caigouName, partNo1);
                        caigoudan.setInPrice(inPrice);
                        caigoudan.setSalePrice(salePrice);
                        caigoudan.setNote(note);
                        caigoudan.setClientID(clientID);
                        caigoudan.setProvider(provder);
                        caigoudan.setAskPriceMan(askPriceMan);
                        caigoudan.setCaigouPlace(caigouPlace);
                        caigoudan.setForeignClient(isForeignClient);
                        caigoudan.setCounts(counts);
                        caigoudan.setProviderID(providerID);
                        caigoudan.setCorpID(corpID);
                        caigoudans.add(caigoudan);
                    }
                    mHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            final String pid = data.getStringExtra("result");
            edPid.setText(pid);
            caigoudans.clear();
            final String partNo = edPartNo.getText().toString();
            if (MyApp.id == null) {
                MyToast.showToast(CaigoudanTakePicActivity.this, "当前登录人为空，请重新登录");
                return;
            }
            getData("", pid);
        }
    }

    //采购单图片地址
    //    172.16.6.22
    //    用户名：mingming
    //    密码：ryDl42QF
    public String getCaigoudanByPidAndPartNo(String checkWord, int buyerId, String partNo, String pid) throws IOException,
            XmlPullParserException {

        //        GetBillByPartNo
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("buyerID", buyerId);
        map.put("pid", pid);
        map.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(map, "GetBillByPartNoAndPid");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils
                .MartService);
        return response.toString();
    }
}
