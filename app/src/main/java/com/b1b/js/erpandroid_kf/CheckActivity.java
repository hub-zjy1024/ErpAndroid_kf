package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.CheckInfoAdapter;
import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.common.MyJsonUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;

public class CheckActivity extends ToolbarHasSunmiActivity implements NoLeakHandler.NoLeakCallback, View.OnClickListener {
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
    private final int err_code = 1;
    private Handler mHandler = new NoLeakHandler(this);
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                SoftKeyboardUtils.closeInputMethod(edPartno, this);
                mAdapter.notifyDataSetChanged();
                showMsgToast( "查询到" + data.size() + "条数据");
                break;
            case err_code:
                mAdapter.notifyDataSetChanged();
                String mmsg = "其他异常";
                if (msg.obj != null) {
                    mmsg = msg.obj.toString();
                }
                showMsgToast( mmsg);
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
                CheckInfo checkInfo = data.get(position);
                onItemClickMy(checkInfo);
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


    protected void onItemClickMy(CheckInfo minfo) {
        Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
        intent.putExtra(IntentKeys.key_pid, minfo.getPid());
        startActivity(intent);
    }
//    @Override
//    public void startScanActivity() {
//        Intent intent = new Intent(this, ZbarScanActivity.class);
//        startActivityForResult(intent,REQ_CODE);
//    }

    @Override
    public void resultBack(String result) {
        edPid.setText(result);
        try {
            Integer.parseInt(result);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showMsgToast( getString(R.string.error_numberformate));
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

    public void OnAutoGo(CheckInfo fInfo) {
        final Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
        intent.putExtra(IntentKeys.key_pid, fInfo.getPid());
        startActivity(intent);
    }
    public void getData(final int typeId, final String pid, final String partNo, boolean auto) {
        final boolean tempAuto = Boolean.valueOf(auto);

        Runnable searchThread = new Runnable() {
            @Override
            public void run() {
                try {
                    String json = getChuKuCheckInfoByTypeID(typeId, pid, partNo, loginID);
                    final List<CheckInfo> list = MyJsonUtils.getCheckInfo(json);
                    if (list != null && list.size() > 0) {
                        data.addAll(list);
                        mHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                SoftKeyboardUtils.closeInputMethod(edPartno, CheckActivity.this);
                                mAdapter.notifyDataSetChanged();
                                if (pd != null && pd.isShowing()) {
                                    pd.cancel();
                                }
                                showMsgToast( "查询到" + data.size() + "条数据");
                                if (tempAuto) {
                                    CheckInfo fInfo = list.get(0);
                                    OnAutoGo(fInfo);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    String errMsg = "查询失败，网络状态不佳";
                    mHandler.obtainMessage(err_code, errMsg).sendToTarget();
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    String errMsg =  "请输入正确的查询条件";
                    mHandler.obtainMessage(err_code, errMsg).sendToTarget();
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(searchThread);
    }

    private String getChuKuCheckInfoByTypeID(int typeId, String pid, String partNo, String uid) throws IOException,
            XmlPullParserException {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return false;
    }

    @Override
    public String setTitle() {
        return "出库审核";
    }

    /**
     * This method will be invoked when a menu item is clicked if the item itself did
     * not already handle the event.
     *
     * @param item {@link MenuItem} that was clicked
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
