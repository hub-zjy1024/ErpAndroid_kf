package com.b1b.js.erpandroid_kf.contract;

import android.os.Looper;

import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;
import com.b1b.js.erpandroid_kf.presenter.BasePresenter;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import utils.MyJsonUtils;
import utils.wsdelegate.ChuKuServer;

public class CktzContract {
    interface IcktzDataProvider extends ListResultCallback<ChukuTongZhiInfo>{

    }
   public interface IcktzPresenter extends BasePresenter{
        public void getData(String uid, String partNo, String pid, String stime, String etime);

    }

    public interface IcktzView extends CkBaseInterface<ChukuTongZhiInfo>{
        void finishSearch(String msg);
        void searchBefore();

        void setPresenter(IcktzPresenter presenter);
    }

    static class CktzDataSource {
        private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

        public void getData(final IcktzDataProvider callBack, final String uid, final String partNo, final
        String pid, final String stime, final String etime) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String json = ChuKuServer.GetChuKuTongZhiInfoList("", uid, stime, etime, pid, partNo);
                        final List<ChukuTongZhiInfo> list = MyJsonUtils.getCKTZList(json);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.back(list);
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
                    callBack.back(null);
                }
            };
            TaskManager.getInstance().execute(runnable);
        }
    }
    public static class CktzPresent implements IcktzPresenter{
        private CktzDataSource mDatasource;
        private IcktzView iview;

        public CktzPresent(CktzDataSource mDatasource, IcktzView iview) {
            this.mDatasource = mDatasource;
            this.iview = iview;
            iview.setPresenter(this);
        }
        public CktzPresent( IcktzView iview) {
            this.mDatasource = new CktzDataSource();
            this.iview = iview;
            iview.setPresenter(this);
        }

        @Override
        public void start() {

        }

        @Override
        public void getData(String uid, String partNo, String pid, String stime, String etime) {
            mDatasource.getData(new IcktzDataProvider() {
                @Override
                public void back(List<ChukuTongZhiInfo> list) {

                }
            },uid,partNo,pid,stime,etime);
        }
    }
}
