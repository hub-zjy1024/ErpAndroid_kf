package com.b1b.js.erpandroid_kf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuLogAdapter;
import com.b1b.js.erpandroid_kf.adapter.PankuMfcAdapter;
import com.b1b.js.erpandroid_kf.adapter.ViewPicAdapter;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.entity.PankuLog;
import com.b1b.js.erpandroid_kf.entity.PankuMFC;
import com.b1b.js.erpandroid_kf.mvcontract.PankuDetailContract;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.myview.helper.OnRecyclerViewScrollListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.MyDecoration;
import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;
import utils.common.UploadUtils;
import utils.framwork.ItemClickWrapper;
import utils.framwork.ItemRateLimitClickWrapper;
import utils.framwork.MyDensityUtils;

public class PankuDetailActivity extends ToolbarHasSunmiActivity implements PankuDetailContract.IView,
                                                                            View.OnClickListener {

    GridView gvImages;
    RecyclerView rvImages;
    RecyclerView rvLogs;
    RvImageAdapter rvImageAdapter;
    PankuLogAdapter rvLogsAdapter;
    PankuDetailContract.Presenter mPresenter;
    ViewPicAdapter adapter;
    List<FTPImgInfo> imgsData;
    List<PankuLog> pankuLogs;
    public static int ResultCode = 10001;

    public static String extra_DATA = "data";
    PankuInfo nowInfo;
    SharedPreferences pfInfo;
    EditText dialogPlace;
    List<PankuMFC> dataMfcs;
    PankuMfcAdapter mMfcAdapter;
    int imageCols = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panku_detail);
        gvImages = getViewInContent(R.id.activity_panku_detail_imgs);
        rvImages = getViewInContent(R.id.activity_panku_detail_rv_imgs);
        rvLogs = getViewInContent(R.id.activity_panku_detail_logs);

        mPresenter = new PankuDetailContract.Presenter(this);
        imgsData = new ArrayList<>();

        rvImageAdapter = new RvImageAdapter(imgsData, R.layout.item_panku_rv_gv_pics, mContext,
                new RvImageAdapter.ItemClickWrapperWithPosition<FTPImgInfo>() {
                    @Override
                    public void allClick2(View v, FTPImgInfo data, int poi) {
                        jumpPicDetail(data, poi);
                    }
                });


