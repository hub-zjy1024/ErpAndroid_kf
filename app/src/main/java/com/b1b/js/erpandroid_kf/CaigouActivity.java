package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.entity.Caigoudan;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.task.CheckUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.MartService;

public class CaigouActivity extends ToolbarHasSunmiActivity implements NoLeakHandler.NoLeakCallback {

    private ListView lv;
    private EditText edPid;
    private EditText edPartNo;
    private ArrayList<Caigoudan> caigoudans;
    private ArrayAdapter<Caigoudan> caigouAdapter;
    final Context packageContext = this;
    private Handler mHandler = new NoLeakHandler(this);
    private static final int ERROR_CODE = 3;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                int counts = caigoudans.size();
                if (counts != 0) {
                    caigouAdapter.notifyDataSetChanged();
                    showMsgToast( "查询到" + counts + "条数据");
                    SoftKeyboardUtils.closeInputMethod(edPartNo, packageContext);
                }
                break;
            case 1:
                showMsgToast( "当前网络状态不佳");
                break;
            case 2:
                showMsgToast( "条件有误，请重新输入");
                break;
            case ERROR_CODE:
                String errMsg = "";
                if (msg.obj != null) {
                    errMsg = msg.obj.toString();
                }
                showMsgToast( errMsg);
                break;
        }
    }

    void sendErrorMsg(String errMsg) {
        mHandler.obtainMessage(ERROR_CODE, errMsg).sendToTarget();
    }

    @Override
    public String setTitle() {
        return getResString(R.string.title_cai_gou);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigoudan_take_pic);
        lv = getViewInContent(R.id.activity_caigoudan_take_pic_lv);
        edPid = getViewInContent(R.id.activity_caigoudan_take_pic_ed_pid);
        edPartNo = getViewInContent(R.id.activity_caigoudan_take_pic_ed_partno);
        Button btnSearch =getViewInContent(R.id.activity_caigoudan_take_pic_btn_search);
        Button btnSaoma = getViewInContent(R.id.activity_caigoudan_take_pic_btn_saoma);
        btnSaoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > caigoudans.size() - 1) {
                    return;
                }
                final Caigoudan item = (Caigoudan) parent.getItemAtPosition(position);

                Intent temp = new Intent(packageContext, com.b1b.js.erpandroid_kf.CaigouDetailActivity.class);
                temp.putExtra("corpID", item.getCorpID());
                temp.putExtra("providerID", item.getProviderID());
                temp.putExtra(IntentKeys.key_pid, item.getPid());
                temp.putExtra("date", item.getCreatedDate());
                startActivity(temp);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caigoudans.clear();
                SoftKeyboardUtils.closeInputMethod(edPid, packageContext);
                final String partNo = edPartNo.getText().toString();
                final String pid = edPid.getText().toString();
                if (!CheckUtils.checkUID(packageContext, "当前登录人为空，请重新登录")) {
                    return;
                }
                getData(partNo, pid);
            }
        });
        caigoudans = new ArrayList<>();
        caigouAdapter = new ArrayAdapter<Caigoudan>(this, R.layout.zjy_spinner_simple_item, R.id
                .spinner_item_tv, caigoudans);
        lv.setAdapter(caigouAdapter);
    }


    @Override
    public void resultBack(String result) {
        final String pid = result;
        edPid.setText(pid);
        caigoudans.clear();
        final String partNo = edPartNo.getText().toString();
        if (!CheckUtils.checkUID(packageContext, "当前登录人为空，请重新登录")) {
            return;
        }
        getData(partNo, pid);
    }


    private void getData(final String partNo, final String pid) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String res = getCaigoudanByPidAndPartNo("", Integer.parseInt(loginID), partNo, pid);
                    Log.e("zjy", "CaigouActivity->run(): json==" + res);
                    JSONObject object = new JSONObject(res);
                    JSONArray array = object.getJSONArray("表");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String pid = obj.getString("PID");
                        String createdDate = obj.getString("制单日期");
                        String state = obj.getString("单据状态");
                        String ywName = obj.getString("业务员");
                        String caigouName = obj.getString("采购员");
                        String partNo1 = obj.getString("型号");
                        String inPrice = obj.getString("进价");
                        String counts = obj.getString("数量");

                        String salePrice = obj.getString("售价");
                        String note = obj.getString("备注");
                        String clientID = obj.getString("客户编码");
                        String provder = obj.getString("供应商");
                        String askPriceMan = obj.getString("询价员");
                        String caigouPlace = obj.getString("采购地");
                        boolean isForeignClient = obj.getBoolean("IsForeignClient");
                        String corpID = obj.getString("InvoiceCorp");
                        String providerID = obj.getString("ProviderID");
                        Caigoudan caigoudan = new Caigoudan(state, pid, createdDate, ywName, caigouName,
                                partNo1);
                        caigoudan.setInPrice(inPrice);
                        caigoudan.setSalePrice(salePrice);
                        caigoudan.setNote(note);
                        caigoudan.setClientID(clientID);
                        caigoudan.setProvider(provder);
                        caigoudan.setAskPriceMan(askPriceMan);
                        caigoudan.setCaigouPlace(caigouPlace);
                        caigoudan.setForeignClient(isForeignClient);
                        caigoudan.setCounts(counts);
                        caigoudan.setProviderID(providerID);
                        caigoudan.setCorpID(corpID);
                        caigoudans.add(caigoudan);
                    }
                    mHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    if (e instanceof ConnectException) {
                        sendErrorMsg("网络连接失败，请检查wifi连接是否正常");
                    }else{
                        sendErrorMsg("其他网络错误," + e.getMessage());
                    }
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    sendErrorMsg("接口调用失败," + e.getMessage());
                } catch (JSONException e) {
                    sendErrorMsg("查询结果为空," + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //采购单图片地址
    //    172.16.6.22
    //    用户名：mingming
    //    密码：ryDl42QF
    public String getCaigoudanByPidAndPartNo(String checkWord, int buyerId, String partNo, String pid)
            throws IOException,
            XmlPullParserException {
        return MartService.GetBillByPartNoAndPid(checkWord, buyerId, pid, partNo);
    }

    @Override
    public void getCameraScanResult(String result) {
        final String pid = result;
        edPid.setText(pid);
        caigoudans.clear();
        final String partNo = edPartNo.getText().toString();
        if (!CheckUtils.checkUID(packageContext, "当前登录人为空，请重新登录")) {
            return;
        }
        getData("", pid);
    }
}
