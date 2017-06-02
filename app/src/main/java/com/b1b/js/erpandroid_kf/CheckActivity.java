package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

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
    private CheckInfoAdapter mAdapter;
    private List<CheckInfo> data = new ArrayList<>();
    private int checkId = 1;
    private Button btnSearch;
    private Button btnScancode;
    private ProgressDialog pd;
    private boolean isFirst = true;
    private RadioButton rdb_checkFirst;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mAdapter.notifyDataSetChanged();
                    MyToast.showToast(CheckActivity.this, "查询到" + data.size() + "条数据");
                    break;
                case 1:
                    mAdapter.notifyDataSetChanged();
                    MyToast.showToast(CheckActivity.this, "请输入正确的查询条件");
                    break;
                case 2:
                    mAdapter.notifyDataSetChanged();
                    MyToast.showToast(CheckActivity.this, "查询失败，网络状态不佳");
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
        mAdapter = new CheckInfoAdapter(data, this);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
                intent.putExtra("pid", data.get(position).getPid());
                startActivity(intent);
            }
        });
        lv.setAdapter(mAdapter);
    }

    public void getData(final int typeId, final String pid, final String partNo) {

        new Thread() {
            @Override
            public void run() {
                try {
                    String json = getChuKuCheckInfoByTypeID(typeId, pid, partNo,MyApp.id);
                    List<CheckInfo> list = MyJsonUtils.getCheckInfo(json);
                    if (list != null && list.size() > 0) {
                        data.addAll(list);
                        mHandler.sendEmptyMessage(0);
                    }
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

    private String getChuKuCheckInfoByTypeID(int typeId, String pid, String partNo,String uid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", "");
        properties.put("typeid", typeId);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        properties.put("uid", uid);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuCheckInfoByTypeID");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        return response.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_btn_search:
                pid = edPid.getText().toString().trim();
                partNo = edPartno.getText().toString().trim();
                if (data.size() > 0) {
                    data.clear();
                    mAdapter.notifyDataSetChanged();
                }
                getData(2, pid, partNo);
                break;
            case R.id.check_btn_scancode:
                Intent intent = new Intent(CheckActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 100);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirst) {
            if (this.data.size() > 0) {
                this.data.clear();
                mAdapter.notifyDataSetChanged();
            }
            pid = edPid.getText().toString().trim();
            partNo = edPartno.getText().toString().trim();
            getData(2, pid, partNo);
        }
        isFirst = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            pid = data.getStringExtra("result");
            edPid.setText(pid);
        }
    }
}
