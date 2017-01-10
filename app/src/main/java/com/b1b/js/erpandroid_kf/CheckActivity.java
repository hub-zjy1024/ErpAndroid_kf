package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.b1b.js.erpandroid_kf.adapter.CheckInfoAdapter;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.utils.MyJsonUtils;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WcfUtils;

import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView lv;
    private EditText edPid;
    private EditText edPartno;
    private String pid;
    private String partNo;
    private CheckInfoAdapter adapter;
    private List<CheckInfo> data = new ArrayList<>();
    private int checkId = 1;
    private Button btnSearch;
    private Button btnScancode;
    private ProgressDialog pd;
    private RadioButton rdb_checkFirst;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    MyToast.showToast(CheckActivity.this, "查询成功");
                    break;
                case 1:
                    adapter.notifyDataSetChanged();
                    MyToast.showToast(CheckActivity.this, "请输入正确的查询条件");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        lv = (ListView) findViewById(R.id.check_lv);
        edPartno = (EditText) findViewById(R.id.check_ed_partNo);
        edPid = (EditText) findViewById(R.id.check_ed_pid);
        btnSearch = (Button) findViewById(R.id.check_btn_search);
        btnScancode = (Button) findViewById(R.id.check_btn_scancode);
        rdb_checkFirst = (RadioButton) findViewById(R.id.check_rdb_first);
        btnSearch.setOnClickListener(this);
        btnScancode.setOnClickListener(this);
        rdb_checkFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkId = 1;
                } else {
                    checkId = 2;
                }
            }
        });
        adapter = new CheckInfoAdapter(data, this);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
                intent.putExtra("pid", data.get(position).getPid());
                startActivity(intent);
            }
        });
        lv.setAdapter(adapter);
//      getData(1, "", "");
    }

    public void getData(final int typeId, final String pid, final String partNo) {

        new Thread() {
            @Override
            public void run() {
                try {
                    String json = getChuKuCheckInfoByTypeID(typeId, pid, partNo);
                    List<CheckInfo> list = MyJsonUtils.getCheckInfo(json);
                    if (list != null && list.size() > 0) {
                        data.addAll(list);
                        mHandler.sendEmptyMessage(0);
                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(1);
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

    private String getChuKuCheckInfoByTypeID(int typeId, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", "");
        properties.put("typeid", typeId);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        SoapObject request = WcfUtils.getRequest(properties, "GetChuKuCheckInfoByTypeID");
        SoapPrimitive response = WcfUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WcfUtils.ChuKuServer);
        return response.toString();
    }

    private String GetSetCheckInfo(String checkWord, String uid, String stime, String etime, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("t", uid);
        properties.put("info", stime);
        properties.put("pid", etime);
        properties.put("tp", pid);//pass，0不通过，1通过
        properties.put("uname", partNo);
        properties.put("uid", partNo);
        SoapObject request = WcfUtils.getRequest(properties, "GetSetCheckInfo");
        SoapPrimitive response = WcfUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "ChuKuServer.svc");
        Log.e("zjy", "ChuKuActivity.java->GetChuKuTongZhiInfoList(): re" + response.toString());
        return response.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_btn_search:
                pid = edPid.getText().toString().trim();
                partNo = edPartno.getText().toString().trim();
                data.clear();
                adapter.notifyDataSetChanged();
                getData(checkId, pid, partNo);
                break;
            case R.id.check_btn_scancode:
                data.clear();
                adapter.notifyDataSetChanged();
                Intent intent = new Intent(CheckActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 100);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            pid = data.getStringExtra("result");
            edPid.setText(pid);
            getData(1, pid, partNo);
        }
    }
}
