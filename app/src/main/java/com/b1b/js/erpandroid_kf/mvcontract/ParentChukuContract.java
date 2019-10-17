package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.ChukuDetail;
import com.b1b.js.erpandroid_kf.entity.ChukuInfo;
import com.b1b.js.erpandroid_kf.entity.ChukuInfoNew;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IBoolCallback;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IObjCallback;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.http2.DyjInterface2;
import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2019/7/26.
 */
public class ParentChukuContract {
    public interface ParentChukuView extends BaseView<Presenter> {
        void fillList(List<ChukuInfoNew> infos);

        void onPreCkInfoCb(ChukuInfo info);

        void onChangeSuccess(String flag);

        void loading(String msg);

        int loading2(String msg);

        void cancelLoading2(int id);

        void cancelLoading();

        void alert(String msg);
    }

    public static class Presenter {
        ParentChukuView iView;
        Context mContext;
        DataProvider mProvider;

        @MainThread
        public Presenter(ParentChukuView iView, Context mContext) {
            this.iView = iView;
            this.mContext = mContext;
            mProvider = new DataProvider();
        }

        public void setChukuFail(final String pid, final String uid, final String info,
                                 final String uname) {
            final int ldId = iView.loading2("正在停止出库");
            mProvider.getSetCheckInfo(pid, uid, info, uname, 1, new IObjCallback<String>() {
                @Override
                public void onError(String msg) {
                    iView.alert("操作失败!!," + msg);
                    iView.cancelLoading2(ldId);
                }

                @Override
                public void callback(String obj) {
                    iView.onChangeSuccess("8");
                    iView.cancelLoading2(ldId);
                }
            });

        }
        public void SetChuKuTongZhiChuKu(final String pid,final String uid,  final String flag, String uname) {
            iView.loading("正在出库");
            mProvider.SetChuKuTongZhiChuKu(pid, uid,flag, uname,new IBoolCallback() {
                @Override
                public void callback(Boolean result) {
                    iView.cancelLoading();
                    iView.onChangeSuccess(flag);
                }

                @Override
                public void onError(String msg) {
                    iView.cancelLoading();
                    iView.alert("更新" + pid + "状态失败，" + msg);
                }
            });
        }
        public void UpdateStoreChekerInfo(final String pid,final String uid,  final String flag, String uname) {
            //            /UpdateStoreChekerInfo?pid=&flag=&checker=&checkerName&key=
            iView.loading("正在更新单据状态");
            mProvider.UpdateStoreChekerInfo(pid, uid,flag, uname,new IBoolCallback() {
                @Override
                public void callback(Boolean result) {
                    iView.cancelLoading();
                    iView.onChangeSuccess(flag);
                }

                @Override
                public void onError(String msg) {
                    iView.cancelLoading();
                    iView.alert("更新" + pid + "状态失败，" + msg);
                }
            });

        }

        public void getDataNew(final String pid, final String ip) {
            final int loadId = iView.loading2("正在查询信息");
            if (ip == null || "".equals(ip)) {
                iView.alert("还未获取到ip");
                return;
            }
            mProvider.getDataNewByIp(pid, ip, new IObjCallback<ChukuInfo>() {
                @Override
                public void callback(ChukuInfo result) {
                    iView.cancelLoading2(loadId);
                    iView.onPreCkInfoCb(result);
                }

                @Override
                public void onError(final String msg) {
                    iView.cancelLoading2(loadId);
                    iView.alert("查询'" + pid + "'信息失败，" + msg);
                }
            });
        }
        public void getData(final String pid) {
            iView.loading("正在查询信息");
            mProvider.getData(pid, new DataProvider.IDataCallback() {
                @Override
                public void callback(final List<ChukuInfoNew> minfos) {
                    iView.cancelLoading();
                    iView.fillList(minfos);
                }

                @Override
                public void onError(final String msg) {
                    iView.cancelLoading();
                    iView.alert("查询'" + pid + "'信息失败，" + msg);
                }
            });
        }

