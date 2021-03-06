package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.KqAdapter;
import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;
import com.b1b.js.erpandroid_kf.task.CheckUtils;
import com.b1b.js.erpandroid_kf.task.WebCallback;
import com.b1b.js.erpandroid_kf.task.WebServicesTask;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import utils.common.MyJsonUtils;
import utils.common.UploadUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.net.wsdelegate.WebserviceUtils;

public class KaoQinActivity extends ToolbarHasSunmiActivity {

    private List<KaoqinInfo> data = new ArrayList<>();
    private KqAdapter adapter;
    private EditText inputDate;
    private EditText inputId;


    @Override
    public String setTitle() {
        return getResString(R.string.title_kaoqin);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kao_qin);
        ListView listView = (ListView) findViewById(R.id.kq_lv);
        Button btnSearch = (Button) findViewById(R.id.kq_serach);
        inputId = (EditText) findViewById(R.id.kq_edId);
        inputDate = (EditText) findViewById(R.id.kq_edTime);
        inputId.requestFocus();
        Button btnSaixuan = (Button) findViewById(R.id.kq_saixuan);
        String cDate = UploadUtils.getKaoqinDate();
        inputDate.setText(cDate);
        inputId.setText(loginID);
        if (CheckUtils.isAdmin()) {
            inputId.setVisibility(View.VISIBLE);
        } else {
            inputId.setVisibility(View.GONE);
        }
        adapter = new KqAdapter(data, KaoQinActivity.this, R.layout.kaoqin_lvitems);
        listView.setAdapter(adapter);
        btnSaixuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = data.size() - 1; i >= 0; i--) {
                    if (data.get(i).getState().equals("正常")) {
                        data.remove(i);
                    }
                }
                if (data.size() == 0) {
                    showMsgToast("当前无迟到早退记录");
                }
                adapter.notifyDataSetChanged();
            }
        });
        //搜索
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = inputId.getText().toString().trim();
                String date = inputDate.getText().toString().trim();
                if (date.equals("") || id.equals("")) {
                    showMsgToast( "请输入完整查询条件");
                } else {
                    initData(new String[]{date, id});
                }
            }
        });
        initData(new String[]{cDate, loginID});
    }

    private void initData(String[] arr) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("month", arr[0]);
        map.put("uid", arr[1]);
        map.put("checkWord", "");
        new WebServicesTask<>(new WebCallback<String>() {
            @Override
            public void errorCallback(Throwable e) {
                showMsgToast( "网络较差");
            }

            @Override
            public void okCallback(String obj) {
                try {
                    if (obj == null) {
                        return;
                    }
                    List<KaoqinInfo> kqList = MyJsonUtils.getKaoQinList(obj);
                    SoftKeyboardUtils.closeInputMethod(inputId, KaoQinActivity.this);
                    data.clear();
                    data.addAll(kqList);
                    adapter.notifyDataSetChanged();
                    showMsgToast( "查询到" + kqList.size() + "条考勤记录");
                } catch (JSONException e) {
                    showMsgToast( "查询条件有误");
                    e.printStackTrace();
                }
            }

            @Override
            public void otherCallback(Object obj) {

            }
        }, map).execute("GetMyKaoQinInfoJson", WebserviceUtils.MyBasicServer);
    }

}
