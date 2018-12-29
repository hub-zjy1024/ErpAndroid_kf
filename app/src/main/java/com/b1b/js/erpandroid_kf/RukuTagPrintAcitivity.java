package com.b1b.js.erpandroid_kf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.XiaopiaoAdapter;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.receiver.AlarmRepeatReceive;
import com.b1b.js.erpandroid_kf.receiver.OneShotReceiver;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import printer.entity.XiaopiaoInfo;
import utils.DialogUtils;
import utils.MyToast;
import utils.PrinterStyle;
import utils.SoftKeyboardUtils;
import utils.btprint.BtHelper;
import utils.btprint.MyBluePrinter;
import utils.btprint.SPrinter;
import utils.handler.NoLeakHandler;
import utils.wsdelegate.ChuKuServer;

public class RukuTagPrintAcitivity extends BaseScanActivity {
    private Handler mHandler = new NoLeakHandler(this);
    private final static int FLAG_PRINT = 3;
    private String storageID = "";
    public static final String storageKey = "storageID";

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case BtHelper.STATE_CONNECTED:
                MyToast.showToast(this, "连接成功");
                this.tvState.setTextColor(Color.GREEN);
                this.tvState.setText("已连接");
                break;
            case BtHelper.STATE_DISCONNECTED:
                MyToast.showToast(this, "连接失败");
                this.tvState.setTextColor(Color.RED);
                this.tvState.setText("连接失败");
                break;
            case FLAG_PRINT:
                if (this.cboAuto.isChecked()) {
                    Runnable printRun = new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i <infos.size(); i++) {
                                XiaopiaoInfo tInfo = infos.get(i);
                                PrinterStyle.printXiaopiao2(printer2, tInfo);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    TaskManager.getInstance().execute(printRun);
                }
                this.xpAdapter.notifyDataSetChanged();
                break;
            case BtHelper.STATE_OPENED:
                if (!this.btAddress.equals("")) {
                    this.tvState.setText("正在连接");
                    Runnable connectRun = new Runnable() {
                        @Override
                        public void run() {
                            printer2.connect(btAddress);
                            Log.e("zjy", "RukuTagPrintAcitivity->run(): state open==");
                        }
                    };
                    TaskManager.getInstance().execute(connectRun);
                }
                break;
        }
    }


    private MyBluePrinter printer;
    private SPrinter printer2;
    private Context mContext = this;
    private int reqCode = 500;
    private TextView tvState;
    private Button btnSetting;
    private Button btnScan;
    private EditText edPid;
    private Button btnPrint;
    private List<XiaopiaoInfo> infos;
    private XiaopiaoAdapter xpAdapter;
    private Button btnSearch;
    private ImageView ivTest;
    private String btAddress;
    private TextView tvTitle;
    private boolean isOffline = false;
    public static String extraMode = "mode";
    public static String MODE_OFFLINE = "offline";
    private CheckBox cboAuto;
    private SharedPreferences prefKF;
    ProgressDialog pdDialog;
    private MaterialDialog alertDialog;
    private long time1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruku_tag_print_acitivity);
        ListView lv = (ListView) findViewById(R.id.ruku_lv);
        tvState = (TextView) findViewById(R.id.rukutag_activity_tv_state);
        edPid = (EditText) findViewById(R.id.rukutag_activity_ed_pid);
        btnSetting = (Button) findViewById(R.id.rukutag_activity_btn_setting);
        tvTitle = (TextView) findViewById(R.id.rukutag_activity_title);
        btnScan = (Button) findViewById(R.id.rukutag_activity_btn_scancode);
        btnPrint = (Button) findViewById(R.id.rukutag_activity_btn_print);
        btnSearch = (Button) findViewById(R.id.rukutag_activity_btn_search);
        final CheckBox oboOnlyCode = (CheckBox) findViewById(R.id.ruku_cbo_offline);
        cboAuto = (CheckBox) findViewById(R.id.ruku_cbo_autoprint);
        ivTest = (ImageView) findViewById(R.id.test_iv);
        alertDialog = new MaterialDialog(this);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setTitle("错误");
        prefKF = getSharedPreferences(SettingActivity.PREF_KF, Context.MODE_PRIVATE);
        String storageInfo = prefKF.getString(storageKey, "");
        storageID = getStorageIDFromJson(storageInfo);
                time1 = System.currentTimeMillis();
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();

                //                Intent intent = new Intent(mContext, CaptureActivity.class);
                //                startActivityForResult(intent, CaptureActivity.REQ_CODE);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneShotAlarm();
                //                                ivTest.setImageBitmap(BarcodeCreater.creatBarcode(RukuTagPrintAcitivity.this,
                // "123487523", 40
                //                 * 8, 50, true, 10));
                if (System.currentTimeMillis() - time1 < 500) {
                    MyToast.showToast(RukuTagPrintAcitivity.this, "点击频率过快");
                    return;
                }
                time1 = System.currentTimeMillis();
                final String pid = edPid.getText().toString();
                SoftKeyboardUtils.closeInputMethod(edPid, mContext);
                if (pid.equals("")) {
                    MyToast.showToast(mContext, "请输入明细ID号");
                    return;
                }
                infos.clear();
                getData(pid);
            }
        });
        final String mode = getIntent().getStringExtra(extraMode);
        if (MODE_OFFLINE.equals(mode)) {
            btnSearch.setVisibility(View.GONE);
            oboOnlyCode.setVisibility(View.GONE);
            tvTitle.setText("条码打印");
            isOffline = true;
        }
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PrintSettingActivity.class);
                startActivityForResult(intent, reqCode);
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printer2 == null) {
                    MyToast.showToast(mContext, "请先配置蓝牙打印机，并确认已连接");
                    return;
                }

                Runnable printRun = new Runnable() {
                    @Override
                    public void run() {
//                        printer2.getPaperStatus();
                        String code = edPid.getText().toString().trim();
                        if (code.equals("")) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MyToast.showToast(RukuTagPrintAcitivity.this, "请输入明细ID");
                                }
                            });
                            return;
                        }
                        if (oboOnlyCode.isChecked()) {
                            printer2.printBarCode(code, 0, 1, 80);
                            printer2.printText("M" + code);
                            printer2.newLine(3);
                            return;
                        }
                        if (isOffline) {
                            printer2.printBarCode(code, 0, 1, 80);
                            printer2.printText(code);
                            printer2.newLine(3);
                            return;
                        }
                        for (int i = 0; i < infos.size(); i++) {
                            XiaopiaoInfo tInfo = infos.get(i);
                            PrinterStyle.printXiaopiao2(printer2, tInfo);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        MyApp.myLogger.writeInfo("start print rkTag,pid=" + edPid.getText().toString());
                    }
                };
                TaskManager.getInstance().execute(printRun);
            }
        });
        infos = new ArrayList<>();
        xpAdapter = new XiaopiaoAdapter(this, infos, R.layout.item_rukutag);
        lv.setAdapter(xpAdapter);
        SharedPreferences userInfo = getSharedPreferences(SettingActivity.PREF_USERINFO, Context.MODE_PRIVATE);
        btAddress = userInfo.getString("btPrinterMac", "");
        Log.e("zjy", "RukuTagPrintAcitivity->run(): printerAddress==" + btAddress);

        if (btAddress.equals("")) {
            DialogUtils.getSpAlert(mContext, "暂无连接打印机记录，是否前往配置", "提示", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(mContext, PrintSettingActivity.class);
                    startActivityForResult(intent, reqCode);
                }
            }, "是", null, "否").show();
        } else {
            printer2 = SPrinter.getPrinter(mContext, new SPrinter.MListener() {
                @Override
                public void sendMsg(int what) {
                    mHandler.sendEmptyMessage(what);
                }

                @Override
                public void onDeviceReceive(BluetoothDevice d) {

                }
            });
//            printer2.registeBroadCast();
            Runnable connetRunnable = new Runnable() {
                @Override
                public void run() {
                    if (printer2.isOpen()) {
                        boolean isConnect = printer2.initPrinter();
                        if (!isConnect) {
                            printer2.connect(btAddress);
                        } else {
                            mHandler.sendEmptyMessage(SPrinter.STATE_CONNECTED);
                        }
                    } else {
                        printer2.open();
                    }
                }
            };
            TaskManager.getInstance().execute(connetRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (printer2 != null) {
            printer2.unRegisterReceiver();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (printer2 != null) {
            printer2.registeBroadCast();
        }
    }

    public void registerOnshotAlarm(Context mContext) {
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction(mContext.getPackageName() + ".alarm.oneshot");
        mContext.registerReceiver(new OneShotReceiver(), alarmFilter);
    }

    public void setRepeatAlarm() {
        Intent intent = new Intent(this,
                AlarmRepeatReceive.class);
        intent.setAction(mContext.getPackageName() + ".alarm.repeat");
        PendingIntent sender = PendingIntent.getBroadcast(
                this, 0, intent, 0);
        // We want the alarm to go off 10 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        // Schedule the alarm!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), 60 * 1000, sender);
        }
    }
    public void oneShotAlarm(){
        Context mContext = this;
        Intent intent = new Intent(mContext, OneShotReceiver.class);
        intent.setAction(mContext.getPackageName() + ".alarm.oneshot");
        PendingIntent sender = PendingIntent.getBroadcast(
                mContext, 0, intent, 0);
        // We want the alarm to go off 10 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }
    @Override
    public void resultBack(String result) {
        boolean isNum = MyToast.checkNumber(result);
        edPid.setText(result);
        if (isNum) {
            infos.clear();
            getData(result);
        } else {
            MyToast.showToast(this, getString(R.string.error_numberformate));
        }
    }

    public void getData(final String pid) {
        Runnable dataRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (storageID.equals("")) {
                        String info = getStorageByIp();
                        storageID = getStorageIDFromJson(info);
                        prefKF.edit().putString(storageKey, info).commit();
                    }
                    MyApp.myLogger.writeInfo("Storageid " + storageID);
                    String balaceInfo = getBalaceInfo(pid, storageID, "");
                    JSONObject jobj = new JSONObject(balaceInfo);
                    JSONArray jarray = jobj.getJSONArray("表");
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject tj = jarray.getJSONObject(i);
                        String parno = tj.getString("型号");
                        String pid = tj.getString("单据号");
                        String time = tj.getString("入库日期");
                        String temp[] = time.split(" ");
                        if (temp.length > 1) {
                            time = temp[0];
                        }
                        time = time.replaceAll("/", "-");
                        String deptno = tj.getString("部门号");
                        String counts = tj.getString("剩余数量");
                        String factory = tj.getString("厂家");
                        String producefrom = "";
                        String pihao = tj.getString("批号");
                        String fengzhuang = tj.getString("封装");
                        String description = tj.getString("描述");
                        String place = tj.getString("位置");
                        String storageID = RukuTagPrintAcitivity.this.storageID;
                        String flag = tj.getString("SQInvoiceType");
                        String company = tj.getString("name");
                        String notes = tj.getString("备注");
                        String detailPID = tj.getString("明细ID");
                        XiaopiaoInfo info = new XiaopiaoInfo(parno, deptno, time, deptno, counts, factory,
                                producefrom, pihao, fengzhuang, description, place, notes, flag, detailPID, storageID,
                                company);
                        info.setPid(pid);
                        infos.add(info);
                    }
                    //                    instorageInfo(pid);
                    mHandler.sendEmptyMessage(FLAG_PRINT);
                } catch (final IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.setMessage("查询错误：" + e.getMessage());
                            alertDialog.show();
                        }
                    });
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyToast.showToast(RukuTagPrintAcitivity.this, "查询不到相关信息");
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(dataRunnable);
    }

    public static String getStorageIDFromJson(String info) {
        return getStorageInfo(info, "StoreRoomID");
    }
    public static String getStorageInfo(String info, String tag) {
        String storageID = "";
        try {
            JSONObject obj = new JSONObject(info);
            JSONArray table = obj.getJSONArray("表");
            storageID = table.getJSONObject(0).getString(tag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return storageID;
    }
    protected void instorageInfo(String mxID) throws IOException, XmlPullParserException, JSONException {
        String detailInfo = getDetailInfoByDetailId(mxID);
        Log.e("zjy", "RukuTagPrintAcitivity->run(): detailInfo==" + detailInfo);
        JSONObject jobj = new JSONObject(detailInfo);
        JSONArray jarray = jobj.getJSONArray("表");
        for (int i = 0; i < jarray.length(); i++) {
            JSONObject tj = jarray.getJSONObject(i);
            String parno = tj.getString("型号");
            String pid = tj.getString("PID");
            String time = tj.getString("制单日期");
            String temp[] = time.split(" ");
            if (temp.length > 1) {
                time = temp[0];
            }
            String deptno = tj.getString("DeptID");
            String counts = tj.getString("数量");
            String factory = tj.getString("厂家");
            String producefrom = "";
            String pihao = tj.getString("批号");
            String fengzhuang = tj.getString("封装");
            String description = tj.getString("描述");
            String place = "";
            String storageID = tj.getString("StorageID");
            String flag = tj.getString("InvoiceType");
            String company = tj.getString("开票公司");
            String notes = tj.getString("备注");
            String detailPID = tj.getString("detailPID");
            XiaopiaoInfo info = new XiaopiaoInfo(parno, deptno, time, deptno, counts, factory,
                    producefrom, pihao, fengzhuang, description, place, notes, flag, detailPID, storageID,
                    company);
            info.setPid(pid);
            infos.add(info);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == reqCode) {
                printer = PrintSettingActivity.getPrint();
                tvState.setTextColor(Color.GREEN);
                tvState.setText("已连接");
                printer2 = SPrinter.getPrinter();
            }
        }
    }

    public static String getStorageByIp() throws IOException {
        //        GetStoreRoomIDByIP
        String ip = StorageUtils.getCurrentIp();
        if ("".equals(ip)) {
            throw new IOException("获取当前IP失败");
        }
        String bodyString = "";
        String info = "";
        try {
            bodyString = ChuKuServer.GetStoreRoomIDByIP(ip);
        } catch (IOException e) {
            info = e.getMessage();
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        if (bodyString.equals("")) {
            throw new IOException("获取库房ID出错：" + info);
        }
        return bodyString;
    }


    public static String getBalaceInfo(String pid, String storageID, String partno) throws IOException, XmlPullParserException {
//        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
//        properties.put("pid", pid);
//        properties.put("partno", partno);
//        properties.put("storageid", storageID);
//        SoapObject req = WebserviceUtils.getRequest(properties, "GetStorageBlanceInfoByID");
//        SoapPrimitive result = WebserviceUtils.getSoapPrimitiveResponse(req, WebserviceUtils.ChuKuServer);
        return ChuKuServer.GetStorageBlanceInfoByID(Integer.parseInt(pid), partno, storageID);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (printer != null) {
            printer.close();
        }
    }

    public String getDetailInfoByDetailId(String pid) throws IOException, XmlPullParserException {
//        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//        map.put("mxid", pid);
//        SoapObject request = WebserviceUtils.getRequest(map, "GetInstorectInfoByMXID");
//        SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(request,
//                WebserviceUtils.ChuKuServer);
        return ChuKuServer.GetInstorectInfoByMXID(Integer.parseInt(pid));
    }
    @Override
    public void getCameraScanResult(String result) {
        edPid.setText(result);
        infos.clear();
        try {
            Integer.parseInt(result);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            MyToast.showToast(mContext, "扫码结果不为数字");
            return;
        }
        if (isOffline) {
            if (printer2 == null) {
                MyToast.showToast(mContext, "请先连接蓝牙打印机");
                return;
            }
            printer2.printBarCode(result, 0, 1, 80);
            printer2.printText(result);
            printer2.newLine(3);
        } else {
            getData(result);
        }
    }
}
