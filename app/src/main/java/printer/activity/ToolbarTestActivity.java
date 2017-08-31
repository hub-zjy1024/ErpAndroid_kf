package printer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.SettingActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import printer.entity.PrinterItem;
import utils.DialogUtils;
import utils.MyToast;

public class ToolbarTestActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private EditText edIP;
    private Spinner spiPrintes;
    private ListView searchLv;
    private List<String> spiItems;
    private ArrayAdapter<String> pinterAdapter;
    private List<PrinterItem> lvItems;
    private ArrayAdapter<PrinterItem> lvAdapter;
    private SharedPreferences sp;
    private final Object lock = new Object();
    private Handler mHandler = new Handler();
    private ProgressDialog pd;
    private CheckBox cboAll;
    private ProgressDialog pDialog;
    private String ipAddress="";

    /**
     @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_test);
        edIP = (EditText) findViewById(R.id.ed_IP);
        spiPrintes = (Spinner) findViewById(R.id.spi_printers);
        searchLv = (ListView) findViewById(R.id.print_lv);
        cboAll = (CheckBox) findViewById(R.id.print_cbo_searchall);
        lvItems = new ArrayList<>();
        spiItems = new ArrayList<>();
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("正在搜索打印机");
        pDialog.setTitle("提示");
        sp = getSharedPreferences("UserInfo", 0);
        ipAddress= sp.getString("serverPrinter", "");
        pinterAdapter = new ArrayAdapter<String>(this, R.layout.lv_item_printer, R
                .id.spinner_item_tv, spiItems);

        if (ipAddress.equals("")) {
            showAlert(ToolbarTestActivity.this, SettingActivity.class);
        } else {
            pd = ProgressDialog.show(this, "提示", "正在搜索打印机", true, false);
//            pDialog.show();
            getPrinter(ipAddress);
        }
        lvAdapter = new ArrayAdapter<>(this, R.layout.printing_lv_item, R
                .id.spinner_item_tv, lvItems);
        searchLv.setAdapter(lvAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    333);
        }
        spiPrintes.setAdapter(pinterAdapter);
        searchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                final PrinterItem clickItem = (PrinterItem) parent.getItemAtPosition
                        (position);
                if (ipAddress.equals("")) {
                    showAlert(ToolbarTestActivity.this, SettingActivity.class);;
                    return;
                }
                final String strUrl = "http://" + ipAddress + ":8080/PrinterServer/PrintServlet?";
                Object selectItem = spiPrintes.getSelectedItem();
                String printer = "";
                if (selectItem != null) {
                    printer = selectItem.toString();
                }
                final String temPrinter = printer;
                new Thread() {
                    @Override
                    public void run() {
                        print(clickItem.getFile(), strUrl,
                                clickItem.getFlag()
                                , temPrinter);
                    }
                }.start();

            }
        });
        final File file = Environment.getExternalStorageDirectory();
    }

    private void showAlert(Context current, Class target) {
        DialogUtils.getSpAlert(ToolbarTestActivity.this, "暂时未配置打印服务器地址，是否去配置？", "提示", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ToolbarTestActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        }, "是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, "否").show();
        MyToast.showToast(this, "请配置打印服务器地址");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String string = uri.toString();
            File file;
            String a[] = new String[2];
            String schema = "";
            Log.e("zjy", "ToolbarTestActivity->onActivityResult(): uri==" + string);
            //判断文件是否在sd卡中
            if (string.contains(String.valueOf(Environment.getExternalStorageDirectory()
            ))) {
                schema = String.valueOf(Environment.getExternalStorageDirectory
                        ());
                //对Uri进行切割
                a = string.split(schema);
                Log.e("zjy", "ToolbarTestActivity->onActivityResult(): sd==" + schema);
                //获取到file
                file = new File(Environment.getExternalStorageDirectory(), a[1]);
            } else if (string.contains(String.valueOf(Environment.getDataDirectory()))
                    ) { //判断文件是否在手机内存中
                //对Uri进行切割
                schema = String.valueOf(Environment.getDataDirectory
                        ());
                //对Uri进行切割
                a = string.split(schema);
                //获取到file
                file = new File(Environment.getDataDirectory(), a[1]);
            } else {
                //出现其他没有考虑到的情况
            }
            //
        } else if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            String[] paths = data.getStringArrayExtra("imgPaths");
            String[] flags = data.getStringArrayExtra("flags");
            String ip = edIP.getText()
                    .toString().trim();
            if (ipAddress.equals("")) {
                showAlert(ToolbarTestActivity.this, SettingActivity.class);;
                return;
            }
            String strUrl = "http://" + ipAddress + ":8080/PrinterServer/PrintServlet?";
            Object selectItem = spiPrintes.getSelectedItem();
            String printer = "";
            if (selectItem != null) {
                printer = selectItem.toString();
            }
            pDialog.setMessage("正在打印中");
            Log.e("zjy", "ToolbarTestActivity->onActivityResult(): startPrinterImg==");
            for (int i = 0; i < paths.length; i++) {
                print(new File(paths[i]), strUrl, flags[i], printer);
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ipAddress = sp.getString("serverPrinter", "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        if (requestCode == 333){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                File root = Environment.getExternalStorageDirectory();
                File tencent = new File(root, "tencent");
//                getFileList(tencent, "", false);
                //用户同意使用write
            } else {
                //用户不同意，自行处理即可
                Toast.makeText(this, "无权限、、、", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean uploadandPrint(File file, String strUrl) throws IOException {
        if (file.exists()) {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            FileInputStream fis = new FileInputStream(file);
            OutputStream outputStream = conn.getOutputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader
                    (inputStream, "UTF-8"));
            String result = reader.readLine();
            boolean isOk = false;
            if (result.equals("ok")) {
                isOk = true;
            }
            Log.e("zjy", "ToolbarTestActivity->run(): ==write over:" + reader
                    .readLine());
            reader.close();
            return isOk;
        } else {
            return false;
        }
    }

    public void getFileList(File dir, String type, boolean isAll) {

        LinkedList<File> dirs = new LinkedList<>();
        if (isAll) {
            dir = Environment.getExternalStorageDirectory();
            dirs.add(dir);
        } else {
             File file = Environment.getExternalStorageDirectory();
             File file2 = new File(file, "tencent/qqfile_recv/");
             File file3 = new File(file, "tencent/MicroMsg/Download");
             File file4 = new File(file, "tencent/MicroMsg/WeiXin");
            dirs.add(file2);
            dirs.add(file3);
            dirs.add(file4);

        }

        File temp_file;
        while (!dirs.isEmpty()) {
            temp_file = dirs.removeFirst();
            File[] files = temp_file.listFiles();
            if (files != null) {
                Log.e("zjy", "ToolbarTestActivity->getFileList(): dir==" + temp_file);
                for (File f : files) {
                    if (f.isDirectory()) {
                        if (!f.getName().startsWith(".")) {
                            dirs.addLast(f);
                        }
                    } else {
                        boolean isNeed = checkFile(f, type);
                        if (isNeed) {
                            lvItems.add(new PrinterItem(f, type));
                        }
                    }
                }
            }
        }
    }

    public boolean checkFile(File f, String type) {
        if (type.equals("word")) {
            if (f.getName().endsWith(".doc")) {
                return true;
            } else if (f.getName().endsWith(".docx")) {
                return true;
            }
        } else if (type.equals("excel")) {
            if (f.getName().endsWith(".xls")) {
                return true;
            } else if (f.getName().endsWith(".xlsx")) {
                return true;
            }
        } else if (type.equals("pdf")) {
            if (f.getName().endsWith(".pdf")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param view
     */
    public void btnOnClick(final View view) {
        final File file = Environment.getExternalStorageDirectory();
        final File file2 = new File(file, "tencent/qqfile_recv/");
        final String ip = edIP.getText()
                .toString().trim();
        if (ipAddress.equals("")) {
            showAlert(ToolbarTestActivity.this, SettingActivity.class);;
            return;
        }
        final boolean isAll = cboAll.isChecked();
        switch (view.getId()) {
            case R.id.btn1:
                preSearch(isAll);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getFileList(file2, "word", isAll);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "搜索完成", Toast
                                        .LENGTH_SHORT).show();
                                lvAdapter.notifyDataSetChanged();
                                pd.cancel();
                            }
                        });
                    }
                }.start();
                break;
            case R.id.btn3:
                preSearch(isAll);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getFileList(file2, "excel", isAll);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "搜索完成", Toast
                                        .LENGTH_SHORT).show();
                                lvAdapter.notifyDataSetChanged();
                                pd.cancel();
                            }
                        });
                    }
                }.start();
                break;
            case R.id.btn8:
                preSearch(isAll);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getFileList(file2, "pdf", isAll);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "搜索完成", Toast
                                        .LENGTH_SHORT).show();
                                lvAdapter.notifyDataSetChanged();
                                pd.cancel();
                            }
                        });
                    }
                }.start();
                break;
            case R.id.btn6:
                Intent intent = new Intent(ToolbarTestActivity.this,
                        PickPicPrintingActivity.class);
                startActivityForResult(intent, 300);
                break;
            case R.id.btn7:
                spiItems.clear();
                pd.show();
                getPrinter(ipAddress);
                break;
            case R.id.activity_toolbar_test_btn_setting:
                Intent intent2 = new Intent(ToolbarTestActivity.this, SettingActivity.class);
                startActivity(intent2);
                break;
        }
    }

    private void preSearch(boolean isAll) {
        if (!lvAdapter.isEmpty()) {
            lvItems.clear();
        }
        if (isAll) {
            pd.setMessage("搜索全部文件，请稍后");
        }
        pd.show();
    }

    private void getPrinter(final String ip) {
        new Thread() {
            @Override
            public void run() {
                String urlPrinter = "http://" + ip +
                        ":8080/PrinterServer/GetPrinterInfoServlet";
                try {
                    URL url = new URL(urlPrinter);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    InputStream in = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                    BufferedReader bis = new BufferedReader(reader);
                    String s = "";
                    String result = "";
                    while ((s = bis.readLine()) != null) {
                        result += s;
                    }
                    Log.e("zjy", "ToolbarTestActivity->run():printer: reuslt=="
                            + result);
                    if (!result.equals("")) {
                        String[] printers = result.split(",");
                        for (String p : printers) {
                            spiItems.add(p);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pinterAdapter.notifyDataSetChanged();
                                DialogUtils.dismissDialog(pd);
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.dismissDialog(pd);
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private boolean print(final File file2, String strUrl, String flag, String printer) {
        try {
            strUrl += "flag=" + flag;
            strUrl += "&filename=" + URLEncoder
                    .encode(file2.getName(), "UTF-8");
//            strUrl += "&printer=" + printer;
            strUrl += "&printer=" + URLEncoder
                    .encode(printer, "UTF-8");
            final String finalStrUrl = strUrl;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        boolean isOk = uploadandPrint(file2, finalStrUrl);
                        if (isOk) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.dismiss();
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.dismiss();
                                    DialogUtils.getSpAlert(ToolbarTestActivity.this, "打印错误", "提示").show();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.getSpAlert(ToolbarTestActivity.this, "连接服务器失败，请重试", "提示").show();
                            }
                        });
                    }
                }
            }.start();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

}
