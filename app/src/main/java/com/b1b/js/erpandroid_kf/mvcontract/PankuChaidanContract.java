package com.b1b.js.erpandroid_kf.mvcontract;

import android.os.Handler;

import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.PankuChaidanJsonData;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.net.wsdelegate.RKServer;

/**
 * Created by 张建宇 on 2020/4/1.
 */
public class PankuChaidanContract {
    public interface IView extends BaseView<Presenter> {
        public int showProgressWithID(String msg);

        public void cancelLoading(int id);

        public void onChaidanResult(int code, String msg);

    }

    public static class Presenter {
        IView mView;
        private Handler mHandler = new Handler();

        public Presenter(IView mView) {
            this.mView = mView;
        }

        public void panKuChaidan(final PankuChaidanJsonData data) {
            final int id = mView.showProgressWithID("正在拆单");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    int tcode = 1;
                    String tmsg = "未知错误";
                    String res = "";
                    try {
                        int instorageDetailID = Integer.parseInt(data.instorageDetailID);
                        int instorageMainID = Integer.parseInt(data.instorageMainID);
                        String loginID = data.loginID;
                        String json = JSONObject.toJSONString(data.json);
                        res = RKServer.BatchChaiDan(instorageMainID, instorageDetailID, json, loginID);
                        if (res.equals("1")) {
                            tcode = 0;
                            tmsg = "成功";
                        } else {
                            tmsg = "返回异常,ret=" + res;
                        }
                    } catch (NumberFormatException e) {
                        tmsg = "拆单失败," + String.format("pid= %s,detailId=%s,errMsg=%s",
                                data.instorageMainID, data.instorageDetailID, e.getMessage());
                    } catch (IOException e) {
                        tmsg ="拆单失败,"+ e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        tmsg ="拆单失败,"+ e.getMessage();
                    }
                    final int code = tcode;
                    final String msg = tmsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onChaidanResult(code, msg);
                            mView.cancelLoading(id);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }
    }
}
