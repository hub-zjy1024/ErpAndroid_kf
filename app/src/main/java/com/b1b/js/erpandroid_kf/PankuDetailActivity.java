package com.b1b.js.erpandroid_kf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.PankuLogAdapter;
import com.b1b.js.erpandroid_kf.adapter.PankuMfcAdapter;
import com.b1b.js.erpandroid_kf.adapter.ViewPicAdapter;
import com.b1b.js.erpandroid_kf.bussiness.BottomPicViewer;
import com.b1b.js.erpandroid_kf.bussiness.PicViewer;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.entity.PankuLog;
import com.b1b.js.erpandroid_kf.entity.PankuMFC;
import com.b1b.js.erpandroid_kf.mvcontract.PankuDetailContract;
import com.b1b.js.erpandroid_kf.mvcontract.callback.DataObj;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.myview.helper.OnRecyclerViewScrollListener;
import com.b1b.js.erpandroid_kf.service.PankuPicChooser;
import com.squareup.picasso.Picasso;

import org.vudroid.pdfdroid.codec.PdfContext;
import org.vudroid.pdfdroid.codec.PdfPage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import utils.MyDecoration;
import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;
import utils.common.MyFileUtils;
import utils.common.UploadUtils;
import utils.framwork.ItemClickWrapper;
import utils.framwork.ItemRateLimitClickWrapper;

