package com.b1b.js.erpandroid_kf.contract;

import android.os.Handler;

import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.MyJsonUtils;
import utils.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2018/11/19.
 */
public class ChukuCheckContract {
    static class DataSrc {
        private BaseDataCallBack<CheckInfo> callBack;

        public void searchData(final int typeId, final String pid, final String partNo, final String loginId,
                               final BaseDataCallBack<CheckInfo> callBack) {
            Runnable run = new Runnable() {
                @Override
                public void run() {

                    List<CheckInfo> list = new ArrayList<>();
                    try {
                        String json = ChuKuServer.GetChuKuCheckInfoByTypeID("", typeId, pid, partNo, loginId);
                        list = MyJsonUtils.getCheckInfo(json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.dataResult(list);
                }
            };
            TaskManager.getInstance().execute(run);

        }
    }

    public interface ChukuCheckView {

        void onSearchFinish(int code, List<CheckInfo> dataList, String msg);

        void showProgress(String msg);

    }

    public static class ChukuCheckPresenter {
        private Handler mhandler = new Handler();
        DataSrc dataSrc;

        public ChukuCheckPresenter(ChukuCheckView mView) {
            this.mView = mView;
            dataSrc = new DataSrc();
        }

        private ChukuCheckView mView;

        public void searchData(int typeId, final String pid, final String partNo, String loginId) {
            mView.showProgress("正在查询 " + pid + " 的相关信息");
            dataSrc.searchData(typeId, pid, partNo, loginId, new BaseDataCallBack<CheckInfo>() {
                @Override
                public void dataResult(final List<CheckInfo> result) {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result.size() == 0) {
                                mView.onSearchFinish(0, result, "查询不到相关信息");
                            } else {
                                mView.onSearchFinish(1, result, "");
                            }
                        }
                    });
                }
            });
        }
    }
}
