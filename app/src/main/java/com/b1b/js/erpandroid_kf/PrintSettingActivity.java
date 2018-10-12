package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.MyToast;
import utils.btprint.BtHelper;
import utils.btprint.MyBluePrinter;
import utils.btprint.MyPrinterParent;
import utils.btprint.SPrinter;
import utils.handler.NoLeakHandler;

public class PrintSettingActivity extends AppCompatActivity implements NoLeakHandler.NoLeakCallback {
    private String TAG = "BtSetting";
    private Button bt_scan;
    private TextView tv_status;
    private String macAddr;
    Context _context;
    SimpleAdapter simpleAdapter;
    MyBluePrinter printer;
    MyPrinterParent printer2;
    List<Map<String, Object>> listData = new ArrayList<>();
    private long nowTime = 0;
    private Handler bHandler = new NoLeakHandler(this);

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BtHelper.STATE_SCAN_FINISHED:
                pdScanDialog.cancel();
                MyToast.showToast(PrintSettingActivity.this, "扫描完成");
                tv_status.setText("搜索完成");
                progress.setVisibility(View.INVISIBLE);
                break;
            case BtHelper.STATE_CONNECTED:
                pdDialog.cancel();
                MyToast.showToast(PrintSettingActivity.this, "连接成功");
                getSharedPreferences(SettingActivity.PREF_USERINFO, MODE_PRIVATE).edit().putString
                        ("btPrinterMac", macAddr)
                        .apply();
                setResult(RESULT_OK);
                finish();
                break;
            case BtHelper.STATE_DISCONNECTED:
                pdDialog.cancel();
                MyToast.showToast(PrintSettingActivity.this, "连接失败");
                break;
            case BtHelper.STATE_OPENED:
                Set<BluetoothDevice> bindedDevice = ((SPrinter) printer2).getBindedDevice();
                List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
                for (BluetoothDevice d : bindedDevice) {
                    Map<String, String> map = new HashMap<>();
                    if (d.getName() != null) {
                        map.put("title", d.getName());
                    } else {
                        map.put("title", "未知");
                    }
                    map.put("deviceAddress", d.getAddress());
                    listData.add(map);
                }
                SimpleAdapter bonedAdapter = new SimpleAdapter(PrintSettingActivity.this, listData, android.R.layout
                        .simple_list_item_2, new
                        String[]{"title", "deviceAddress"}, new int[]{android.R.id.text1, android.R.id.text2});
                lvBounded.setAdapter(bonedAdapter);
                lvBounded.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Map map = (Map) parent.getItemAtPosition(position);
                        macAddr = map.get("deviceAddress").toString();
                        pdDialog.show();
                        Runnable run = new Runnable() {
                            @Override
                            public void run() {
                                printer2.connect(macAddr);
                            }
                        };
                        TaskManager.getInstance().execute(run);
                    }
                });
                break;
        }
    }
    private static MyBluePrinter mPrinter;
    private ProgressDialog pdDialog;
    private ProgressDialog pdScanDialog;
    private ProgressBar progress;
    private ListView lvBounded;

    public static MyBluePrinter getPrint() {
        return mPrinter;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printsetting);
        _context = this;
        pdDialog = new ProgressDialog(this);
        pdDialog.setMessage("正在连接中");
        pdDialog.setTitle("提示");
        pdScanDialog = new ProgressDialog(this);
        pdScanDialog.setMessage("正在搜索蓝牙设备");
        pdScanDialog.setTitle("提示");
        tv_status = (TextView) findViewById(R.id.tv_status);
        progress = (ProgressBar) findViewById(R.id.progressBar2);
         lvBounded = (ListView) findViewById(R.id.bt_setting_lv_bonded);
        bt_scan = (Button) findViewById(R.id.bt_scan);
        simpleAdapter = new SimpleAdapter(this, listData, android.R.layout.simple_list_item_2, new
                String[]{"title", "deviceAddress"}, new int[]{android.R.id.text1, android.R.id.text2});
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setText(data.toString());
                    return true;
                }
                return false;
            }

        });
        ListView lv = (ListView) findViewById(R.id.activity_print_setting_lv);
        lv.setAdapter(simpleAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = (Map) parent.getItemAtPosition(position);
                macAddr = map.get("deviceAddress").toString();
                pdDialog.show();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        ((SPrinter) printer2).stopScan();
                        printer2.connect(macAddr);
                    }
                };
                TaskManager.getInstance().execute(run);
            }
        });
        bt_scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listData.clear();
                simpleAdapter.notifyDataSetChanged();
                tv_status.setText("开始搜索");
                MyApp.myLogger.writeInfo("startToScan BtDevices");
                progress.setVisibility(View.VISIBLE);
                printer2.scan();
            }
        });
        printer2 = SPrinter.getPrinter(this, new SPrinter.MListener() {
            @Override
            public void sendMsg(int what) {
                bHandler.sendEmptyMessage(what);
            }

            @Override
            public void onDeviceReceive(BluetoothDevice d) {
                Map<String, Object> map = new HashMap<>();
                if (d.getName() != null) {
                    map.put("title", d.getName());
                } else {
                    map.put("title", "未知");
                }
                map.put("deviceAddress", d.getAddress());
                map.put("device", d);
                Log.e("zjy", "PrintSettingActivity->onDeviceReceive(): device==" + d.toString());
                listData.add(map);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        Runnable openBtRun=new Runnable(){
            @Override
            public void run() {
                if (!printer2.isOpen()) {
                    printer2.open();
                } else {
                    bHandler.sendEmptyMessage(BtHelper.STATE_OPENED);
                }
            }
        };
        TaskManager.getInstance().execute(openBtRun);
    }

    private void addBindedDevice() {
        Set<BluetoothDevice> bindedDevice = ((SPrinter) printer2).getBindedDevice();
        for (BluetoothDevice d : bindedDevice) {
            Log.e("zjy", "PrintSettingActivity->onCreate(): d==" + d.getName());
            Map<String, Object> map = new HashMap<>();
            map.put("title", d.getName());
            map.put("deviceAddress", d.getAddress());
            map.put("device", d);
            listData.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((SPrinter)printer2).unRegisterReceiver();
    }
}
