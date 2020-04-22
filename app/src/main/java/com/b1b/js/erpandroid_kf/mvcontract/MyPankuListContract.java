package com.b1b.js.erpandroid_kf.mvcontract;

import android.os.Handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2020/4/8.
 */
public class MyPankuListContract {
    public interface IView extends IViewWithLoading<Presenter> {
        void onMyListRet(RetObject retObj, List<PankuInfo> infos);
    }
    public static class Presenter  {
        IView mView;
        Handler mHandler = new android.os.Handler();

        public Presenter(IView mView) {
            this.mView = mView;
        }

        public void getMyList(final String uid) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final RetObject mObj = new RetObject();
                    final List<PankuInfo> list = new ArrayList<>();
                    try {
                        String pankRes = ChuKuServer.GetPanKuTask(uid);
                        JSONObject jObj = JSONObject.parseObject(pankRes);
                        JSONArray mArr = jObj.getJSONArray("表");
                        if (mArr.size() > 0) {
                            //                        "objid": "1",
                            //                                "InstorageDetailID": "3022764",
                            //                                "PanKuData": "2020/4/9 0:00:00",
                            //                                "PartNo": "TEST20200407001",
                            //                                "Quentity": "100",
                            //                                "MFC": "cj",
                            //                                "Pack": "+",
                            //                                "BatchNo": "+",
                            //                                "Place": "+",
                            //                                "UserID": "101",
                            //                                "UserName": "管理员"
                            for (int i = 0; i < mArr.size(); i++) {
                                JSONObject tobj = mArr.getJSONObject(i);
                                String detailId = tobj.getString("InstorageDetailID");
                                String panKuData = tobj.getString("入库日期");
//                                String panKuData = tobj.getString("PanKuData");
                                String PartNo = tobj.getString("PartNo");
                                String Quentity = tobj.getString("Quentity");
                                String MFC = tobj.getString("MFC");
                                String Pack = tobj.getString("Pack");
                                String BatchNo = tobj.getString("BatchNo");
                                String Place = tobj.getString("Place");
                                String UserID = tobj.getString("UserID");
                                String UserName = tobj.getString("UserName");
                                String storagename= tobj.getString("仓库");
                                String description = tobj.getString("描述");
//                                "入库日期":"2020/4/3 11:27:59","描述":"++","仓库":"深圳赛格"
                                PankuInfo minfo = new PankuInfo("", detailId, PartNo, Quentity, MFC, description,
                                        Pack, BatchNo, Place, panKuData, storagename, "0");
                                minfo.setMark("");
                                list.add(minfo);
                            }
                            mObj.errCode = 0;
                            mObj.errMsg = "成功";
                        }else {
                            throw new IOException("待盘库列表为空");
                        }
                    } catch (JSONException e) {
                        mObj.errMsg = "获取待盘库列表为空," + e.getMessage();
                    } catch (Exception e) {
                        mObj.errMsg = "获取待盘库列表失败," + e.getMessage();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onMyListRet(mObj, list);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }
    }
}
