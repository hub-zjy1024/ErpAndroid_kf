package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.framwork.DialogUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.WebserviceUtils;

public class SettingActivity extends BaseMActivity implements NoLeakHandler.NoLeakCallback {
    private static final int MSG_ERROR_DATA_ILLEGAL_JSON = 1;
    private static final int MSG_ERROR_KFINOFAILED = 2;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ERROR_DATA_ILLEGAL_JSON:
                aDialog.setMessage("解析数据出错");
                aDialog.show();
                break;
            case MSG_ERROR_KFINOFAILED:
                aDialog.setMessage("获取库房信息失败：" + getString(R.string.bad_connection));
                aDialog.show();
                break;
        }
    }

    private Handler zHandler = new NoLeakHandler(this);

    public static final String NAME = "kfName";
    public static final String KYACCOUNT = "kyAccount";
    public static final String KYUUID = "kyUuid";
    public static final String KYKEY = "kyKey";
    public static final String PRINTERSERVER = "printerServer";
    public static final String FTPSERVER = "ftpAddress";
    public static final String SFACCOUNT = "sfAccount";
    public static final String KY_SMSNUM = "smsNum";
    public static final String CONFIG_JSON = "configJson";
    public static final String CHUKU_PRINTER = "chukuPrinter";
    public static final String PREF_KF = "pref_kf";
    public static final String PREF_USERINFO = "UserInfo";
    public static final String PREF_TKPIC = "pref_takepic_style";
    public static final String PREF_CAMERA_INFO = "cameraInfo";
    public static final String PREF_EXPRESS = "prefExpress";

    private List<Map<String, Object>> mlist = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private SimpleAdapter sAdapter;
    private AlertDialog aDialog;
    private SharedPreferences sp;
    private SharedPreferences spPicUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button btnSave = (Button) findViewById(R.id.activity_setting_btnsave);
        final EditText edPrinterIP = (EditText) findViewById(R.id.activity_setting_edip);
        final EditText edPrinterServer = (EditText) findViewById(R.id.activity_setting_ed_printerserver);
        final EditText edDiaohuoAccount = (EditText) findViewById(R.id.activity_setting_ed_diaohuo_account);
        final RadioButton rdoSF = (RadioButton) findViewById(R.id.activity_setting_rdo_SF);
        final Spinner spiKF = (Spinner) findViewById(R.id.activity_setting_spiKF);
        final TextView tvSavedKf = (TextView) findViewById(R.id.activity_setting_tvkf);
        final RadioButton rdoKY = (RadioButton) findViewById(R.id.activity_setting_rdo_ky);
        final Spinner spiUpLoad = (Spinner) findViewById(R.id.activity_setting_spi_picupload_style);
        final Button btnCheckUpdate = (Button) findViewById(R.id.activity_setting_btncheckupdate);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AboutActivity.class));
            }
        });
        String[] arrays = getResources().getStringArray(R.array.upload_type);
        String[] strs = Arrays.copyOf(arrays, arrays.length + 1);
        strs[strs.length - 1] = "手动";
        spiUpLoad.setAdapter(new ArrayAdapter<String>(this, R.layout.item_province, R.id.item_province_tv, 
                strs));
        sp = getSharedPreferences(PREF_KF, 0);
        spPicUpload = getSharedPreferences(PREF_TKPIC, 0);
        aDialog = (AlertDialog) DialogUtils.getSpAlert(this, "msg", "提示");
        LinkedHashMap<String, Object> initMap = new LinkedHashMap<>();
        initMap.put(NAME, "请选择");
        mlist.add(initMap);
        sAdapter = new SimpleAdapter(this, mlist, R.layout.item_province, new String[]{NAME}, new int[]{R
                .id.item_province_tv});
        spiKF.setAdapter(sAdapter);
        setViewValue(tvSavedKf, rdoSF, rdoKY, edPrinterIP, edPrinterServer, edDiaohuoAccount);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}" +
                        "(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                final String ip = edPrinterIP.getText().toString().trim();
                final String serverIp = edPrinterServer.getText().toString().trim();
                final String diaohuoAccount = edDiaohuoAccount.getText().toString().trim();
                Matcher matcher = pattern.matcher(ip);
                Matcher serverMatcher = pattern.matcher(serverIp);
                boolean matches = matcher.matches();
                SharedPreferences.Editor editor = sp.edit();
                if (!ip.equals("")) {
                    if (!matches) {
                        showMsgToast( "保存失败，请输入正确的小票打印机ip格式");
                        return;
                    }
                }
                editor.putString("printerIP", ip);
                if (!serverIp.equals("")) {
                    if (!serverMatcher.matches()) {
                        showMsgToast( "保存失败，请输入正确的ip格式");
                        return;
                    }
                }
                editor.putString(PRINTERSERVER, serverIp);
                editor.putString("diaohuoAccount", diaohuoAccount);
                if (rdoKY.isChecked()) {
                    editor.putString(PREF_EXPRESS, getString(R.string.express_ky));
                } else if (rdoSF.isChecked()) {
                    editor.putString(PREF_EXPRESS, getString(R.string.express_sf));
                }
                @SuppressWarnings("unchecked") Map<String, Object> selectedItem = (Map<String, Object>) 
                        spiKF.getSelectedItem();
                if (selectedItem != null) {
                    if ("请选择".equals(selectedItem.get(NAME))) {
                        showMsgToast( "请先选择库房");
                        return;
                    }
                    editor.putString(NAME, selectedItem.get(NAME).toString());
                    editor.putString(KYUUID, selectedItem.get(KYUUID).toString());
                    editor.putString(KYKEY, selectedItem.get(KYKEY).toString());
                    editor.putString(KYACCOUNT, selectedItem.get(KYACCOUNT).toString());
                    editor.putString(FTPSERVER, selectedItem.get(FTPSERVER).toString());
                    editor.putString(SFACCOUNT, selectedItem.get(SFACCOUNT).toString());
                    editor.putString(CHUKU_PRINTER, selectedItem.get(CHUKU_PRINTER).toString());
                    editor.putString(CONFIG_JSON, selectedItem.get(CONFIG_JSON).toString());
                    MyApp.myLogger.writeInfo("have set config:" + selectedItem.get(CONFIG_JSON));
                }
                Object selectedItem1 = spiUpLoad.getSelectedItem();
                spPicUpload.edit().putString("style", selectedItem1.toString()).commit();
                MyApp.myLogger.writeInfo(SettingActivity.class, "set takepic style :" + 
                        selectedItem1.toString());
                boolean commit = editor.commit();
                if (commit) {
                    showMsgToast( "保存成功");
                } else {
                    showMsgToast( "保存失败！！");
                }
                setViewValue(tvSavedKf, rdoSF, rdoKY, edPrinterIP, edPrinterServer, edDiaohuoAccount);
            }
        });
        spiKF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked") Map<String, Object> itemAtPosition = (Map<String, Object>) 
                        parent.getItemAtPosition(position);
                if (!"请选择".equals(itemAtPosition.get(NAME))) {
                    edPrinterServer.setText(itemAtPosition.get(PRINTERSERVER).toString());
                    edPrinterIP.setText(itemAtPosition.get(CHUKU_PRINTER).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Runnable getCofigRun = new Runnable() {
            @Override
            public void run() {
                getOnlineConfig();
            }
        };
        TaskManager.getInstance().execute(getCofigRun);

    }

    @Override
    public void init() {
        
    }

    @Override
    public void setListeners() {

    }

    public void setViewValue(TextView tvSavedKf, RadioButton rdoSF, RadioButton rdoKY, EditText 
            edPrinterIP, EditText
            edPrinterServer, EditText edDiaohuoAccount) {

        String localPrinterIP = sp.getString("printerIP", "");
        final String serverIP = sp.getString(PRINTERSERVER, "");
        final String prefExpress = sp.getString(PREF_EXPRESS, "");
        final String saveKF = sp.getString(NAME, "");
        tvSavedKf.setText(String.format("当前存储的库房是：%s", saveKF));
        //        String[] kfNames = new String[]{"深圳", "北京中转库"};

        if (prefExpress.equals(getResources().getString(R.string.express_sf))) {
            rdoSF.setChecked(true);
        } else if (prefExpress.equals(getResources().getString(R.string.express_ky))) {
            rdoKY.setChecked(true);
        }
        final String diaohuoAccount = sp.getString("diaohuoAccount", "");
        edPrinterIP.setText(localPrinterIP);
        edPrinterServer.setText(serverIP);
        edDiaohuoAccount.setText(diaohuoAccount);
    }

    public void getOnlineConfig() {
        try {
            URL url = new URL(WebserviceUtils.ROOT_URL + "/DownLoad/dyj_kf/config.txt");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            InputStream in = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuilder builder = new StringBuilder();
            String temps = "";
            while ((temps = br.readLine()) != null) {
                builder.append(temps);
                builder.append("\n");
            }
            Log.e("zjy", "SettingActivity->getOnlineConfig(): result==" + builder.toString());
            JSONArray array = new JSONArray(builder.toString());

            for (int i = 0; i < array.length(); i++) {
                JSONObject temp = array.getJSONObject(i);
                Map<String, Object> map = new LinkedHashMap<>();
                Log.e("zjy", "SettingActivity->getOnlineConfig(): name==" + temp.getString(NAME));
                map.put(NAME, temp.getString(NAME));
                map.put(KYACCOUNT, temp.getString(KYACCOUNT));
                map.put(KYUUID, temp.getString(KYUUID));
                map.put(KYKEY, temp.getString(KYKEY));
                map.put(PRINTERSERVER, temp.getString(PRINTERSERVER));
                map.put(FTPSERVER, temp.getString(FTPSERVER));
                map.put(SFACCOUNT, temp.getString(SFACCOUNT));
                map.put(CHUKU_PRINTER, temp.getString(CHUKU_PRINTER));
                map.put(CONFIG_JSON, temp.toString());
                mlist.add(map);
            }
            zHandler.post(new Runnable() {
                @Override
                public void run() {
                    sAdapter.notifyDataSetChanged();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            zHandler.sendEmptyMessage(MSG_ERROR_KFINOFAILED);
            e.printStackTrace();
        } catch (final JSONException e) {
            zHandler.sendEmptyMessage(MSG_ERROR_DATA_ILLEGAL_JSON);
            e.printStackTrace();
        }
    }
}