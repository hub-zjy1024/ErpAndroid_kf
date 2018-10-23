package utils.net.push;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import utils.net.HttpUtils;

/**
 * Created by 张建宇 on 2019/6/20.
 */
public class DingDingPusher implements IPush {
    public static String HOST = "http://oa.wl.net.cn:6060";
    @Override
    public void push(String msg, String userid) {
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
