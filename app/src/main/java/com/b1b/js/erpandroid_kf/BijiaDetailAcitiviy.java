package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.BijiaDetailAdapter;
import com.b1b.js.erpandroid_kf.entity.BijiadetailInfo;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BijiaDetailAcitiviy extends AppCompatActivity {

    private ListView lv;
    private TextView emptyView;
    private List<BijiadetailInfo> data;
    private BijiaDetailAdapter adapter;
    private final int REQ_SUCCESS = 0;
    private final int REQ_ERROR = 1;
    private String pid;
    private Button btnAdd;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REQ_SUCCESS:
                    adapter.notifyDataSetChanged();
                    if (data.size() > 0) {
                        emptyView.setVisibility(View.INVISIBLE);
                    } else {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    break;
                case REQ_ERROR:
                    MyToast.showToast(BijiaDetailAcitiviy.this, "当前网络连接出错");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bijia_detail_acitiviy);
        lv = (ListView) findViewById(R.id.bijiadetail_lv);
        emptyView = (TextView) findViewById(R.id.bijiadetail_empty);
        btnAdd = (Button) findViewById(R.id.bijiadetail_btn_add);

        data = new ArrayList<>();
        adapter = new BijiaDetailAdapter(data, BijiaDetailAcitiviy.this);
        lv.setAdapter(adapter);
        final Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(BijiaDetailAcitiviy.this, AddBijiaActivity.class);
                intent1.putExtra("pid", pid);
                startActivity(intent1);
            }
        });
    }

    private void getBijiaDetail(final String pid) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    getBijiaDetail("", pid);
                    mHandler.sendEmptyMessage(REQ_SUCCESS);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(REQ_ERROR);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    mHandler.sendEmptyMessage(REQ_ERROR);
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(REQ_ERROR);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        data.clear();
        adapter.notifyDataSetChanged();
        getBijiaDetail(pid);

    }

    //  name="GetBiJiaDetail"
    //   checkWord"  type="xs:string"
    //   mainID" type="xs:int"
    // id 908353
    public void getBijiaDetail(String checkWord, String mainID) throws IOException, XmlPullParserException, JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("mainID", Integer.valueOf(mainID));
        SoapObject request = WebserviceUtils.getRequest(map, "GetBiJiaDetail");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.MartService);
        String json = response.toString().substring(7);
        JSONObject root = new JSONObject(json);
        JSONArray array = root.getJSONArray("Details");
        //        {
        //            "时间": "2017/2/22 15:41:10",
        //                "用户ID": "1963",
        //                "用户名": "张超阵",
        //                "备注": "开的发票不是原型号　不行的"
        //        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject tempObj = array.getJSONObject(i);
            String date = tempObj.getString("时间");
            String uid = tempObj.getString("用户ID");
            String userName = tempObj.getString("用户名");
            String mark = tempObj.getString("备注");
            BijiadetailInfo dInfo = new BijiadetailInfo(date, uid, userName, mark);
            data.add(dInfo);
        }
        Log.e("zjy", "BijiaDetailAcitiviy.java->getBijiaDetail(): ==" + response.toString());
    }
}