//        image_preview_height_w2
        GridLayoutManager mgrid = new GridLayoutManager(mContext, imageCols);
        rvImages.setLayoutManager(mgrid);
        rvImages.setAdapter(rvImageAdapter);
        OnRecyclerViewScrollListener listener = new OnRecyclerViewScrollListener() {
            @Override
            public void onBottom(View v) {
                showMsgToast("已经到达底部");
            }
        };
        rvImages.addOnScrollListener(listener);
        //        D:\as_workspace\ErpAndroid_kf\app\src\main\res\layout\item_panku_logs.xml
        pankuLogs = new ArrayList<>();
        rvLogsAdapter = new PankuLogAdapter(pankuLogs, R.layout.item_panku_logs, mContext);
        LinearLayoutManager manager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvLogs.setLayoutManager(manager);
        rvLogs.setAdapter(rvLogsAdapter);
        rvLogs.addOnScrollListener(listener);

        Drawable mDivider = getResources().getDrawable(R.drawable.recyclerview_divider);
        MyDecoration myDecoration = new MyDecoration(mDivider, MyDecoration.VERTICAL);
        rvLogs.addItemDecoration(myDecoration);
        adapter = new ViewPicAdapter(imgsData, mContext, R.layout.item_viewpicbypid);
        gvImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FTPImgInfo item = (FTPImgInfo) parent.getItemAtPosition(position);
                jumpPicDetail(item, position);
            }
        });

        /*Button btnViewPic = getViewInContent(R.id.panku_dialog_viewpic);
        btnViewPic.setOnClickListener(this);*/

        gvImages.setAdapter(adapter);
        String json = getIntent().getStringExtra(extra_DATA);
        if (json != null) {
            nowInfo = JSONObject.parseObject(json, PankuInfo.class);
            if (!"0".equals(nowInfo.getHasFlag())) {
                mPresenter.getRealInfo(nowInfo);
            } else if ("".equals(nowInfo.getPid())) {
                mPresenter.getNormalInfo(nowInfo.getDetailId());
            } else {
                initView(nowInfo);
            }
            mPresenter.getPankuLog(nowInfo.getPid(), nowInfo.getDetailId());
        } else {
            showMsgDialogWithCallback("传递参数异常,即将返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        pfInfo = getSharedPreferences(SettingActivity.PREF_USERINFO, MODE_PRIVATE);

        AutoCompleteTextView mFactoryView = getViewInContent(R.id.panku_dialog_auto_factory);
        dataMfcs = new ArrayList<>();
        mMfcAdapter = new PankuMfcAdapter(mContext, dataMfcs, R.layout.item_simple_autocomplete_tv);
        mFactoryView.setAdapter(mMfcAdapter);
        mPresenter.getFactoryList("");
    }

    @Override
    public void onPankuLogRet(List<PankuLog> minfos, RetObject retObj) {
        TextView viewInContent = getViewInContent(R.id.activity_panku_detail_tv_log_title);
        if (retObj.errCode == 0) {
            pankuLogs.clear();
            pankuLogs.addAll(minfos);
            rvLogsAdapter.notifyDataSetChanged();
            viewInContent.setText("盘库记录(" +
                    "" + minfos.size() +
                    ")");
            int nowWidth = rvLogs.getHeight();
            Log.e("zjy", "PankuDetailActivity->onPankuLogRet():rvLogs-height ==" + nowWidth);

            rvLogs.post(new Runnable() {
                @Override
                public void run() {
                    int nowHeight = rvLogs.getHeight();
                    int height = MyDensityUtils.dp2px(mContext, 130);
                    if (nowHeight == 0) {
                        MyApp.myLogger.writeBug("");
                        Log.e("zjy", "PankuDetailActivity->run()setLayoutParams -rvLogs: height=0==");
                        return;
                    }
                    if (nowHeight > height) {
                        rvLogs.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                height));
                    }
                }
            });
            int max = minfos.size() - 1;
            rvLogs.scrollToPosition(max);
            viewInContent.setVisibility(View.VISIBLE);
        } else {
            showMsgToast(retObj.errMsg);
            viewInContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.panku_dialog_viewpic:
                String pid = getIntent().getStringExtra(IntentKeys.key_pid);
                mPresenter.getImages(pid);
                break;
            case R.id.panku_dialog_chaidan:
                break;
            case R.id.panku_dialog_panku:
                break;
            case R.id.panku_dialog_scan:
                break;

        }

    }

    @Override
    public String setTitle() {
        return "盘库详情";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.relaseCache();
    }

    @Override
    public void onImageRet(List<FTPImgInfo> list, int code, String msg) {
        TextView viewInContent = getViewInContent(R.id.activity_panku_detail_tv_pic_title);
        viewInContent.setText("图片列表(" + list.size() +
                ")");
        if (list.size() > 0) {
            imgsData.clear();
            imgsData.addAll(list);
            adapter.notifyDataSetChanged();
            viewInContent.setVisibility(View.VISIBLE);
            int maxHeight = (int) getResources().getDimension(R.dimen.image_preview_height_w2);
            maxHeight = (int) (maxHeight * (2 + 0.2f));
            final int height = maxHeight;
            rvImages.post(new Runnable() {
                @Override
                public void run() {
                    int nowHeight = rvImages.getHeight();
                    Log.e("zjy", "PankuDetailActivity->onPankuLogRet():rvImages-nowHeight2 ==" + nowHeight);
                    if (nowHeight > height) {
                        rvImages.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                height));
                    }
                }
            });
            rvImageAdapter.notifyDataSetChanged();
        } else {
            viewInContent.setVisibility(View.GONE);
        }
        if (code != 0) {
            showMsgDialog(msg);
        }
    }

    @Override
    public void onRealInfoRet(PankuInfo minfo, int code, String msg) {
        if (code == 0) {
            nowInfo = minfo;
            initView(minfo);
        } else {
            showMsgDialog(msg);
        }
    }

    public void initView(final PankuInfo info) {
        final TextView detailId = (TextView) getViewInContent(R.id.panku_dialog_id);
        final EditText dialogPartno = (EditText) getViewInContent(R.id.panku_dialog_partno);
        final EditText dialogCounts = (EditText) getViewInContent(R.id.panku_dialog_counts);
        final EditText dialogFactory = (EditText) getViewInContent(R.id.panku_dialog_factory);
        final EditText dialogDescription = (EditText) getViewInContent(R.id.panku_dialog_description);
        final EditText dialogFengzhuang = (EditText) getViewInContent(R.id.panku_dialog_fengzhuang);
        final EditText dialogPihao = (EditText) getViewInContent(R.id.panku_dialog_pihao);
        dialogPlace = (EditText) getViewInContent(R.id.panku_dialog_place);
        final EditText dialogBz = (EditText) getViewInContent(R.id.panku_dialog_minbz);
        final EditText dialogMark = (EditText) getViewInContent(R.id.panku_dialog_mark);
        final Button dialogPanku = (Button) getViewInContent(R.id.panku_dialog_panku);
        final Button dialogScanPlace = (Button) getViewInContent(R.id.panku_dialog_scan);
        final Button btnCaidan = getViewInContent(R.id.panku_dialog_chaidan);
        final Button btnViewPic = getViewInContent(R.id.panku_dialog_viewpic);
        final ItemClickWrapper itemListener = new ItemRateLimitClickWrapper<PankuInfo>(info) {
            @Override
            public void allClick(View v, PankuInfo data, boolean isOverRate) {
                if (isOverRate) {
                    showMsgToast("请不要点击过快");
                    return;
                }
                switch (v.getId()) {
                    case R.id.panku_dialog_viewpic:
                        // openPicView(data.getDetailId());
                        String pid = nowInfo.getDetailId();
                        //getIntent().getStringExtra(IntentKeys.key_pid);
                        mPresenter.getImages(pid);
                        break;
                    case R.id.panku_dialog_chaidan:
                        Intent cdIntent = new Intent(mContext, PankuChaidanActivity.class);
                        String dataJson = com.alibaba.fastjson.JSONObject.toJSONString(data);
                        cdIntent.putExtra(PankuChaidanActivity.mIntent_Data_key, dataJson);
                        startActivity(cdIntent);
                    case R.id.panku_dialog_cancel:
                        finish();
                        break;
                    case R.id.panku_dialog_reset:
                        break;
                    case R.id.panku_dialog_scan:
                       /* if (!"0".equals(data.getHasFlag())) {
                            showMsgToast("请先解锁再修改位置");
                            return;
                        }*/
                        startScanActivity();
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
                        int OperID = 0;
                        try {
                            OperID = Integer.valueOf(loginID);
                        } catch (Exception e) {
                            showMsgToast("登录人信息获取失败,请重新登录");
                            return;
                        }
                        String OperName = pfInfo.getString("oprName", "");
                        String tempDisk = getDiskId(loginID);
                        String DiskID = tempDisk;
                        mPresenter.startPk(pkPartNo, info, minpack, PKQuantity, PKmfc, PKDescription, PKPack,
                                PKBatchNo, Note, PKPlace, OperID, OperName, DiskID);
                        break;
                }
            }
        };
        btnViewPic.setOnClickListener(itemListener);
        btnCaidan.setOnClickListener(itemListener);
        dialogScanPlace.setOnClickListener(itemListener);
        //        btnPk = dialogPanku;
        final Button dialogReset = (Button) getViewInContent(R.id.panku_dialog_reset);
        //        btnReset = dialogReset;
        final Button dialogCancel = (Button) getViewInContent(R.id.panku_dialog_cancel);
        dialogReset.setOnClickListener(itemListener);
        dialogCancel.setOnClickListener(itemListener);
        dialogPanku.setOnClickListener(itemListener);
        if (info.getHasFlag().equals("0")) {
            showHide(dialogPanku, dialogReset, true);
        } else {
            showHide(dialogPanku, dialogReset, false);
        }

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
    }

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        getCameraScanResult(result);
    }

    @Override
    public void getCameraScanResult(String result) {
        //        super.getCameraScanResult(result);
        dialogPlace.setText(result);
    }

    private String getDiskId(String operID) {
        String tempDisk = pfInfo.getString("nowDevicesId", "");
        String nowDevId = UploadUtils.getDeviceID(mContext);
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

    void showHide(View v1, View v2, boolean flag) {

        if (flag) {
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.INVISIBLE);
        } else {
            v1.setVisibility(View.INVISIBLE);
            v2.setVisibility(View.VISIBLE);
        }
        //关闭解锁功能
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
    }

    @Override
    public int loadingWithId(String msg) {
        return showProgressWithID(msg);
    }

    @Override
    public void onPankuRet(PankuInfo minfo, RetObject retObj) {
        if (retObj.errCode == 0) {
            setResult(ResultCode);
            showMsgToast("盘库完成");
            mPresenter.getPankuLog(nowInfo.getPid(), nowInfo.getDetailId());
        } else {
            showMsgDialog(retObj.errMsg);
        }
    }

    @Override
    public void updateDownProgress(int pIndex, String msg) {
        Dialog dialogById = getDialogById(pIndex);
        if (dialogById != null) {
            if (dialogById instanceof ProgressDialog) {
                ProgressDialog mDialog = (ProgressDialog) dialogById;
                mDialog.setMessage(msg);
            }
        }
    }

    public void jumpPicDetail(FTPImgInfo item, int position) {

        if (item != null) {
            Intent mIntent = new Intent(mContext, PicDetailActivity.class);
            mIntent.putExtra(PicDetailActivity.ex_Path, item.getImgPath());
            ArrayList<String> paths = new ArrayList<>();
            for (int i = 0; i < imgsData.size(); i++) {
                paths.add(imgsData.get(i).getImgPath());
            }
            mIntent.putStringArrayListExtra(PicDetailActivity.ex_Paths, paths);
            mIntent.putExtra("pos", position);
            startActivity(mIntent);
        }
    }

    @Override
    public void cancelLoading(int pIndex) {
        cancelDialogById(pIndex);
    }

    @Override
    public void setPrinter(PankuDetailContract.Presenter presenter) {

    }

    @Override
    public void onGetFactoryRet(List<PankuMFC> minfo, RetObject retObj) {
        if (retObj.errCode == 0) {
            dataMfcs.clear();
            dataMfcs.addAll(minfo);
            mMfcAdapter.notifyDataSetChanged();
            mMfcAdapter.setAllData(minfo);
        }else {
            showMsgToast("获取厂家信息失败," + retObj.errMsg);
        }
    }

    static class RvImageAdapter extends BaseRvAdapter<FTPImgInfo> {

        public RvImageAdapter(List<FTPImgInfo> mData, int layoutId, Context mContext) {
            super(mData, layoutId, mContext);
        }

        public RvImageAdapter(List<FTPImgInfo> mData, int layoutId, Context mContext,
                              ItemClickWrapperWithPosition<FTPImgInfo> mCLick) {
            super(mData, layoutId, mContext);
            this.mCLick = mCLick;
        }

        public abstract static class ItemClickWrapperWithPosition<T> extends ItemClickWrapper {

            @Override
            public void allClick(View v, Object data) {

            }

            public abstract void allClick2(View v, T data, int poi);
        }

        ItemClickWrapperWithPosition<FTPImgInfo> mCLick;

        @Override
        protected void convert(final BaseRvViewholder holder, final FTPImgInfo item) {
            holder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCLick != null) {
                        mCLick.allClick2(v, item, holder.getAdapterPosition());
                    }
                }
            });
            ImageView finalIv = holder.getView(R.id.item_panku_rv_gv_iv);
            final String realPath = item.getImgPath();
            if (realPath != null) {
                Picasso.with(mContext).load(new File(realPath)).placeholder(R.drawable.ic_pic_placeholder).resize(200, 200).into(finalIv);
            } else {
                Picasso.with(mContext).load(R.drawable.ic_pic_placeholder).resize(200, 200).into(finalIv);
            }
        }
    }

}
