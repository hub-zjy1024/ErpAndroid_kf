package com.b1b.js.erpandroid_kf.acview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.ChuKuActivity;
import com.b1b.js.erpandroid_kf.ChukuBaseFragment;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.adapter.ChuKuTongZhiAdapter;
import com.b1b.js.erpandroid_kf.contract.CktzContract;
import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;

import java.util.Date;
import java.util.List;

import utils.MyToast;

public class CktzFragment extends ChukuBaseFragment implements CktzContract.IcktzView{

    private Button btnSearch;
    private Button btnCleartime;
    private TextView tvStime;
    private TextView tvEtime;
    private EditText edPartNo;
    private EditText edPid;
    private RadioGroup radioGroup;
    private String pid;
    private String partNo;
    private boolean isFinish = true;
    private ListView lv;
    private ChuKuTongZhiAdapter adapter;
    private String loginID;
    private List<ChukuTongZhiInfo> data;
    private CktzContract.IcktzPresenter mPresenter;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loginID = getArguments().getString("loginID");
        View view = inflater.inflate(R.layout.fragment_chukutongzhi, container, false);
        tvStime = (TextView) view.findViewById(R.id.chukutongzhi_stime);
        tvEtime = (TextView) view.findViewById(R.id.chukutongzhi_etime);
        edPartNo = (EditText) view.findViewById(R.id.frag_chukutongzhi_GoodNo);
        edPid = (EditText) view.findViewById(R.id.frag_chukutongzhi_pid);
        btnSearch = (Button) view.findViewById(R.id.frag_chukutongzhi_search);
        btnCleartime = (Button) view.findViewById(R.id.chukutongzhi_cleartime);
        Button btnScan = (Button) view.findViewById(R.id.chukutongzhi_btn_scan);
        btnScan.setOnClickListener(this);
        radioGroup = (RadioGroup) view.findViewById(R.id.chukutongzhi_rgroup);
        //初始化listView并填充
        lv = (ListView) view.findViewById(R.id.frag_chukutongzhidan_lv);
        adapter = new ChuKuTongZhiAdapter(data, getActivity());
        lv.setAdapter(adapter);

        tvStime.setOnClickListener(this);
        tvEtime.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnCleartime.setOnClickListener(this);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                ChukuTongZhiInfo item = (ChukuTongZhiInfo) parent.getItemAtPosition(position);
                tv.setText(item.toString());
                TextView tvMore = (TextView) view.findViewById(R.id.chukudan_items_tvMore);
                tvMore.setVisibility(View.GONE);
                return true;
            }
        });
        return view;

    }
    public void finishSearch(String msg){
        MyToast.showToast(getContext(),msg);
    }
    public void setBeginTv(){
        setTvTime(tvStime);
    }

    public Button getBtnSearch() {
        return btnSearch;
    }

    public EditText getEdPid() {
        return edPid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chukutongzhi_cleartime:
                tvStime.setText("起(点击设置)");
                tvEtime.setText("终(点击设置)");
                break;
            case R.id.chukutongzhi_stime:
                setBeginTv();
                break;
            case R.id.chukutongzhi_btn_scan:
                ChuKuActivity activity = (ChuKuActivity) getActivity();
                activity.startScanActivity();
                break;
            case R.id.chukutongzhi_etime:
               setEndTv();
                break;
            case R.id.frag_chukutongzhi_search:
                String sttime = "";
                String endtime = "";
                if (tvStime.getText().toString().trim().equals("起(点击设置)")) {
                    //不设置起始时间，起始时间为radiobution选择的，默认终止时间为当天
                    endtime = getFormatDate(new Date());
                    switch (radioGroup.getCheckedRadioButtonId()) {
                        case R.id.chukutongzhi_rbn_amonth:
                            sttime = getStringDateBefore(30);
                            break;
                        case R.id.chukutongzhi_rbn_halfamonth:
                            sttime = getStringDateBefore(15);
                            break;
                        case R.id.chukutongzhi_rbn_halfayear:
                            sttime = getStringDateBefore(180);
                            break;
                    }
                } else if (tvEtime.getText().toString().trim().equals("终(点击设置)")) {
                    //不设置终止时间，起始时间由tvStime决定，默认终止时间为当天
                    sttime = tvStime.getText().toString();
                    endtime = getFormatDate(new Date());
                } else {
                    //都设置了
                    sttime = tvStime.getText().toString();
                    endtime = tvStime.getText().toString();
                }
                searchBefore();
                mPresenter.getData(loginID,partNo,pid,sttime,endtime);
                break;

        }
    }
    public void searchBefore(){
        pid = edPid.getText().toString().trim();
        partNo = edPartNo.getText().toString().trim();
    }

    @Override
    public void setPresenter(CktzContract.IcktzPresenter presenter) {
        mPresenter = presenter;
    }

    public void updateList(List<ChukuTongZhiInfo> list, String msg){

        if(list!=null){
            data.clear();
            data.addAll(list);
            adapter.notifyDataSetChanged();
            finishSearch("查询到" + data.size() + "条数据");
        }else {
            finishSearch("查询不到相关信息");
        }
    }

    public void setEndTv(){
        setTvTime(tvEtime);
    }
}
