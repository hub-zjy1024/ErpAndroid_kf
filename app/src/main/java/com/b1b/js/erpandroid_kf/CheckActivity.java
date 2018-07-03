package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.CheckInfoAdapter;
import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.MyJsonUtils;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.wsdelegate.ChuKuServer;

public class CheckActivity extends SavedLoginInfoWithScanActivity implements NoLeakHandler.NoLeakCallback, View.OnClickListener {
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
    private Handler mHandler = new NoLeakHandler(this);
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                SoftKeyboardUtils.closeInputMethod(edPartno, this);
                mAdapter.notifyDataSetChanged();
                MyToast.showToast(this, "查询到" + data.size() + "条数据");
                break;
            case 1:
                mAdapter.notifyDataSetChanged();
                MyToast.showToast(this, "请输入正确的查询条件");
                break;
            case 2:
                mAdapter.notifyDataSetChanged();
                MyToast.showToast(this, "查询失败，网络状态不佳");
                break;
        }
        if (pd != null) {
            pd.cancel();
        }
    }
    private CheckBox cboStart;
    private boolean isScan = false;

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
        cboStart = (CheckBox) findViewById(R.id.check_cbo_autostart);
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
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                CheckInfo item = (CheckInfo) parent.getItemAtPosition(position);
                tv.setText(item.toString());
                TextView tvMore = (TextView) view.findViewById(R.id.chukudan_items_tvMore);
                tvMore.setVisibility(View.GONE);
                return true;
            }
        });
        lv.setAdapter(mAdapter);
        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在查询。。。");
    }

    @Override
    public void resultBack(String result) {
        edPid.setText(result);
        try {
            Integer.parseInt(result);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            MyToast.showToast(CheckActivity.this, getString(R.string.error_numberformate));
            return;
        }
        pid = result;
        partNo = edPartno.getText().toString().trim();
        data.clear();
        mAdapter.notifyDataSetChanged();
        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
        getData(2, pid, partNo, cboStart.isChecked());
    }

    public void getData(final int typeId, final String pid, final String partNo, boolean auto) {
        final boolean tempAuto = Boolean.valueOf(auto);

        Runnable searchThread = new Runnable() {
            @Override
            public void run() {
                try {
                    String json = getChuKuCheckInfoByTypeID(typeId, pid, partNo, loginID);
                    List<CheckInfo> list = MyJsonUtils.getCheckInfo(json);
                    if (list != null && list.size() > 0) {
                        data.addAll(list);
                        final Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
                        CheckInfo fInfo = list.get(0);
                        intent.putExtra("pid", fInfo.getPid());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                SoftKeyboardUtils.closeInputMethod(edPartno, CheckActivity.this);
                                mAdapter.notifyDataSetChanged();
                                if (pd != null && pd.isShowing()) {
                                    pd.cancel();
                                }
                                MyToast.showToast(CheckActivity.this, "查询到" + data.size() + "条数据");
                                if (tempAuto) {
                                    startActivity(intent);
                                }
                            }
                        });
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
        };
        TaskManager.getInstance().execute(searchThread);
    }

    private String getChuKuCheckInfoByTypeID(int typeId, String pid, String partNo, String uid) throws IOException,
            XmlPullParserException {
//        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
//        properties.put("checkWord", "");
//        properties.put("typeid", typeId);
//        properties.put("pid", pid);
//        properties.put("partNo", partNo);
//        properties.put("uid", uid);
//        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuCheckInfoByTypeID");
//        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils.ChuKuServer);
//        return response.toString()
        return ChuKuServer.GetChuKuCheckInfoByTypeID("",typeId, pid, partNo, uid);
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
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }
                SoftKeyboardUtils.closeInputMethod(edPid, CheckActivity.this);
                getData(2, pid, partNo, false);
                break;
            case R.id.check_btn_scancode:
                //                Intent intent = new Intent(CheckActivity.this, CaptureActivity.class);
                //                startActivityForResult(intent, 100);
                startScanActivity();
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
            if (isScan) {
                getData(2, pid, partNo, cboStart.isChecked());
                isScan = false;
            } else {
                getData(2, pid, partNo, false);
            }
        }
        isFirst = false;
    }

    @Override
    public void getCameraScanResult(String result) {
        pid = result;
        edPid.setText(pid);
        isScan = true;
    }
}
