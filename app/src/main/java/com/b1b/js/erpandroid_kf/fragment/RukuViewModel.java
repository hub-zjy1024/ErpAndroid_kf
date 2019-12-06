package com.b1b.js.erpandroid_kf.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.RukuInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.wsdelegate.ChuKuServer;
import utils.net.wsdelegate.RKServer;

public class RukuViewModel extends ViewModel {
    IViewViewModel mView;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<RukuInfo>> selected = new MutableLiveData<>();
    private final MutableLiveData<String> errMsg = new MutableLiveData<>();
    private final MutableLiveData<String> chaidan = new MutableLiveData<>();



    public void registerView(IViewViewModel mView) {
        this.mView = mView;
    }
    public LiveData<String> getError() {
        return errMsg;
    }

    public LiveData<String> getChaidan() {
        return chaidan;
    }

    public LiveData<String> SpiltBill() {
        return errMsg;
    }

    public LiveData<List<RukuInfo>> getData() {
        return selected;
    }


    public void commitSplit(final List<RukuInfo> mDatas, final String loginID) {
        if (mDatas == null || mDatas.size() <= 1) {
            mView.showError("拆单数至少为2");
            return;
        }
        RukuInfo mainInfo = mDatas.get(0);
        final String mainPId = mainInfo.getPid();
        final String detailId = mainInfo.getDetailID();
        int pdid = mView.preLoading("正在拆分入库单:" + mainPId);
        final int finalPdid = pdid;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {

                String tempError = "未知错误";
                try {
                    //                    String mJson = JSONArray.toJSONString(mDatas);
                    JSONArray marr = new JSONArray();
                    for (RukuInfo mData : mDatas) {
                        JSONObject mobj = new JSONObject();
                        mobj.put("BatchNo", mData.getPihao());
                        mobj.put("Number", mData.getCount() + "");
                        marr.add(mobj);
                    }
                    String mJson = marr.toJSONString();
                    Log.e("zjy", "RukuViewModel->run(): arrJSon==" + mJson);

                    int mdetailId = Integer.parseInt(detailId);
                    int IntPId = Integer.parseInt(mainPId);
//                    RKServer.ChaiDan(IntPId, mdetailId, mJson, loginID);
//                    String res = RKServer.ChaiDanLocal(IntPId, mdetailId, mJson, loginID);
                    String res = RKServer.ChaiDan(IntPId, mdetailId, mJson, loginID);
                    //                    String mDatas = RKServer.CommitSplit(mJson, pid);
                    if ("1".equals(res)) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.cancelLoading(finalPdid);
                                chaidan.setValue("拆单成功");
                            }
                        });
                        return;
                    } else {
                        tempError = "接口返回异常," + res;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    tempError = "网络异常," + e.getMessage();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    tempError = "接口异常xml," + e.getMessage();
                }
                final String finalTempError = "拆单结果," + tempError;
                errMsg.postValue(tempError);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.cancelLoading(finalPdid);
                    }
                });
            }
        };
        TaskManager.getInstance().execute(mRun);

    }

    public void startSearch(final String detailId, final String partno,final String storageID) {

        final String pid = detailId;
        int pdid = mView.preLoading("正在查询入库明细:" + pid);
        final int finalPdid = pdid;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {

                String tempError = "未知错误";
                try {
                    final List<RukuInfo> mList = new ArrayList<>();
                    int finalid = 0;
                    if (detailId == null || "".equals(detailId)) {
                        finalid = 0;
                        //                            throw new IOException("请输入数字形式的明细id,错误的明细号=" +
                        //                            detailId);
                    } else {
                        try {
                            finalid = Integer.parseInt(detailId);
                        } catch (NumberFormatException e) {
                            throw new IOException("不合法的明细号," + e.getMessage());
                        }
                    }
                    String balaceInfo = ChuKuServer.GetStorageBlanceInfoByID(finalid
                            , partno,
                            storageID);
                    org.json.JSONObject jobj = new org.json.JSONObject(balaceInfo);
                    org.json.JSONArray jarray = jobj.getJSONArray("表");
//                    List<XiaopiaoInfo> tempList = new ArrayList<>();
                    for (int i = 0; i < jarray.length(); i++) {
                        org.json.JSONObject tj = jarray.getJSONObject(i);
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
                        //                    String storageID = RukuTagPrintAcitivity.this.storageID;
                        String flag = tj.getString("SQInvoiceType");
                        String company = tj.getString("name");
                        String notes = tj.getString("备注");
                        String detailPID = tj.getString("明细ID");
//                        XiaopiaoInfo info = new XiaopiaoInfo(parno, deptno, time, deptno, counts, factory,
//                                producefrom, pihao, fengzhuang, description, place, notes, flag,
//                                detailPID,
//                                storageID,
//                                company);
//                        info.setPid(pid);
                        RukuInfo tinfo = new RukuInfo();
                        tinfo.setPid(pid);
                        tinfo.setDetailID(detailPID);
                        tinfo.setPihao(pihao);
                        int count = 0;

                        try {
                            count = Integer.parseInt(counts);
                        } catch (NumberFormatException e) {

                        }
                        tinfo.setCount(count);
                        tinfo.setPartno(parno);
                        mList.add(tinfo);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(finalPdid);
                            selected.setValue(mList);
                        }
                    });
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    tempError = "json解析异常," + e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    tempError = "网络异常," + e.getMessage();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    tempError = "接口异常xml," + e.getMessage();
                }
                final String finalTempError = tempError;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.cancelLoading(finalPdid);
                    }
                });
                errMsg.postValue(tempError);
            }
        };
        TaskManager.getInstance().execute(mRun);

    }

    public interface IViewViewModel {
        void showError(String msg);

        int preLoading(String msg);
        void cancelLoading(int id);
    }
}
