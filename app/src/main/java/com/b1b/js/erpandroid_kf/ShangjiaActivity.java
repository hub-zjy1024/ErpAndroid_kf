package com.b1b.js.erpandroid_kf;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.SunmiScanActivity;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.entity.ShangJiaInfo;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.MyDecoration;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;
import utils.net.wsdelegate.RKServer;

public class ShangjiaActivity extends SunmiScanActivity implements NoLeakHandler.NoLeakCallback {
    private Handler mHandler = new NoLeakHandler(this);
    private List<ShangJiaInfo> sjInfos = new ArrayList<>();
    private SharedPreferences spKf;
    private String storageID = "";
    private final int GET_DATA = 0;
    private final int GET_DATA_New = 1;
    private RVAdapter mAdapter;
    private EditText edMxID;
    public static final String KuQq_ID = "kq_id";
    private String kuquID = "";
    private boolean isShangjia = false;
    private ShangJiaInfo currentItem = null;
    private String storageInfo;
    private String currentIp;
    private ProgressDialog pd;
    private static final long delay_of_changeFocus = 100;
    private RecyclerView rv;
    @Override

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case GET_DATA:
                pd.cancel();
                mAdapter.notifyDataSetChanged();
                break;
            case GET_DATA_New:
                List<ShangJiaInfo> listData = (List<ShangJiaInfo>) msg.obj;
                sjInfos.addAll(listData);
                mAdapter.notifyDataSetChanged();
                //列表数量大于0，选择第一个item的位置框
                rv.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter.getItemCount() > 0) {
                            //默认上架第一个
                            int defIndex = 0;
                            View childAt = rv.getChildAt(defIndex);
                            currentItem = sjInfos.get(defIndex);
                            if(childAt!=null){
                                View viewById = childAt.findViewById(R.id.item_shangjia_ed_place);
                                changeFocusTo(viewById);
                            }
                        }
                    }
                });
                pd.cancel();
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sangjia);
         rv = (RecyclerView) findViewById(R.id.activity_shangjia_rv);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.addItemDecoration(new MyDecoration(this));
        Button btnScan = (Button) findViewById(R.id.shangjia_activity_btn_scancode);
        edMxID = (EditText) findViewById(R.id.shangjia_activity_ed_pid);
        Button btnSearch = (Button) findViewById(R.id.shangjia_activity_btn_search);
        pd = new ProgressDialog(this);
        pd.setMessage("");
        pd.setTitle("提示");
        pd.setCancelable(false);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShangjia = false;
                startScanActivity();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mxID = edMxID.getText().toString();sjInfos.clear();
                if (mxID.equals("")) {
                    showMsgToast( "请先输入明细ID");
                    return;
                }
                getData(mxID);
            }
        });
        spKf = getSharedPreferences(SpSettings.PREF_KF, MODE_PRIVATE);
        storageInfo = spKf.getString(SpSettings.storageKey, "");
        storageID = StorageUtils.getStorageIDFromJson(storageInfo);
        kuquID = spKf.getString(KuQq_ID, "");
