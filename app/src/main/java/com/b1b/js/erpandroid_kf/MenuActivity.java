package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.b1b.js.erpandroid_kf.adapter.MenuAdapter;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import printer.activity.SFActivity;
import printer.activity.ToolbarTestActivity;
import utils.MyToast;

public class MenuActivity extends AppCompatActivity {
    private ListView menuList;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> listItems = new ArrayList<>();
    private AlertDialog choiceMethodDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        menuList = (ListView) findViewById(R.id.lv);
        simpleAdapter = new SimpleAdapter(this, listItems, R.layout.menu_items, new String[]{"title"}, new int[]{R.id
                .menu_title});
        // 为菜单项设置点击事件
        setItemOnclickListener();
        addItem();
        // 设置adapter
        //        menuList.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setTitle("上传方式选择");
        builder.setItems(new String[]{"拍照", "从手机选择", "后台上传"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApp.myLogger.writeInfo("菜单拍照：" + which);
                switch (which) {
                    case 0:
                        Intent intent1 = new Intent(MenuActivity.this, TakePicActivity.class);
                        startActivity(intent1);
                        MyApp.myLogger.writeInfo("takepic");
                        break;
                    case 1:
                        Intent intent2 = new Intent(MenuActivity.this, ObtainPicFromPhone.class);
                        startActivity(intent2);
                        MyApp.myLogger.writeInfo("obtain");
                        break;
                    case 2:
                        Intent intent3 = new Intent(MenuActivity.this, TakePic2Activity.class);
                        MyApp.myLogger.writeInfo("takepic2");
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
                //                Map<String, String> item = listItems.get(position);
                MyMenuItem data = (MyMenuItem) parent.getItemAtPosition(position);
                String value = data.content;
                Intent intent = new Intent();
                switch (value) {
                    case "出库单":
                        intent.setClass(MenuActivity.this, ChuKuActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> chukudan");
                        break;
                    case "出库审核(拍照)":
                        intent.setClass(MenuActivity.this, CheckActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> chukucheck");
                        break;
                    case "考勤":
                        intent.setClass(MenuActivity.this, KaoQinActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> kaoqin");
                        break;
                    case "上传图片(3种方式)":
                        if (!choiceMethodDialog.isShowing() && choiceMethodDialog != null) {
                            choiceMethodDialog.show();
                        }
                        break;
                    case "盘库":
                        intent.setClass(MenuActivity.this, PankuActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> panku");
                        break;
                    case "查看单据关联图片":
                        intent.setClass(MenuActivity.this, ViewPicByPidActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> searchpic");
                        break;
                    case "出库单打印":
                        intent.setClass(MenuActivity.this, PreChukuActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> chukudanprint");
                        break;
                    case "配置":
                        intent.setClass(MenuActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case "库存发布":
                        intent.setClass(MenuActivity.this, KucunFBActivity.class);
                        startActivity(intent);
                        MyApp.myLogger.writeInfo("<page> kucunfabu");
                        break;
                    case "采购拍照":
                        intent.setClass(MenuActivity.this, CaigoudanTakePicActivity.class);
                        startActivity(intent);
                        break;
                    case "特殊":
                        AlertDialog.Builder specialDialog = new AlertDialog.Builder(MenuActivity.this);
                        View v = LayoutInflater.from(MenuActivity.this).inflate(R.layout.admin_manager_layout, null);
                        final EditText edID = (EditText) v.findViewById(R.id.admin_manager_ed_id);
                        final EditText edFtp = (EditText) v.findViewById(R.id.admin_manager_ed_ftp);
                        Button btnChange = (Button) v.findViewById(R.id.admin_manager_btnCommit);
                        btnChange.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyApp.id = edID.getText().toString();
                                MyApp.ftpUrl = edFtp.getText().toString();
                                Log.e("zjy", "MenuActivity->onClick(): uid==" + MyApp.id);
                            }
                        });
                        specialDialog.setView(v);
                        specialDialog.show();
                        break;
                    case "打印(暂仅供北京使用)":
                        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                        builder.setTitle("打印");
                        builder.setItems(new String[]{"SF打印", "打印手机文件", "配置打印地址"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                switch (which) {
                                    case 0:
                                        intent = new Intent(MenuActivity.this, SFActivity.class);
                                        startActivity(intent);
                                        MyApp.myLogger.writeInfo("<page> SFprint");
                                        break;
                                    case 1:
                                        intent = new Intent(MenuActivity.this, ToolbarTestActivity.class);
                                        startActivity(intent);
                                        MyApp.myLogger.writeInfo("<page> fileprint");
                                        break;
                                    case 2:
                                        intent = new Intent(MenuActivity.this, SettingActivity.class);
                                        startActivity(intent);
                                        MyApp.myLogger.writeInfo("<page> printServer peizhi");
                                        break;
                                }
                            }
                        });

                        builder.show();
                        break;
                }
            }
        });
    }

    public static AlertDialog getDialog(Context mContext, String title, String msg, boolean cancelAble, String leftBtn,
                                        DialogInterface.OnClickListener l, String rightBtn, DialogInterface.OnClickListener r) {
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
                getDialog(MenuActivity.this, "提示", "后台还有" + size + "张图片未上传完成，强制退出将导致图片上传失败", true, null, null, "否", null).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    // 添加菜单项
    private void addItem() {
        Map<String, String> map = new HashMap<>();
        map.put("title", "出库单");
        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "出库单打印");
        //        listItems.add(map);
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
        map = new HashMap<>();
        map.put("title", "采购拍照");
        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "库存发布");
        //        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "配置");
        //        listItems.add(map);
        ArrayList<MyMenuItem> data = new ArrayList<>();
        data.add(new MyMenuItem(R.mipmap.menu_chuku, "出库单", "查看出库单和出库通知单"));
        if ("101".equals(MyApp.id)) {
            data.add(new MyMenuItem(R.mipmap.menu_chuku, "特殊", "101"));
        }
        data.add(new MyMenuItem(R.mipmap.menu_preprint, "出库单打印", "出库单单据信息打印"));
        data.add(new MyMenuItem(R.mipmap.menu_check, "出库审核(拍照)", "出库审核功能和审核完成的拍照功能"));
        data.add(new MyMenuItem(R.mipmap.menu_kaoqin, "考勤", "查询考勤状态"));
        //        data.add(new MyMenuItem(R.mipmap.menu_photo, "上传图片(3种方式)", "通过三种不同的方式上传图片"));
        data.add(new MyMenuItem(R.mipmap.menu_pic, "查看单据关联图片", "查询单据与相关联的照片"));
        data.add(new MyMenuItem(R.mipmap.menu_panku, "盘库", "货物位置管理"));
        data.add(new MyMenuItem(R.mipmap.menu_caigou_96, "采购拍照", "采购单拍照功能"));
        data.add(new MyMenuItem(R.mipmap.menu_print, "打印(暂仅供北京使用)", "顺丰下单并打印功能,以及打印手机接受的文件的功能"));
        MenuAdapter adapter = new MenuAdapter(data, this, R.layout.menu_item);
        menuList.setAdapter(adapter);
    }
}
