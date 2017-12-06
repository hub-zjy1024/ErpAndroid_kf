package com.b1b.js.erpandroid_kf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.DialogUtils;
import utils.MyToast;
import utils.SafeHandler;
import utils.WebserviceUtils;

public class SettingActivity extends AppCompatActivity {

    static class LHandler extends SafeHandler{

        private LHandler(Activity mContext) {
            super(mContext);
        }

        @Override
        public void handleMessage(Message msg) {
            SettingActivity activity = (SettingActivity) mContext.get();
            switch (msg.what) {
                case 1:
                    activity.aDialog.setMessage("解析数据出错");
                    activity.aDialog.show();
                    break;
                case 2:
                    activity.aDialog.setMessage("获取库房信息失败：" + activity.getString(R.string.bad_connection));
                    activity.aDialog.show();
                    break;
            }
        }
    }

    private LHandler zHandler = new LHandler(this);
    public static final String NAME = "kfName";
    public static final String KYACCOUNT = "kyAccount";
    public static final String KYUUID = "kyUuid";
    public static final String KYKEY = "kyKey";
    public static final String PRINTERSERVER = "printerServer";
    public static final String FTPSERVER = "ftpAddress";
    public static final String SFACCOUNT = "sfAccount";
    public static final String CONFIG_JSON = "configJson";
    public static final String CHUKU_PRINTER = "chukuPrinter";
    public static final String PREF_KF = "pref_kf";
    public static final String PREF_EXPRESS = "prefExpress";

    private List<Map<String, Object>> mlist = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private SimpleAdapter sAdapter;
    private AlertDialog aDialog;
    private SharedPreferences sp;

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
        sp = getSharedPreferences(PREF_KF, 0);
        aDialog = (AlertDialog) DialogUtils.getSpAlert(this, "msg", "提示");
        LinkedHashMap<String, Object> initMap = new LinkedHashMap<>();
        initMap.put(NAME, "请选择");
        mlist.add(initMap);
        sAdapter = new SimpleAdapter(this, mlist, R.layout.item_province, new String[]{NAME}, new int[]{R.id.item_province_tv});
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
                        MyToast.showToast(SettingActivity.this, "保存失败，请输入正确的小票打印机ip格式");
                        return;
                    }
                }
                editor.putString("printerIP", ip);
                if (!serverIp.equals("")) {
                    if (!serverMatcher.matches()) {
                        MyToast.showToast(SettingActivity.this, "保存失败，请输入正确的ip格式");
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
                @SuppressWarnings("unchecked") Map<String, Object> selectedItem = (Map<String, Object>) spiKF.getSelectedItem();
                if (selectedItem != null) {
                    if ("请选择".equals(selectedItem.get(NAME))) {
                        MyToast.showToast(SettingActivity.this, "请先选择库房");
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
                }
                boolean commit = editor.commit();
                if (commit) {
                    MyToast.showToast(SettingActivity.this, "保存成功");
                } else {
                    MyToast.showToast(SettingActivity.this, "保存失败！！");
                }
                setViewValue(tvSavedKf, rdoSF, rdoKY, edPrinterIP, edPrinterServer, edDiaohuoAccount);
            }
        });
        spiKF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("zjy", "SettingActivity->onItemSelected(): isselect==");
                @SuppressWarnings("unchecked")  Map<String, Object> itemAtPosition = (Map<String, Object>) parent.getItemAtPosition(position);
                if (!"请选择".equals(itemAtPosition.get(NAME))) {
                    edPrinterServer.setText(itemAtPosition.get(PRINTERSERVER).toString());
                    edPrinterIP.setText(itemAtPosition.get(CHUKU_PRINTER).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        new Thread() {
            @Override
            public void run() {
                super.run();
                getOnlineConfig();
            }
        }.start();

    }

    public void setViewValue(TextView tvSavedKf, RadioButton rdoSF, RadioButton rdoKY, EditText edPrinterIP, EditText
            edPrinterServer,EditText edDiaohuoAccount) {

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
            byte[] buf = new byte[1024];
            int len;
            StringBuilder builder = new StringBuilder();
            while ((len = in.read(buf)) != -1) {
                builder.append(new String(buf, 0, len, "UTF-8"));
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
            zHandler.sendEmptyMessage(2);
            e.printStackTrace();
        } catch (final JSONException e) {
            zHandler.sendEmptyMessage(1);
            e.printStackTrace();
        }
    }
}