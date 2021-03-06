package com.b1b.js.erpandroid_kf;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PreChukuAdapter2;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import utils.framwork.MyToast;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;

public class PreChukuActivity extends ToolbarHasSunmiActivity implements View.OnClickListener {

    private Button btnSearch;
    private Button btnSTime;
    private Button btnETime;
    private Button btnClearDate;
    private ListView lv;
    private EditText edPid;
    private List<PreChukuInfo> data;
    private static final int REQUEST_SUCCESS = 1;
    private static final int REQUEST_ERROR = 0;
    private static final int REQUEST_NO_DATA = 2;
    private CheckBox cbo;
    private PreChukuAdapter2 adapter;
    private Handler zHandler = new NoLeakHandler(this);
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case REQUEST_ERROR:
                showMsgToast( "连接服务器失败，请检查网络");
                break;
            case REQUEST_NO_DATA:
                showMsgToast( "查询条件有误");
                break;
            case REQUEST_SUCCESS:
                adapter.notifyDataSetChanged();
                break;
        }
    }
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_chuku);
        btnSearch = (Button) findViewById(R.id.prechuku_search);
        btnSTime = (Button) findViewById(R.id.prechuku_sdate);
        btnETime = (Button) findViewById(R.id.prechuku_edate);
        btnClearDate = (Button) findViewById(R.id.prechuku_cleartime);
        edPid = (EditText) findViewById(R.id.prechuku_ed);
        cbo = (CheckBox) findViewById(R.id.prechuku_cbo);
        btnScan = (Button) findViewById(R.id.prechuku_scan);
        //绑定adapter
        lv = (ListView) findViewById(R.id.prechuku_lv);
        data = new Vector<>();
        adapter = new PreChukuAdapter2(data, this, R.layout.item_caigou_simpleitem);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PreChukuActivity.this, PreChukuDetailActivity.class);
                intent.putExtra("pid", data.get(position).getPid());
                intent.putExtra("storageID", data.get(position).getStorageID());
                startActivity(intent);
            }
        });
        btnScan.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnSTime.setOnClickListener(this);
        btnETime.setOnClickListener(this);
        btnClearDate.setOnClickListener(this);
    }


    @Override
    public String setTitle() {
        return getResString(R.string.title_pre_chuku);
    }

    @Override
    public void resultBack(String result) {
        if (!CheckUtils.checkUID(PreChukuActivity.this)) {
            return;
        }
        edPid.setText(result);
        boolean isANum = MyToast.checkNumber(result);
        if (isANum) {
            data.clear();
            adapter.notifyDataSetChanged();
            SoftKeyboardUtils.closeInputMethod(edPid, PreChukuActivity.this);
            getPreChukuList("", "", Integer.parseInt(result));
        } else {
            showMsgToast( getString(R.string.error_numberformate));
        }
    }

    public String getList(String beginDate, String endDate, String partNo, int pid, int uid) throws IOException, XmlPullParserException {
        //        GetOutStorageNotifyPrintViewList
//        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//        map.put("beginDate", beginDate);
//        map.put("endDate", endDate);
//        map.put("partNo", partNo);
//        map.put("pid", pid);
//        map.put("uid", uid);
//        SoapObject request = WebserviceUtils.getRequest(map, "GetOutStorageNotifyPrintViewList");
//        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils.ChuKuServer);
//        String result = response.toString();
//        if (result.equals("")) {
//            MyApp.myLogger.writeError(PreChukuActivity.class, getResources().getString(R.string.error_soapobject) + pid + "\t" + uid);
//        }
        String result = ChuKuServer.GetOutStorageNotifyPrintViewList(beginDate, endDate, partNo, pid, uid);
        Log.e("zjy", "PreChukuActivity->getList(): result==" + result);
        return result;
//        return response.toString();
    }

    @Override
    public void getCameraScanResult(String result) {
        super.getCameraScanResult(result);
        edPid.setText(result);
        data.clear();
        adapter.notifyDataSetChanged();
        try {
            getPreChukuList("", "", Integer.parseInt(result));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showMsgToast( "扫码结果不是数字");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prechuku_sdate:
                setTime(btnSTime, this);
                break;
            case R.id.prechuku_scan:
//                Intent intent = new Intent(PreChukuActivity.this, CaptureActivity.class);
//                startActivityForResult(intent, 300);
                startScanActivity();
                break;

            case R.id.prechuku_edate:
                setTime(btnETime, this);
                break;

            case R.id.prechuku_cleartime:
                btnSTime.setText("起始时间");
                btnETime.setText("终止时间");
                break;

            case R.id.prechuku_search:
                String pid = edPid.getText().toString().trim();
                if (pid.equals("")) {
                    pid = "0";
                }
                if (data.size()> 0) {
                    data.clear();
                    adapter.notifyDataSetChanged();
                }
                String sTime = btnSTime.getText().toString();
                String eTime = btnETime.getText().toString();
                if (sTime.equals("起始时间")) {
                    sTime = "";
                }
                if (eTime.equals("终止时间")) {
                    eTime = "";
                }
                final String temS = sTime;
                final String temE = eTime;
                int tempID = 0;
                try {
                    tempID = Integer.parseInt(pid);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    showMsgToast( "单据号不合法");
                    return;
                }
                final int id = tempID;
                Log.e("zjy", "PreChukuActivity->onClick(): integer pid==" + id);
                if (!CheckUtils.checkUID(PreChukuActivity.this)) {
                    return;
                }
                SoftKeyboardUtils.closeInputMethod(edPid, PreChukuActivity.this);
                getPreChukuList(temS, temE, id);
                break;
        }
    }

    private void getPreChukuList(final String temS, final String temE, final int id) {
        new Thread() {
            @Override
            public void run() {
                try {
                    MyApp.myLogger.writeInfo("prechuku:getList" + loginID);
                    String res = getList(temS, temE, "", id, Integer.parseInt(loginID));
                    JSONObject root = new JSONObject(res);
                    JSONArray array = root.getJSONArray("表");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String pid = obj.getString("PID");
                        String createDate = obj.getString("制单日期");
                        String chukuType = obj.getString("出库类型");
                        String weituo = obj.getString("委托人");
                        String storageID = obj.getString("仓库");
                        String fahuoPart = obj.getString("发货库区");
                        String diaoruKf = obj.getString("调入仓库");
                        String printCounts = obj.getString("打印次数");
                        String partNo = obj.getString("型号");
                        String couts = obj.getString("数量");
                        String pihao = obj.getString("批号");
                        String factory = obj.getString("厂家");
                        String placedID = obj.getString("位置");
                        String kuqu = obj.getString("库区");
                        String fahuoType = obj.getString("发货类型");
                        String weituoCompanyID = obj.getString("委托公司");
                        PreChukuInfo info = new PreChukuInfo(pid, createDate, chukuType, weituo, storageID, fahuoPart, diaoruKf, printCounts, partNo, couts, pihao, factory, placedID, kuqu, fahuoType, weituoCompanyID);
                        if (printCounts.equals("0")) {
                            data.add(info);
                        } else if (printCounts.equals("")) {
                            data.add(info);
                        } else {
                            if (!cbo.isChecked()) {
                                data.add(info);
                            }
                        }
                    }
                    zHandler.sendEmptyMessage(REQUEST_SUCCESS);
                } catch (IOException e) {
                    zHandler.sendEmptyMessage(REQUEST_ERROR);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    zHandler.sendEmptyMessage(REQUEST_NO_DATA);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void setTime(final Button button, Context mContext) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                button.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

}
