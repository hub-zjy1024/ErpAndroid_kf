package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoActivity;
import com.b1b.js.erpandroid_kf.activity.base.SlideBackActivity;
import com.b1b.js.erpandroid_kf.adapter.MenuActivityRvAdapter;
import com.b1b.js.erpandroid_kf.adapter.MenuGvAdapter;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.yundan.SFActivity;

import java.util.ArrayList;
import java.util.Date;

import utils.adapter.recyclerview.BaseItemClickListener;
import utils.adapter.recyclerview.BaseRvViewholder;
import utils.btprint.SPrinter;
import utils.common.log.LogUploader;
import utils.dbutils.ActivityRecoderDB;
import utils.framwork.DialogUtils;

public class MenuActivity extends SavedLoginInfoActivity implements OnItemClickListener {
    private final String tag_Ruku = "库存标签";
    private final String tag_Print = "运单打印";
    private final String tag_Kaoqin = "考勤";
    private final String tag_Chukudan = "出库单";
    private final String tag_ChukudanPrint = "出库单打印";
    private final String tag_Viewpic = "单据图片";
    private final String tag_Panku = "盘库";
    private final String tag_CaigouTakePic = "采购拍照";
    private final String tag_ChukuCheck = "出库拍照";
    private final String tag_Admin = "特殊";
    private final String tag_TEST = "测试1";

    private final String tag_Setting = "设置";
    private final String tag_shangjia = "货物上架";
    private final String tag_SHQD = "送货清单";

    private final String tag_Zbar = "TestZbar";
    private final String tag_SlideBack = "SlidebackAc";
    private final String tag_TestReupload = "图片重传";

    private GridView gv;
    private final String tag_hetong = "hetong";
    private final String tag_HKPIC = "HK出库拍照";
    private final String tag_ScanCheck = "扫码复核";
    private final String tag_newChuku = "新出库";
    private final String tag_Modify = "库存管理";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        Toolbar tb = (Toolbar) findViewById(R.id.dyjkf_normalTb);
        tb.setTitle("菜单");
        tb.setSubtitle("登陆人:" + loginID);
        setSupportActionBar(tb);
        gv = (GridView) findViewById(R.id.menu_gv);
        gv.setOnItemClickListener(this);
        addItemGV();
        MyApp.myLogger.writeInfo("user=" +loginID);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 按下BACK，同时没有重复
            int size = (int) MyApp.cachedThreadPool.getActiveCount() - 1;
            if (size > 0) {
                DialogUtils.getSpAlert(this, "后台还有" + size +
                        "张图片未上传完成，强制退出将导致图片上传失败", "提示").show();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void addItemGV() {
        ArrayList<MyMenuItem> data = new ArrayList<>();
        data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_Chukudan, "查看出库单和出库通知单"));
        if (CheckUtils.isAdmin()) {
            data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_Admin, "101"));
            data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_TEST, "测试1"));
//            data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_SHQD, "清单"));
            //            data.add(new MyMenuItem(R.mipmap.menu_setting_press, tag_Zbar, "测试zbar"));
