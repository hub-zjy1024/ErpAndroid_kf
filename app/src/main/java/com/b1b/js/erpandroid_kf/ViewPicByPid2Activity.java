package com.b1b.js.erpandroid_kf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.common.MyFileUtils;
import utils.net.http2.DyjInterface2;

/**
 * 新的图片上传接口对应的查看页
 * Created by 张建宇 on 2019/8/8.
 */
public class ViewPicByPid2Activity extends ViewPicByPidActivity {

    @Override
    public String setTitle() {
        return "图片查看(新)";
    }

    @Override
    public List<FTPImgInfo> getPicList(String pid) throws IOException {
        String errMsg = "";
        List<FTPImgInfo> mList = new ArrayList<>();
        String result = "";
        try {
            String mPics = DyjInterface2.GetBILL_Pictures(pid);
            result = mPics;
            JSONArray marr = com.alibaba.fastjson.JSONArray.parseArray(mPics);
            for (int i = 0; i < marr.size(); i++) {
                JSONObject tObj = marr.getJSONObject(i);
                String imgName = tObj.getString("pictureName");

                String dir = tObj.getString("dir");
                String PicUrl = tObj.getString("PicUrl");
                String imgUrl = "ftp://" + dyjFTP + "/" + dir + "/" + imgName;
                if (!"".equals(PicUrl) && PicUrl != null) {
                    imgUrl = PicUrl;
                }
                //                Log.e("zjy", getClass() + "->getPicList(): ==" + imgUrl);
                File fileParent = MyFileUtils.getFileParent();
                File file = new File(fileParent, "dyj_img/" + imgName);
                String localPath = file.getAbsolutePath();
                FTPImgInfo fti = new FTPImgInfo();
                fti.setFtp(imgUrl);
                fti.setImgPath(localPath);
                mList.add(fti);
            }
            return mList;
        } catch (DyjInterface2.DyjException e) {
            e.printStackTrace();
            errMsg = e.getMessage();
        } catch (JSONException e) {
            e.printStackTrace();
            errMsg = "查询图片为空，json2异常";
        } catch (IOException e) {
            e.printStackTrace();
            errMsg = "查询图片，IO异常" + e.getMessage();
        }
        throw new IOException(errMsg + ",res=" + result);
    }
}
