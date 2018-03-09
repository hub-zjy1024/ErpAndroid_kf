package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dev.ScanBaseActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuAdapter;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.CaptureActivity;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.WebserviceUtils;

public class PankuActivity extends ScanBaseActivity {

    private EditText edID;
    private EditText edPartNo;
    private Button btnSearch;
    private List<PankuInfo> pkData;
    private PankuAdapter mAdapter;
    private ListView lv;
    private final int GET_DATA = 0;
    private final int GET_FAIL = 1;
    private final int GET_NUll = 2;
    private final int INSERT_SUCCESS = 3;
    private final int INSERT_FAIL = 4;
    private final int CHANGEFLAG_SUCCESS = 5;
    private final int CHANGEFLAG_ERROR = 6;
    private final int GET_PANKUINFO = 7;
    private ProgressDialog pdDialog;
    private AlertDialog editDialog;
    private Button btnPk;
    private Button btnReset;
    private PankuInfo currentInfo;
    private EditText eText;
    private View nowViwe;
    AlertDialog choiceMethodDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_DATA:
                    MyToast.showToast(PankuActivity.this, "获取到" + pkData.size() + "条数据");
                    mAdapter.notifyDataSetChanged();
                    break;
                case GET_FAIL:
                    MyToast.showToast(PankuActivity.this, "网络质量较差，请检查网络");
                    mAdapter.notifyDataSetChanged();
                    break;
                case GET_NUll:
                    MyToast.showToast(PankuActivity.this, "条件有误");
                    mAdapter.notifyDataSetChanged();
                    break;
                case INSERT_SUCCESS:
                    Toast.makeText(PankuActivity.this, "插入成功", Toast.LENGTH_SHORT).show();
                    btnPk.setVisibility(View.INVISIBLE);
                    btnReset.setVisibility(View.VISIBLE);
                    pkData.clear();
                    mAdapter.notifyDataSetChanged();
                    final String id = msg.obj.toString();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                List<PankuInfo> pankuList = getPankuList(id, "");
                                pkData.addAll(pankuList);
                                mHandler.sendEmptyMessage(GET_DATA);
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
                    }.start();
                    mAdapter.notifyDataSetChanged();
                    break;
                case INSERT_FAIL:
                    MyToast.showToast(PankuActivity.this, "插入盘库信息失败");
                    mAdapter.notifyDataSetChanged();
                    break;
                case CHANGEFLAG_SUCCESS:
                    Toast.makeText(PankuActivity.this, "解锁成功", Toast.LENGTH_SHORT).show();
                    final String did = msg.obj.toString();
                    btnPk.setVisibility(View.VISIBLE);
                    btnReset.setVisibility(View.INVISIBLE);
                    pkData.clear();
                    mAdapter.notifyDataSetChanged();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                List<PankuInfo> pankuList = getPankuList(did, "");
                                pkData.addAll(pankuList);
                                mHandler.sendEmptyMessage(GET_DATA);
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
                    }.start();

                    break;
                case CHANGEFLAG_ERROR:
                    MyToast.showToast(PankuActivity.this, "解锁失败");
                    mAdapter.notifyDataSetChanged();
                    break;
                case GET_PANKUINFO:
                    showEditDialog((PankuInfo) msg.obj);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            if (pdDialog != null && pdDialog.isShowing()) {
                pdDialog.cancel();
            }
        }
    };
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
        View empty = findViewById(R.id.panku_lv_emptyview);
        mAdapter = new PankuAdapter(pkData, PankuActivity.this);
        pdDialog = new ProgressDialog(PankuActivity.this);
        pdDialog.setMessage("正在查询");
        pdDialog.setCancelable(false);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(empty);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                Log.e("zjy", "PankuActivity->onItemClick(): flag==" + item.getHasFlag());
                currentInfo = item;
                if (item.getHasFlag().equals("0")) {
                    showEditDialog(item);
                } else {
                    new DetailThread(item).start();
                }
                Log.e("zjy", "PankuActivity.java->onItemClick(): item.detail==" + item.getDetailId());
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PankuInfo item = (PankuInfo) parent.getItemAtPosition(position);
                TextView tv = (TextView) view.findViewById(R.id.chukudan_items_tv);
                tv.setText(item.toStringDetail());
                return true;
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();

            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PankuActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_CODE);
            }
        });
    }
    private void getData() {
        SoftKeyboardUtils.closeInputMethod(edID, PankuActivity.this);
        final String id = edID.getText().toString().trim();
        final String partno = edPartNo.getText().toString().trim();
        pkData.clear();
        mAdapter.notifyDataSetChanged();
        pdDialog.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    List<PankuInfo> pankuList = getPankuList(id, partno);
                    pkData.addAll(pankuList);
                    mHandler.sendEmptyMessage(GET_DATA);
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
        }.start();
    }
    @Override
    public int getLayoutResId() {
        return R.layout.activity_panku;
    }

    @Override
    public void resultBack(String result) {
        edID.setText(result);
        try {
            Integer.parseInt(result);
            getData();
        } catch (NumberFormatException e) {
            MyToast.showToast(this, getString(R.string.error_numberformate));
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CaptureActivity.REQ_CODE) {
                String result = data.getStringExtra("result");
                edID.setText(result);
                try {
                    Integer.parseInt(result);
                    getData();
                } catch (NumberFormatException e) {
                    MyToast.showToast(this, "扫码结果有误");
                    e.printStackTrace();
                }
            } else if (requestCode == reqScan) {
                String result = data.getStringExtra("result");
                final PankuInfo info = currentInfo;
                final TextView detailId = (TextView) nowViwe.findViewById(R.id.panku_dialog_id);
                final EditText dialogPartno = (EditText) nowViwe.findViewById(R.id.panku_dialog_partno);
                final EditText dialogCounts = (EditText) nowViwe.findViewById(R.id.panku_dialog_counts);
                final EditText dialogFactory = (EditText) nowViwe.findViewById(R.id.panku_dialog_factory);
                final EditText dialogDescription = (EditText) nowViwe.findViewById(R.id.panku_dialog_description);
                final EditText dialogFengzhuang = (EditText) nowViwe.findViewById(R.id.panku_dialog_fengzhuang);
                final EditText dialogPihao = (EditText) nowViwe.findViewById(R.id.panku_dialog_pihao);
                final EditText dialogPlace = (EditText) nowViwe.findViewById(R.id.panku_dialog_place);
                final EditText dialogBz = (EditText) nowViwe.findViewById(R.id.panku_dialog_minbz);
                final EditText dialogMark = (EditText) nowViwe.findViewById(R.id.panku_dialog_mark);
                final Button dialogPanku = (Button) nowViwe.findViewById(R.id.panku_dialog_panku);
                final Button dialogTakePic = (Button) nowViwe.findViewById(R.id.panku_dialog_takepic);
                final Button dialogScanPlace = (Button) nowViwe.findViewById(R.id.panku_dialog_scan);
                dialogPlace.setText(result);
                dialogScanPlace.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(PankuActivity.this, CaptureActivity.class),reqScan);
                    }
                });
                btnPk = dialogPanku;
                final Button dialogReset = (Button) nowViwe.findViewById(R.id.panku_dialog_reset);
                btnReset = dialogReset;
                final Button dialogCancel = (Button) nowViwe.findViewById(R.id.panku_dialog_cancel);
                Runnable panKuRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String pkPartNo = dialogPartno.getText().toString().trim();
                        String PKQuantity = dialogCounts.getText().toString().trim();
                        String PKmfc = dialogFactory.getText().toString().trim();
                        String PKDescription = dialogDescription.getText().toString().trim();
                        String PKPack = dialogFengzhuang.getText().toString().trim();
                        String PKBatchNo = dialogPihao.getText().toString().trim();
                        String minpack = dialogBz.getText().toString().trim();
                        int MinPack = 0;
                        if (!minpack.equals("")) {
                            MinPack = Integer.valueOf(minpack);
                        }
                        int OperID = Integer.valueOf(MyApp.id);
                        String OperName = getSharedPreferences("UserInfo", 0).getString("oprName", "");
                        String DiskID = "";
                        String Note = dialogMark.getText().toString().trim();
                        String PKPlace = dialogPlace.getText().toString().trim();
                        try {
                            int result = insertPankuInfo(Integer.parseInt(info.getDetailId()), info.getPartNo(), Integer
                                            .parseInt(info.getLeftCounts()), pkPartNo, PKQuantity, PKmfc, PKDescription, PKPack,
                                    PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);
                            if (result == 0) {
                                mHandler.sendEmptyMessage(INSERT_FAIL);
                            } else if (result == 1) {
                                final String id = info.getDetailId();
//                                Message message = mHandler.obtainMessage(INSERT_SUCCESS);
//                                message.obj = id;
//                                message.sendToTarget();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PankuActivity.this, "插入成功", Toast.LENGTH_SHORT).show();
                                        btnPk.setVisibility(View.INVISIBLE);
                                        btnReset.setVisibility(View.VISIBLE);
                                    }
                                });
                                try {
                                    List<PankuInfo> pankuList = getPankuList(id, "");
                                    pkData.clear();
                                    pkData.addAll(pankuList);
                                    mHandler.sendEmptyMessage(GET_DATA);
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
                        } catch (IOException e) {
                            mHandler.sendEmptyMessage(INSERT_FAIL);
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                };
                TaskManager.getInstance().execute(panKuRunnable);
            }
        }
    }

    //    string GetDataListForPanKu(string id, string part);
    //
    public List<PankuInfo> getPankuList(String detailId, String partno) throws IOException, XmlPullParserException,
            JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", detailId);
        map.put("part", partno);
        SoapObject request = WebserviceUtils.getRequest(map, "GetDataListForPanKu");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .ChuKuServer);
        List<PankuInfo> tempList = new ArrayList<>();
        JSONObject jObj = new JSONObject(response.toString());
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
            PankuInfo pkInfo = new PankuInfo(pid, mxId, sPartno, leftCounts, factory, description, fengzhuang, pihao, placeId,
                    rkDate, storageName, pkFlag);
            tempList.add(pkInfo);
        }
        return tempList;
    }

    void showEditDialog(final PankuInfo info) {
        View v = LayoutInflater.from(PankuActivity.this).inflate(R.layout.panku_dialog, null);
        nowViwe = v;
        final TextView detailId = (TextView) v.findViewById(R.id.panku_dialog_id);
        final EditText dialogPartno = (EditText) v.findViewById(R.id.panku_dialog_partno);
        final EditText dialogCounts = (EditText) v.findViewById(R.id.panku_dialog_counts);
        final EditText dialogFactory = (EditText) v.findViewById(R.id.panku_dialog_factory);
        final EditText dialogDescription = (EditText) v.findViewById(R.id.panku_dialog_description);
        final EditText dialogFengzhuang = (EditText) v.findViewById(R.id.panku_dialog_fengzhuang);
        final EditText dialogPihao = (EditText) v.findViewById(R.id.panku_dialog_pihao);
        final EditText dialogPlace = (EditText) v.findViewById(R.id.panku_dialog_place);
        final EditText dialogBz = (EditText) v.findViewById(R.id.panku_dialog_minbz);
        final EditText dialogMark = (EditText) v.findViewById(R.id.panku_dialog_mark);
        final Button dialogPanku = (Button) v.findViewById(R.id.panku_dialog_panku);
        final Button dialogTakePic = (Button) v.findViewById(R.id.panku_dialog_takepic);
        final Button dialogScanPlace = (Button) v.findViewById(R.id.panku_dialog_scan);
        dialogScanPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PankuActivity.this, CaptureActivity.class),reqScan);
            }
        });
        dialogTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String detailID = info.getDetailId();
                if (choiceMethodDialog != null&&!choiceMethodDialog.isShowing()) {
                    choiceMethodDialog.show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(PankuActivity.this);
                builder.setTitle("上传方式选择");
                builder.setItems(new String[]{"拍照", "从手机选择"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApp.myLogger.writeInfo("菜单拍照：" + which);
                        Intent intent = new Intent();
                        intent.putExtra("pid", detailID);
                        switch (which) {
                            case 0:
                                intent.setClass(PankuActivity.this, TakePicChildPanku.class);
                                MyApp.myLogger.writeInfo("takepic_panku");
                                break;
                            case 1:
                                intent.setClass(PankuActivity.this, ObtainPicPanku.class);
                                MyApp.myLogger.writeInfo("obtain_panku");
                                break;
                            case 2:
                                break;
                        }
                        startActivity(intent);
                    }
                });
                choiceMethodDialog = builder.show();
            }
        });
        btnPk = dialogPanku;
        final Button dialogReset = (Button) v.findViewById(R.id.panku_dialog_reset);
        btnReset = dialogReset;
        final Button dialogCancel = (Button) v.findViewById(R.id.panku_dialog_cancel);
        dialogReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            int ok = cancelPk(Integer.parseInt(info.getDetailId()));
                            Log.e("zjy", "PankuActivity.java->run(): cancel==" + ok);
                            if (ok == 0) {
                                mHandler.sendEmptyMessage(CHANGEFLAG_ERROR);
                            } else if (ok == 1) {
                                info.setHasFlag("0");
                                Message msg = mHandler.obtainMessage(CHANGEFLAG_SUCCESS, info.getDetailId());
                                msg.sendToTarget();
                            }
                        } catch (IOException e) {
                            mHandler.sendEmptyMessage(CHANGEFLAG_ERROR);
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog.dismiss();
            }
        });
        if (info.getHasFlag().equals("0")) {
            dialogReset.setVisibility(View.INVISIBLE);
            dialogPanku.setVisibility(View.VISIBLE);
        } else {
            dialogPanku.setVisibility(View.INVISIBLE);
            dialogReset.setVisibility(View.VISIBLE);
        }
        dialogPanku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable panKuRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String pkPartNo = dialogPartno.getText().toString().trim();
                        String PKQuantity = dialogCounts.getText().toString().trim();
                        String PKmfc = dialogFactory.getText().toString().trim();
                        String PKDescription = dialogDescription.getText().toString().trim();
                        String PKPack = dialogFengzhuang.getText().toString().trim();
                        String PKBatchNo = dialogPihao.getText().toString().trim();
                        String minpack = dialogBz.getText().toString().trim();
                        int MinPack = 0;
                        if (!minpack.equals("")) {
                            MinPack = Integer.valueOf(minpack);
                        }
                        int OperID = Integer.valueOf(MyApp.id);
                        String OperName = getSharedPreferences("UserInfo", 0).getString("oprName", "");
                        String DiskID = "";
                        String Note = dialogMark.getText().toString().trim();
                        String PKPlace = dialogPlace.getText().toString().trim();
                        try {
                            int result = insertPankuInfo(Integer.parseInt(info.getDetailId()), info.getPartNo(), Integer
                                            .parseInt(info.getLeftCounts()), pkPartNo, PKQuantity, PKmfc, PKDescription, PKPack,
                                    PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);
                            if (result == 0) {
                                mHandler.sendEmptyMessage(INSERT_FAIL);
                            } else if (result == 1) {
                                String id = info.getDetailId();
                                Message message = mHandler.obtainMessage(INSERT_SUCCESS);
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
            }
        });
        detailId.setText("明细id是" + info.getDetailId());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(PankuActivity.this);
        editDialog = builder.create();
        if (editDialog != null && !editDialog.isShowing()) {
            editDialog.show();
            editDialog.setContentView(v);
            editDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            detailId.requestFocus();
        }
    }

    /**
     @param InstorageDetailID 明细号
     @param OldPartNo         原始型号
     @param OldQuantity       原始数量
     @param PKPartNo          盘库型号
     @param PKQuantity        盘库数量
     @param PKmfc             厂家
     @param PKDescription     描述
     @param PKPack            封装
     @param PKBatchNo         批号
     @param MinPack           最小包装
     @param OperID            盘库人ID
     @param OperName          盘库人
     @param DiskID            电脑地址
     @param Note              盘库备注
     @param PKPlace           盘库位置
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    public int insertPankuInfo(int InstorageDetailID, String OldPartNo, int OldQuantity, String PKPartNo, String PKQuantity,
                               String PKmfc, String PKDescription, String PKPack
            , String PKBatchNo, int MinPack, int OperID, String OperName, String DiskID, String Note, String PKPlace) throws
            IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("InstorageDetailID", InstorageDetailID);
        map.put("OldPartNo", OldPartNo);
        map.put("OldQuantity", OldQuantity);
        map.put("PKPartNo", PKPartNo);
        map.put("PKQuantity", PKQuantity);
        map.put("PKmfc", PKmfc);
        map.put("PKDescription", PKDescription);
        map.put("PKPack", PKPack);
        map.put("PKBatchNo", PKBatchNo);
        map.put("MinPack", MinPack);
        map.put("OperID", OperID);
        map.put("OperName", OperName);
        map.put("DiskID", DiskID);
        map.put("Note", Note);
        map.put("PKPlace", PKPlace);
        SoapObject request = WebserviceUtils.getRequest(map, "PanKu");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .ChuKuServer);
        int result = Integer.parseInt(response.toString());
        Log.e("zjy", "PankuActivity.java->insertPankuInfo(): res==" + response.toString());
        return result;
    }

    //    CancelPanKuFlag
    public int cancelPk(int detailid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("instoragedetailPID", detailid);
        SoapObject request = WebserviceUtils.getRequest(map, "CancelPanKuFlag");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .ChuKuServer);
        return Integer.parseInt(response.toString());
    }

    //    GetPauKuDataInfoByID
    public String getUpdateInfo(String detailId) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", detailId);
        SoapObject request = WebserviceUtils.getRequest(map, "GetPauKuDataInfoByID");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, WebserviceUtils
                .ChuKuServer);
        return response.toString();
    }

    class DetailThread extends Thread {
        PankuInfo item;

        public DetailThread(PankuInfo item) {
            this.item = item;
        }

        @Override
        public void run() {
            try {
                String s = getUpdateInfo(item.getHasFlag());
                JSONObject root = new JSONObject(s);
                JSONArray jsonArray = root.getJSONArray("表");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject tempJ = jsonArray.getJSONObject(i);
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
                    PankuInfo info = new PankuInfo("", detailId, PKPartNo, PKQuantity, PKmfc, PKDescription, PKPack, PKBatchNo,
                            PKPlace, "", "", flag);
                    info.setMinBz(MinPack);
                    info.setMark(Mark);
                    Message message = mHandler.obtainMessage(GET_PANKUINFO, info);
                    mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
