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
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.adapter.MenuGvAdapter;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;
import com.b1b.js.erpandroid_kf.service.LogUploadService;

import java.util.ArrayList;

import printer.activity.SFActivity;
import utils.btprint.SPrinter;

public class MenuActivity extends AppCompatActivity implements OnItemClickListener {
    private final String tag_Ruku = "库存标签";
    private final String tag_Print = "运单打印";
    private final String tag_Kaoqin = "考勤";
    private final String tag_Chukudan = "出库单";
    private final String tag_ChukudanPrint = "出库单打印";
    private final String tag_Viewpic = "单据图片";
    private final String tag_Panku ="盘库";
    private final String tag_CaigouTakePic ="采购拍照";
    private final String tag_ChukuCheck ="出库拍照";
    private final String tag_Admin ="特殊";
    private final String tag_Setting = "设置";
    private final String tag_shangjia = "货物上架";

    private GridView gv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        gv = (GridView) findViewById(R.id.menu_gv);
        gv.setOnItemClickListener(this);
        addItemGV();
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
            int size = (int) MyApp.cachedThreadPool.getActiveCount()-1;
            if (size > 0) {
                getDialog(MenuActivity.this, "提示", "后台还有" + size +
                        "张图片未上传完成，强制退出将导致图片上传失败",
                        true, null, null, "否", null).show();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void addItemGV() {
        ArrayList<MyMenuItem> data = new ArrayList<>();
        data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_Chukudan, "查看出库单和出库通知单"));
        if ("101".equals(MyApp.id)) {
            data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_Admin, "101"));
        }
        data.add(new MyMenuItem(R.mipmap.menu_preprint, tag_ChukudanPrint, "出库单单据信息打印"));
        data.add(new MyMenuItem(R.mipmap.menu_check, tag_ChukuCheck, "出库审核功能和审核完成的拍照功能"));
        data.add(new MyMenuItem(R.mipmap.menu_print, tag_Print, "顺丰下单并打印功能,以及打印手机接受的文件的功能"));
        data.add(new MyMenuItem(R.mipmap.menu_pic, tag_Viewpic, "查询单据关联的照片"));
        data.add(new MyMenuItem(R.mipmap.menu_panku, tag_Panku, "货物位置管理"));
        data.add(new MyMenuItem(R.mipmap.menu_shangjia, tag_shangjia, "上架"));
        data.add(new MyMenuItem(R.mipmap.menu_caigou_96, tag_CaigouTakePic, "采购单拍照功能"));
        data.add(new MyMenuItem(R.mipmap.menu_print, tag_Ruku, "蓝牙打印，打印入库标签"));
        data.add(new MyMenuItem(R.mipmap.menu_kaoqin, tag_Kaoqin, "查询考勤状态"));
        data.add(new MyMenuItem(R.mipmap.menu_setting_press, tag_Setting, "设置"));
        MenuGvAdapter adapter = new MenuGvAdapter(this, data, R.layout.item_menu_gv);
        gv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, LogUploadService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SPrinter printer = SPrinter.getPrinter(this, null);
        printer.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyMenuItem data = (MyMenuItem) parent.getItemAtPosition(position);
        String value = data.content;
        Intent intent = new Intent();
        switch (value) {
            case tag_Chukudan:
                intent.setClass(MenuActivity.this, ChuKuActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> chukudan");
                break;
            case tag_ChukuCheck:
                intent.setClass(MenuActivity.this, CheckActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> CheckActivity");
                break;
            case tag_Kaoqin:
                intent.setClass(MenuActivity.this, KaoQinActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> KaoQinActivity");
                break;
            case tag_Panku:
                intent.setClass(MenuActivity.this, PankuActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> PankuActivity");
                break;
            case tag_Viewpic:
                intent.setClass(MenuActivity.this, ViewPicByPidActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> ViewPicByPidActivity");
                break;
            case tag_ChukudanPrint:
                intent.setClass(MenuActivity.this, PreChukuActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> PreChukuActivity");
                break;
            case tag_CaigouTakePic:
                intent.setClass(MenuActivity.this, CaigouActivity.class);
                MyApp.myLogger.writeInfo("<page> CaigouActivity");
                startActivity(intent);
                break;
            case tag_Ruku:
                intent.setClass(MenuActivity.this, RukuTagPrintAcitivity.class);
                MyApp.myLogger.writeInfo("<page> RukuTagPrintAcitivity");
                startActivity(intent);
                break;
            case tag_Admin:
                AlertDialog.Builder specialDialog = new AlertDialog.Builder(MenuActivity.this);
                View v = LayoutInflater.from(MenuActivity.this).inflate(R.layout.dialog_admin_manager_layout, null);
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
            case tag_Setting:
                intent.setClass(MenuActivity.this, SettingActivity.class);
                MyApp.myLogger.writeInfo("<page> SettingActivity");
                startActivity(intent);
                break;
            case tag_Print:
                intent = new Intent(MenuActivity.this, SFActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> SFActivity");
                break;
            case tag_shangjia:
                intent.setClass(MenuActivity.this, ShangjiaActivity.class);
                startActivity(intent);
                MyApp.myLogger.writeInfo("<page> shangjia");
                break;
        }
    }
}
