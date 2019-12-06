package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PreChukuParentAdapter;
import com.b1b.js.erpandroid_kf.entity.ChukuDetail;
import com.b1b.js.erpandroid_kf.entity.ChukuInfo;
import com.b1b.js.erpandroid_kf.entity.ChukuInfoNew;
import com.b1b.js.erpandroid_kf.entity.PreChukuItem;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.mvcontract.ParentChukuContract;
import com.b1b.js.erpandroid_kf.myview.ScrollInnerListview;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.yundan.SFActivity;

import java.util.ArrayList;
import java.util.List;

import utils.framwork.SoftKeyboardUtils;


/**
 * Created by 张建宇 on 2019/7/26.
 */
public class ParentChukuActivity extends ToolbarHasSunmiActivity implements View.OnClickListener,
                                                                            ParentChukuContract.ParentChukuView {
    EditText editTextPid;
    ParentChukuContract.Presenter mPresenter;

    TextView tvMinfo;
    EditText edChukuInfo;

    TextView tvDetail;
    TextView tvMxTag;
    ScrollInnerListview lvDetail;

    private List<ChukuDetail> mListData;
    private PreChukuParentAdapter mAdapter;

    private String uname;
    private String mKuqu;

    private String mFlag;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_chuku);
//        android.support.v4.app.FragmentManager supportFragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
//        Fragment mFrag = new ChuKudanFragment();
//        Bundle args = new Bundle();
//        args.putString("args", loginID);
//        mFrag.setArguments(args);
//        fragmentTransaction.add(R.id.activity_parent_chuku_fragcontainner, mFrag);
//        fragmentTransaction.show(mFrag);
//        fragmentTransaction.commit();
        mPresenter = new ParentChukuContract.Presenter(this, this);
        tvMinfo = getViewInContent(R.id.activity_parent_chuku_tv_maininfo);
