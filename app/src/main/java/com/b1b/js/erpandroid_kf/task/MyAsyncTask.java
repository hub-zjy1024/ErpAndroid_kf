package com.b1b.js.erpandroid_kf.task;

import android.os.AsyncTask;
import android.util.Log;

import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 Created by js on 2016/12/27. */

public class MyAsyncTask extends AsyncTask<String, Void, String> {
    private TaskCallback myCallBack;

    public MyAsyncTask(TaskCallback myCallBack) {
        this.myCallBack = myCallBack;
    }


    @Override
    protected void onPostExecute(String list) {
        super.onPostExecute(list);
        if (myCallBack != null) {
            myCallBack.callback(list);
        }

    }

    @Override
    protected String doInBackground(String... params) {
        Log.e("zjy", "MyAsyncTask->doInBackground(): current==" + Thread.currentThread().getId());
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("month", params[0]);
        map.put("uid", params[1]);
        map.put("checkWord", "");
        SoapObject request = WebserviceUtils.getRequest(map, "GetMyKaoQinInfoJson");
        try {
            SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils
                    .MyBasicServer);
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }
}