//            data.add(new MyMenuItem(R.mipmap.menu_setting_press, tag_hetong, "测试zbar"));
            //            data.add(new MyMenuItem(R.mipmap.menu_setting_press, tag_SlideBack, "测试zbar"));
            data.add(new MyMenuItem(R.mipmap.menu_chuku, tag_ScanCheck, "101"));
        }
        data.add(new MyMenuItem(R.mipmap.menu_preprint, tag_ChukudanPrint, "出库单单据信息打印"));
        data.add(new MyMenuItem(R.mipmap.menu_check, tag_ChukuCheck, "出库审核功能和审核完成的拍照功能"));
        data.add(new MyMenuItem(R.drawable.menu_new_ck, tag_newChuku, "新出库流程"));
        data.add(new MyMenuItem(R.mipmap.menu_print, tag_Print, "顺丰下单并打印功能,以及打印手机接受的文件的功能"));
        data.add(new MyMenuItem(R.mipmap.menu_pic, tag_Viewpic, "查询单据关联的照片"));
        data.add(new MyMenuItem(R.mipmap.menu_panku, tag_Panku, "货物位置管理"));
        data.add(new MyMenuItem(R.mipmap.menu_shangjia, tag_shangjia, "上架"));
        data.add(new MyMenuItem(R.mipmap.menu_caigou_96, tag_CaigouTakePic, "采购单拍照功能"));
        data.add(new MyMenuItem(R.mipmap.menu_print, tag_Ruku, "蓝牙打印，打印入库标签"));
        data.add(new MyMenuItem(R.mipmap.menu_kucun_edit, tag_Modify, "查看出库单和出库通知单"));
        data.add(new MyMenuItem(R.mipmap.menu_hk_outstor, tag_HKPIC, "HK拍照"));
        data.add(new MyMenuItem(R.mipmap.menu_restart, tag_TestReupload, "tag_TestReupload"));
        data.add(new MyMenuItem(R.mipmap.menu_kaoqin, tag_Kaoqin, "查询考勤状态"));
        data.add(new MyMenuItem(R.mipmap.menu_setting_press, tag_Setting, "设置"));
        RecyclerView mView = getViewInContent(R.id.activity_menu_dataview);
        MenuActivityRvAdapter adap = new MenuActivityRvAdapter(data, R.layout.item_menu_rv, mContext, new BaseItemClickListener<MyMenuItem>() {
            public void onItemClick(BaseRvViewholder holder, MyMenuItem item) {
                MenuActivity.this.onItemClick(item);
            }
        });
        GridLayoutManager gridLayoutMgr = new GridLayoutManager(mContext, 3);
        mView.setLayoutManager(gridLayoutMgr );
        mView.setAdapter(adap);
        MenuGvAdapter adapter = new MenuGvAdapter(this, data, R.layout.item_menu_gv);
        gv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startService(new Intent(this, LogUploadService.class));
        LogUploader uploader = new LogUploader(this);
        uploader.ScheduelUpload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityRecoderDB mDB = ActivityRecoderDB.newInstance(this);
        String msg = mDB.getRecorderStrByDate(new Date());
        SPrinter printer = SPrinter.getPrinter();
        if (printer != null) {
            printer.closeBt();
//            printer.close();
        }
    }

    void onItemClick(MyMenuItem data) {
        String value = data.content;
        Intent intent = new Intent();
        switch (value) {
            case tag_SHQD:
                intent.setClass(mContext, QdListActivity.class);
                startActivity(intent);
                break;
            case tag_Chukudan:
                intent.setClass(mContext, ChuKuActivity.class);
                startActivity(intent);
                break;
            case tag_ChukuCheck:
                intent.setClass(mContext, CheckActivity.class);
                startActivity(intent);
                break;
            case tag_Kaoqin:
                intent.setClass(mContext, KaoQinActivity.class);
                startActivity(intent);
                break;
            case tag_Panku:
                intent.setClass(mContext, PankuActivity.class);
                startActivity(intent);
                break;
            case tag_Viewpic:
                intent.setClass(mContext, ViewPicByPidActivity.class);
                startActivity(intent);
                break;
            case tag_ChukudanPrint:
                intent.setClass(mContext, PreChukuActivity.class);
                startActivity(intent);
                break;
            case tag_CaigouTakePic:
                intent.setClass(mContext, CaigouActivity.class);
                startActivity(intent);
                break;
            case tag_Ruku:
                intent.setClass(mContext, RukuTagPrintAcitivity.class);
                startActivity(intent);
                break;
            case tag_TEST:
                intent.setClass(mContext, Check2_scan_activity.class);
                startActivity(intent);
                break;
            case tag_Admin:
                AlertDialog.Builder specialDialog = new AlertDialog.Builder(mContext);
                View v = LayoutInflater.from(mContext).inflate(R.layout
                        .dialog_admin_manager_layout, null);
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
                intent.setClass(mContext, SettingActivity.class);
                MyApp.myLogger.writeInfo("<page> SettingActivity");
                startActivity(intent);
                break;
            case tag_Print:
                intent = new Intent(mContext, SFActivity.class);
                startActivity(intent);
                break;
            case tag_shangjia:
                intent.setClass(mContext, ShangjiaActivity.class);
                startActivity(intent);
                break;
            case tag_SlideBack:
                intent.setClass(mContext, SlideBackActivity.class);
                startActivity(intent);
                break;
            case tag_TestReupload:
                intent.setClass(mContext, ReUpLoadPicActivity.class);
                startActivity(intent);
                break;
            case tag_hetong:
                intent.setClass(mContext, HetongActivity.class);
                startActivity(intent);
                break;
            case tag_Zbar:
                intent.setClass(mContext, KyExpressAcitivity.class);
                startActivity(intent);
                break;
            case tag_HKPIC:
                intent.setClass(mContext, HonkongChukuCheck.class);
                startActivity(intent);
                break;
            case tag_ScanCheck:
                //                intent.setClass(mContext, Check2_scan_activity.class);
                //                startActivity(intent);
                //                intent.setClass(mContext, ViewPicByPid2Activity.class);
                //                intent.putExtra(SettingActivity.extra_PID, "1234567");
                intent.setClass(mContext, PicDetailActivity2.class);
                startActivity(intent);
                break;
            case tag_newChuku:
                intent.setClass(mContext, ParentChukuActivity.class);
                startActivity(intent);
                break;
            case tag_Modify:
                intent.setClass(mContext, KucunEditActivity.class);
                startActivity(intent);
                break;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyMenuItem data = (MyMenuItem) parent.getItemAtPosition(position);
        onItemClick(data);
    }
}
