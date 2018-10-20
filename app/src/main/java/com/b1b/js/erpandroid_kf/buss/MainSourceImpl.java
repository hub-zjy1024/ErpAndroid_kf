package com.b1b.js.erpandroid_kf.buss;

import android.content.Context;
import android.os.Looper;

import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.WebserviceUtils;
import utils.wsdelegate.MartService;

/**
 * Created by 张建宇 on 2018/10/20.
 */
public class MainSourceImpl implements IMainDataSource {
    DataCallback callback;

    private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

    public String getLogin(final String uname, final String pwd, final String version, final DataCallback
            mCall) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                String deviceID = WebserviceUtils.DeviceID + "," + WebserviceUtils.DeviceNo;
                String soapResult = null;
                try {
                    soapResult = MartService.AndroidLogin("sdr454fgtre6e655t5rt4", uname, pwd, deviceID,
                            version);
                } catch (IOException e) {
                    e.printStackTrace();
                    soapResult = e.getMessage();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    soapResult = e.getMessage();
                }
                final String finalSoapResult = soapResult;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCall.result(finalSoapResult);
                    }
                });
            }
        };
        TaskManager.getInstance().execute(run);
        return null;
    }

    @Override
    public void getScanResult(String code, DataCallback mCall) {

    }

    public interface DataCallback {
        void result(String result);
    }

    private Context mContext;

    public MainSourceImpl(Context mContext) {
        this.mContext = mContext;
    }
}
