package utils.net.push;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Date;

import utils.net.HttpUtils;

/**
 * Created by 张建宇 on 2019/6/20.
 */
public class PushManager {

    public static String HOST = "http://oa.wl.net.cn:6060";

    //    请求体
    //    {"msg":"这是测试 2019-6-14 17:18:44","userid":"3693"}
    //    响应
    //    {"errCode":0,"errMsg":"成功"}
    //    errCode 0 成功，其他，异常
    public void testPush() {
        String msg = "这是dyjkf app测试," + new Date().toLocaleString();
        String userid = "3693";
        dingdingPush(msg, userid);
    }
    public void testPushWx() {
        String msg = "这是dyjkf app测试," + new Date().toLocaleString();
        String userid = "3693";
        wxPush(msg, userid);
    }

    void wxPush(String msg, String userid) {
        String url = HOST + "/WxPushServlet";
        String json = "";
        try {
            JSONObject mObj = new JSONObject();
            mObj.put("content", msg);
            mObj.put("toUser", userid);
            json = mObj.toJSONString();
            String responseRes = HttpUtils.create(url).post().addReqBody(json)
                    .sendRequest();
            Log.e("zjy", getClass() + "->dingdingPush(): ==" + responseRes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void dingdingPush(String msg, String userid) {
        String url = HOST + "/DDServer/pushmsg";
        String json = "";
        try {
            JSONObject mObj = new JSONObject();
            mObj.put("msg", msg);
            mObj.put("userid", userid);
            json = mObj.toJSONString();
            String responseRes = HttpUtils.create(url).post().addReqBody(json)
                    .sendRequest();
//            {"errCode":0,"errMsg":"成功"}
            Log.e("zjy", getClass() + "->dingdingPush(): ==" + responseRes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
