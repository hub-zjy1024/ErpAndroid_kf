package com.b1b.js.erpandroid_kf.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.b1b.js.erpandroid_kf.entity.PicUploadInfo;
import com.b1b.js.erpandroid_kf.mvcontract.ReUploadContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张建宇 on 2019/4/1.
 */
public class ReUploadService extends Service implements ReUploadContract.IView {
    ReUploadContract.IPresent mPresent;
    List<PicUploadInfo> mInfos;
    private int count = 0;
    ReupBinder mBinber;

    private int finished = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        String status = finished + "/" + mInfos.size();
        if (mBinber == null) {
            mBinber = new ReupBinder(status);
        }
        return mBinber;
    }

    public static class ReupBinder extends Binder {
        public String status = "";

        public ReupBinder(String status) {
            this.status = status;
        }
    }

    @Override
    public void onCreate() {
//        startService(new Intent(this, this.getClass()));
//        stopService(new Intent(this, this.getClass()));
        super.onCreate();
        mInfos = new ArrayList<>();
        mPresent = new ReUploadContract.IPresentImpl(this, this);
//        mPresent.getFailedImgInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("zjy", "ReUploadService->onDestroy(): start destory==");
    }

    @Override
    public void uploadCounts(List<PicUploadInfo> infos) {
        mInfos.addAll(infos);
        mPresent.startUpload(mInfos);
    }

    @Override
    public void onUpload(int index, PicUploadInfo info, String msg) {
        finished = index;
    }

    @Override
    public void showProgress(String msg) {

    }

    @Override
    public void uploadFinished() {

    }

    @Override
    public void setPrinter(ReUploadContract.IPresent iPresent) {

    }
}