//        tvDetail = getViewInContent(R.id.activity_parent_chuku_tv_detail);
        lvDetail = getViewInContent(R.id.activity_parent_chuku_lv_detail);
        final ScrollView mview = getViewInContent(R.id.activity_parent_chuku_tv_scroll);
        tvMxTag = getViewInContent(R.id.activity_parent_chuku_tv_tagMx);
        edChukuInfo = getViewInContent(R.id.activity_parent_chuku_tv_ed_res);

        edChukuInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mview.requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mview.requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_UP:

                        case MotionEvent.ACTION_CANCEL:
                            mview.requestDisallowInterceptTouchEvent(false);
                            break;
                        default:
                            break;
                    }
                    return edChukuInfo.onTouchEvent(ev);
            }
        });
        mListData = new ArrayList<>();
        mAdapter = new PreChukuParentAdapter(this, mListData, R.layout.item_pre_chuku_detail);
        lvDetail.setAdapter(mAdapter);
        lvDetail.setParent(mview);
        SharedPreferences mPref = getSharedPreferences(SettingActivity.PREF_USERINFO, MODE_PRIVATE);
        uname = mPref.getString("oprName", "");
        hideAll();
    }

    void hideAll() {
        showToolbar2("00000000");
    }

    void disableAll() {
        ViewGroup mContainer = getViewInContent(R.id.activity_parent_chuku_oprBar_container);
        int childCount = mContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View mView = mContainer.getChildAt(i);
            if (R.id.activity_parent_chuku_opr_btn_yundan_takepic == mView.getId()) {
                Button mbtn = (Button) mView;
                mbtn.setClickable(false);
                mbtn.setText("单据状态不可操作");
                mView.setVisibility(View.VISIBLE);
            } else {
                mView.setVisibility(View.GONE);
            }
        }
    }

    void showToolbarById(  int showId ) {
        View mView= getViewInContent(showId);
        mView.setVisibility(View.VISIBLE);
    }
    void showToolbar2(String flag) {
        List<PreChukuItem> mIds = new ArrayList<>();
        int hasCheck = 0;
        int showYundanCounts = 0;
        for (int i = 0; i < flag.length(); i++) {
            char c = flag.charAt(i);
            String str = c + "";
//            Log.e("zjy", getClass() + "->showToolbar2(): ==" + str + "\tindex=" + i);
            int id = R.id.activity_parent_chuku_opr_btn_pre_ck;
            int show = 0;
            PreChukuItem mItem = new PreChukuItem();
            if (i == 0 ) {
                id = R.id.activity_parent_chuku_opr_btn_diaobo;
                if ("1".equals(str)) {
                    show = 1;
                }
            }else if(i == 1) {
                id = R.id.activity_parent_chuku_opr_btn_pre_ck;
                if ("1".equals(str)) {
                    show = 1;
                }
            }else if(i == 2 ) {
                id = R.id.activity_parent_chuku_opr_btn_check_1;
                if ("1".equals(str)) {
                    show = 1;
                    hasCheck++;
                }
            }else if(i == 3 ) {
                id = R.id.activity_parent_chuku_opr_btn_check_2;
                if ("1".equals(str)) {
                    hasCheck++;
                    show = 1;
                }
            }else if(i == 4 ) {
                id = R.id.activity_parent_chuku_opr_btn_sp_check;
                if ("1".equals(str)) {
                   show = 1;
                }
            } else if (i == 5 || i == 6) {
                id = R.id.activity_parent_chuku_opr_btn_yundan;
                if ("1".equals(str)) {
                    show = 1;
                    showYundanCounts++;
                    continue;
                }
            } else if (i == 7) {
                id = R.id.activity_parent_chuku_opr_btn_finish_ck;
                if ("1".equals(str)) {
                    show = 1;
                }
            } else {
                break;
            }
            mItem.id = id;
            mItem.isShow = show;
            mIds.add(mItem);
        }
        PreChukuItem failCk = new PreChukuItem();
        failCk.id = R.id.activity_parent_chuku_opr_btn_chukufail;
        failCk.isShow = 0;
        if (hasCheck > 0) {
            failCk.isShow = 1;
        }
        mIds.add(failCk);
        PreChukuItem yundanItem = new PreChukuItem();
        yundanItem.id = R.id.activity_parent_chuku_opr_btn_yundan;
        yundanItem.isShow = 0;
        if (showYundanCounts > 0) {
            yundanItem.isShow = 1;
        }
        mIds.add(yundanItem);
//        if (CheckUtils.isAdmin()) {
//            mIds.add(new PreChukuItem(R.id.activity_parent_chuku_opr_btn_chukufail, 1));
//        }
        for (int i = 0; i < mIds.size(); i++) {
            PreChukuItem item = mIds.get(i);
            int nowId = item.id;
            int isShow = item.isShow;
            View mView = getViewInContent(nowId);
            int isVi = View.GONE;
            if (isShow == 1) {
                isVi = View.VISIBLE;
            }
            mView.setVisibility(isVi);
        }
    }
    void showToolbar(String flag) {
        int showId = -1;
        if("-1".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_diaobo;
        } else if("2".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_pre_ck;
        } else if("3".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_check_1;
        } else if("4".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_check_2;
        } else if("5".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_sp_check;
        } else if("6".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_yundan;
        } else if("7".equals(flag)){
            showId = R.id.activity_parent_chuku_opr_btn_finish_ck;
        }
        ViewGroup mContainer = getViewInContent(R.id.activity_parent_chuku_oprBar_container);
        int childCount = mContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View mView = mContainer.getChildAt(i);
            if (showId == mView.getId()) {
                mView.setVisibility(View.VISIBLE);
            } else if (R.id.activity_parent_chuku_opr_btn_yundan_takepic == mView.getId()) {
                Button mbtn = (Button) mView;
                int color = getResources().getColor(R.color.controlTextSelectedColor);
                boolean enable = true;
                if (flag.equals("0")) {
                    color = getResources().getColor(R.color.controlTextDisabledColor);
                    enable = false;
                }
                mbtn.setEnabled(enable);
                mbtn.setTextColor(color);
                mView.setVisibility(View.VISIBLE);
            } else {
                mView.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public void init() {
        super.init();
        editTextPid = getViewInContent(R.id.activity_parent_chuku_tv_pid);
        SharedPreferences spKf = getSharedPreferences(SpSettings.PREF_KF, MODE_PRIVATE);
        String storageInfo = spKf.getString(SpSettings.storageKey, "");
        if ("".equals(storageInfo)) {
            alert("当前库区为空，请重新登录");
        }
        mKuqu = StorageUtils.getStorageInfo(storageInfo, "ChildStorageID");
        Log.e("zjy", getClass() + "->init(): mKuqu==" + mKuqu);

        setOnClickListener(this, R.id.activity_parent_chuku_btn_search);
        setOnClickListener(this, R.id.activity_parent_chuku_btn_scan);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_yundan_takepic);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_diaobo);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_check_1);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_check_2);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_sp_check);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_pre_ck);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_yundan);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_finish_ck);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_viewpic);
        setOnClickListener(this, R.id.activity_parent_chuku_opr_btn_chukufail);
        Runnable ipRun = new Runnable() {
            @Override
            public void run() {
                ip = StorageUtils.getCurrentIp();
            }
        };
        TaskManager.getInstance().execute(ipRun);
    }

    @Override
    public void getCameraScanResult(String result, int code) {
        super.getCameraScanResult(result, code);
        editTextPid.setText(result);
        mPresenter.getDataNew(result, ip);
    }

    @Override
    public void onPreCkInfoCb(ChukuInfo info) {
        SoftKeyboardUtils.closeInputMethod(editTextPid, this);
        String stateNow = info.StateNow;
        String flag = info.flag;
        mFlag = flag;
//        mFlag = "6";
        mListData.clear();
        mListData.addAll(info.details);
        tvMxTag.setText("明细(" +
                "" + info.details.size() +
                "):");
        tvMinfo.setText(info.toString());
        edChukuInfo.setText(info.chukuResult);
        mAdapter.notifyDataSetChanged();
        tvMxTag.setVisibility(View.VISIBLE);
        showToolbar2(stateNow);
    }

    private String testAlert = "";
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        final String tempPid = editTextPid.getText().toString();
        switch (v.getId()){
            case R.id.activity_parent_chuku_btn_scan:
                startScanActivity();
                break;
            case R.id.activity_parent_chuku_btn_search:
                testAlert += "这次测试数据1这次测试数据2这次测试数据3这次测试数据4|";
//                okAlert2(testAlert);
                if ("".equals(tempPid.trim())) {
                    showMsgToast("请先输入单据号");
                    return;
                }

                mPresenter.getDataNew(tempPid, ip);
                break;
            case R.id.activity_parent_chuku_opr_btn_diaobo:
//                （-1：调拨，1预出库，2：一次复核，3：二次复核 6：运单拍照)）
                mPresenter.UpdateStoreChekerInfo(tempPid, loginID, "-1", uname);
                break;
            case R.id.activity_parent_chuku_opr_btn_pre_ck:
                mPresenter.UpdateStoreChekerInfo(tempPid, loginID,"1", uname);
                break;

            case R.id.activity_parent_chuku_opr_btn_check_1:
                mPresenter.UpdateStoreChekerInfo(tempPid,loginID, "2", uname);
                break;
            case R.id.activity_parent_chuku_opr_btn_check_2:
                mPresenter.UpdateStoreChekerInfo(tempPid, loginID,"3", uname);
                break;
            case R.id.activity_parent_chuku_opr_btn_sp_check:
                mPresenter.SpCheckInfo(tempPid, loginID);
                break;
            case R.id.activity_parent_chuku_opr_btn_finish_ck:
                mPresenter.SetChuKuTongZhiChuKu(tempPid,loginID, "7", uname);
                break;
            case R.id.activity_parent_chuku_opr_btn_chukufail:
                AlertDialog mdialog = null;
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_chuku_checkinfo,
                        null, false);
                builder.setView(dialogView);
                builder.setTitle("复核不通过");
                final EditText editText = (EditText) dialogView.findViewById(R.id.dialog_chuku_checkinfo_ed);
                editText.setHint("请输入不通过原因");
                builder.setNegativeButton("不通过", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String info = editText.getText().toString();
                        info = "未能出库," + info;
                        mPresenter.setChukuFail(tempPid, loginID, uname, info);
                    }
                });
                builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                final AlertDialog finalMdialog = mdialog;
                builder.setCancelable(true);
                mdialog = builder.show();
                break;
