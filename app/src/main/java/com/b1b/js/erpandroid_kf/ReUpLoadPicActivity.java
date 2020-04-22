package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.entity.PicUploadInfo;
import com.b1b.js.erpandroid_kf.mvcontract.ReUploadContract;

import java.util.ArrayList;
import java.util.List;

public class ReUpLoadPicActivity extends ToolbarHasSunmiActivity implements View.OnClickListener, ReUploadContract
        .IView {
    TextView tvTotalCount;
    TextView tvStatus;
    TextView tvProcess;
    private ReUploadContract.IPresent mPresent;
    List<PicUploadInfo> mInfos;

    private ProgressDialog progressDialog;
    private List<PicUploadInfo> failedInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_up_load_pic);
    }

    @Override
    public String setTitle() {
        return getResString(R.string.title_img_reupload);
    }
    @Override
    public void init() {
        super.init();
        tvTotalCount = getViewInContent(R.id.activity_reupload_tv_totalcount);
        tvProcess = getViewInContent(R.id.activity_reupload_tv_process);
        tvStatus = getViewInContent(R.id.activity_reupload_tv_status);
        mPresent = new ReUploadContract.IPresentImpl(this, this);
        progressDialog = new ProgressDialog(mContext);
        mPresent.getFailedImgInfo();
        failedInfos = new ArrayList<>();
    }

    @Override
    public void uploadCounts(List<PicUploadInfo> infos) {
        progressDialog.cancel();
        mInfos = infos;
        int count = infos.size();
        tvTotalCount.setText(""+count);
    }

    @Override
    public void showProgress(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void uploadFinished() {
        progressDialog.cancel();
        mInfos.clear();
    }

    @Override
    public void onUpload(int index, PicUploadInfo info, String msg) {
//        String nowProcess = "进度:" + index + "/" + mInfos.size();
//        tvProcess.setText(nowProcess);
        String log = tvStatus.getText().toString();
        String mlog = "";
        if (!"成功".equals(msg)) {
             mlog = info.remoteName + ",上传失败," + msg;
            log += "\n" + mlog;
            failedInfos.add(info);
        }
        tvStatus.setText(log);
        Log.e("zjy", "ReUpLoadPicActivity->onUpload(): finsh==" + index + "/" + mInfos.size() + ",msg=" +
                mlog);
        if (mInfos.size() == index) {
           progressDialog.cancel();
            mInfos.clear();
            tvTotalCount.setText(""+failedInfos.size());
        }
      }

    @Override
    public void setListeners() {
        setOnClickListener(this, R.id.activity_reupload_btn_start);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_reupload_btn_start:
                if (mInfos == null || mInfos.size() == 0) {
                    showMsgDialog("暂无需要上传的图片");
                    return;
                }
                tvStatus.setText(null);
                mPresent.startUpload(mInfos);
                break;
        }
    }

    @Override
    public void setPrinter(ReUploadContract.IPresent iPresent) {
    }
}
