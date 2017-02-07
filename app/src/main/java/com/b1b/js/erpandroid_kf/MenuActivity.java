package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.b1b.js.erpandroid_kf.utils.MyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    private ListView menuList;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> listItems = new ArrayList<>();

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
    }

    private void setItemOnclickListener() {
        menuList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 出库单
                if (position == 0) {
                    Intent intent = new Intent(MenuActivity.this, ChuKuActivity.class);
                    startActivity(intent);
                    //                    WebserviceUtils.getRequest();
                }
                // 预出库
                if (position == 1) {
                    Intent intent = new Intent(MenuActivity.this, CheckActivity.class);
                    startActivity(intent);
                }
                //                 采购
                if (position == 2) {
                    Intent intent = new Intent(MenuActivity.this, CaigouActivity.class);
                    startActivity(intent);
                }
                //                                 考勤
                if (position == 3) {
                    Intent intent = new Intent(MenuActivity.this, KaoQinActivity.class);
                    startActivity(intent);
                }
                //                拍照
                if (position == 4) {
                    //                    Intent intent = new Intent(MenuActivity.this, TakePicActivity.class);
                    //                    AlertDialog alertDialog = new AlertDialog(MenuActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    builder.setItems(new String[]{"拍照", "从手机选择"}, new DialogInterface.OnClickListener() {
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
                            }
                        }
                    });
                    builder.create().show();

                }
                // 取消登录
                if (position == 5) {
                    SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                    boolean al = sp.getBoolean("autol", false);
                    if (!al) {
                        MyToast.showToast(MenuActivity.this, "当前已是非自动登录状态");
                    } else {
                        SharedPreferences.Editor editor = sp.edit();
                        if (editor.putBoolean("autol", false).commit()) {
                            MyToast.showToast(MenuActivity.this, "取消登录成功");
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //防止ftp不销毁，会默认使用上一次的ftp
        System.exit(0);
    }

    // 添加菜单项
    private void addItem() {
        Map<String, String> map = new HashMap<>();
        map.put("title", "出库单");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "出库审核");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "采购");
        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "入库");
        //        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "考勤");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "上传图片");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "取消登录");
        listItems.add(map);
    }
}
