package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;

import com.alibaba.fastjson.JSONArray;
import com.b1b.js.erpandroid_kf.entity.Scan2Info;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.http2.DyjInterface2;

/**
 * Created by 张建宇 on 2019/7/16.
 */
public class ScanCheckContract {
    public interface IScanCheckView extends BaseView<Presenter> {
        void fillList(List<Scan2Info> infos);

        void loading(String msg);

        void cancelLoading();

        void alert(String msg);
    }

    public static class Presenter {
        IScanCheckView iView;
        Context mContext;

        DataProvider mProvider;

        @MainThread
        public Presenter(IScanCheckView iView, Context mContext) {
            this.iView = iView;
            this.mContext = mContext;
            mProvider = new DataProvider();
        }

        public void getData(final String pid) {
            iView.loading("正在查询信息");
            mProvider.getData(pid, new DataProvider.IDataCallback() {
                @Override
                public void callback(final List<Scan2Info> minfos) {
                    iView.cancelLoading();
                    iView.fillList(minfos);
                }

                @Override
                public void onError(final String msg) {
                    iView.cancelLoading();
                    iView.alert("查询" + pid + "信息失败，" + msg);
                }
            });
        }
    }

    static class DataProvider {
        private Handler mHandler = new Handler();
        interface IDataCallback {
            void callback(List<Scan2Info> minfos);

            void onError(String msg);
        }

        void getData(final String pid, final IDataCallback callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<Scan2Info> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
//                        try {
//                            if (!"".equals(pid)) {
//                                DyjInterface2.GetChuKuTongZhiInfoToString(pid);
//                            }
//                        } catch (DyjInterface2.DyjException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            if (!"".equals(pid)) {
//                                DyjInterface2.GetChuKuTongZhiInfoByPIDToString(pid);
//                            }
//                        } catch (DyjInterface2.DyjException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        String dataJson = DyjInterface2.GetChKuTongZhiDetailInfoToString(pid);
                        com.alibaba.fastjson.JSONArray mArray = com.alibaba.fastjson.JSONArray.parseArray
                                (dataJson);
                        List<Scan2Info> scan2Infos = JSONArray.parseArray(dataJson, Scan2Info.class);
                        minfos.addAll(scan2Infos);

//                        for(int i=0;i<mArray.size();i++) {
//                            JSONObject mObj = mArray.getJSONObject(i);
//                            String pid = mObj.getString("ID");
//                            Scan2Info scan2Info = new Scan2Info();
//                            scan2Info.pid = pid;
//                            minfos.add(scan2Info);
//                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(minfos);
                            }
                        });
                        return;
                    } catch (DyjInterface2.DyjException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    }
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(finalErrMsg);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }
    }

}


