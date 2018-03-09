package printer.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dev.ScanBaseActivity;
import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;
import com.b1b.js.erpandroid_kf.YundanPrintAcitivity;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.WebCallback;
import com.b1b.js.erpandroid_kf.task.WebServicesTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import printer.adapter.SFYundanAdapter;
import printer.entity.Yundan;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;

public class SFActivity extends ScanBaseActivity {

    private List<Yundan> yundanData;
    private EditText edPid;
    private EditText edPartNo;
    private SFYundanAdapter adapter;
    List<WebServicesTask> tasks = new LinkedList<>();


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
                    builder.setItems(new String[]{"顺丰(仅北京)", "跨越"}, new DialogInterface.OnClickListener() {
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
                                    MyApp.myLogger.writeInfo("<page> KYPrint");
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
                                    MyApp.myLogger.writeInfo("<page> KYPrint");
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
                    MyApp.myLogger.writeInfo("<page> KYPrint");
                }

            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.sf_tv);
                TextView tvAlert = (TextView) view.findViewById(R.id.sf_more);
                Yundan item = (Yundan) parent.getItemAtPosition(position);
                tv.setText(item.toString());
                tvAlert.setVisibility(View.GONE);
                return true;
            }
        });

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_sf;
    }

    @Override
    public void resultBack(String result) {
        edPid.setText(result);
        SoftKeyboardUtils.closeInputMethod(edPid, this);
        boolean isNum = MyToast.checkNumber(result);
        if (isNum) {
            yundanData.clear();
            adapter.notifyDataSetChanged();
            getYundanResult();
        }else {
            MyToast.showToast(this, getString(R.string.error_numberformate));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(SettingActivity.PREF_KF, Context.MODE_PRIVATE);
        prefExpress = sp.getString(SettingActivity.PREF_EXPRESS, "");
    }

    public void myOnclick(View view) {
        switch (view.getId()) {
            case R.id.sf_btnSFScan:
                Intent intent = new Intent(SFActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_CODE);
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
        String parno = edPartNo.getText().toString();
        String pid = edPid.getText().toString();
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("pid", pid);
        map.put("xh", parno);
        WebServicesTask<String> t = new WebServicesTask<>(new WebCallback<String>() {
            @Override
            public void errorCallback(Throwable e) {
                String msg = "无数据";
                if (e != null) {
                    msg = e.getMessage();
                }
                MyToast.showToast(SFActivity.this, "查找失败：" + msg);
            }

            @Override
            public void okCallback(String obj) {
                yundanData.clear();
                if (obj == null) {
                    MyToast.showToast(SFActivity.this, "查找失败");
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
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyToast.showToast(SFActivity.this, "找不到相关数据");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void otherCallback(Object obj) {

            }
        }, map);
        try {
            t.executeOnExecutor(TaskManager.getInstance().getExecutor(), "GetYunDanList", WebserviceUtils.SF_SERVER);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            MyToast.showToast(SFActivity.this, "查询太过频繁，请稍后再试。。");
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
            Log.e("zjy", "SFActivity->onStop(): remove==" +i);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE && resultCode == RESULT_OK) {
            String pid = data.getStringExtra("result");
            edPid.setText(pid);
            yundanData.clear();
            getYundanResult();
        }
    }
}
