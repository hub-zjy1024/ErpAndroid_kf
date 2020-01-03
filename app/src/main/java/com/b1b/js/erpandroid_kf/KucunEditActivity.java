package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.KuCunEditAdapter;
import com.b1b.js.erpandroid_kf.config.ExtraParams;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.fragment.RukuFragment;
import com.b1b.js.erpandroid_kf.mvcontract.KucunEditContract;
import com.b1b.js.erpandroid_kf.printer.entity.XiaopiaoInfo;
import com.b1b.js.erpandroid_kf.task.StorageUtils;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.MyDecoration;

public class KucunEditActivity extends ToolbarHasSunmiActivity implements KucunEditContract.IView {

    KucunEditContract.Presenter presenter;
    private String storID;

    EditText editDpId;
    EditText editPartno;
    Button btnSearch;
    Button btnScan;
    RecyclerView datalistView;
    RecyclerView.Adapter rvAdatper;
    List<XiaopiaoInfo> mDatas;

    private int stat = 0;
    RukuFragment fragChaidan;
    AlertDialog editDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kucun_edit);
        presenter = new KucunEditContract.Presenter(this);

        datalistView = getViewInContent(R.id.activity_kucun_edit_dataList);
        editDpId = getViewInContent(R.id.activity_kucun_edit_ed_dpid);
        editPartno = getViewInContent(R.id.activity_kucun_edit_ed_partno);
        btnSearch = getViewInContent(R.id.activity_kucun_edit_btn_search);
        btnScan = getViewInContent(R.id.activity_kucun_edit_btn_scan);
        final Button btnChaidan = getViewInContent(R.id.activity_kucun_container_btn_chaidan);
        btnChaidan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();

                if (fragChaidan == null) {
                     fragChaidan = new RukuFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(ExtraParams.NM_LOGIN_ID, loginID);
                    fragChaidan.setArguments(bundle);
                    fragmentTransaction.add(R.id.activity_kucun_container_line, fragChaidan);
                    fragmentTransaction.commit();
                    mDatas.clear();
                    rvAdatper.notifyDataSetChanged();
                    stat = 1;
                } else {
                    fragmentTransaction.remove(fragChaidan);
                    fragmentTransaction.commit();

                    fragChaidan = null;
                    stat = 0;
                }
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempDpid = editDpId.getText().toString();
                String tempPartno = editPartno.getText().toString();
                if (stat == 1) {
//                    fragChaidan.startSearch(tempDpid, tempPartno, storID);
                    fragChaidan.startSearch(tempDpid, tempPartno, storID);
                } else {
                    presenter.startSearch(tempDpid, tempPartno, storID);
                }
            }
        });
        mDatas = new ArrayList<>();
        rvAdatper = new KuCunEditAdapter(mDatas, R.layout.item_kucun_edit_list, this, new KuCunEditAdapter.OnItemClickListener() {
            @Override
            public void onClick(XiaopiaoInfo item) {
                showEditView(item);
            }
        });
        Drawable mDivider =
                getApplicationContext().getResources().getDrawable(R.drawable.recyclerview_divider);
        datalistView.addItemDecoration(new MyDecoration(mDivider, MyDecoration.VERTICAL));
        datalistView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        datalistView.setAdapter(rvAdatper);
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences spKf = getSharedPreferences(SpSettings.PREF_KF, MODE_PRIVATE);
                    String storageInfo = spKf.getString(SpSettings.storageKey, "");
                    if ("".equals(storageInfo)) {
                        storageInfo = StorageUtils.getStorageByIp();
                        spKf.edit().putString(SpSettings.storageKey, storageInfo).apply();
                    }
                    storID = StorageUtils.getStorageIDFromJson(storageInfo);
                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showError("获取库房信息失败," + e.getMessage());
                        }
                    });
                }
            }
        };
        TaskManager.getInstance().execute(updateRun);
    }

    @Override
    public String setTitle() {
        return "库存信息修改";
    }


    public void showEditView(final XiaopiaoInfo xiaopiaoInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // TODO: 2019/11/18 添加弹窗内容，执行修改操作，创建itemLayout
        final View itemView = LayoutInflater.from(mContext).inflate(R.layout.dialog_kucun_edit, null, false);
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
//        if (layoutParams == null) {
//            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//        } else {
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        }
//        int paddingVetical = (int) mContext.getResources().getDimension(R.dimen.item_view_padding_vetical);
//        int paddingHorizontal =
//                (int) mContext.getResources().getDimension(R.dimen.item_view_padding_horizontal);
//        itemView.setPadding(paddingVetical, paddingHorizontal, paddingVetical, paddingHorizontal);

        builder.setTitle("修改批号：" + xiaopiaoInfo.getPid());
        Button btnModify = itemView.findViewById(R.id.dialog_kucun_btn_update);
        final EditText editText = itemView.findViewById(R.id.dialog_kucun_ed_partno);
        final TextView tvOldPartno = itemView.findViewById(R.id.dialog_kucun_tv_oldpartno);
        final String oldpihao = xiaopiaoInfo.getPihao();
        tvOldPartno.setText(oldpihao);
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPihao = editText.getText().toString();
                presenter.setPihao(xiaopiaoInfo.getCodeStr(), oldpihao, tempPihao, loginID);
            }
        });
        builder.setView(itemView);
        editDialog = builder.create();
        editDialog.show();
    }


    @Override
    public void onDataCallback(List<XiaopiaoInfo> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        showMsgToast("获取到" + datas.size() + "条库存信息");
        rvAdatper.notifyDataSetChanged();
    }


    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        getCameraScanResult(result);
    }

    @Override
    public void getCameraScanResult(String result) {
        super.getCameraScanResult(result);
        editDpId.setText("");
        editDpId.setText(result);
        String partno = "";

        if (stat == 1) {
            fragChaidan.startSearch(result , partno, storID);
        } else {
            presenter.startSearch(result, partno, storID);
        }
    }

    @Override
    public void onUpdate(boolean isOk, String dpid) {
        if (isOk) {
            showMsgDialog("批号已修改完成");
            String partno = "";
            if (editDialog != null) {
                editDialog.cancel();
            }
            presenter.startSearch(dpid, partno, storID);
        }
    }

    @Override
    public void showError(String msg) {
        showMsgDialog(msg);
    }

    @Override
    public int preLoading(String msg) {
        return showProgressWithID(msg);
    }

    @Override
    public void cancelLoading(int id) {
        cancelDialogById(id);
    }
}