public class PankuDetailActivity extends ToolbarHasSunmiActivity  {

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
    PankuPicChooser mPicChooser;
    PicViewer mPicViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panku_detail);
        rvImages = getViewInContent(R.id.activity_panku_detail_rv_imgs);
        rvLogs = getViewInContent(R.id.activity_panku_detail_logs);
        PankuDetailContract.IView mvpView = new PankuDetailContract.IView() {
            @Override
            public void cancelLoading(int pIndex) {
                cancelDialogById(pIndex);
            }

            @Override
            public void setPrinter(PankuDetailContract.Presenter presenter) {

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
            @Override
            public void onImageRet2(DataObj<List<FTPImgInfo>> mObj) {
                onImageRet(mObj.mData, mObj.errCode, mObj.errMsg);
            }

            @Override
            public void onGetFactoryRet(List<PankuMFC> minfo, RetObject retObj) {
                if (retObj.errCode == 0) {
                    dataMfcs.clear();
                    dataMfcs.addAll(minfo);
                    Log.e("zjy", "PankuDetailActivity->onGetFactoryRet(): msize==" + minfo.size());
                    mMfcAdapter.notifyDataSetChanged();
                    mMfcAdapter.setAllData(minfo);
                } else {
                    showMsgToast("获取厂家信息失败," + retObj.errMsg);
                }
            }

            @Override
            public void onPankuLogRet(final List<PankuLog> minfos, RetObject retObj) {
                TextView viewInContent = getViewInContent(R.id.activity_panku_detail_tv_log_title);
                if (retObj.errCode == 0) {
                    pankuLogs.clear();
                    pankuLogs.addAll(minfos);
                    rvLogsAdapter.notifyDataSetChanged();
                    viewInContent.setText("盘库记录(" +
                            "" + minfos.size() +
                            ")");
                    final int max = minfos.size() - 1;
                    rvLogs.post(new Runnable() {
                        @Override
                        public void run() {
                            int nowHeight = rvLogs.getHeight();
                            float maxItem = 3.5f;
                            int item0Height = 0;
                            if (rvLogs.getChildCount() > 0) {
                                int coun = 123;
                                View childAt = rvLogs.getChildAt(0);
                                item0Height = childAt.getHeight();
                            }
                            int height = (int) (item0Height * maxItem);
                            Log.e("zjy", "PankuDetailActivity->run() rvLogs: maxHeight==" + height);
                            if (nowHeight == 0) {
                                MyApp.myLogger.writeBug("");
                                Log.e("zjy", "PankuDetailActivity->run() rvLogs setLayoutParams -rvLogs: height=0==");
                                return;
                            }
                            if (nowHeight > height) {
                                rvLogs.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        height));
                            }
                            rvLogs.scrollToPosition(max);


                        }
                    });
                    viewInContent.setVisibility(View.VISIBLE);
                } else {
                    showMsgToast(retObj.errMsg);
                    viewInContent.setVisibility(View.GONE);
                }
            }
            @Override
            public void onImageRet(List<FTPImgInfo> list, int code, String msg) {
                TextView viewInContent = getViewInContent(R.id.activity_panku_detail_tv_pic_title);
                viewInContent.setText("图片列表(" + list.size() +
                        ")");
                if (list.size() > 0) {
                    imgsData.clear();
                    imgsData.addAll(list);
//                    adapter.notifyDataSetChanged();
//                    viewInContent.setVisibility(View.VISIBLE);
//                    int maxHeight = (int) getResources().getDimension(R.dimen.image_preview_height_w2);
//                    maxHeight = (int) (maxHeight * (2 + 0.2f));
//                    final int height = maxHeight;
//                    rvImages.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            int nowHeight = rvImages.getHeight();
//                            Log.e("zjy", "PankuDetailActivity->onPankuLogRet():rvImages-nowHeight2 ==" + nowHeight);
//                            if (nowHeight > height) {
//                                rvImages.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                                        height));
//                            }
//                        }
//                    });
//                    rvImageAdapter.notifyDataSetChanged();
                    mPicViewer.reFreshImages(list);
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
        };




        mPresenter = new PankuDetailContract.Presenter(mvpView);
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
//        rvImages.setAdapter(rvImageAdapter);
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

        /*Button btnViewPic = getViewInContent(R.id.panku_dialog_viewpic);
        btnViewPic.setOnClickListener(this);*/

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
        mPicChooser = new PankuPicChooser(mContext);
//        mPicViewer = new PicViewer(mContext);
        mPicViewer = new BottomPicViewer(mContext);
        mPicViewer.init();
    }

    @Override
    public void showMsgDialog(String msg) {
//        super.showMsgDialog(msg);
        final MaterialDialog mDialog = new MaterialDialog(mContext);
        mDialog.setTitle("提示");
        mDialog.setMessage(msg);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setNegativeButton("确认", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }




    public boolean onCreateOptionsMenu(Menu menu) {
            //导入菜单布局
            getMenuInflater().inflate(R.menu.toolbar_panku_detail, menu);
            return true;
    }

//    @Override
//    public boolean onMenuItemClick(MenuItem item) {
//        Log.e("zjy", "PankuDetailActivity->onMenuItemClick(): mToolbar onMenu==");
//        return true;
//    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_panku_deatil_viewPic:
                String pid = nowInfo.getDetailId();
                mPresenter.getImages(pid);
                break;
            case R.id.action_panku_deatil_takePic:
                String pid2 = nowInfo.getDetailId();
                mPicChooser.openTakePic(pid2);
                break;
            case R.id.action_panku_deatil_print_tag:
//                mPicChooser.openPrintPage(nowInfo.getDetailId());
                int childCount = mToobar.getChildCount();
                if (childCount > 0) {
                    for(int i=0;i<childCount;i++){
                        View cView = mToobar.getChildAt(i);
                        Log.e("zjy", "PankuDetailActivity->onOptionsItemSelected():  mView==" + cView);
                    }
                }
                mPicChooser.openPrintPageWithShared(nowInfo.getDetailId(), mToobar);
                break;
            default:
                //                showMsgToast("点击了" + item.getTitle());
                break;
        }
        return false;
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

    public void initView(final PankuInfo info) {
        mToobar.setSubtitle("明细id:" + info.getDetailId());
//        TextView tempText = new TextView(mContext);
//        tempText.setText("123123123明细id:" + info.getDetailId());
//        int marginRight = getResDimen(R.dimen.toolbar_panku_detail_margin);
//        android.support.v7.widget.Toolbar.LayoutParams mParams =
//                new android.support.v7.widget.Toolbar.LayoutParams(android.widget.Toolbar.LayoutParams.WRAP_CONTENT,
//                        android.widget.Toolbar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
//        mParams.rightMargin = marginRight;
//        addViewToToolBar(tempText, mParams);
        final TextView detailId = getViewInContent(R.id.panku_dialog_id);
        detailId.setVisibility(View.GONE);

        final EditText dialogPartno = getViewInContent(R.id.panku_dialog_partno);
        final EditText dialogCounts = getViewInContent(R.id.panku_dialog_counts);
      //  final EditText dialogFactory = (EditText) getViewInContent(R.id.panku_dialog_factory);
        final EditText dialogFactory = getViewInContent(R.id.panku_dialog_auto_factory);
        final EditText dialogDescription = getViewInContent(R.id.panku_dialog_description);
        final EditText dialogFengzhuang = getViewInContent(R.id.panku_dialog_fengzhuang);
        final EditText dialogPihao = getViewInContent(R.id.panku_dialog_pihao);
        dialogPlace = getViewInContent(R.id.panku_dialog_place);
        final EditText dialogBz = getViewInContent(R.id.panku_dialog_minbz);
        final EditText dialogMark = getViewInContent(R.id.panku_dialog_mark);
        final Button dialogPanku = getViewInContent(R.id.panku_dialog_panku);
        final Button dialogScanPlace = getViewInContent(R.id.panku_dialog_scan);
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
                            OperID = Integer.parseInt(loginID);
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
        final Button dialogReset = getViewInContent(R.id.panku_dialog_reset);
        //        btnReset = dialogReset;
        final Button dialogCancel = getViewInContent(R.id.panku_dialog_cancel);
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
        if ("" .equals(tempDisk)) {
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


    public void jumpPicDetail(FTPImgInfo item, int position) {

        if (item != null) {
            Intent mIntent = new Intent(mContext, FileViewerActivity.class);
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


    public static class RvImageAdapter extends BaseRvAdapter<FTPImgInfo> {

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

        /**
         * 依赖vudroid
         *     implementation 'com.joanzapata.pdfview:android-pdfview:1.0.4@aar'
         * @param filePath
         * @param width
         * @param height
         * @return
         */
        private static Bitmap getPDFThumbnail(String filePath, int width, int height) {
            PdfContext pdfContext = new PdfContext();
            org.vudroid.pdfdroid.codec.PdfDocument pdfDocument =
                    (org.vudroid.pdfdroid.codec.PdfDocument) pdfContext.openDocument(filePath);//path
            // 为要截图的pdf的路径,String类型
            Log.e("zjy", "PankuDetailActivity->getPDFThumbnail(): pageSize==" + pdfDocument.getPageCount());
            //加载首页的预览图
            PdfPage pdfPage = (PdfPage) pdfDocument.getPage(0);//page为要截取的页数,int型
            RectF rf = new RectF();//使用一个矩形去截图
            rf.bottom = rf.right = (float) 1.0;//上方与左方不指定，下方与右方指定为1.0的位置，即截下整幅图
            Bitmap bitmap = pdfPage.renderBitmap(width, height, rf);//这个bitmap就是我们要截的图
            Log.e("zjy", "PankuDetailActivity->getPDFThumbnail(): bitmapSize==" + bitmap.getByteCount());
            return bitmap;
        }
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
            TextView tvMsg = holder.getView(R.id.item_panku_rv_gv_tv_errmsg);
            boolean isPdf = false;
            //仅支持pdf和image类型的文件
            if (MyFileUtils.isImage(item.getImgPath())) {
                tvMsg.setText("");
            } else if (MyFileUtils.isPdf(item.getImgPath())) {
                tvMsg.setText("");
                isPdf = true;
            } else {
                String suffix = item.getImgPath().substring(item.getImgPath().lastIndexOf(".") + 1);
                if (suffix.length() < 5) {
                    tvMsg.setText(String.format("暂不支持预览%s格式", suffix));
                }
            }
            final String realPath = item.getImgPath();
            if (realPath != null) {
                Picasso.with(mContext).load(new File(realPath)).placeholder(R.drawable.ic_pic_placeholder).resize(200, 200).into(finalIv);
            } else {
                Picasso.with(mContext).load(R.drawable.ic_pic_placeholder).resize(200, 200).into(finalIv);
            }
            if (isPdf) {
//                int len=MyDensityUtils.dp2px())
                int len = (int) mContext.getResources().getDimension(R.dimen.image_preview_height_w2);
                Log.e("zjy", "PankuDetailActivity->convert(): width==" + finalIv.getWidth());
                finalIv.setImageBitmap(getPDFThumbnail(realPath, len, len));
            }
        }
    }

}
