package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.entity.QdInfo;
import com.b1b.js.erpandroid_kf.fragment.RukuViewModel;
import com.b1b.js.erpandroid_kf.mvcontract.QdContract;
import com.b1b.js.erpandroid_kf.mvcontract.callback.DataObj;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utils.MyDecoration;
import utils.common.UploadUtils;

public class QdListActivity extends BaseScanActivity implements View.OnClickListener {

    private EditText edPid;
    private Handler mHandler = new Handler();
    private ProgressDialog pd;
    private TextView tv;
    private QdContract.SHQDPresenter presenter;
    private QdContract.QdView mvpView;
    private List<QdInfo> mdata;
   private RcAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qd_list);
        android.support.v7.widget.Toolbar tb = (android.support.v7.widget.Toolbar) findViewById(R.id
                .dyjkf_normalTb);
        tb.setTitle("送货清单");
        tb.setSubtitle("");
        tv = (TextView) findViewById(R.id.qd_ed_dataview);
        edPid = (EditText) findViewById(R.id.qd_ed_date);
        Button btnSearch = (Button) findViewById(R.id.qd_btn_search);
        Button btnScan = (Button) findViewById(R.id.qd_btn_scan);
        RecyclerView recycleView = (RecyclerView) findViewById(R.id.myRecycle);
        btnSearch.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        mvpView = new QdContract.QdView() {
            @Override
            public void getDataRet(DataObj<List<QdInfo>> mData) {
                if (mData.errCode == 0) {
                    pd.cancel();
                    mdata.clear();
                    mdata.addAll(mData.mData);
                    adapter.notifyDataSetChanged();
                } else {
                    pd.cancel();
                    showMsgToast("查询不到相关信息！！！");
                }
            }

            public void startSearch(String pid) {
                pd = new ProgressDialog(mContext);
                pd.setTitle(pid);
                pd.setMessage("正在搜索...");
                pd.show();
            }

            @Override
            public void setPrinter(QdContract.SHQDPresenter shqdPresenter) {

            }
        };
        RukuViewModel model = new RukuViewModel();
        model.startSearch("", "", "");
        presenter = new QdContract.QdPresenterImpl(mvpView);
        mdata = new ArrayList<>();
        adapter = new RcAdapter(this, mdata, new RcAdapter.ClickListener() {
            @Override
            public void itemClick(int positon) {
                QdInfo qdInfo = mdata.get(positon);
                Intent mIntent = new Intent(QdListActivity.this, QdTakePicActivity.class);
                mIntent.putExtra("pid", qdInfo.getTvProId() + "_" + qdInfo.getTvDate());
                startActivity(mIntent);
            }
        });
        recycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycleView.addItemDecoration(new MyDecoration(this));
        recycleView.setAdapter(adapter);
        edPid.setText(UploadUtils.getSQDate(new Date()));
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }

    @Override
    public void getCameraScanResult(String result, int code) {
        super.getCameraScanResult(result, code);
        readCode(result);
    }

    void readCode(String code) {
        String[] isOk = code.split("_");
        if (isOk.length == 2) {
            presenter.startSearch2(isOk[1],isOk[0] );
        }else{
            showMsgToast( "条码格式有误");
        }
    }

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        readCode(result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qd_btn_scan:
                startScanActivity();
                break;
            case R.id.qd_btn_search:
                String pid = edPid.getText().toString();
                presenter.startSearch(pid);
                break;
        }
    }

    /*{"制单月份":"201810","供应商":"北京恒成伟业电子有限公司","供应商ID":"9204","开票公司":"北京北方科讯电子技术有限公司
        ","单数":"2","批注":""}*/
    static class MyHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvProId;
        TextView tvProName;
        TextView tvKpName;
        TextView tvCounts;
        TextView tvNote;
        public MyHolder(View itemView) {
            super(itemView);

            tvNote = (TextView) itemView.findViewById(R.id.item_qdlist_tv_notes);
            tvDate = (TextView) itemView.findViewById(R.id.item_qdlist_tv_date);
            tvProId = (TextView) itemView.findViewById(R.id.item_qdlist_tv_providerid);
            tvProName = (TextView) itemView.findViewById(R.id.item_qdlist_tv_providername);
            tvKpName = (TextView) itemView.findViewById(R.id.item_qdlist_tv_kaipiaoname);
            tvCounts = (TextView) itemView.findViewById(R.id.item_qdlist_tv_counts);
        }
    }


    static class RcAdapter extends RecyclerView.Adapter<MyHolder> {
        interface ClickListener{
            void itemClick(int positon);
        }
        private Context mContext;
        private List<QdInfo> qdInfos;

        private ClickListener mListener;

        public RcAdapter(Context mContext, List<QdInfo> qdInfos, ClickListener mListener) {
            this.mContext = mContext;
            this.qdInfos = qdInfos;
            this.mListener = mListener;
        }

        public RcAdapter(Context mContext, List<QdInfo> qdInfos) {
            this.mContext = mContext;
            this.qdInfos = qdInfos;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemview = LayoutInflater.from(mContext).inflate(R.layout.item_qdlist, parent, false);
            return new MyHolder(itemview);
        }

        @Override
        public void onBindViewHolder(MyHolder holder,  int position) {
            holder.tvCounts.setText("单数:"+qdInfos.get(position).getTvCounts());
            holder.tvKpName.setText("开票公司:"+qdInfos.get(position).getTvKpName());
            holder.tvProName.setText("供应商:"+qdInfos.get(position).getTvProName());
            holder.tvDate.setText("制单月份:"+qdInfos.get(position).getTvDate());
            holder.tvProId.setText("供应商ID:"+qdInfos.get(position).getTvProId());
            holder.tvNote.setText("批注:"+qdInfos.get(position).getTvNote());
            final int tempPosition = holder.getAdapterPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.itemClick(tempPosition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return qdInfos.size();
        }
    }

}