//            case R.id.activity_parent_chuku_opr_btn_yd_takepic:
//                Intent intent2 = new Intent(this, YundanPicActivity.class);
//                intent2.putExtra(SettingActivity.extra_PID, tempPid);
//                intent2.putExtra("newFlag", mFlag);
//                if ("".equals(tempPid)) {
//                    showMsgToast("请先输入单据号");
//                    return;
//                }
//                startActivity(intent2);
//                break;
            case R.id.activity_parent_chuku_opr_btn_yundan:
                Intent mIntent = new Intent(this, SFActivity.class);
                mIntent.putExtra(SettingActivity.extra_PID, tempPid);
                if ("".equals(tempPid)) {
                    showMsgToast("请先输入单据号");
                    return;
                }
                startActivity(mIntent);

                break;
            case R.id.activity_parent_chuku_opr_btn_viewpic:
                Intent intent2 = new Intent(mContext, ViewPicByPid2Activity.class);
                intent2.putExtra(SettingActivity.extra_PID, tempPid);
                intent2.putExtra(ChukuTakePicActivity.ex_Flag, mFlag);
                if ("".equals(tempPid)) {
                    showMsgToast("请先输入单据号");
                    return;
                }
                startActivity(intent2);
                break;
            case R.id.activity_parent_chuku_opr_btn_yundan_takepic:
                Intent intent = new Intent(this, ChukuTakePicActivity.class);
                intent.putExtra(SettingActivity.extra_PID, tempPid);
                intent.putExtra(ChukuTakePicActivity.ex_Flag, mFlag);
                if ("".equals(tempPid)) {
                    showMsgToast("请先输入单据号");
                    return;
                }
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public String setTitle() {
        return "新版出库";
    }


    @Override
    public void fillList(List<ChukuInfoNew> infos) {
    }

    @Override
    public void onChangeSuccess(String flag) {
//        （-1：调拨，1预出库，2：一次复核，3：二次复核 6：运单拍照)）
        Intent intent = new Intent(mContext, CheckActivity.class);
        String tempId = editTextPid.getText().toString();
        String oprName = "调拨";
        if("-1".equals(flag)){
            oprName = "调拨完成";
        } else if("1".equals(flag)){
            oprName = "预出库完成";
        } else if("2".equals(flag)){
            oprName = "一次复核完成";
//            intent.setClass(mContext, SetCheckInfoActivity.class);
//            intent.putExtra("pid", tempId);
//            startActivity(intent);
        } else if("3".equals(flag)){
            oprName = "二次复核完成";
        } else if("5".equals(flag)){
            oprName = "特殊审批完成";
        } else if ("7".equals(flag)) {
            oprName = "出库完成";
//            mPresenter.getDataNew(tempId);
        } else if ("8".equals(flag)) {
            oprName = "已停止出库";
        } else {
            return;
        }
        okAlert(oprName);
        mPresenter.getDataNew(tempId, ip);
    }

    @Override
    public int loading2(String msg) {
        return showProgressWithID(msg);
    }

    @Override
    public void cancelLoading2(int id) {
        cancelDialogById(id);
    }

    @Override
    public void loading(String msg) {
        showProgress(msg);
    }

    @Override
    public void cancelLoading() {
        cancelProgress();
    }

    @Override
    public void alert(String msg) {
        showMsgDialog(msg);
    }

    public void okAlert2(String msg) {
        showMsgDialog(msg);
//        Dialog dialog =
//                DialogUtils.getDialog(mContext).showCommMsgDialog(msg);
//        dialog.show();
    }

    public void okAlert(String msg) {
        showMsgDialog(msg);
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle("成功");
//        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_common_contentview, null, false);
//        TextView mContent = (TextView) mView.findViewById(R.id.dialog_cm_tv);
//        mContent.setText(msg);
//        mContent.setTextColor(getResources().getColor(R.color.color_green));
//        builder.setView(mView);
//        builder.setCancelable(true);
//        AlertDialog alertDialog = builder.create();
//
//        alertDialog.show();
//        ViewParent parent = mView.getParent();
//        if (parent != null) {
//            ViewGroup mp = (ViewGroup) (parent);
//            mp.removeView(mView);
//        }
//        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.material_dialog_bg));
//        alertDialog.setContentView(mView);
    }

    public void errorAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("失败");
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_common_contentview, null, false);
        TextView mContent = (TextView) mView.findViewById(R.id.dialog_cm_tv);
        mContent.setTextColor(getResources().getColor(R.color.color_alert_red));
        mContent.setText(msg);
        builder.setView(mView);
        builder.setCancelable(true);
        builder.create().show();
    }

    /**
     * 当BaseView为Fragment时，在Activity中初始化Presenter，并传递到Fragment中，
     *
     * @param presenter
     */
    @Override
    public void setPrinter(ParentChukuContract.Presenter presenter) {

    }
}
