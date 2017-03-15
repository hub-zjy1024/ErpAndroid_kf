package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.adapter.BijiadanAdapter;
import com.b1b.js.erpandroid_kf.entity.BijiadanInfo;
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

public class BijiaActivity extends AppCompatActivity {

    private ListView lv;
    private EditText edPartNo;
    private Button btnSearch;
    private List<BijiadanInfo> list;
    private BijiadanAdapter lvAdapter;
    private final int RES_SUCCESS = 0;
    private final int RES_ERROR =1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RES_SUCCESS:
                    lvAdapter.notifyDataSetChanged();
                    MyToast.showToast(BijiaActivity.this, "获取到" + list.size() + "条数据");
                    break;
                case RES_ERROR:
                    MyToast.showToast(BijiaActivity.this, "查询出现错误，请检查网络或更改条件");
                    break;
                case 2:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bijia);
        lv = (ListView) findViewById(R.id.bijia_lv);
        edPartNo = (EditText) findViewById(R.id.activity_bijia_partno);
        btnSearch = (Button) findViewById(R.id.activity_bijia_search);
        list = new ArrayList<>();
        lvAdapter = new BijiadanAdapter(list, BijiaActivity.this);
        lv.setAdapter(lvAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BijiaActivity.this, BijiaDetailAcitiviy.class);
                BijiadanInfo info = list.get(position);
                if (info != null) {
                    intent.putExtra("pid", info.getPid());
                    startActivity(intent);
                }

            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                lvAdapter.notifyDataSetChanged();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            getBijiaList("", edPartNo.getText().toString());
                            mHandler.sendEmptyMessage(RES_SUCCESS);
                            //                            getBijiaDetail("", "908353");
                        } catch (IOException e) {
                            mHandler.sendEmptyMessage(RES_ERROR);
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            mHandler.sendEmptyMessage(RES_ERROR);
                            e.printStackTrace();
                        } catch (JSONException e) {
                            mHandler.sendEmptyMessage(RES_ERROR);
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        list.clear();
        lvAdapter.notifyDataSetChanged();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    getBijiaList("", "");
                    mHandler.sendEmptyMessage(RES_SUCCESS);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(RES_ERROR);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    mHandler.sendEmptyMessage(RES_ERROR);
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(RES_ERROR);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //    checkWord"  type="xs:string" />
    //       partNo"  type="xs:string" />
    //                GetBiJiaList
    public void getBijiaList(String checkWord, String partNo) throws IOException, XmlPullParserException, JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(map, "GetBiJiaList");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.MartService);
        String json = response.toString().substring(7);
        JSONObject object = new JSONObject(json);
        JSONArray res = object.getJSONArray("RES");
        for (int i = 0; i < res.length(); i++) {
            JSONObject tempObj = res.getJSONObject(i);
            String pid = tempObj.getString("单据号");
            String createDate = tempObj.getString("制单日期");
            String partNo1 = tempObj.getString("型号");
            String counts = tempObj.getString("数量");
            String fengzhuang = tempObj.getString("封装");
            String pihao = tempObj.getString("批号");
            String description = tempObj.getString("描述");
            String factory = tempObj.getString("厂家");
            String startTime = tempObj.getString("比价开始时间");
            String kpCompany = tempObj.getString("开票公司");
            String mark = tempObj.getString("备注");
            BijiadanInfo info = new BijiadanInfo(pid, createDate, partNo1, counts, fengzhuang, pihao, factory, description, startTime, kpCompany, mark);
            list.add(info);
        }
    }
}
