package printer.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;
import com.b1b.js.erpandroid_kf.YundanPrintAcitivity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import printer.adapter.SFYundanAdapter;
import printer.entity.Yundan;
import utils.SoftKeyboardUtils;

public class SFActivity extends AppCompatActivity {

    private List<Yundan> yundanData;
    private EditText edPid;
    private EditText edPartNo;
    private SFYundanAdapter adapter;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    Toast.makeText(SFActivity.this, "查询不到数据，请更换查询条件", Toast
                            .LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(SFActivity.this, "连接服务器失败", Toast
                            .LENGTH_SHORT).show();
                    break;
                case 3:
                    break;

            }
        }
    };

    private String prefExpress = "";
    private CheckBox changeExpress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sf);
        yundanData = new ArrayList<>();
        edPid = (EditText) findViewById(R.id.yundan_ed_pid);
        edPartNo = (EditText) findViewById(R.id.yundan_ed_partno);
        ListView lv = (ListView) findViewById(R.id.yundan_lv);
        changeExpress = (CheckBox) findViewById(R.id.sf_cbo_chage);
        adapter = new SFYundanAdapter(yundanData, this, R.layout.item_sfyundanlist);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (position > yundanData.size()) {
                    Log.e("zjy", "SFActivity->onItemClick(): noYundan==");
                    return;
                }
                final Intent intent = new Intent(SFActivity.this, SetYundanActivity.class);
                Yundan item = (Yundan) parent.getItemAtPosition(position);
                String goodInfos = "";
                int n = 0;
                for (int i = 0; i < yundanData.size(); i++) {
                    Yundan good = yundanData.get(i);
                    if (good.getPid().equals(item.getPid())) {
                        String counts = good.getCounts();
                        if (counts.equals("")) {
                            counts = "null";
                        }
                        goodInfos += good.getPartNo() + "&" +counts  + "$";
                        n++;
                        if (n == 4) {
                            break;
                        }
                    }
                }
                Log.e("zjy", "SFActivity->onItemClick(): goodCounts==" + n);
                goodInfos = goodInfos.substring(0, goodInfos.lastIndexOf("$"));
                intent.putExtra("goodInfos", goodInfos);
                intent.putExtra("client", item.getCustomer());
                intent.putExtra("pid", item.getPid());
                intent.putExtra("times", item.getPrint());
                intent.putExtra("type", item.getType());
                if (prefExpress.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SFActivity.this);
                    builder.setTitle("请选择快递");
                    builder.setItems(new String[]{"顺丰快递", "跨越快递(深圳可用)"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    intent.setClass(SFActivity.this, SetYundanActivity.class);
                                    startActivity(intent);
                                    MyApp.myLogger.writeInfo("<page> SFprint");
                                    break;
                                case 1:
                                    intent.setClass(SFActivity.this, YundanPrintAcitivity.class);
                                    startActivity(intent);
                                    MyApp.myLogger.writeInfo("<page> fileprint");
                                    break;
                            }
                        }
                    });
                    builder.setNegativeButton("配置默认快递", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sIntent = new Intent(SFActivity.this, SettingActivity.class);
                            startActivity(sIntent);
                        }
                    });
                    builder.show();
                } else if (changeExpress.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SFActivity.this);
                    builder.setTitle("请选择快递");
                    builder.setItems(new String[]{"顺丰快递", "跨越快递(深圳可用)"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    intent.setClass(SFActivity.this, SetYundanActivity.class);
                                    startActivity(intent);
                                    MyApp.myLogger.writeInfo("<page> SFprint");
                                    break;
                                case 1:
                                    intent.setClass(SFActivity.this, YundanPrintAcitivity.class);
                                    startActivity(intent);
                                    MyApp.myLogger.writeInfo("<page> fileprint");
                                    break;
                            }
                        }
                    });
                    builder.setNegativeButton("配置默认快递", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sIntent = new Intent(SFActivity.this, SettingActivity.class);
                            startActivity(sIntent);
                        }
                    });
                    builder.show();
                }else if (prefExpress.equals(getString(R.string.express_sf))) {
                    intent.setClass(SFActivity.this, SetYundanActivity.class);
                    startActivity(intent);
                    MyApp.myLogger.writeInfo("<page> SFprint");
                } else if (prefExpress.equals(getString(R.string.express_ky))) {
                    intent.setClass(SFActivity.this, YundanPrintAcitivity.class);
                    startActivity(intent);
                    MyApp.myLogger.writeInfo("<page> fileprint");
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        prefExpress = sp.getString("prefExpress", "");
    }

    public void myOnclick(View view) {
        switch (view.getId()) {
            case R.id.sf_btnSFScan:
                Intent intent = new Intent(SFActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 300);
                break;
            case R.id.sf_btnSFservice:

                SoftKeyboardUtils.closeInputMethod(edPid,this);
                if (yundanData.size() > 0) {
                    yundanData.clear();
                    adapter.notifyDataSetChanged();
                }
                getYundanResult();
                break;
            case R.id.sf_btnSFdiaohuo:
                Intent dhIntent = new Intent(SFActivity.this, SetYundanActivity.class);
                dhIntent.putExtra("prefExpress", "diaohuo");
                dhIntent.putExtra("pid", "00000");
                dhIntent.putExtra("times", "");
                startActivity(dhIntent);
                break;
        }
    }

    private void getYundanResult() {

        new Thread() {
            @Override
            public void run() {
                try {
                    String parno = edPartNo.getText().toString();
                    String pid = edPid.getText().toString();
                    //                    pid = "1154510";
                    getYundanList(pid, parno);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300&&resultCode==RESULT_OK) {
            String pid = data.getStringExtra("result");
            edPid.setText(pid);
            yundanData.clear();
            getYundanResult();
        }
    }

    private void getYundanList(String pid, String xh) throws IOException,
            XmlPullParserException, JSONException {
        SoapObject requestList = new SoapObject("http://tempuri.org/", "GetYunDanList");
        requestList.addProperty("pid", pid); requestList.addProperty("xh", xh);
        /*设置版本号，ver11，和ver12比较常见*/
        SoapSerializationEnvelope envelopeList = new SoapSerializationEnvelope
                (SoapEnvelope.VER11);
        envelopeList.setOutputSoapObject(requestList);
        envelopeList.dotNet = true;
        HttpTransportSE transList = new HttpTransportSE("http://172.16.6" +
                ".160:8006/SF_Server.svc?wsdl", 15 * 1000);
        String actionLIst = "http://tempuri.org/ISF_Server/GetYunDanList";
        transList.call(actionLIst, envelopeList);
        SoapPrimitive soapList = (SoapPrimitive) envelopeList.getResponse();
        Log.e("zjy", "SFActivity->run(): yundanInfoList==" + soapList
                .toString());
        JSONObject object = new JSONObject(soapList.toString());
        ArrayList<String> list = new ArrayList<>();
        JSONArray jArray = object.getJSONArray("表");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject obj = jArray.getJSONObject(i);
            String sPid = obj.getString("PID");
            String createDate = obj.getString("制单日期");
            String state = obj.getString("状态");
            String deptID = obj.getString("部门ID");
            String saleMan = obj.getString("业务员");
            String storageName = obj.getString("仓库");
            String client = obj.getString("客户");
            String backOrderID = obj.getString("回执单号");
            String print = obj.getString("打印次数");
            String shouHuiDan = obj.getString("收回单");
            String partNo = obj.getString("型号");
            String count = obj.getString("数量");
            String pihao = obj.getString("批号");
            String type = obj.getString("单据类型");
            Yundan yundan = new Yundan();
            yundan.setType(type);
            yundan.setPid(sPid);
            yundan.setCreateDate(createDate);
            yundan.setState(state);
            yundan.setDeptID(deptID);
            yundan.setSaleMan(saleMan);
            yundan.setStorageName(storageName);
            yundan.setCustomer(client);
            yundan.setRecieveBackNo(backOrderID);
            yundan.setPrint(print);
            yundan.setShouHuiDan(shouHuiDan);
            yundan.setPartNo(partNo);
            yundan.setCounts(count);
            yundan.setPihao(pihao);
//            if (client.equals("")) {
//                yundanData.add(0, yundan);
//            } else {
//                yundanData.add(yundan);
//            }
            yundanData.add(yundan);
        }
        mHandler.sendEmptyMessage(0);
    }

    public String getData() {
        return null;
    }
}
