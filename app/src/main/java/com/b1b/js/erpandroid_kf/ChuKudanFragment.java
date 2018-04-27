package com.b1b.js.erpandroid_kf;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.b1b.js.erpandroid_kf.adapter.ChuKuDanAdapter;
import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;

import org.json.JSONException;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import utils.MyJsonUtils;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChuKudanFragment extends Fragment implements View.OnClickListener {
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
    private DatePickerDialog datePickerDialog;

    public ChuKudanFragment() {

    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
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
    };

    public Button getBtnSearch() {
        return btnSearch;
    }

    public EditText getEdPid() {
        return edPid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chu_kudan, container, false);
        lv = (ListView) view.findViewById(R.id.frag_chukudan_lv);
        edPartNo = (EditText) view.findViewById(R.id.frag_chukudan_GoodNo);
        edPid = (EditText) view.findViewById(R.id.frag_chukudan_pid);
        adapter = new ChuKuDanAdapter(data, getActivity());
        btnSearch = (Button) view.findViewById(R.id.frag_chukudan_search);
        tvStime = (TextView) view.findViewById(R.id.chukudan_stime);
        tvEtime = (TextView) view.findViewById(R.id.chukudan_etime);
        radioGroup = (RadioGroup) view.findViewById(R.id.chukudan_rgroup);
        timeClear = (Button) view.findViewById(R.id.chukudan_cleartime);
        Button btnScan = (Button) view.findViewById(R.id.chukudan_btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.b1b.js.erpandroid_kf.ChuKuActivity activity = (com.b1b.js.erpandroid_kf.ChuKuActivity) getActivity();
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
        //默认半年内，查询结果最多100条
//        getData("2309", "", "", getStringDateBefore(180), getFormatDate(new Date()));
        return view;
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


    private void setTvTime(final TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                textView.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void getData(final String uid, final String partNo, final String pid, final String stime, final String etime) {
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

    public static String getGetChuKuInfoList(String checkWord, String uid, String stime, String etime, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("uid", uid);
        properties.put("stime", stime);
        properties.put("etime", etime);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuInfoList");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils.ChuKuServer);
        return response.toString();
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
                    getData(com.b1b.js.erpandroid_kf.MyApp.id, partNo, pid, sttime, endtime);
                } else {
                    MyToast.showToast(getActivity(), "请稍后，上次查询还未完成");
                }
                break;

        }
    }
}
