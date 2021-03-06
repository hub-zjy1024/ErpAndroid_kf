package com.b1b.js.erpandroid_kf.fragment;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.ChuKuActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.adapter.ChuKuTongZhiAdapter;
import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utils.common.MyJsonUtils;
import utils.framwork.MyToast;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;


/**
 A simple {@link Fragment} subclass. */
public class ChuKuTongZhiFragment extends ChukuBaseFragment implements View.OnClickListener,NoLeakHandler.NoLeakCallback {

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
    private List<ChukuTongZhiInfo> data = new ArrayList<>();
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                List<ChukuTongZhiInfo> list = (List<ChukuTongZhiInfo>) msg.obj;
                data.addAll(list);
                adapter.notifyDataSetChanged();
                isFinish = true;
                SoftKeyboardUtils.closeInputMethod(edPartNo, mContext);
                MyToast.showToast(mContext, "查询到" + data.size() + "条数据");
                break;
            case 1:
                isFinish = true;
                MyToast.showToast(mContext, "查询条件有误");
                break;
            case 2:
                isFinish = true;
                MyToast.showToast(mContext, "当前网络质量较差，请稍后尝试");
                break;
        }
    }
    private Handler mHandler = new NoLeakHandler(this);
    private Button btnScan;
    private String loginID;

    public ChuKuTongZhiFragment() {
    }

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
        View view = inflater.inflate(R.layout.fragment_chukutongzhi, container, false);
        tvStime = (TextView) view.findViewById(R.id.chukutongzhi_stime);
        tvEtime = (TextView) view.findViewById(R.id.chukutongzhi_etime);
        edPartNo = (EditText) view.findViewById(R.id.frag_chukutongzhi_GoodNo);
        edPid = (EditText) view.findViewById(R.id.frag_chukutongzhi_pid);
        btnSearch = (Button) view.findViewById(R.id.frag_chukutongzhi_search);
        btnCleartime = (Button) view.findViewById(R.id.chukutongzhi_cleartime);
        btnScan = (Button) view.findViewById(R.id.chukutongzhi_btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChuKuActivity activity = (ChuKuActivity) mContext;
                activity.startScanActivity();
            }
        });
        radioGroup = (RadioGroup) view.findViewById(R.id.chukutongzhi_rgroup);
        //初始化listView并填充
        lv = (ListView) view.findViewById(R.id.frag_chukutongzhidan_lv);
        adapter = new ChuKuTongZhiAdapter(data, mContext);
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

    private void setTvTime(final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                textView.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    public static String getFormatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public String getStringDateBefore(int day) {
        Calendar c = Calendar.getInstance(); // 当时的日期和时间
        int oldtime = c.get(Calendar.DAY_OF_MONTH) - day;
        c.set(Calendar.DAY_OF_MONTH, oldtime);
        return getFormatDate(c.getTime());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chukutongzhi_cleartime:
                tvStime.setText("起(点击设置)");
                tvEtime.setText("终(点击设置)");
                break;
            case R.id.chukutongzhi_stime:
                setTvTime(tvStime);
                break;
            case R.id.chukutongzhi_etime:
                setTvTime(tvEtime);
                break;
            case R.id.frag_chukutongzhi_search:
                //                if (isFinish) {
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
                getData(loginID, partNo, pid, sttime, endtime);
                //                } else {
                //                    MyToast.showToast(mContext, "请稍后，上次查询还未完成");
                //                }
                break;

        }
    }

    private void getData(final String uid, final String partNo, final String pid, final String stime, final String etime) {
        Runnable getDataRun = new Runnable() {
            @Override
            public void run() {
                try {
                    String json = getChuKuTongZhiInfoList("", uid, stime, etime, pid, partNo);
                    List<ChukuTongZhiInfo> list = MyJsonUtils.getCKTZList(json);
                    if (list != null && list.size() > 0) {
                        mHandler.obtainMessage(0, list).sendToTarget();
                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(getDataRun);
    }

    private String getChuKuTongZhiInfoList(String checkWord, String uid, String stime, String etime, String pid, String partNo) throws IOException, XmlPullParserException {
        String soapRes = ChuKuServer.GetChuKuTongZhiInfoList(checkWord, uid, stime, etime, pid, partNo);
        return soapRes;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
