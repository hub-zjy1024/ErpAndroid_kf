package com.b1b.js.erpandroid_kf;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.ChuKuDanAdapter;
import com.b1b.js.erpandroid_kf.contract.CkdContract;
import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utils.MyJsonUtils;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.wsdelegate.ChuKuServer;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChuKudanFragment extends ChukuBaseFragment implements NoLeakHandler.NoLeakCallback,CkdContract.ICkdView{
    private ListView lv;
    private List<ChuKuDanInfo> data = new ArrayList<>();
    private ChuKuDanAdapter adapter;
    private String partNo;
    private String pid;
    private EditText edPartNo;
    private EditText edPid;
    private Button btnSearch;
    private Button timeClear;
    private boolean isFinish = true;
    private TextView tvStime;
    private TextView tvEtime;
    private RadioGroup radioGroup;
    private Calendar calendar;// 用来装日期的
    private String loginID;
    private ProgressDialog pd;
    private CkdContract.Presenter mPresenter;
    public ChuKudanFragment() {

    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 0) {
            List<ChuKuDanInfo> list = (List<ChuKuDanInfo>) msg.obj;
            data.addAll(list);
            adapter.notifyDataSetChanged();
            isFinish = true;
            SoftKeyboardUtils.closeInputMethod(edPartNo, getActivity());
            MyToast.showToast(getActivity(), "查询到" + data.size() + "条数据");
        } else if (msg.what == 1) {
            isFinish = true;
            MyToast.showToast(getActivity(), "查询条件有误");
        } else if (msg.what == 2) {
            isFinish = true;
            MyToast.showToast(getActivity(), "当前网络质量较差，查询失败");
        }
    }
    private Handler handler = new NoLeakHandler(this);

    public Button getBtnSearch() {
        return btnSearch;
    }

    public EditText getEdPid() {
        return edPid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loginID = getArguments().getString("loginID");
        View view = inflater.inflate(R.layout.fragment_chu_kudan, container, false);
        lv = (ListView) view.findViewById(R.id.frag_chukudan_lv);
        edPartNo = (EditText) view.findViewById(R.id.frag_chukudan_GoodNo);
        edPid = (EditText) view.findViewById(R.id.frag_chukudan_pid);
        adapter = new ChuKuDanAdapter(data, getActivity(), R.layout.chukudanlist_items);
        btnSearch = (Button) view.findViewById(R.id.frag_chukudan_search);
        tvStime = (TextView) view.findViewById(R.id.chukudan_stime);
        tvEtime = (TextView) view.findViewById(R.id.chukudan_etime);
        radioGroup = (RadioGroup) view.findViewById(R.id.chukudan_rgroup);
        timeClear = (Button) view.findViewById(R.id.chukudan_cleartime);
        Button btnScan = (Button) view.findViewById(R.id.chukudan_btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.b1b.js.erpandroid_kf.ChuKuActivity activity = (com.b1b.js.erpandroid_kf.ChuKuActivity)
                        getActivity();
                activity.startScanActivity();
            }
        });
        timeClear.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        tvEtime.setOnClickListener(this);
        tvStime.setOnClickListener(this);
        calendar = Calendar.getInstance();
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvMore = (TextView) view.findViewById(R.id.chukudan_items_tvMore);
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                ChuKuDanInfo item = (ChuKuDanInfo) parent.getItemAtPosition(position);
                tv.setText(item.toString());
                tvMore.setVisibility(View.GONE);
                return true;
            }

        });
        pd = new ProgressDialog(getActivity());
        pd.setMessage("加载中");
        //默认半年内，查询结果最多100条
        //        getData("2309", "", "", getStringDateBefore(180), getFormatDate(new Date()));
        return view;
    }

    private void getData(final String uid, final String partNo, final String pid, final String stime, final
    String etime) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String json = getGetChuKuInfoList("", uid, stime, etime, pid, partNo);
                    List<ChuKuDanInfo> list = MyJsonUtils.getCKDList(json);
                    Log.e("zjy", "ChuKudanFragment->run(): JSon==" + json);
                    if (list != null && list.size() > 0) {
                        Message msg = handler.obtainMessage(0, list);
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    handler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String getGetChuKuInfoList(String checkWord, String uid, String stime, String etime,
                                             String pid, String partNo) throws IOException,
            XmlPullParserException {
        return ChuKuServer.GetChuKuInfoList(checkWord, uid, stime, etime, pid, partNo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chukudan_cleartime:
                tvStime.setText("起(点击设置)");
                tvEtime.setText("终(点击设置)");
                break;
            case R.id.chukudan_stime:
                setTvTime(tvStime);
                break;
            case R.id.chukudan_etime:
                setTvTime(tvEtime);
                break;
            case R.id.frag_chukudan_search:
                if (isFinish) {
                    data.clear();
                    adapter.notifyDataSetChanged();
                    pid = edPid.getText().toString().trim();
                    partNo = edPartNo.getText().toString().trim();
                    String sttime = "";
                    String endtime = "";
                    if (tvStime.getText().toString().trim().equals("起(点击设置)")) {
                        //不设置起始时间，起始时间为radiobution选择的，默认终止时间为当天
                        endtime = getFormatDate(new Date());
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.chukudan_rbn_amonth:
                                sttime = getStringDateBefore(30);
                                break;
                            case R.id.chukudan_rbn_halfamonth:
                                sttime = getStringDateBefore(15);
                                break;
                            case R.id.chukudan_rbn_halfayear:
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
                    mPresenter.getData(loginID, partNo, pid, sttime, endtime);
              //      getData(loginID, partNo, pid, sttime, endtime);
                } else {
                    MyToast.showToast(getActivity(), "请稍后，上次查询还未完成");
                }
                break;

        }
    }

    @Override
    public void finishSearch(String msg) {
        MyToast.showToast(getActivity(), msg);

    }

    @Override
    public void searchBefore() {
        pd.setMessage("正在查询");
        pd.show();
    }

    @Override
    public void setPresenter(CkdContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void updateList(List<ChuKuDanInfo> list, String msg) {
        if (list != null) {
            data.clear();
            data.addAll(list);
            adapter.notifyDataSetChanged();
            finishSearch("查询到" + data.size() + "条数据");
        } else {
            finishSearch("查询不到相关信息");
        }
        pd.cancel();
    }
}
