package com.b1b.js.erpandroid_kf.task;

import android.os.AsyncTask;

import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 Created by 张建宇 on 2017/6/1. */

public class MyBaseTask extends AsyncTask<Void, Void, String> {
    private String methodName;
    private String serviceName;
    private LinkedHashMap<String, Object> properties;
    private TaskCallback mCallback;

    public MyBaseTask(String methodName, String serviceName, LinkedHashMap<String, Object> properties, TaskCallback mCallback) {
        this.methodName = methodName;
        this.serviceName = serviceName;
        this.properties = properties;
        this.mCallback = mCallback;
    }

    @Override
    protected String doInBackground(Void... params) {
        SoapObject object = WebserviceUtils.getRequest(properties, methodName);
        try {
            SoapPrimitive soapPrimitive = WebserviceUtils.getSoapPrimitiveResponse(object, SoapEnvelope.VER11, serviceName);
            return soapPrimitive.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String objects) {
        super.onPostExecute(objects);
        if (mCallback != null) {
            mCallback.callback(objects);
        }
    }
}
