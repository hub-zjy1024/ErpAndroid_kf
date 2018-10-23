package utils.net.push;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import utils.net.HttpUtils;

/**
 * Created by 张建宇 on 2019/6/20.
 */
public class WxPusher implements IPush{
    public static String HOST = "http://oa.wl.net.cn:6060";
    @Override
    public void push(String msg, String userid) {
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
}
