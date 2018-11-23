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
import com.b1b.js.erpandroid_kf.contract.ChukuCheckContract;
import com.b1b.js.erpandroid_kf.entity.CheckInfo;

import java.util.ArrayList;
import java.util.List;

import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;

public class CheckActivity extends SavedLoginInfoWithScanActivity implements NoLeakHandler.NoLeakCallback, View.OnClickListener,ChukuCheckContract.ChukuCheckView {
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
    private ChukuCheckContract.ChukuCheckPresenter mPresenter;
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
        mPresenter = new ChukuCheckContract.ChukuCheckPresenter(this);
    }

    public boolean validScanResult(String result) {
        try {
            Integer.parseInt(result);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            MyToast.showToast(CheckActivity.this, getString(R.string.error_numberformate));
            return false;
        }
        return true;
    }
    @Override
    public void resultBack(String result) {
        if (!validScanResult(result)) {
            return;
        }
        edPid.setText(result);
        pid = result;
        partNo = edPartno.getText().toString().trim();
      /*   data.clear();
       mAdapter.notifyDataSetChanged();
        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
        getData(2, pid, partNo, cboStart.isChecked());*/
        mPresenter.searchData(2, pid, partNo, loginID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_btn_search:
                SoftKeyboardUtils.closeInputMethod(edPid, CheckActivity.this);
                pid = edPid.getText().toString().trim();
                partNo = edPartno.getText().toString().trim();
               /* if (data.size() > 0) {
                    data.clear();
                    mAdapter.notifyDataSetChanged();
                }
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }

                getData(2, pid, partNo, false);*/
                mPresenter.searchData(2, pid, partNo, loginID);
                break;
            case R.id.check_btn_scancode:
                startScanActivity();
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
      /*  if (!isFirst) {
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
        isFirst = false;*/
        if (!isFirst) {
            pid = edPid.getText().toString().trim();
            partNo = edPartno.getText().toString().trim();
            if (isScan) {
                if (!validScanResult(pid)) {
                    return;
                }
                isScan = false;
            }
            mPresenter.searchData(2, pid, partNo, loginID);
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
    public void onSearchFinish(int code, List<CheckInfo> dataList, String msg) {
        pd.cancel();
        if (code == 1) {
            data.clear();
            data.addAll(dataList);
            mAdapter.notifyDataSetChanged();
            MyToast.showToast(CheckActivity.this, "查询到" + data.size() + "条数据");
            if (isScan) {
                if (cboStart.isChecked()) {
                    final Intent intent = new Intent(CheckActivity.this, SetCheckInfoActivity.class);
                    CheckInfo fInfo = data.get(0);
                    intent.putExtra("pid", fInfo.getPid());
                    startActivity(intent);
                }
            }
        }else{
            MyToast.showToast(this, "查询不到相关信息！！");
        }
        SoftKeyboardUtils.closeInputMethod(edPartno, CheckActivity.this);

    }

    @Override
    public void showProgress(String msg) {
        pd.setMessage(msg);
        pd.show();
    }
}
