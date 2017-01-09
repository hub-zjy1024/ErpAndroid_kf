package com.b1b.js.erpandroid_kf.task;

import android.os.AsyncTask;

import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;
import com.b1b.js.erpandroid_kf.utils.MyCallBack;
import com.b1b.js.erpandroid_kf.utils.MyJsonUtils;
import com.b1b.js.erpandroid_kf.utils.WcfUtils;

import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by js on 2016/12/27.
 */

public class MyAsyncTask extends AsyncTask<String, Void, List> {
    private String month;
    private MyCallBack myCallBack;

    public MyAsyncTask(MyCallBack myCallBack) {
        this.myCallBack = myCallBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List list) {
        super.onPostExecute(list);
        if (myCallBack != null) {
            myCallBack.postRes(list);
        }

    }
//"EmployeeID": "100",
//"员工": "朱强",
//"考勤年月": "20161101",
//"考勤状态": "迟到早退",
//"上班时间": "10:41:01",
//"下班时间": "10:41:01",
//"早IP": "172.16.1.102",
//"晚IP": "172.16.1.102"

    @Override
    protected List doInBackground(String... params) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("month", params[0]);
        map.put("uid", params[1]);
        map.put("checkWord", "");
        SoapObject request = WcfUtils.getRequest(map, "GetMyKaoQinInfoJson");
        try {
            SoapPrimitive response = WcfUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "MyBasicServer.svc");
            if (response != null) {
                List<KaoqinInfo> kqList = MyJsonUtils.getKaoQinList(response.toString());
                return kqList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MyAsyncTask() {
        super();
    }
}
