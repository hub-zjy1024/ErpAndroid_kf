package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuAdapter;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.mvcontract.MyPankuListContract;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;

import java.util.ArrayList;
import java.util.List;

public class MyPankuListActivity extends ToolbarHasSunmiActivity implements MyPankuListContract.IView {
    private List<PankuInfo> pkData;
    private PankuAdapter mAdapter;
    AlertDialog choiceMethodDialog;
    MyPankuListContract.Presenter mPresenter;
    private PankuInfo currentInfo;
    SwipeRefreshLayout mRefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_panku_list);
        ListView lv = getViewInContent(R.id.activity_my_panku_list_lv_items);
        pkData = new ArrayList<>();
        mAdapter = new PankuAdapter(mContext, pkData, R.layout.item_panku_mylist);
        // lv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mAdapter.addListener(new PankuAdapter.ItemListener() {
            @Override
            public void itemClick(int id, PankuInfo mInfo) {
                switch (id) {
                    case R.id.item_pk_btn_rprint:
                        if (mInfo != null) {
                            openPrintPage(mInfo.getDetailId());
                        }

                        break;
                    case R.id.item_pk_btn_takepic:
                        if (mInfo != null) {
                            startTakePic(mInfo.getDetailId());
                        }
                        break;
                }
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                currentInfo = item;
                if (item == null) {
                    return;
                }
                openDetail(currentInfo);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                TextView tvMore = (TextView) view.findViewById(R.id.chukudan_items_tvMore);
                PankuAdapter.CheckClass checkClass = new PankuAdapter.CheckClass(item, "1");
                tvMore.setTag(checkClass);
                tvMore.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                tv.setText(item.toExtraString());
                return true;
            }
        });
        lv.setAdapter(mAdapter);
        mPresenter = new MyPankuListContract.Presenter(this);
         mRefresher = getViewInContent(R.id.activity_my_panku_list_swipe_refresh);
        mRefresher.setColorSchemeColors(getResColor(R.color.colorPrimary),getResColor(R.color.color_green),getResColor(R.color.button_light_bg) );
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.getMyList(loginID);
            }
        });
        mPresenter.getMyList(loginID);
    }
    public void openDetail(PankuInfo mInfo) {
        Intent mIntent = new Intent(this, PankuDetailActivity.class);
        mIntent.putExtra(PankuDetailActivity.extra_DATA, com.alibaba.fastjson.JSONObject.toJSONString(mInfo));
        startActivityForResult(mIntent, PankuDetailActivity.ResultCode);
    }

    public int getResColor(int id) {
        return getResources().getColor(id);
    }
    private void openPrintPage(String mid) {
        Intent mINten = new Intent(this, RukuTagPrintAcitivity.class);
        mINten.putExtra(RukuTagPrintAcitivity.extra_DPID, mid);
        startActivity(mINten);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PankuDetailActivity.ResultCode) {

            mPresenter.getMyList(loginID);
            Log.e("zjy", "MyPankuListActivity->onActivityResult(): backFromPanKu==");
        } else {
            Log.e("zjy", "MyPankuListActivity->onActivityResult(): backFromPanKu==" + resultCode);
        }
    }
    void startTakePic(final String detailID) {
        if (choiceMethodDialog != null && !choiceMethodDialog.isShowing()) {
            choiceMethodDialog.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("上传方式选择");
        builder.setItems(new String[]{"拍照", "从手机选择"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApp.myLogger.writeInfo("菜单拍照：" + which);
                Intent intent = new Intent();
                intent.putExtra(IntentKeys.key_pid, detailID);
                switch (which) {
                    case 0:
                        intent.setClass(mContext, TakePicChildPanku.class);
                        MyApp.myLogger.writeInfo("takepic_panku");
                        break;
                    case 1:
                        intent.setClass(mContext, ObtainPicPanku.class);
                        MyApp.myLogger.writeInfo("obtain_panku");
                        break;
                    case 2:
                        break;
                }
                startActivity(intent);
            }
        });
        choiceMethodDialog = builder.show();
    }

    @Override
    public String setTitle() {
        return "待盘库列表";
    }

    @Override
    public void onMyListRet(RetObject retObj, List<PankuInfo> infos) {
        if (retObj.errCode == 0) {
            pkData.clear();
            pkData.addAll(infos);
            mAdapter.notifyDataSetChanged();
        } else {
            showMsgToast(retObj.errMsg);
        }
        if (mRefresher != null) {
            mRefresher.setRefreshing(false);
        }
    }

    @Override
    public int loadingWithId(String msg) {
        return showProgressWithID(msg);
    }

    @Override
    public void cancelLoading(int pIndex) {
        cancelDialogById(pIndex);
    }

    @Override
    public void setPrinter(MyPankuListContract.Presenter presenter) {

    }
}
