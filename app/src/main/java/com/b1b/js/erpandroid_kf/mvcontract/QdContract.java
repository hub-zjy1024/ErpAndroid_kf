package com.b1b.js.erpandroid_kf.mvcontract;

import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.UploadUtils;
import utils.wsdelegate.MartService;

/**
 * Created by 张建宇 on 2018/11/1.
 */
public class QdContract {
    public interface SHQDPresenter {
        void startSearch(String pid);

        void getData(String pid);
    }

    public interface QdView extends BaseView<SHQDPresenter> {
        void getDataOk();

        void getDataFailed();

        void startSearch(String pid);

    }

    public static class QdPresenterImpl implements SHQDPresenter {

        private QdView mView;
        private QdDataProvider dataSrc;

        void getData() {

        }

        public void startSearch(String pid) {
            mView.startSearch(pid);
            getData(pid);
        }

        @Override
        public void getData(String pid) {

        }
    }

    static class QdDataProvider {
        interface CallBack {

        }

        void getData(String pid, CallBack callBack) {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String error = "";
                    try {
                        String rq = UploadUtils.getyyMM();
                        String sellList = MartService.getSellList(rq, "", "", "");
                        JSONObject jonj = new JSONObject(sellList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            TaskManager.getInstance().execute(runnable);
        }
    }
}
