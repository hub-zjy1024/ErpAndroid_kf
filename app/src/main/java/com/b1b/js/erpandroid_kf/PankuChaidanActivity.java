package com.b1b.js.erpandroid_kf;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuChaidanAdapter;
import com.b1b.js.erpandroid_kf.entity.PankuChaidanJsonData;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.entity.PkChaidanItem;
import com.b1b.js.erpandroid_kf.mvcontract.PankuChaidanContract;
import com.b1b.js.erpandroid_kf.myview.ScrollInnerListview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PankuChaidanActivity extends ToolbarHasSunmiActivity implements View.OnClickListener, PankuChaidanContract.IView {
    private List<PkChaidanItem> mDatas;
    private PankuChaidanAdapter mAdapter;
    public static String mIntent_Data_key = "pkData";
    private PankuChaidanContract.Presenter mPresenter;
    PankuInfo nowInfo;
    ScrollInnerListview dataView;
    private AtomicInteger mId = new AtomicInteger(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panku_chaidan);

        Button btnCommit = getViewInContent(R.id.activity_panku_chaidan_btn_commit);
        Button btnNew = getViewInContent(R.id.activity_panku_chaidan_btn_new_split);
         dataView = getViewInContent(R.id.activity_panku_chaidan_lv_datas);
        ScrollView mScroll = getViewInContent(R.id.style_pk_chaidan_detail_scrollView);
        dataView.setParent(mScroll);

        mDatas = new ArrayList<>();
        mAdapter = new PankuChaidanAdapter(mContext, mDatas, R.layout.item_panku_chaidan, new PankuChaidanAdapter.DataChangeNotifyListener() {
            @Override
            public void onChange() {
                TextView tvDataCount = getViewInContent(R.id.activity_panku_chaidan_tv_data_count);
                tvDataCount.setText("拆分列表(" +mDatas.size()+
                        ")");
            }
        });
        dataView.setAdapter(mAdapter);
        btnCommit.setOnClickListener(this);
        btnNew.setOnClickListener(this);
        mPresenter = new PankuChaidanContract.Presenter(this);

        String data = getIntent().getStringExtra(mIntent_Data_key);
        if (data != null) {
            PankuInfo minfo = JSONObject.parseObject(data, PankuInfo.class);
//            Log.e("zjy", "PankuChaidanActivity->onCreate(): mdata==" + minfo.toStringDetail());
            nowInfo = minfo;
        }
        TextView tvPid = getViewInContent(R.id.activity_panku_chaidan_tv_pid);
        TextView tvPihao = getViewInContent(R.id.activity_panku_chaidan_tv_pihao);
        TextView tvDtPid = getViewInContent(R.id.activity_panku_chaidan_tv_dtpid);
        TextView tvCount = getViewInContent(R.id.activity_panku_chaidan_tv_count);
        TextView tvPartno = getViewInContent(R.id.activity_panku_chaidan_tv_partno);

        if (nowInfo != null) {
            tvPid.setText(nowInfo.getPid());
            tvPihao.setText(nowInfo.getPihao());
            tvDtPid.setText(nowInfo.getDetailId());
            tvCount.setText(nowInfo.getLeftCounts());
            tvPartno.setText(nowInfo.getPartNo());
        }else {
            btnCommit.setEnabled(false);
            btnNew.setEnabled(false);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_panku_chaidan_btn_commit:
                if (nowInfo == null) {
                    showMsgDialog("传递的盘库信息为空");
                    return;
                }
                if (mDatas.size() > 0) {
                    int count = 0;
                    for (int i = 0; i < mDatas.size(); i++) {

                        int tempCount = mDatas.get(i).Number;
                        if (tempCount == 0) {
                            showMsgDialog("拆分数量不能0,编号为" + mDatas.get(i).id);
                            dataView.smoothScrollToPosition(i);
                            return;
                        }
                        count += tempCount;

                    }
                    int mCount = 0;
                    try {
                        mCount = Integer.parseInt(nowInfo.getLeftCounts());
                    } catch (Throwable e) {
                        showMsgDialog("剩余数量不为数字,str=" + nowInfo.getLeftCounts());
                        return;
                    }
                    if (count > mCount) {
                        showMsgDialog("拆分总数超过原有数量" + mCount);
                        return;
                    }
                    if (count == 0) {
                        showMsgDialog("拆分总数不能为" + mCount);
                        return;
                    }
                    PankuChaidanJsonData mdata = new PankuChaidanJsonData();
                    mdata.loginID = loginID;
                    mdata.instorageDetailID = nowInfo.getDetailId();
                    mdata.instorageMainID = nowInfo.getPid();
                    mdata.json = mDatas;
                    mPresenter.panKuChaidan(mdata);
                } else {
                    showMsgToast("请至少拆分出一个批号");
                }
                break;
            case R.id.activity_panku_chaidan_btn_new_split:
                if (nowInfo == null) {
                    showMsgDialog("传递盘库信息为空，无法拆分");
                    return;
                }
                PkChaidanItem mItem = new PkChaidanItem();
                mItem.Number = 0;
//                String id = Myuuid.create2(6);
                String id = mId.getAndIncrement() + "";
                mItem.id = id;
                mItem.BatchNo = "";
                mDatas.add(mItem);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
    @Override
    public String setTitle() {
        return getString(R.string.title_panku_chaidan);
    }

    public void onChaidanOk() {
        showMsgDialogWithCallback("拆单完成,即将返回", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    @Override
    public void init() {
        super.init();


    }

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
    }

    @Override
    public void getCameraScanResult(String result) {
        super.getCameraScanResult(result);
    }


    @Override
    public void cancelLoading(int id) {
        cancelDialogById(id);
    }

    @Override
    public void onChaidanResult(int code, String msg) {
        if (code == 0) {
            onChaidanOk();
        }else {
            showMsgDialog(msg);
        }
    }

    @Override
    public void setPrinter(PankuChaidanContract.Presenter presenter) {

    }
}
