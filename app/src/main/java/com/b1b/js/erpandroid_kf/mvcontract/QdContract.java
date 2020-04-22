package com.b1b.js.erpandroid_kf.mvcontract;

import com.b1b.js.erpandroid_kf.entity.QdInfo;
import com.b1b.js.erpandroid_kf.mvcontract.callback.DataObj;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IDataListCallback;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.net.wsdelegate.MartService;

/**
 * Created by 张建宇 on 2018/11/1.
 */
public class QdContract {
    public interface SHQDPresenter {
        void startSearch(String pid);

        void startSearch2(String pid, String pro_id);

        void getData(String pid);
    }

    public interface QdView extends BaseView<SHQDPresenter> {

        void getDataRet(DataObj<List<QdInfo>> mData);

        void startSearch(String pid);

    }

    public interface IDataCallBack<T> {
        public void onCallback(T data);
    }

    public static class QdPresenterImpl implements SHQDPresenter {

        private QdView mView;
        private QdDataProvider dataSrc;
        private android.os.Handler mHandler = new android.os.Handler();

        public QdPresenterImpl(QdView mView) {
            this.mView = mView;
            dataSrc = new QdDataProvider();
        }

        @Override
        public void startSearch2(String pid, final String pro_id) {
            mView.startSearch(pid);
            dataSrc.getData(pid, new  IDataListCallback<QdInfo>() {
                @Override
                public void callback(final List<QdInfo> infos) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DataObj<List<QdInfo>> mret = new DataObj<>();
                            RetObject mobj = mret;
                            List<QdInfo> nInfos = new ArrayList<>();
                            if (infos != null) {
                                for (int i = 0; i < infos.size(); i++) {
                                    if (pro_id.equals(infos.get(i).getTvProId())) {
                                        nInfos.add(infos.get(i));
                                    }
                                }
                                if (nInfos.size() > 0) {
                                    mobj.errCode = 0;
                                    mret.mData = nInfos;
                                    mobj.errMsg = "成功";
                                } else {
                                    mobj.errMsg = "获取数据条数为0";
                                }
                            } else {
                                mobj.errMsg = "获取数据失败";
                            }
                            mView.getDataRet(mret);
                        }
                    });
                }

                @Override
                public void onError(String msg) {

                }
            });
        }

        void getKaipiao(String code) {

        }

        public void startSearch(String pid) {
            mView.startSearch(pid);
            dataSrc.getData2(pid, new IDataCallBack<DataObj<List<QdInfo>>>() {
                @Override
                public void onCallback(DataObj<List<QdInfo>> data) {
                    mView.getDataRet(data);
                }
            });
        }

        @Override
        public void getData(String pid) {

        }
    }

    static class QdDataProvider {

        void getData(final String pid, final  IDataListCallback<QdInfo> callBack) {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String error = "";
                    try {
                        String sellList = MartService.getSellList(pid, "", "");
                        JSONObject jonj = new JSONObject(sellList);
                        JSONArray jsonArray = jonj.getJSONArray("表");
                        List<QdInfo> infos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                             /*{"制单月份":"201810","供应商":"北京恒成伟业电子有限公司","供应商ID":"9204","开票公司":"北京北方科讯电子技术有限公司
        ","单数":"2","批注":""}*/
                            String date = jsonObject.getString("制单月份");
                            String pron = jsonObject.getString("供应商");
                            String proid = jsonObject.getString("供应商ID");
                            String kpcomp = jsonObject.getString("开票公司");
                            String counts = jsonObject.getString("单数");
                            String notes = jsonObject.getString("批注");
                            QdInfo qdInfo = new QdInfo(date, proid, pron, kpcomp, counts
                                    , notes);
                            infos.add(qdInfo);
                        }
                        callBack.callback(infos);
                    } catch (IOException e) {
                        error = e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        error = e.getMessage();
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        error = e.getMessage();
                    }
                    if (!"".equals(error)) {
//                        callBack.onError(error);
                        callBack.callback(null);
                    }
                }
            };
            TaskManager.getInstance().execute(runnable);
        }

        void getData2(final String pid, final IDataCallBack<DataObj<List<QdInfo>>> callBack) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String error = "";
                    DataObj<List<QdInfo>> mRetObj = new DataObj<>();
                    try {
                        String sellList = MartService.getSellList(pid, "", "");
                        JSONObject jonj = new JSONObject(sellList);
                        JSONArray jsonArray = jonj.getJSONArray("表");
                        List<QdInfo> infos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                             /*{"制单月份":"201810","供应商":"北京恒成伟业电子有限公司","供应商ID":"9204","开票公司":"北京北方科讯电子技术有限公司
        ","单数":"2","批注":""}*/
                            String date = jsonObject.getString("制单月份");
                            String pron = jsonObject.getString("供应商");
                            String proid = jsonObject.getString("供应商ID");
                            String kpcomp = jsonObject.getString("开票公司");
                            String counts = jsonObject.getString("单数");
                            String notes = jsonObject.getString("批注");
                            QdInfo qdInfo = new QdInfo(date, proid, pron, kpcomp, counts
                                    , notes);
                            infos.add(qdInfo);
                        }
                        mRetObj.mData = infos;
                        mRetObj.errCode = 0;
                        mRetObj.errMsg = "成功";
                    } catch (IOException e) {
                        mRetObj.errMsg = e.getMessage();
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        mRetObj.errMsg = e.getMessage();
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mRetObj.errMsg = e.getMessage();
                    }
                    callBack.onCallback(mRetObj);
                }
            };
            TaskManager.getInstance().execute(runnable);
        }
    }
}
