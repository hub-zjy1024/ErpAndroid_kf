package com.b1b.js.erpandroid_kf;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.MyToast;
import utils.btprint.MyBluePrinter;
import utils.btprint.MyPrinterParent;
import utils.btprint.SPrinter;

public class PrintSettingActivity extends ListActivity {
    private String TAG = "BtSetting";
    private Button bt_scan;
    private LinearLayout layoutscan;
    private TextView tv_status;
    private Thread updateStatusThread;
    private boolean update = true;
    private SharedPreferences sp;
    Context _context;
    SimpleAdapter simpleAdapter;
    MyBluePrinter printer;
    MyPrinterParent printer2;
    List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
    private long nowTime = 0;
    private Handler bHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MyBluePrinter.STATE_SCAN_FINISHED:
                    Log.e("zjy", "PrintSettingActivity->handleMessage(): scan finish==");
//                    addBindedDevice();
                    simpleAdapter.notifyDataSetChanged();
                    MyToast.showToast(PrintSettingActivity.this, "扫描完成");
                    layoutscan.setVisibility(View.INVISIBLE);
                    break;
                case MyBluePrinter.STATE_CONNECTED:
                    MyToast.showToast(PrintSettingActivity.this, "连接成功");
//                    if (msg.obj == null) {
//                        mPrinter = printer;
//                    }

//                    getSharedPreferences("UserInfo", MODE_PRIVATE).edit().putString("btPrinterMac", mPrinter.getAddress())
//                            .commit();
                    mPrinter2 = printer2;
                    setResult(RESULT_OK);
                    finish();
                    break;
                case MyBluePrinter.STATE_DISCONNECTED:
                    MyToast.showToast(PrintSettingActivity.this, "连接失败");
                    break;
            }
        }
    };
    private static MyBluePrinter mPrinter;
    private static MyPrinterParent mPrinter2;

    public static MyBluePrinter getPrint() {
        return mPrinter;
    }

    public static MyPrinterParent getSPrint() {
        return mPrinter2;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printsetting);
        sp = getSharedPreferences("SuccessDevice", 0);
        _context = this;
        layoutscan = (LinearLayout) findViewById(R.id.layoutscan);
        tv_status = (TextView) findViewById(R.id.tv_status);
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
        setListAdapter(simpleAdapter);
        bt_scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listData.size() > 0) {
                    listData.clear();
                    simpleAdapter.notifyDataSetChanged();
                }
//                printer.scan();
                printer2.scan();
            }
        });
        printer = new MyBluePrinter(PrintSettingActivity.this, bHandler, new MyBluePrinter
                .OnReceiveDataHandleEvent() {
            @Override
            public void OnReceive(BluetoothDevice var1) {
                Log.e("zjy", "PrintSettingActivity->OnReceive(): device==" + var1.getAddress() + "\t" +
                        var1.getBondState() + "\t" + var1.getName());
                Map<String, Object> map = new HashMap<String, Object>();
                if (var1.getName() != null) {
                    map.put("title", var1.getName());
                } else {
                    map.put("title", "未知");
                }
                map.put("deviceAddress", var1.getAddress());
                map.put("device", var1);
                listData.add(map);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        printer2 = new SPrinter( bHandler,PrintSettingActivity.this, new MyBluePrinter
                .OnReceiveDataHandleEvent() {
            @Override
            public void OnReceive(BluetoothDevice var1) {
                Log.e("zjy", "PrintSettingActivity->OnReceive(): device==" + var1.getAddress() + "\t" +
                        var1.getBondState() + "\t" + var1.getName());
                Map<String, Object> map = new HashMap<String, Object>();
                if (var1.getName() != null) {
                    map.put("title", var1.getName());
                } else {
                    map.put("title", "未知");
                }
                map.put("deviceAddress", var1.getAddress());
                map.put("device", var1);
                listData.add(map);
//                simpleAdapter.notifyDataSetChanged();
            }
        });
        new Thread(){
            @Override
            public void run() {
//                        printer.open();
                printer2.open();
            }
        };
        addBindedDevice();
    }

    private void addBindedDevice() {
        Set<BluetoothDevice> bindedDevice = printer.getBindedDevice();
        for (BluetoothDevice d : bindedDevice) {
            Log.e("zjy", "PrintSettingActivity->onCreate(): d==" + d.getName());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", d.getName());
            map.put("deviceAddress", d.getAddress());
            map.put("device", d);
            listData.add(map);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printer.unregistReceiver();
        ((SPrinter)printer2).unRegisterReceiver();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    /**
     当List的项被选中时触发
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        Map map = (Map) listView.getItemAtPosition(position);
        //        printer.connect((BluetoothDevice) map.get("device"));
//        printer.connect(map.get("deviceAddress").toString());
        printer2.connect(map.get("deviceAddress").toString());
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - nowTime < 2000)
            finish();
        else {
            MyToast.showToast(this, "再按一次退出");
            nowTime = System.currentTimeMillis();
        }
    }
}
