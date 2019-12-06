package com.b1b.js.erpandroid_kf.yundan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;
import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoWithScanActivity;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.printer.entity.Yundan;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.WebCallback;
import com.b1b.js.erpandroid_kf.task.WebServicesTask;
import com.b1b.js.erpandroid_kf.yundan.kyeexpress.KyPrintAcitivity;
import com.b1b.js.erpandroid_kf.yundan.sf.SetYundanActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import utils.framwork.DialogUtils;
import utils.framwork.MyToast;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.WebserviceUtils;

public class SFActivity extends SavedLoginInfoWithScanActivity {
    private String storageID = "";
    private List<Yundan> yundanData;
    private EditText edPid;
    private EditText edPartNo;
    private SFYundanAdapter adapter;
    List<WebServicesTask> tasks = new LinkedList<>();
    private ProgressDialog pdDialog;
    private Handler mHandler = new NoLeakHandler(this);
    private SharedPreferences prefKF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sf);
        yundanData = new ArrayList<>();
        edPid = (EditText) findViewById(R.id.yundan_ed_pid);
        edPartNo = (EditText) findViewById(R.id.yundan_ed_partno);
        ListView lv = (ListView) findViewById(R.id.yundan_lv);
        adapter = new SFYundanAdapter(yundanData, this, R.layout.item_sfyundanlist);
        final Intent expressIntent = new Intent();
        adapter.setListener(new SFYundanAdapter.OnExpressListener() {
            private void setIntent(Yundan yundan, Intent intent) {
                Yundan item = yundan;
                String goodInfos = "";
                int n = 0;
                for (int i = 0; i < yundanData.size(); i++) {
                    Yundan good = yundanData.get(i);
                    if (good.getPid().equals(item.getPid())) {
                        String counts = good.getCounts();
                        if (counts.equals("")) {
                            counts = "null";
                        }
                        goodInfos += good.getPartNo() + "&" + counts + "$";
                        n++;
                        if (n == 4) {
                            break;
                        }
                    }
                }
              //  Log.e("zjy", "SFActivity->setIntent(): goodCounts==" + n);
                goodInfos = goodInfos.substring(0, goodInfos.lastIndexOf("$"));
                intent.putExtra("goodInfos", goodInfos);
                intent.putExtra("client", item.getCustomer());
                intent.putExtra(SettingActivity.extra_PID , item.getPid());
                intent.putExtra("times", item.getPrint());
                intent.putExtra("type", item.getType());
            }
            @Override
            public void Ky(Yundan yundan) {
                setIntent(yundan, expressIntent);
                expressIntent.setClass(SFActivity.this, KyPrintAcitivity.class);
                startActivity(expressIntent);
                MyApp.myLogger.writeInfo("<page> KYPrint");
            }

            @Override
            public void Sf(Yundan yundan) {
                setIntent(yundan, expressIntent);
                expressIntent.setClass(SFActivity.this,SetYundanActivity.class);
                startActivity(expressIntent);
                MyApp.myLogger.writeInfo("<page> SFprint");
            }
        });
        pdDialog = new ProgressDialog(this);
        pdDialog.setMessage("正在查询。。。");
        prefKF = getSharedPreferences(SettingActivity.PREF_KF, Context.MODE_PRIVATE);
        String info = prefKF.getString(SpSettings.storageKey, "");
        storageID = StorageUtils.getStorageIDFromJson(info);
        if (storageID.equals("")) {
            pdDialog.setMessage("正在判断库房");
            pdDialog.show();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    String finalStr = "";
                    try {
                        String info = null;
                        info = StorageUtils.getStorageByIp();
                        storageID = StorageUtils.getStorageIDFromJson(info);
                        prefKF.edit().putString(SpSettings.storageKey, info).commit();
                        finalStr = "当前库房ID是：" + storageID;
                    } catch (IOException e) {
                        String msg = e.getMessage();
                        msg += "！！！";
                        finalStr = "获取库房ID出错：" + msg;
                        e.printStackTrace();
                    }
                    final String str = finalStr;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.getSpAlert(mContext, str, "提示").show();
                            pdDialog.setMessage("正在查询。。。");
                            pdDialog.cancel();
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(run);
        }
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.sf_tv);
                TextView tvAlert = (TextView) view.findViewById(R.id.sf_more);
                Yundan item = (Yundan) parent.getItemAtPosition(position);
                tv.setText(item.toString());
                tvAlert.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        String tempPid = getIntent().getStringExtra(SettingActivity.extra_PID);
        if (tempPid != null) {
            edPid.setText(tempPid);
            getYundanResult();
        }
    }


    public void myOnclick(View view) {
        switch (view.getId()) {
            case R.id.sf_btnSFScan:
                startScanActivity();
                break;
            case R.id.sf_btnSFservice:
                if (storageID.equals("")) {
                    showMsgToast( "当前库房ID未知,请重新进入");
                    return;
                }
                SoftKeyboardUtils.closeInputMethod(edPid, this);
                getYundanResult();
                break;
        }
    }

    private void getYundanResult() {
        pdDialog.show();
        String parno = edPartNo.getText().toString();
        String pid = edPid.getText().toString();
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("pid", pid);
        map.put("xh", parno);
        Log.e("zjy", getClass() + "->getYundanResult(): stor==" + storageID);
        map.put("SID", storageID);
        WebServicesTask<String> t = new WebServicesTask<>(new WebCallback<String>() {
            @Override
            public void errorCallback(Throwable e) {
                String msg = "无数据";
                if (e != null) {
                    msg = e.getMessage();
                }
                pdDialog.cancel();
                showMsgToast( "查找失败：" + msg);
            }

            @Override
            public void okCallback(String obj) {
                yundanData.clear();
                if (obj == null) {
                    showMsgToast( "查找失败");
                    return;
                }
                try {
                    JSONObject object = new JSONObject(obj);
                    ArrayList<String> list = new ArrayList<>();
                    JSONArray jArray = object.getJSONArray("表");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject tempObj = jArray.getJSONObject(i);
                        String sPid = tempObj.getString("PID");
                        String createDate = tempObj.getString("制单日期");
                        String state = tempObj.getString("状态");
                        String deptID = tempObj.getString("部门ID");
                        String saleMan = tempObj.getString("业务员");
                        String storageName = tempObj.getString("仓库");
                        String client = tempObj.getString("客户");
                        String backOrderID = tempObj.getString("回执单号");
                        String print = tempObj.getString("打印次数");
                        String shouHuiDan = tempObj.getString("收回单");
                        String partNo = tempObj.getString("型号");
                        String count = tempObj.getString("数量");
                        String pihao = tempObj.getString("批号");
                        String type = tempObj.getString("单据类型");
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
                        yundanData.add(yundan);
                    }
                    showMsgToast( "查找到：" + yundanData.size() + "条数据");
                } catch (JSONException e) {
                    e.printStackTrace();
                    showMsgToast( "找不到相关数据");
                }
                adapter.notifyDataSetChanged();
                pdDialog.cancel();
            }

            @Override
            public void otherCallback(Object obj) {

            }
        }, map);
        try {
            //            t.executeOnExecutor(TaskManager.getInstance().getExecutor(), "GetYunDanList", WebserviceUtils
            // .SF_SERVER);
            t.executeOnExecutor(TaskManager.getInstance().getExecutor(), "GetYunDanListNew", WebserviceUtils.SF_Server);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            showMsgToast( "查询太过频繁，请稍后再试。。");
        }
        tasks.add(t);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        for (int i = 0; true; i++) {
            WebServicesTask webServicesTask = ((LinkedList<WebServicesTask>) tasks).pollLast();
            Log.e("zjy", "SFActivity->onStop(): remove==" + i);
            if (webServicesTask != null) {
                if (!webServicesTask.isCancelled() && webServicesTask.getStatus() == AsyncTask.Status
                        .RUNNING) {
                    webServicesTask.cancel(true);
                }
            } else {
                break;
            }
            webServicesTask.cancel(true);
        }
    }

    public void resultBack(String result) {
        edPid.setText(result);
        SoftKeyboardUtils.closeInputMethod(edPid, this);
        boolean isNum = MyToast.checkNumber(result);
        if (isNum) {
            getYundanResult();
        } else {
            showMsgToast( getString(R.string.error_numberformate));
        }
    }

    @Override
    public void getCameraScanResult(String result) {
        edPid.setText(result);
        SoftKeyboardUtils.closeInputMethod(edPid, this);
        boolean isNum = MyToast.checkNumber(result);
        if (isNum) {
            getYundanResult();
        } else {
            showMsgToast( getString(R.string.error_numberformate));
        }
    }
}
