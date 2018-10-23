package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoWithScanActivity;
import com.b1b.js.erpandroid_kf.adapter.YanhuoAdapter;
import com.b1b.js.erpandroid_kf.entity.YanhuoInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.framwork.SoftKeyboardUtils;
import utils.net.wsdelegate.MartService;

public class CaigouYanhuoActivity extends SavedLoginInfoWithScanActivity {
    private Handler mHandler = new Handler();
    private ListView lv;
    private EditText edpid;
    private YanhuoAdapter mAdapter;
    private List<YanhuoInfo> yanhuoInfos;
    private ProgressDialog pd;
    private EditText edpartno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigou_yanhuo);
        edpid = (EditText) findViewById(R.id.activity_caigou_yanhuo_edpid);
        edpid.requestFocus();
         edpartno = (EditText) findViewById(R.id
                .activity_caigou_yanhuo_edpartno);
        lv = (ListView) findViewById(R.id.activity_caigou_yanhuo_lv);
        yanhuoInfos = new ArrayList<>();
        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在搜索");
        mAdapter = new YanhuoAdapter(yanhuoInfos, CaigouYanhuoActivity.this, R.layout
                .activity_caigouyanhuo_item);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CaigouYanhuoActivity.this,
                        YanhuoCheckActivity.class);
                if (yanhuoInfos.size() > position) {
                    YanhuoInfo info = yanhuoInfos.get(position);
                    intent.putExtra("pid", info.getPid());
                    startActivity(intent);
                }
            }
        });
        Button btnSearch = (Button) findViewById(R.id.activity_caigou_yanhuo_btn_search);
        Button btnScan = (Button) findViewById(R.id.activity_caigou_yanhuo_btn_scan);
        btnSearch.setFocusable(true);
        btnSearch.requestFocus();
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pid = edpid.getText().toString();
                String partno = edpartno.getText().toString();
                SoftKeyboardUtils.closeInputMethod(edpid, CaigouYanhuoActivity.this);
                if (yanhuoInfos.size() > 0) {
                    yanhuoInfos.clear();
                    mAdapter.notifyDataSetChanged();
                }
                getDate(pid, partno);
            }
        });
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }

    public void getDate(final String pid, final String partno) {
        pd.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    String result = getResponse(partno, pid);
//                    {
//                        "PID":"830257", "采购地":"深圳市场", "制单日期":"2017/8/25 9:55:15", "公司":"美商利华分公司", "部门":
//                        "北京美商利华国际", "业务员":"谭晓燕", "单据状态":"等待验货", "收款":"现款现货", "客户开票":"普通发票", "供应商开票":
//                        "普通发票", "供应商":"深圳市同亨微科技有限公司", "采购员":"郭峰建", "询价员":"郭峰建"
//                    }
                    JSONObject object = new JSONObject(result);
                    JSONArray jArray = object.getJSONArray("表");
                    for(int i=0;i<jArray.length();i++) {
                        JSONObject tObj = jArray.getJSONObject(i);
                        String pid = tObj.getString("PID");
                        String caigouPlace = tObj.getString("采购地");
                        String pidDate = tObj.getString("制单日期");
                        String company = tObj.getString("公司");
                        String deptName = tObj.getString("部门");
                        String saleMan = tObj.getString("业务员");
                        String pidState= tObj.getString("单据状态");
                        String payType = tObj.getString("收款");
                        String userFapiao = tObj.getString("客户开票");
                        String providerFapiao = tObj.getString("供应商开票");
                        String providerName = tObj.getString("供应商");
                        String caigouMan = tObj.getString("采购员");
                        String askPriceBy = tObj.getString("询价员");
                        YanhuoInfo yhInfo = new YanhuoInfo(pid, caigouPlace, pidDate,
                                company, deptName, saleMan, pidState, payType,
                                userFapiao, providerFapiao, providerName, caigouMan,
                                askPriceBy);
                        yanhuoInfos.add(yhInfo);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                            pd.cancel();
                        }
                    });
                } catch (IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMsgToast( "连接服务器超时，请重试");
                            pd.cancel();
                        }
                    });
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMsgToast( "查询条件有误");
                            pd.cancel();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    public void getCameraScanResult(String result, int code) {
        super.getCameraScanResult(result, code);
        String pid = result;
        edpid.setText(pid);
        if (yanhuoInfos.size() > 0) {
            yanhuoInfos.clear();
            mAdapter.notifyDataSetChanged();
        }
        getDate(pid, "");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (yanhuoInfos.size() > 0) {
            yanhuoInfos.clear();
            mAdapter.notifyDataSetChanged();
        }
        String pid = edpid.getText().toString();
        String partno = edpartno.getText().toString();
        getDate(pid, partno);
    }

    public String getResponse(String partno, String pid) throws IOException,
            XmlPullParserException {
        return MartService.GetSSCGInfoByDDYH(partno, pid);
    }
}
