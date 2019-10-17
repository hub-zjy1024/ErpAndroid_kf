package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.ChukuDetail;
import com.b1b.js.erpandroid_kf.entity.ChukuInfo;
import com.b1b.js.erpandroid_kf.entity.ChukuInfoNew;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IDataListCallback;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.http2.DyjInterface2;

/**
 * Created by 张建宇 on 2019/7/26.
 */
public class PCContract2 {

    static class Mp extends AbsListDataContract.BaseProvider<ChukuInfoNew> {

        public Mp(Context mContext) {
            super(mContext);
        }

        @Override
        public void getData(final IDataListCallback<ChukuInfoNew> callback, Object[] params) {
            final String pid = params[0].toString();
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    try {
                        String dataJson = DyjInterface2.GetChuKuTongZhiInfoToString2(pid);
                        JSONObject listObj = JSONObject.parseObject(dataJson);
                        JSONArray mobjJSONArray = listObj.getJSONArray("data");
                        if (mobjJSONArray.size() == 0) {
                            throw new IOException("查询不到结果");
                        }
                        JSONObject pidObj = mobjJSONArray.getJSONObject(0);
                        final ChukuInfo cInfo = new ChukuInfo();
                        String tempResult = pidObj.getString("出库结果");
                        cInfo.chukuResult = tempResult.replaceAll("\\r", "\n");
                        cInfo.PID = pidObj.getString("PID");
                        cInfo.makeName = pidObj.getString("制单人");
                        cInfo.pidStat = pidObj.getString("单据状态");
                        cInfo.StateNow = pidObj.getString("StateNow");
                        cInfo.ckStorName = pidObj.getString("出库库房");
                        cInfo.makePidTime = pidObj.getString("制单日期");
                        cInfo.kpType = pidObj.getString("开票类型");
                        cInfo.fhKuqu = pidObj.getString("FaHuoKuQu");
                        cInfo.isDiaobo = pidObj.getIntValue("需要调拨");
                        cInfo.kpCompany = pidObj.getString("开票公司");
                        cInfo.fhType = pidObj.getString("发货类型");
                        cInfo.comp = pidObj.getString("公司");
                        cInfo.partName = pidObj.getString("部门");
                        cInfo.notes = pidObj.getString("备注");
                        cInfo.preChukuPrint = pidObj.getString("预出库打印");
                        List<ChukuDetail> mDetail = new ArrayList<>();
                        com.alibaba.fastjson.JSONArray mArray = listObj.getJSONArray("list");
                        for (int i = 0; i < mArray.size(); i++) {
                            JSONObject tobj = mArray.getJSONObject(i);
                            ChukuDetail tDetail = new ChukuDetail();
                            tDetail.setPartNo(tobj.getString("型号"));
                            tDetail.setFactory(tobj.getString("厂家"));
                            tDetail.setDescription(tobj.getString("描述"));
                            tDetail.setdNotes(tobj.getString("明细备注"));
                            tDetail.setFengzhuang(tobj.getString("封装"));
                            tDetail.setCounts(tobj.getIntValue("数量"));
                            mDetail.add(tDetail);
                        }
                        cInfo.details = mDetail;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback((List<ChukuInfoNew>) cInfo);
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

    public class Presenter extends AbsListDataContract.BasePresenter<ChukuInfoNew> {

        public Presenter(AbsListDataContract.IView<ChukuInfoNew> iView, Context mContext) {
            super(iView, mContext);
        }

        @Override
        public AbsListDataContract.IProvider<ChukuInfoNew> intProVider() {
            return new Mp(mContext);
        }

        @Override
        public void getData(final Object[] params) {
            iView.loading("正在查询信息");
            mProvider.getData(new IDataListCallback<ChukuInfoNew>() {

                @Override
                public void callback(List<ChukuInfoNew> minfos) {
                    iView.cancelLoading();
                    iView.fillList(minfos);
                }

                @Override
                public void onError(String msg) {
                    iView.cancelLoading();
                    String pid = (String) params[0];
                    iView.alert("查询" + pid + "信息失败，" + msg);
                }
            }, params);
        }
    }
}
