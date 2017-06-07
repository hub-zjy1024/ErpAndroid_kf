package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.adapter.KqAdapter;
import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;
import com.b1b.js.erpandroid_kf.task.MyAsyncTask;
import com.b1b.js.erpandroid_kf.task.TaskCallback;
import com.b1b.js.erpandroid_kf.utils.MyJsonUtils;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.SoftKeyboardUtils;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KaoQinActivity extends AppCompatActivity {

    private List<KaoqinInfo> data = new ArrayList<>();
    private KqAdapter adapter;
    private EditText inputDate;
    private EditText inputId;


    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        return sdf.format(new Date());
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
        inputDate.setText(getCurrentDate());
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
                    MyToast.showToast(getApplicationContext(), "当前无迟到早退记录");
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
                    MyToast.showToast(KaoQinActivity.this, "请输入完整查询条件");
                } else {
                        initData(new String[]{date, id});
                }
            }
        });
        initData(new String[]{getCurrentDate(), MyApp.id});

    }

    private void initData(String[] arr) {
        new MyAsyncTask(new TaskCallback() {
            @Override
            public void callback(String list) {
                if (list != null) {
                    List<KaoqinInfo> kqList = null;
                    try {
                        kqList = MyJsonUtils.getKaoQinList(list);
                        SoftKeyboardUtils.closeInputMethod(inputId, KaoQinActivity.this);
                        data.clear();
                        data.addAll(kqList);
                        adapter.notifyDataSetChanged();
                        MyToast.showToast(KaoQinActivity.this, "查询到" + kqList.size() + "条考勤记录");
                    } catch (JSONException e) {
                        MyToast.showToast(KaoQinActivity.this, "查询条件有误");
                        e.printStackTrace();
                    }
                } else {
                    MyToast.showToast(KaoQinActivity.this, "连接服务器失败，请检查网络");
                }
            }
        }).execute(arr);
    }

}
