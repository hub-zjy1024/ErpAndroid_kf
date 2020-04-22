package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuAdapter;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.service.PankuPicChooser;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.common.UploadUtils;
import utils.framwork.ItemClickWrapper;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;

public class PankuActivity extends ToolbarHasSunmiActivity implements NoLeakHandler.NoLeakCallback {

    private EditText edID;
    private EditText edPartNo;
    private Button btnSearch;
    private List<PankuInfo> pkData;
    private PankuAdapter mAdapter;
    private ListView lv;
    private final int GET_DATA = 0;
    private final int GET_DATA_New = 8;
    private final int GET_FAIL = 1;
    private final int GET_NUll = 2;
    private final int INSERT_SUCCESS = 3;
    private final int INSERT_FAIL = 4;
    private final int CHANGEFLAG_SUCCESS = 5;
    private final int CHANGEFLAG_ERROR = 6;
    private final int GET_PANKUINFO = 7;
    private final int GET_PANKUINFO_ERROR = 2;
    private ProgressDialog pdDialog;
    private AlertDialog editDialog;
    private Button btnPk;
    private Button btnReset;
    private PankuInfo currentInfo;
    private EditText eText;
    private View nowViwe;
    SharedPreferences pfInfo;
    AlertDialog choiceMethodDialog;
    PankuPicChooser mPicChooser;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_DATA_New:
                Object mobj = msg.obj;
                if (mobj != null) {
                    List<PankuInfo> pankuList = (List<PankuInfo>) mobj;
                    pkData.addAll(pankuList);
                    mHandler.sendEmptyMessage(GET_DATA);
                }
                break;
            case GET_DATA:
                showMsgToast("获取到" + pkData.size() + "条数据");
                mAdapter.notifyDataSetChanged();
                break;
            case GET_FAIL:
                showMsgToast("网络质量较差，请检查网络");
                mAdapter.notifyDataSetChanged();
                break;
            case GET_NUll:
                showMsgToast("条件有误");
                mAdapter.notifyDataSetChanged();
                break;
            case INSERT_SUCCESS:
                showMsgToast("盘库成功");
                showHide(btnPk, btnReset, false);
                pkData.clear();
                mAdapter.notifyDataSetChanged();
                final String id = msg.obj.toString();
                edID.setText(id);
                if (editDialog != null) {
                    editDialog.cancel();
                }
                asyncGetData(id, "");
                break;
            case INSERT_FAIL:
                showMsgToast("插入盘库信息失败");
                mAdapter.notifyDataSetChanged();
                break;
            case CHANGEFLAG_SUCCESS:
                showMsgToast("解锁成功,可进行盘库");
                final String did = msg.obj.toString();
                showHide(btnPk, btnReset, true);
                pkData.clear();
                mAdapter.notifyDataSetChanged();
//                if (editDialog != null) {
//                    editDialog.cancel();
//                }
//                asyncGetData(did, "");
                break;
            case CHANGEFLAG_ERROR:
                String tempMsg = "解锁失败";
                if (msg.obj != null) {
                    String errMsg = (String) msg.obj;
                    tempMsg += "," + errMsg;
                }
                showMsgToast(tempMsg);
                break;
            case GET_PANKUINFO:
                if (msg.arg1 == GET_PANKUINFO_ERROR) {
                    String errmsg = (String) msg.obj;
                    showMsgToast("获取实盘信息失败," + errmsg);
                }else {
                    showEditDialog((PankuInfo) msg.obj);
                }
                break;
        }
        if (pdDialog != null && pdDialog.isShowing()) {
            pdDialog.cancel();
        }
    }

    private Handler mHandler = new NoLeakHandler(this);
    private int reqScan = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panku);
        edID = (EditText) findViewById(R.id.panku_id);
        edPartNo = (EditText) findViewById(R.id.panku_partno);
        btnSearch = (Button) findViewById(R.id.panku_search);
        Button btnScan = (Button) findViewById(R.id.panku_scan);
        lv = (ListView) findViewById(R.id.panku_lv);
        pkData = new ArrayList<>();
        pfInfo = getSharedPreferences(SettingActivity.PREF_USERINFO, 0);
        View empty = findViewById(R.id.panku_lv_emptyview);
        //        mAdapter = new PankuAdapter(pkData, PankuActivity.this);
        mAdapter = new PankuAdapter(mContext, pkData, R.layout.item_lv_pk
        );
        // lv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mAdapter.addListener2(new PankuAdapter.ItemListener2<PankuInfo>() {
            @Override
            public void itemClick(View itemView, View nowView, PankuInfo mInfo) {
                int id = nowView.getId();
                switch (id) {
                    case R.id.item_pk_btn_rprint:
                        if (mInfo == null) {
                            return;
                        }
                        View shareView = itemView.findViewById(R.id.item_lv_pk_tv_detailId);
                        mPicChooser.openPrintPageWithShared(mInfo.getDetailId(),shareView );
                        break;
                    case R.id.item_pk_btn_takepic:
                        if (mInfo == null) {
                            return;
                        }
                        mPicChooser.openTakePic(mInfo.getDetailId());
                        break;
                }
            }
        });
        pdDialog = new ProgressDialog(mContext);
        pdDialog.setMessage("正在查询");
        pdDialog.setCancelable(false);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(empty);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                currentInfo = item;
                if (item == null) {
                    return;
                }
                openDetail(item);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                TextView tvMore = (TextView) view.findViewById(R.id.chukudan_items_tvMore);
                PankuAdapter.CheckClass checkClass = new PankuAdapter.CheckClass(item, "1");
                tvMore.setTag(checkClass);
                tvMore.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                tv.setText(item.toExtraString());
                return true;
            }
        });
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.panku_scan:
                        startScanActivity(REQ_CODE);
                        break;
                    case R.id.panku_search:
                        getData();
                        break;
                }
            }
        };

        btnSearch.setOnClickListener(clickListener);
        btnScan.setOnClickListener(clickListener);
        mPicChooser = new PankuPicChooser(mContext);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PankuDetailActivity.ResultCode) {
            if (currentInfo != null) {
                edPartNo.setText(currentInfo.getPartNo());
                edID.setText(currentInfo.getDetailId());
                getData();
            }
            Log.e("zjy", "PankuActivity->onActivityResult(): backFromPanKu==");
        } else {
            Log.e("zjy", "PankuActivity->onActivityResult(): backFromPanKu==" + resultCode);
        }
    }

    public void openDetail(PankuInfo mInfo) {
        Intent mIntent = new Intent(this, PankuDetailActivity.class);
        mIntent.putExtra(PankuDetailActivity.extra_DATA, com.alibaba.fastjson.JSONObject.toJSONString(mInfo));
        startActivityForResult(mIntent, PankuDetailActivity.ResultCode);
    }

    @Override
    public String setTitle() {
        return "盘库";
    }


    private void getData() {
        SoftKeyboardUtils.closeInputMethod(edID, mContext);
        final String id = edID.getText().toString().trim();
        final String partno = edPartNo.getText().toString().trim();
        pkData.clear();
        mAdapter.notifyDataSetChanged();
        pdDialog.show();
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                try {
                    List<PankuInfo> pankuList = getPankuList(id, partno);
                    mHandler.obtainMessage(GET_DATA_New, pankuList).sendToTarget();
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(GET_FAIL);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    mHandler.sendEmptyMessage(GET_FAIL);
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(GET_NUll);
                    e.printStackTrace();
                }
            }
        };
        String tag = "panku_getdata";
        TaskManager.getInstance().executeLimitedTask(tag, mRun);
    }

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        edID.setText(result);
        try {
            Integer.parseInt(result);
            getData();
        } catch (NumberFormatException e) {
            showMsgToast(getString(R.string.error_numberformate));
            e.printStackTrace();
        }
    }


    public <T extends View> T getViewIn(View mView, int id) {
        T retView;
        retView = (T) mView.findViewById(id);
        return retView;
    }
    @Override
    public void getCameraScanResult(String result, int code) {
        Log.e("zjy", "PankuActivity->getCameraScanResult(): ==" + result + "\tcode=" + code);
        super.getCameraScanResult(result, code);
        if (code == reqScan) {
            final PankuInfo info = currentInfo;
            final TextView detailId = (TextView)getViewIn(nowViwe,R.id.panku_dialog_id );
            final EditText dialogPartno = (EditText)getViewIn(nowViwe,R.id.panku_dialog_partno);
            final EditText dialogCounts = (EditText)getViewIn(nowViwe,R.id.panku_dialog_counts);
            final EditText dialogFactory = (EditText)getViewIn(nowViwe,R.id.panku_dialog_factory);
            final EditText dialogDescription = (EditText)getViewIn(nowViwe,R.id.panku_dialog_description);
            final EditText dialogFengzhuang = (EditText)getViewIn(nowViwe,R.id.panku_dialog_fengzhuang);
            final EditText dialogPihao = (EditText)getViewIn(nowViwe,R.id.panku_dialog_pihao);
            final EditText dialogPlace = (EditText)getViewIn(nowViwe,R.id.panku_dialog_place);
            final EditText dialogBz = (EditText)getViewIn(nowViwe,R.id.panku_dialog_minbz);
            final EditText dialogMark = (EditText)getViewIn(nowViwe,R.id.panku_dialog_mark);
            final Button dialogPanku = (Button)getViewIn(nowViwe,R.id.panku_dialog_panku);
            btnPk = dialogPanku;
            final Button dialogReset = (Button)getViewIn(nowViwe,R.id.panku_dialog_reset);
            btnReset = dialogReset;
            final Button dialogCancel = (Button)getViewIn(nowViwe,R.id.panku_dialog_cancel);
            final String pkPartNo = dialogPartno.getText().toString().trim();
            final String PKQuantity = dialogCounts.getText().toString().trim();
            final String PKmfc = dialogFactory.getText().toString().trim();
            final String PKDescription = dialogDescription.getText().toString().trim();
            final String PKPack = dialogFengzhuang.getText().toString().trim();
            final String PKBatchNo = dialogPihao.getText().toString().trim();
            final String minpack = dialogBz.getText().toString().trim();
            final String Note = dialogMark.getText().toString().trim();
            final String PKPlace =result;
            //startPk(pkPartNo, info, minpack, PKQuantity, PKmfc, PKDescription, PKPack, pkPartNo, Note,
            // PKPlace);
            Runnable panKuRunnable = new Runnable() {
                @Override
                public void run() {
                    int MinPack = 0;
                    if (!minpack.equals("")) {
                        MinPack = Integer.valueOf(minpack);
                    }
                    int OperID = Integer.valueOf(loginID);
                    String OperName = pfInfo.getString("oprName", "");
                    String DiskID = getDiskId(OperID);
                    try {
                        int result = insertPankuInfo(Integer.parseInt(info.getDetailId()), info.getPartNo()
                                , Integer
                                        .parseInt(info.getLeftCounts()), pkPartNo, PKQuantity, PKmfc,
                                PKDescription, PKPack,
                                PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);
                        if (result == 0) {
                            mHandler.sendEmptyMessage(INSERT_FAIL);
                        } else if (result == 1) {
                            final String id = info.getDetailId();
                                                            Message message = mHandler.obtainMessage
                             (INSERT_SUCCESS);
                                                            message.obj = id;
                                                            message.sendToTarget();

                        }
                    } catch (IOException e) {
                        mHandler.sendEmptyMessage(INSERT_FAIL);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
            };
            TaskManager.getInstance().execute(panKuRunnable);
        } else if (code == REQ_CODE) {
            edID.setText(result);
            try {
                Integer.parseInt(result);
                getData();
            } catch (NumberFormatException e) {
                showMsgToast("扫码结果有误");
                e.printStackTrace();
            }
        }
    }


    public void asyncGetData(final String dtId, final String partno) {
        Runnable getResultRun = new Runnable() {
            @Override
            public void run() {
                try {
                    List<PankuInfo> pankuList = getPankuList(dtId, partno);
                    //                            pkData.addAll(pankuList);
                    //                            mHandler.sendEmptyMessage(GET_DATA);
                    mHandler.obtainMessage(GET_DATA_New, pankuList).sendToTarget();
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(GET_FAIL);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(GET_NUll);
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(getResultRun);
    }

    //    string GetDataListForPanKu(string id, string part);
    //
    public List<PankuInfo> getPankuList(String detailId, String partno) throws IOException,
            XmlPullParserException,
            JSONException {
        String soapRes = ChuKuServer.GetDataListForPanKu(detailId, partno);
        List<PankuInfo> tempList = new ArrayList<>();
        JSONObject jObj = new JSONObject(soapRes);
        JSONArray jsonArray = jObj.getJSONArray("表");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempJobj = jsonArray.getJSONObject(i);
            String pid = tempJobj.getString("单据号");
            String mxId = tempJobj.getString("明细ID");
            String sPartno = tempJobj.getString("型号");
            String leftCounts = tempJobj.getString("剩余数量");
            String factory = tempJobj.getString("厂家");
            String description = tempJobj.getString("描述");
            String fengzhuang = tempJobj.getString("封装");
            String pihao = tempJobj.getString("批号");
            String placeId = tempJobj.getString("位置");
            String rkDate = tempJobj.getString("入库日期");
            String storageName = tempJobj.getString("仓库");
            String pkFlag = tempJobj.getString("PanKuFlag");
            PankuInfo pkInfo = new PankuInfo(pid, mxId, sPartno, leftCounts, factory, description,
                    fengzhuang, pihao, placeId,
                    rkDate, storageName, pkFlag);
            tempList.add(pkInfo);
        }
        return tempList;
    }

    public void openPicView(String viewId) {
        Intent mIntent = new Intent(this, ViewPicByPidActivity.class);
        mIntent.putExtra(IntentKeys.key_pid, viewId);
        startActivity(mIntent);
    }
    void showEditDialog(final PankuInfo info) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.panku_dialog, null);
        nowViwe = v;
        final TextView detailId = (TextView) getViewIn(v,R.id.panku_dialog_id);
        final EditText dialogPartno = (EditText) getViewIn(v,R.id.panku_dialog_partno);
        final EditText dialogCounts = (EditText) getViewIn(v,R.id.panku_dialog_counts);
        final EditText dialogFactory = (EditText) getViewIn(v,R.id.panku_dialog_factory);
        final EditText dialogDescription = (EditText) getViewIn(v,R.id.panku_dialog_description);
        final EditText dialogFengzhuang = (EditText) getViewIn(v,R.id.panku_dialog_fengzhuang);
        final EditText dialogPihao = (EditText) getViewIn(v,R.id.panku_dialog_pihao);
        final EditText dialogPlace = (EditText) getViewIn(v,R.id.panku_dialog_place);
        final EditText dialogBz = (EditText) getViewIn(v,R.id.panku_dialog_minbz);
        final EditText dialogMark = (EditText) getViewIn(v,R.id.panku_dialog_mark);
        final Button dialogPanku = (Button) getViewIn(v,R.id.panku_dialog_panku);
        final Button dialogScanPlace = (Button) getViewIn(v,R.id.panku_dialog_scan);
        final Button btnCaidan = getViewIn(v,R.id.panku_dialog_chaidan);
        final Button btnViewPic = getViewIn(v, R.id.panku_dialog_viewpic);
        final ItemClickWrapper itemListener = new ItemClickWrapper<PankuInfo>(info) {
            @Override
            public void allClick(View v, final PankuInfo data) {
                switch (v.getId()) {
                    case R.id.panku_dialog_viewpic:
                        openPicView(data.getDetailId());
                        break;
                    case R.id.panku_dialog_chaidan:
                        Intent cdIntent = new Intent(mContext, PankuChaidanActivity.class);
                        String dataJson = com.alibaba.fastjson.JSONObject.toJSONString(data);
                        //                        Log.e("zjy", "PankuActivity->allClick(): sendData==" +
                        //                        data.toStringDetail());
                        cdIntent.putExtra(PankuChaidanActivity.mIntent_Data_key, dataJson);
                        startActivity(cdIntent);
                    case R.id.panku_dialog_cancel:
                        editDialog.dismiss();
                        break;
                    case R.id.panku_dialog_reset:
                        Runnable cancelRun = new Runnable() {
                            @Override
                            public void run() {
                                String errMsg = "";
                                String tempDpid = data.getDetailId();
                                try {
                                    String res = ChuKuServer.CancelPanKuFlag(Integer.parseInt(tempDpid));
                                    if("1".equals(res)){
                                        info.setHasFlag("0");
                                        Message msg = mHandler.obtainMessage(CHANGEFLAG_SUCCESS,
                                                info.getDetailId());
                                        msg.sendToTarget();
                                        return;
                                    }else {
                                        errMsg = "返回异常,ret="+res;
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    errMsg = "明细id必须为纯数字," + tempDpid + "," + e.getMessage();
                                } catch (IOException e) {
                                    errMsg = "IO," + e.getMessage();
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                    errMsg = "接口异常xml," + e.getMessage();
                                }
                                mHandler.obtainMessage(CHANGEFLAG_ERROR, errMsg).sendToTarget();
                            }
                        };
                        TaskManager.getInstance().execute(cancelRun);
                        break;
                    case R.id.panku_dialog_scan:
                       /* if (!"0".equals(data.getHasFlag())) {
                            showMsgToast("请先解锁再修改位置");
                            return;
                        }*/
                        startScanActivity(reqScan);
                        break;
                    case R.id.panku_dialog_panku:
                        String pkPartNo = dialogPartno.getText().toString().trim();
                        String PKQuantity = dialogCounts.getText().toString().trim();
                        String PKmfc = dialogFactory.getText().toString().trim();
                        String PKDescription = dialogDescription.getText().toString().trim();
                        String PKPack = dialogFengzhuang.getText().toString().trim();
                        String PKBatchNo = dialogPihao.getText().toString().trim();
                        String minpack = dialogBz.getText().toString().trim();
                        String Note = dialogMark.getText().toString().trim();
                        String PKPlace = dialogPlace.getText().toString().trim();
                        startPk(pkPartNo, info, minpack, PKQuantity, PKmfc, PKDescription, PKPack,
                                PKBatchNo, Note, PKPlace);
                        break;
                }
            }
        };
        btnViewPic.setOnClickListener(itemListener);
        btnCaidan.setOnClickListener(itemListener);
        dialogScanPlace.setOnClickListener(itemListener);
        btnPk = dialogPanku;
        final Button dialogReset = (Button) getViewIn(v,R.id.panku_dialog_reset);
        btnReset = dialogReset;
        final Button dialogCancel = (Button) getViewIn(v,R.id.panku_dialog_cancel);
        dialogReset.setOnClickListener(itemListener);
        dialogCancel.setOnClickListener(itemListener);
        if (info.getHasFlag().equals("0")) {
            showHide(dialogPanku, dialogReset, true);
        } else {
            showHide(dialogPanku, dialogReset, false);
        }
        dialogPanku.setOnClickListener(itemListener);
        detailId.setText(info.getDetailId());
        dialogPartno.setText(info.getPartNo());
        dialogCounts.setText(info.getLeftCounts());
        dialogFactory.setText(info.getFactory());
        dialogDescription.setText(info.getDescription());
        dialogFengzhuang.setText(info.getFengzhuang());
        dialogPihao.setText(info.getPihao());
        String mark = info.getMark();
        if (mark == null) {
            mark = "";
        }
        dialogMark.setText(mark);
        String minBz = info.getMinBz();
        if (info.getMinBz() == null) {
            minBz = "";
        }
        dialogBz.setText(minBz);
        dialogPlace.setText(info.getPlaceId());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (editDialog == null) {
            editDialog = builder.create();
        }
        if (editDialog != null && !editDialog.isShowing()) {
                editDialog.show();
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                int w = (int) getResources().getDimension(R.dimen.panku_dialog_root_margin_vetical);
                int h = (int) getResources().getDimension(R.dimen.panku_dialog_root_margin_horizontal);
                layoutParams.setMargins(w, h, w, h);
                editDialog.setContentView(v, layoutParams);
                editDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialogPlace.requestFocus();
//                detailId.requestFocus();
            }
    }

    void showHide(View v1, View v2, boolean flag) {
        if (flag) {
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.INVISIBLE);
        }else {
            v1.setVisibility(View.INVISIBLE );
            v2.setVisibility(View.VISIBLE);
        }
//关闭解锁功能
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
    }

    public void startPk(final String pkPartNo, final PankuInfo info, final String minpack,
                        final String PKQuantity, final String PKmfc, final String PKDescription,
                        final String PKPack, final String PKBatchNo, final String Note,
                        final String PKPlace) {
        Runnable panKuRunnable = new Runnable() {
            @Override
            public void run() {

                int MinPack = 0;
                if (!minpack.equals("")) {
                    MinPack = Integer.valueOf(minpack);
                }
                int OperID = Integer.valueOf(loginID);
                String OperName = pfInfo.getString("oprName", "");
                String tempDisk = getDiskId(OperID);
                String DiskID =tempDisk ;
                String detailId = info.getDetailId();
                try {
                    int result = insertPankuInfo(Integer.parseInt(detailId), info
                                    .getPartNo(), Integer
                                    .parseInt(info.getLeftCounts()), pkPartNo, PKQuantity, PKmfc,
                            PKDescription, PKPack,
                            PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);
                    if (result == 0) {
                        mHandler.sendEmptyMessage(INSERT_FAIL);
                    } else if (result == 1) {
                        String id = detailId;
                        Message message = mHandler.obtainMessage(INSERT_SUCCESS);
                        message.obj = id;
                        message.sendToTarget();
                    }
                } catch (NumberFormatException e) {
                    mHandler.sendEmptyMessage(INSERT_FAIL);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(INSERT_FAIL);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(INSERT_FAIL);
                }
            }
        };
        TaskManager.getInstance().execute(panKuRunnable);
    }

    private String getDiskId(int operID) {
        String tempDisk = pfInfo.getString("nowDevicesId", "");
        String nowDevId =  UploadUtils.getDeviceID(mContext);
        if (tempDisk.equals("")) {
            tempDisk = nowDevId;
            pfInfo.edit().putString("nowDevicesId", tempDisk).commit();
        } else if (!tempDisk.equals(nowDevId)) {
            tempDisk = nowDevId;
            pfInfo.edit().putString("nowDevicesId", tempDisk).commit();
            MyApp.myLogger.writeBug("use newDeviceId " + tempDisk + ",LoginId=" + operID);
        }
        return tempDisk;
    }

    /**
     * @param InstorageDetailID 明细号
     * @param OldPartNo         原始型号
     * @param OldQuantity       原始数量
     * @param PKPartNo          盘库型号
     * @param PKQuantity        盘库数量
     * @param PKmfc             厂家
     * @param PKDescription     描述
     * @param PKPack            封装
     * @param PKBatchNo         批号
     * @param MinPack           最小包装
     * @param OperID            盘库人ID
     * @param OperName          盘库人
     * @param DiskID            电脑地址
     * @param Note              盘库备注
     * @param PKPlace           盘库位置
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public int insertPankuInfo(int InstorageDetailID, String OldPartNo, int OldQuantity, String PKPartNo,
                               String PKQuantity,
                               String PKmfc, String PKDescription, String PKPack
            , String PKBatchNo, int MinPack, int OperID, String OperName, String DiskID, String Note,
                               String PKPlace) throws
            IOException, XmlPullParserException {
        String soapRes = ChuKuServer.PanKu(InstorageDetailID, OldPartNo, OldQuantity, PKPartNo, PKQuantity,
                PKmfc, PKDescription, PKPack, PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);
        int result = Integer.parseInt(soapRes);
        Log.e("zjy", "PankuActivity.java->insertPankuInfo(): res==" + result);
        return result;
    }

    //    CancelPanKuFlag
    public int cancelPk(int detailid) throws IOException, XmlPullParserException {
        String res = ChuKuServer.CancelPanKuFlag(detailid);
        return Integer.parseInt(res);
    }


    class DetailThread extends Thread {
        PankuInfo item;

        public DetailThread(PankuInfo item) {
            this.item = item;
        }

        @Override
        public void run() {
            PankuInfo info = null;
            String errMsg = "未知错误";
            try {
                String s = ChuKuServer.GetPauKuDataInfoByID(item.getHasFlag());
                String tempPid = item.getPid();
                JSONObject root = new JSONObject(s);
                JSONArray jsonArray = root.getJSONArray("表");
                if (jsonArray.length() > 0) {
                    JSONObject tempJ = jsonArray.getJSONObject(0);
                    String detailId = tempJ.getString("InstorageDetailID");
                    String PKPartNo = tempJ.getString("PKPartNo");
                    String PKQuantity = tempJ.getString("PKQuantity");
                    String PKmfc = tempJ.getString("PKmfc");
                    String PKDescription = tempJ.getString("PKDescription");
                    String PKPack = tempJ.getString("PKPack");
                    String PKBatchNo = tempJ.getString("PKBatchNo");
                    String MinPack = tempJ.getString("MinPack");
                    String Mark = tempJ.getString("Note");
                    String PKPlace = tempJ.getString("PKPlace");
                    String flag = tempJ.getString("ID");
                    info = new PankuInfo(tempPid, detailId, PKPartNo, PKQuantity, PKmfc,
                            PKDescription, PKPack, PKBatchNo,
                            PKPlace, "", "", flag);
                    info.setMinBz(MinPack);
                    info.setMark(Mark);
                    Message message = mHandler.obtainMessage(GET_PANKUINFO, info);
                    mHandler.sendMessage(message);
                    return;
                }else {
                    errMsg = "返回数据为空";
                }
            } catch (IOException e) {
                e.printStackTrace();
                errMsg = "IO," + e.getMessage();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                errMsg = "xml," + e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                errMsg = "返回json异常," + e.getMessage();
            }
            int errCode =GET_PANKUINFO_ERROR;
            Message message = mHandler.obtainMessage(GET_PANKUINFO, errCode, errCode
                    , errMsg);
            mHandler.sendMessage(message);
        }

    }
}