//        mAdapter = new RVAdapter(sjInfos, R.layout.item_shangjia, this);
        mAdapter = new RVAdapter(sjInfos, R.layout.item_shangjia_new, this);
        rv.setAdapter(mAdapter);
        Runnable getInfoRun = new Runnable() {
            @Override
            public void run() {
                currentIp = StorageUtils.getCurrentIp();
                if (currentIp.equals("")) {
//                    currentIp = "4g";
                }
                if (storageID.equals("")) {
                    String info = null;
                    try {
                        info = StorageUtils.getStorageByIp();
                        storageID = StorageUtils.getStorageIDFromJson(info);
                        spKf.edit().putString(SpSettings.storageKey, info).commit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        TaskManager.getInstance().execute(getInfoRun);

    }

    @Override
    public void resultBack(String result) {
        getCameraScanResult(result);
    }

    @Override
    public void getCameraScanResult(final String result) {
        View currentFocus = getCurrentFocus();
        if(currentFocus!=null){
            isShangjia=true;
            int id = currentFocus.getId();
            View viewInContent = getViewInContent(id);
            switch (id) {
                case R.id.shangjia_activity_ed_pid:
//                    viewInContent;
                    isShangjia=false;
                    break;
                case R.id.item_shangjia_ed_place:
//                    View viewInContent = getViewInContent(R.id.item_shangjia_ed_place);
                    isShangjia=true;
                    break;
                default:
                    isShangjia = false;
            }
        }
        if (isShangjia) {
            startShangjia(result);
        } else {
            edMxID.setText(result);
            sjInfos.clear();
            getData(result);
        }
    }

     void changeFocusTo(final View mView) {
         mHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 if (mView != null) {
                     mView.requestFocus();
                 }
             }
         },delay_of_changeFocus);
    }
    public void startShangjia(final String result) {
        if (currentItem != null) {
            final String id = currentItem.getShangjiaID();
            String newKuqu = getKuquID(storageInfo);
            final String description = String.format("android_%s->%s,%s->%s", currentItem.getPlace(), result, currentItem
                            .getKuqu()
                    , newKuqu);
            pd.setMessage("正在上架。。。");
            pd.show();
            Runnable sjRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    try {
                        final String tip = currentIp;
                        if (tip.equals("")) {
                            throw new IOException("获取IP地址失败");
                        }
                        String sjResult = Shangjia(id, result, kuquID, description, loginID, tip);
                        if (sjResult.equals("上架成功")) {
                            currentItem.setStatus(String.format("位置变更成功：%s->%s", currentItem.getPlace(), result));
                            MyApp.myLogger.writeInfo("上架成功：" + currentItem.getCodeStr() + "," + description);
                        } else {
                            MyApp.myLogger.writeInfo("上架失败：" + currentItem.getCodeStr() + "," + description + ",结果：" + result);
                            throw new IOException(String.format("返回结果=''", sjResult));
                        }
                    } catch (final IOException e) {
                        errMsg = "上架失败：" + e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        errMsg = "上架失败：" + e.getMessage();
                        e.printStackTrace();
                    }
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!"".equals(finalErrMsg)) {
                                showMsgDialog(finalErrMsg);
                            }else{
                                mAdapter.notifyDataSetChanged();

                                showMsgToast( "上架成功");
                                View viewInContent = getViewInContent(R.id.shangjia_activity_ed_pid);
                                changeFocusTo(viewInContent);
                            }
                            pd.cancel();
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(sjRun);
        } else {
            showMsgToast("请先点击上架按钮");
        }
    }

    static class RVAdapter extends RecyclerView.Adapter<RvHolder> {

        private List<ShangJiaInfo> xpInfos;
        private int layoutID;
        private Context mContext;

        private RVAdapter(List<ShangJiaInfo> xpInfos, int layoutID, Context mContext) {
            this.xpInfos = xpInfos;
            this.layoutID = layoutID;
            this.mContext = mContext;
        }

        @Override
        public RvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(layoutID, null);
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT);
            } else {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            v.setLayoutParams(layoutParams);
            return new RvHolder(v);
        }

        @Override
        public void onBindViewHolder(RvHolder holder, int position) {
            final ShangJiaInfo xiaopiaoInfo = xpInfos.get(position);
            holder.btnSangjia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShangjiaActivity activity = (ShangjiaActivity) mContext;
                    //activity.isShangjia = true;
                    activity.currentItem = xiaopiaoInfo;
                    String brand = Build.BRAND;
                    if (!brand.contains("V7000")) {
                        activity.startScanActivity();
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                Instrumentation instru = new Instrumentation();
                                instru.sendKeyDownUpSync(KeyEvent.KEYCODE_MUTE);
                            }
                        }.start();
                    }
                }
            });
            if (!"".equals(xiaopiaoInfo.getStatus())) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(xiaopiaoInfo.getStatus());
            } else {
                holder.tvStatus.setVisibility(View.GONE);
            }
            holder.tv.setText(xiaopiaoInfo.toString());
            holder.edPlace.setText(xiaopiaoInfo.getPlace());
        }


        @Override
        public int getItemCount() {
            return xpInfos.size();
        }
    }

    static class RvHolder extends RecyclerView.ViewHolder {
        private  TextView tvStatus;
        public TextView tv;
        public Button btnSangjia;
        public EditText edPlace;

        public RvHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.item_shangjia_tv);
            btnSangjia = (Button) itemView.findViewById(R.id.item_shangjia_btn);
            tvStatus = (TextView) itemView.findViewById(R.id.item_shangjia_tv_status);
            edPlace = (EditText) itemView.findViewById(R.id.item_shangjia_ed_place);

        }
    }

    public String Shangjia(String detailID, String place, String kuQu, String operDescript, String uid, String ip) throws
            IOException, XmlPullParserException {
        return ChuKuServer.Shangjia(detailID, place, kuQu, operDescript, uid, ip);
    }

    public void getData(final String mxID) {
        pd.setMessage("正在搜索数据。。");
        pd.show();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                String errMsg = "";
                List<ShangJiaInfo> listData = new ArrayList<>();
                try {
                    final String tempStorId = storageID;
                    String balaceInfo = "";
                    if (mxID.contains("|")||mxID.contains("-")) {
                        balaceInfo = RKServer.GetShangJiaInfo(mxID);
                    }else{
                        int iPid = 0;
                        try {
                            iPid = Integer.parseInt(mxID);
                        } catch (Exception e) {
                            throw new IOException("请输入纯数字形式的明细号");
                        }
                        balaceInfo =  ChuKuServer.GetStorageBlanceInfoByID(iPid, "", tempStorId );
                    }
                   Log.e("zjy", "SangjiaActivity->run(): balanceInfo==" + balaceInfo);
                    JSONObject jobj = new JSONObject(balaceInfo);
                    JSONArray jarray = jobj.getJSONArray("表");
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject tj = jarray.getJSONObject(i);
                        String parno = tj.getString("型号");
                        String pid = tj.getString("单据号");
                        String time = tj.getString("入库日期");
                        String temp[] = time.split(" ");
                        if (temp.length > 1) {
                            time = temp[0];
                        }
                        time = time.replaceAll("/", "-");
                        String deptno = tj.getString("部门号");
                        String counts = tj.getString("剩余数量");
                        String factory = tj.getString("厂家");
                        String producefrom = "";
                        String pihao = tj.getString("批号");
                        String fengzhuang = tj.getString("封装");
                        String description = tj.getString("描述");
                        String place = tj.getString("位置");
                        String storageID =tempStorId;
                        String flag = tj.getString("SQInvoiceType");
                        String shangjiaID = tj.getString("ID");
                        String company = tj.getString("name");
                        String notes = tj.getString("备注");
                        String kuqu = tj.getString("库区");
                        String detailPID = tj.getString("明细ID");
                        ShangJiaInfo info = new ShangJiaInfo(parno, deptno, time, deptno, counts, factory,
                                producefrom, pihao, fengzhuang, description, place, notes, flag, detailPID, storageID,
                                company);
                        info.setShangjiaID(shangjiaID);
                        info.setPid(pid);
                        info.setKuqu(kuqu);
//                        sjInfos.add(info);
                        listData.add(info);
                    }
                } catch (final IOException e) {
                    errMsg = "连接服务器失败,"+e.getMessage();
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    errMsg = "xml异常," + e.getMessage();
                    e.printStackTrace();
                } catch (JSONException e) {
                    errMsg = "查询不到数据,"+e.getMessage();
                    e.printStackTrace();
                }
                if (!errMsg.equals("")) {
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMsgToast(finalErrMsg);
                        }
                    });
                }
                mHandler.obtainMessage(GET_DATA_New, listData).sendToTarget();
//                mHandler.sendEmptyMessage(GET_DATA);
            }
        };
        TaskManager.getInstance().execute(run);
    }

    public static String getKuquID(String storageInfo) {
        return StorageUtils.getStorageInfo(storageInfo, "ChildStorageID");
    }
}
