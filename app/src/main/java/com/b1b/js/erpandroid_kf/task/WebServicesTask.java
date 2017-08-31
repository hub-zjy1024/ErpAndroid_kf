package com.b1b.js.erpandroid_kf.task;

import android.os.AsyncTask;
import android.util.Log;

import utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 Created by 张建宇 on 2017/8/17. */

public class WebServicesTask<T> extends AsyncTask<String, Void, T> {
    public WebCallback<T> callback;
    private LinkedHashMap<String, Object> map;

    public WebServicesTask(WebCallback<T> callback, LinkedHashMap<String, Object> map) {
        this.callback = callback;
        this.map = map;
    }

    @Override
    protected T doInBackground(String... params) {
        String method = params[0];
        String serviceName = params[1];
        try {
            Log.e("zjy", "WebServicesTask->doInBackground(): thread==" + Thread.currentThread().getName());
            SoapObject request = WebserviceUtils.getRequest(map, method);
            SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, serviceName);
            return (T)response.toString();
        } catch (IOException e) {
            callback.errorCallback(e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            callback.errorCallback(e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(T s) {
        callback.okCallback(s);
    }
}
