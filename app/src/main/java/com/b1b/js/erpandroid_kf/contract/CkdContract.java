package com.b1b.js.erpandroid_kf.contract;

import android.os.Looper;

import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import utils.MyJsonUtils;
import utils.wsdelegate.ChuKuServer;

public class CkdContract {

    public interface ICkdView extends CkBaseInterface<ChuKuDanInfo>{
        void finishSearch(String msg);
        void searchBefore();
        void setPresenter(Presenter presenter);
    }

    interface ICkdDataProvider extends BaseDataCallBack<ChuKuDanInfo> {


    }
    static class CkdDataSource {
        private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

        public void getData(final ICkdDataProvider callBack, final String uid, final String partNo, final
        String pid, final String stime, final String etime) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String json =ChuKuServer.GetChuKuInfoList("", uid, stime, etime, pid, partNo);
                        final List<ChuKuDanInfo> list = MyJsonUtils.getCKDList(json);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.dataResult(list);
                            }
                        });
                        return;
                    } catch (IOException e) {
                        mHandler.sendEmptyMessage(2);
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        mHandler.sendEmptyMessage(1);
                        e.printStackTrace();
                    }
                    callBack.dataResult(null);
                }
            };
            TaskManager.getInstance().execute(runnable);
        }
    }

    public static class Presenter{
        public Presenter(ICkdView mView) {
            this.mView = mView;
            dataSrc = new CkdDataSource();
            mView.setPresenter(this);
        }

        private CkdDataSource dataSrc;
        private ICkdView mView;
        public void  getData(final String uid, final String partNo, final
        String pid, final String stime, final String etime){
            dataSrc.getData(new ICkdDataProvider() {
                @Override
                public void dataResult(List<ChuKuDanInfo> result) {
                    mView.updateList(result, "");
                   /* if (result != null) {

                    }else{
                        mView.finishSearch("");
                    }*/
                }
            }, uid, partNo, pid, stime, etime);
        }
    }
}
