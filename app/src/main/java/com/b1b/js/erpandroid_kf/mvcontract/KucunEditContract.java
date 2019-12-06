package com.b1b.js.erpandroid_kf.mvcontract;

import com.b1b.js.erpandroid_kf.mvcontract.callback.IDataListCallback;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IObjCallback;
import com.b1b.js.erpandroid_kf.printer.entity.XiaopiaoInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2019/11/18.
 */
public class KucunEditContract {
    public interface IView {
        void onDataCallback(List<XiaopiaoInfo> datas);

        void onUpdate(boolean isOk, String id);

        void showError(String msg);

        int preLoading(String msg);

        void cancelLoading(int id);

    }

    public static class Presenter {
        private IView mView;
        private Model model;

        public Presenter(IView mView) {
            this.mView = mView;
            model = new Model();
        }

        private android.os.Handler mHandler = new android.os.Handler();
        public void setPihao(final String detailId, String oldph, String newph, String uid) {
            final int progressId = mView.preLoading("正在修改批号");

            model.setPihao(detailId, oldph, newph, uid, new IObjCallback<Boolean>() {
                @Override
                public void onError(final String msg) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(progressId);
                            mView.showError(msg);
                        }
                    });
                }

                @Override
                public void callback(final Boolean obj) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(progressId);
                            mView.onUpdate(obj, detailId);
                        }
                    });
                }
            });
        }

        public void startSearch(String detailId, String partno, String storageID ) {
            final int progressId = mView.preLoading(String.format("正在查询'%s'明细", detailId));

            model.searchData(detailId, partno, storageID, new IDataListCallback<XiaopiaoInfo>() {
                @Override
                public void onError(final String msg) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(progressId);
                            mView.showError(msg);
                        }
                    });

                }

                @Override
                public void callback(final List<XiaopiaoInfo> obj) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(progressId);
                            mView.onDataCallback(obj);
                        }
                    });
                }
            });



        }
    }

    private static class Model {
        void setPihao(final String detailId,final String oldph,final String newph, final String uid,
                      final IObjCallback<Boolean> callback) {

            //            (string  pid, string oldph, string newph, string uid)
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "未知错误";
                    try {
                        String setRes = ChuKuServer.SetPiHao(detailId, oldph, newph, uid);
                        if ("成功".endsWith(setRes)) {
                            callback.callback(true);
                            return;
                        } else {
                            throw new Exception("返回有误," + setRes);
                        }
                    } catch (final IOException e) {
                        errMsg = "查询失败," + e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = "查询失败:xmlParse," + e.getMessage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errMsg = "查询不到相关信息," + e.getMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                        errMsg = "其他," + e.getMessage();
                    }
                    callback.onError(errMsg);
                }
            };
            TaskManager.getInstance().execute(mRun);

        }

        void searchData(final String detailId,final String partno,final String storageID,
                        final      IDataListCallback<XiaopiaoInfo> callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "未知错误";
                    try {
                        int finalid = 0;
                        if (detailId == null || "".equals(detailId)) {


                            finalid = 0;
//                            throw new IOException("请输入数字形式的明细id,错误的明细号=" + detailId);
                        } else {
                            try {
                                finalid =Integer.parseInt(  detailId );
                            } catch (NumberFormatException e) {
                                throw new IOException("不合法的明细号," + e.getMessage());
                            }
                        }
                        String balaceInfo = ChuKuServer.GetStorageBlanceInfoByID(finalid
                                , partno,
                                storageID);
                        JSONObject jobj = new JSONObject(balaceInfo);
                        JSONArray jarray = jobj.getJSONArray("表");
                        List<XiaopiaoInfo> tempList = new ArrayList<>();
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject tj = jarray.getJSONObject(i);
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
                            XiaopiaoInfo info = new XiaopiaoInfo(parno, deptno, time, deptno, counts, factory,
                                    producefrom, pihao, fengzhuang, description, place, notes, flag,
                                    detailPID,
                                    storageID,
                                    company);
                            info.setPid(pid);
                            tempList.add(info);
                        }
                        callback.callback(tempList);
                        return;
                    } catch (final IOException e) {
                        errMsg = "查询失败," + e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = "查询失败:xmlParse," + e.getMessage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errMsg = "查询不到相关信息," + e.getMessage();
                    }
                    callback.onError(errMsg);
                }
            };
            TaskManager.getInstance().execute(mRun);

        }
    }
}
