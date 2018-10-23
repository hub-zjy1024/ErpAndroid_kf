package com.b1b.js.erpandroid_kf.task;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

import utils.net.wsdelegate.WebserviceUtils;

/**
 Created by 张建宇 on 2017/8/17. */

public class WebServicesTask<T> extends AsyncTask<String, Void, T> {
    public WebCallback<T> callback;
    private LinkedHashMap<String, Object> map;

    public WebServicesTask(WebCallback<T> callback, LinkedHashMap<String, Object> map) {
        this.callback = callback;
        this.map = map;
    }

    private Exception tempEx;
    @Override
    protected T doInBackground(String... params) {
        String method = params[0];
        String serviceName = params[1];
        try {
//            SoapObject request = WebserviceUtils.getRequest(map, method);
//            SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, serviceName);
//            return (T)response.toString();
            return (T) WebserviceUtils.getWcfResult(map, method, serviceName);
        } catch (IOException e) {
            tempEx = e;
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            tempEx = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(T s) {
        if (tempEx != null) {
            callback.errorCallback(tempEx);
        } else {
            callback.okCallback(s);
        }
    }
}
