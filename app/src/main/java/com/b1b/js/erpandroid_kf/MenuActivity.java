package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    private ListView menuList;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> listItems = new ArrayList<>();
    private AlertDialog choiceMethodDialog;
    private boolean showAlert = true;
    private int counts = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        menuList = (ListView) findViewById(R.id.lv);
        simpleAdapter = new SimpleAdapter(this, listItems, R.layout.menu_items, new String[]{"title"}, new int[]{R.id.menu_title});
        // 为菜单项设置点击事件
        setItemOnclickListener();
        addItem();
        // 设置adapter
        menuList.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setItems(new String[]{"拍照", "从手机选择", "后台上传"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent1 = new Intent(MenuActivity.this, TakePicActivity.class);
                        startActivity(intent1);
                        break;
                    case 1:
                        Intent intent2 = new Intent(MenuActivity.this, ObtainPicFromPhone.class);
                        startActivity(intent2);
                        break;
                    case 2:
                        Intent intent3 = new Intent(MenuActivity.this, TakePic2Activity.class);
                        startActivity(intent3);
                        break;
                }
            }
        });
        choiceMethodDialog = builder.create();
    }

    private void checkImgFileSize(final File file, int size) {
        String[] files = file.list();
        if (files.length > size) {
            AlertDialog.Builder mBd = new AlertDialog.Builder(MenuActivity.this);
            mBd.setTitle("提示");
            mBd.setMessage("缓存图片超过200张，是否清理一下");
            mBd.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyToast.showToast(MenuActivity.this, "清理缓存完成");
                    final File[] files = file.listFiles();
                    for (File f : files) {
                        f.delete();
                    }
                }
            });
            mBd.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            mBd.show();
        }
    }

    private void setItemOnclickListener() {
        menuList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> item = listItems.get(position);
                String value = item.get("title");
                Intent intent = new Intent();
                switch (value) {
                    case "出库单":
                        intent.setClass(MenuActivity.this, ChuKuActivity.class);
                        startActivity(intent);
                        break;
                    case "出库审核(拍照)":
                        intent.setClass(MenuActivity.this, CheckActivity.class);
                        startActivity(intent);
                        break;
                    case "采购":
                        intent.setClass(MenuActivity.this, CaigouActivity.class);
                        startActivity(intent);
                        break;
                    case "考勤":
                        intent.setClass(MenuActivity.this, KaoQinActivity.class);
                        startActivity(intent);
                        break;
                    case "上传图片(3种方式)":
                        if (!choiceMethodDialog.isShowing() && choiceMethodDialog != null) {
                            choiceMethodDialog.show();
                        }
                        //                        MaterialDialog choiceMethodDialog = new MaterialDialog(MenuActivity.this);
                        //                        View v = LayoutInflater.from(MenuActivity.this).inflate(R.layout.mdialog_progress, null);
                        //                        ProgressBar bar = (ProgressBar) v.findViewById(R.id.mdialog_progress_pbar);
                        //                        bar.setProgress(20);
                        //                        bar.setMax(100);
                        //                        choiceMethodDialog.setCanceledOnTouchOutside(true);
                        //                        choiceMethodDialog.setContentView(v);
                        //                        choiceMethodDialog.show();
                        break;
                    case "比价单":
                        intent.setClass(MenuActivity.this, BijiaActivity.class);
                        startActivity(intent);
                        break;
                    case "盘库":
                        intent.setClass(MenuActivity.this, PankuActivity.class);
                        startActivity(intent);
                        break;
                    case "查看单据关联图片":
                        intent.setClass(MenuActivity.this, ViewPicByPidActivity.class);
                        startActivity(intent);
                        break;
                    case "图片后台上传":
                        intent.setClass(MenuActivity.this, TakePic2Activity.class);
                        startActivity(intent);
                        break;
                    case "预出库打印":
                        intent.setClass(MenuActivity.this, PreChukuActivity.class);
                        startActivity(intent);
                        break;
                }
                // 取消登录
                //                if (position == 5) {
                //                    SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                //                    boolean al = sp.getBoolean("autol", false);
                //                    if (!al) {
                //                        MyToast.showToast(MenuActivity.this, "当前已是非自动登录状态");
                //                    } else {
                //                        SharedPreferences.Editor editor = sp.edit();
                //                        if (editor.putBoolean("autol", false).commit()) {
                //                            MyToast.showToast(MenuActivity.this, "取消登录成功");
                //                        }
                //                    }
                //                }
            }
        });
    }

    public static AlertDialog getDialog(Context mContext, String title, String msg, boolean cancelAble, String leftBtn, DialogInterface.OnClickListener l, String rightBtn, DialogInterface.OnClickListener r) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(msg);
        if (leftBtn != null) {
            builder.setNegativeButton(leftBtn, l);
        }
        if (rightBtn != null) {
            builder.setPositiveButton(rightBtn, r);
        }
        builder.setCancelable(cancelAble);
        return builder.create();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 按下BACK，同时没有重复
            int size = MyApp.totoalTask.size();
            if (size > 0) {
                getDialog(MenuActivity.this, "提示", "后台还有" + size + "张图片未上传完成，强制退出可能导致图片上传失败", true, "是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(1);
                    }
                }, "否", null).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyApp.myLogger != null) {
            MyApp.myLogger.close();
            final File localLog = MyApp.myLogger.getlogFile();
            if (localLog == null) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    FTPClient client = new FTPClient();
                    try {
                        client.setConnectTimeout(10 * 1000);
                        client.connect("172.16.6.22", 21);
                        boolean login = client.login("NEW_DYJ", "GY8Fy2Gx");
                        if (!login) {
                            return;
                        }
                        client.setFileType(FTP.BINARY_FILE_TYPE);
                        client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                        FileInputStream fis = new FileInputStream(localLog);
                        String dir = UploadUtils.getRemoteDir();
                        client.changeWorkingDirectory("ZJy");
                        boolean change1 = client.changeWorkingDirectory(dir);
                        if (!change1) {
                            client.makeDirectory(dir);
                            client.changeWorkingDirectory(dir);
                        }
                        String name = MyApp.id + "_log.txt";
                        String[] names = client.listNames();
                        for (String s : names) {
                            if (s.equals(name)) {
                                return;
                            }
                        }
                        boolean isFalse = client.storeFile(name, fis);
                        if (isFalse) {
                            Log.e("zjy", "MenuActivity->run(): upload log success==");
                        }
                        client.completePendingCommand();
                        client.disconnect();
                        //                        client.storeFile()
                        //                        client.storeFile("", "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    // 添加菜单项
    private void addItem() {
        Map<String, String> map = new HashMap<>();
        map.put("title", "出库单");
        listItems.add(map);

        map = new HashMap<>();
        map.put("title", "预出库打印");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "出库审核(拍照)");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "考勤");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "上传图片(3种方式)");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "查看单据关联图片");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "盘库");
        listItems.add(map);
    }
}
