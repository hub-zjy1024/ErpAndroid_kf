package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    private ListView menuList;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> listItems = new ArrayList<>();
    private AlertDialog dialog;
    private boolean showAlert = true;

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
        dialog = builder.create();
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
                    case "上传图片(必须有单据号)":
                        if (!dialog.isShowing() && dialog != null) {
                            dialog.show();
                        }


                        //                        MaterialDialog dialog = new MaterialDialog(MenuActivity.this);
                        //                        View v = LayoutInflater.from(MenuActivity.this).inflate(R.layout.mdialog_progress, null);
                        //                        ProgressBar bar = (ProgressBar) v.findViewById(R.id.mdialog_progress_pbar);
                        //                        bar.setProgress(20);
                        //                        bar.setMax(100);
                        //                        dialog.setCanceledOnTouchOutside(true);
                        //                        dialog.setContentView(v);
                        //                        dialog.show();
                        break;
                    case "比价单":
                        intent.setClass(MenuActivity.this, BijiaActivity.class);
                        startActivity(intent);
                        break;
                    case "查看单据关联图片":
                        intent.setClass(MenuActivity.this, ViewPicByPidActivity.class);
                        startActivity(intent);
                        break;
                    case "图片后台上传":
                        if (showAlert) {
                            String msg = "此方法采取后台上传，不用等待上一张图片上传完成就可以进行下一次拍照上传。\n上传成功会在通知栏显示，上传失败时点击通知栏中失败的项可以进行重新上传。有问题及时反馈,出库审核中的拍照暂时还没修改。";
                            getDialog(MenuActivity.this, "提示(每次重启程序提示)", msg, true, "继续", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setClass(MenuActivity.this, TakePic2Activity.class);
                                    startActivity(intent);
                                    showAlert = false;
                                }
                            }, "取消", null).show();
//                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MenuActivity.this);
//                            mBuilder.setTitle("提示(每次重启程序提示)");
//                            mBuilder.setMessage("此方法支持拍多张图片在后台上传，和普通的拍照上传使用方法类似。上传成功会在通知栏显示，上传失败时点击通知栏中失败的项可以进行重新上传。有问题及时反馈");
//                            mBuilder.setNegativeButton("继续", );
//                            mBuilder.setPositiveButton("取消", null);
//                            mBuilder.show();
                        } else {
                            intent.setClass(MenuActivity.this, TakePic2Activity.class);
                            startActivity(intent);
                        }


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
    protected void onDestroy() {
        super.onDestroy();
    }

    // 添加菜单项
    private void addItem() {
        Map<String, String> map = new HashMap<>();
        map.put("title", "出库单");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "出库审核(拍照)");
        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "采购");
        //        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "入库");
        //        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "考勤");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "上传图片(必须有单据号)");
        listItems.add(map);
        //        map = new HashMap<>();
        //        map.put("title", "比价单");
        //        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "查看单据关联图片");
        listItems.add(map);
        map = new HashMap<>();
        map.put("title", "图片后台上传");
        listItems.add(map);
    }
}
