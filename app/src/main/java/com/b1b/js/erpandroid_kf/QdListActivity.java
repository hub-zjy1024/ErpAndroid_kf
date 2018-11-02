package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.mvcontract.QdContract;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import utils.MyToast;

public class QdListActivity extends BaseScanActivity implements View.OnClickListener, QdContract.QdView {

    private EditText edPid;
    private Handler mHandler = new Handler();
    private ProgressDialog pd;
    private TextView tv;
    private QdContract.SHQDPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qd_list);
        android.support.v7.widget.Toolbar tb = (android.support.v7.widget.Toolbar) findViewById(R.id
                .dyjkf_normalTb);
        tb.setTitle("清单");
        tb.setSubtitle("");
        Button btn = (Button) findViewById(R.id.qd_btn_takepic);
        tv = (TextView) findViewById(R.id.qd_ed_dataview);
        edPid = (EditText) findViewById(R.id.qd_ed_pid);
        Button btnSearch = (Button) findViewById(R.id.qd_btn_search);
        Button btnScan = (Button) findViewById(R.id.qd_btn_scan);
        btnSearch.setOnClickListener(this);
        btn.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        presenter   = new QdContract.QdPresenterImpl();
    }

    @Override
    public void getCameraScanResult(String result, int code) {
        super.getCameraScanResult(result, code);
        startSearch(result);
    }

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        startSearch(result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qd_btn_takepic:
                Intent intent = new Intent(this, QdTakePicActivity.class);
                startActivity(intent);
                break;
            case R.id.qd_btn_scan:
                startScanActivity();
                break;
            case R.id.qd_btn_search:
                String pid = edPid.getText().toString();
                presenter.startSearch(pid);
                break;
        }
    }

    public void startSearch(String pid) {
        pd = new ProgressDialog(this);
        pd.setTitle(pid);
        pd.setMessage("正在搜索...");
        pd.show();
    }

    public void getData(String data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/10/31
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean isFail = false;
                if (isFail) {
                    getDataFailed();
                } else {
                    getDataOk();
                }
            }
        };
        TaskManager.getInstance().execute(runnable);
    }

    public void getDataOk() {
        pd.cancel();
        mHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public void getDataFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                pd.cancel();
                MyToast.showToast(QdListActivity.this, "查询不到相关信息！！！");
            }
        });
    }

    @Override
    public void setPrinter(QdContract.SHQDPresenter shqdPresenter) {

    }
}
