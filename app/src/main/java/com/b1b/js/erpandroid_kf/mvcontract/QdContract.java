package com.b1b.js.erpandroid_kf.mvcontract;

import com.b1b.js.erpandroid_kf.entity.QdInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.wsdelegate.MartService;

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
        void getDataOk();

        void getDataOk(List<QdInfo> infos);

        void getDataFailed();

        void startSearch(String pid);

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
            dataSrc.getData(pid, new QdDataProvider.CallBack() {
                @Override
                public void callBack(final List<QdInfo> infos) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (infos != null) {
                                List<QdInfo> nInfos = new ArrayList<>();
                                for (int i = 0; i < infos.size(); i++) {
                                    if (pro_id.equals(infos.get(i).getTvProId())) {
                                        nInfos.add(infos.get(i));
                                    }
                                }
                                if (nInfos.size() > 0) {
                                    mView.getDataOk(nInfos);
                                } else {
                                    mView.getDataFailed();
                                }

                            } else {
                                mView.getDataFailed();
                            }
                        }
                    });
                }
            });
        }

        void getKaipiao(String code) {

        }

        public void startSearch(String pid) {
            mView.startSearch(pid);
            dataSrc.getData(pid, new QdDataProvider.CallBack() {
                @Override
                public void callBack(final List<QdInfo> infos) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (infos != null) {
                                mView.getDataOk(infos);
                            } else {
                                mView.getDataFailed();
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void getData(String pid) {

        }
    }

    static class QdDataProvider {
        public interface CallBack {
             void callBack(List<QdInfo> infos);
        }

        void getData(final String pid, final CallBack callBack) {

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
                        callBack.callBack(infos);
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
                        callBack.callBack(null);
                    }
                }
            };
            TaskManager.getInstance().execute(runnable);
        }
    }
}