        public void SpCheckInfo(final String pid, String loginID) {
            iView.loading("正在特殊审批");
            mProvider.setSpecCheck(pid, loginID, new DataProvider.ObjectCallback() {
                @Override
                public void callback(ChukuInfo result) {
                    iView.cancelLoading();
                    iView.onChangeSuccess("5");
                }

                @Override
                public void onError(String msg) {
                    iView.cancelLoading();
                    iView.alert("特殊审批'" + pid + "'信息失败，" + msg);
                }
            });
        }
    }

    static class DataProvider {
        private Handler mHandler = new Handler();
        interface IDataCallback {
            void callback(List<ChukuInfoNew> minfos);

            void onError(String msg);
        }

        public abstract static class BoolCallback implements IDataCallback {

            public void callback(List<ChukuInfoNew> minfos) {

            }

            public abstract void callback(boolean result);
        }

        public abstract static class ObjectCallback implements IDataCallback {
            public void callback(List<ChukuInfoNew> minfos) {

            }

            public abstract void callback(ChukuInfo result);
        }

        void getSetCheckInfo(final String pid, final String uid, final String info,
                             final String uname, final int type, final IObjCallback<String> callback) {
            Runnable setCheckInfoThread = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    try {
                        final String soapRes = ChuKuServer.GetSetCheckInfo("", 1, info, pid, type, uname,
                                uid);
                        if (!"审核成功".equals(soapRes)) {
                            throw new Exception("返回异常," + soapRes);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(soapRes);
                            }
                        });
                        return;
                    } catch (IOException e) {
                        errMsg = e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        errMsg = e.getMessage();
                        e.printStackTrace();
                    } catch (Exception e) {
                        errMsg = e.getMessage();
                        e.printStackTrace();
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
            TaskManager.getInstance().execute(setCheckInfoThread);
        }
        void SetChuKuTongZhiChuKu(final String pid, final String uid, final String flag, final String uname,
                                   final IBoolCallback callback) {

                    Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<ChukuInfoNew> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
                        final boolean dataJson = DyjInterface2.SetChuKuTongZhiChuKu(pid,uid, uname, flag);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(dataJson);
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
        void UpdateStoreChekerInfo(final String pid, final String uid, final String flag, final String uname,
                                   final IBoolCallback callback) {

            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<ChukuInfoNew> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
                        final boolean dataJson = DyjInterface2.UpdateStoreChekerInfo(pid,uid, uname, flag);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(dataJson);
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

        void setSpecCheck(final String pid, final String uid, final DataProvider.ObjectCallback callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    try {
                        String dataJson = DyjInterface2.SpCheckInfo(pid, uid);
                        if ("1".equals(dataJson)) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.callback((ChukuInfo) null);
                                }
                            });
                        } else {
                            throw new IOException("返回异常" + dataJson);
                        }
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

        void getDataNewByIp(final String pid, final String ip, final IObjCallback<ChukuInfo> callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";

                    try {
                        String dataJson = DyjInterface2.GetChuKuTongZhiInfoToString2ByIP(pid, ip);
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
                        cInfo.flag = pidObj.getString("flag");
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
                        cInfo.yundanID = pidObj.getString("运单号");
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
                                callback.callback(cInfo);
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
            void getData ( final String pid, final DataProvider.IDataCallback callback){
                Runnable mRun = new Runnable() {
                    @Override
                    public void run() {
                        final List<ChukuInfoNew> minfos = new ArrayList<>();
                        String errMsg = "";
                        try {
                            String dataJson = DyjInterface2.GetChuKuTongZhiInfoToString(pid);
                            String dataJson2 = DyjInterface2.GetChuKuTongZhiInfoByPIDToString(pid);
                            Log.e("zjy", getClass() + "->run(): json2==" + dataJson2);
                            List<ChukuInfoNew> scan2Infos = JSONArray.parseArray(dataJson,
                                    ChukuInfoNew.class);
                            minfos.addAll(scan2Infos);
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
