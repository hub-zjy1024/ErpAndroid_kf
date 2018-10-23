package com.b1b.js.erpandroid_kf.yundan.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.wsdelegate.SF_Server;

/**
 * Created by 张建宇 on 2019/1/24.
 */
public class YunInfoTool {

    public static List<YundanDBData> searchYundanDataByPID(String pid) throws IOException {
        List<YundanDBData> mData = new ArrayList<>();
        try {
            String detail = SF_Server.GetYunDanInfos(pid);
            JSONObject root = new JSONObject(detail);
            JSONArray table = root.getJSONArray("表");
            for (int i = 0; i < table.length(); i++) {
                JSONObject obj = table.getJSONObject(i);
                String jName = obj.getString("业务员");
                String jTel = obj.getString("寄件电话");
                String jAddress = obj.getString("寄件地址1");
                String jComapany = obj.getString("寄件公司");
                String payByWho = obj.getString("谁付运费");
                String pidNotes = obj.getString("Note");
                String dAddress = obj.getString("收件地址");
                String dTel = obj.getString("收件电话");
                String dName = obj.getString("收件人");
                String dCompany = obj.getString("收件公司");

                String corpID = obj.getString("InvoiceCorp");
                String storageID = obj.getString("StorageID");
                String mPid = obj.getString("PID");

                YundanDBData data = new YundanDBData(jName, jTel, jAddress, jComapany, payByWho, pidNotes,
                        dAddress, dTel, dName, dCompany);
                data.setStorageID(storageID);
                data.setCorpID(corpID);
                data.setPid(mPid);
                mData.add(data);
            }
            if (mData.size() == 0) {
                throw new IOException(pid + "对应信息为空");
            }
//            Log.e("zjy", "YunInfoTool->searchByPid(): yundanInfo==" + detail);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("获取单据信息失败", e);
        }
        return mData;
    }


    public static List<DHInfo> getDHInfos() throws IOException {
        List<DHInfo> mlist = new ArrayList<>();
        try {
            String dhJson = getDHAddresss();
            JSONObject addJObj = new JSONObject(dhJson);
            JSONArray addTable = addJObj.getJSONArray("表");
            for (int j = 0; j < addTable.length(); j++) {
                JSONObject obj = addTable.getJSONObject(j);
                String from = obj.getString("FromStorageID");
                String to = obj.getString("ToStotageID");
                String name1 = obj.getString("FromName");
                String phone1 = obj.getString("FromPhone");
                String address1 = obj.getString("FromAddress");
                String name2 = obj.getString("ToName");
                String phone2 = obj.getString("ToPhone");
                String address2 = obj.getString("ToAddress");
                String account = obj.getString("AccountNo");
                DHInfo info = new DHInfo(from, to, name1, phone1, address1, name2, phone2, address2, account);
                mlist.add(info);
            }
            if (mlist.size() == 0) {
                throw new IOException("调货列表为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("获取调货列表失败", e);
        }
        return mlist;
    }

    public static String getDHAddresss() throws IOException, XmlPullParserException {
        return SF_Server.GetBD_DHAddress();
    }

    public static SavedYundanInfo getSaveYundanInfo(String pid) throws IOException, JSONException {
        SavedYundanInfo info = null;
        try {
            String result = getOnlineSavedYdInfo(pid);
//            Log.e("zjy", "YunInfoTool->run(): onlineYundan==" + result);
            //                    "objid":"613","parentid":"0","objname":"1176338","objvalue":"616606640489",
            // "objtype":"顺丰","objexpress":"010",
            JSONObject obj = new JSONObject(result);
            JSONArray root = obj.getJSONArray("表");
            if (root.length() > 0) {
                JSONObject t = root.getJSONObject(0);
                String orderID = t.getString("objvalue");
                String destcode  = t.getString("objexpress");
                String exName   = t.getString("objtype");
                info = new SavedYundanInfo(orderID, destcode, exName);
            }else{
                throw new JSONException("查找不到运单信息,json=" + result);
            }
        } catch (JSONException e) {
            throw new JSONException("查找不到运单信息，" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("获取关联运单信息失败", e);
        }
        return info;
    }

    public static String getOnlineSavedYdInfo(String pid) throws IOException, XmlPullParserException {
        return SF_Server.GetBD_YunDanInfoByID(pid);
    }
}
