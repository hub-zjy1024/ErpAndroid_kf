package com.b1b.js.erpandroid_kf.picupload;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import utils.net.wsdelegate.ChuKuServer;
import utils.net.wsdelegate.MartStock;
import utils.net.wsdelegate.WebserviceUtils;

/**
 Created by 张建宇 on 2019/5/10. */
public abstract class PicUploader {
    protected String sig;

    public String picType_SCCG = "SCCG";
    public String picType_CKTZ = "CKTZ";
    public String picType_PK = "PK";

    public PicUploader(String sig) {
        this.sig = sig;
    }

    abstract void uploadPic(String pid, InputStream in, String path, String uid, String cid, String did, String
            remoteName, String
                                    insertType,
                            String
                                    insertPath, String sig) throws IOException;

    public void upload(String pid, InputStream in, String path, String uid, String cid, String did, String remoteName, String
            insertType, String insertPath) throws IOException {
        uploadPic(pid, in, path, uid, cid, did, remoteName, insertType, insertPath, sig);
        insertToDb(cid, did, uid, pid, remoteName, insertPath, insertType);
    }


    protected void insertToDb(String cid, String did, String loginID, String pid, String remoteName,
                              String insertPath, String type) throws IOException {
        String errMsg = "";
        try {
            String res = "错误";
            if (picType_SCCG.equals(type)) {
                res = MartStock.InsertSSCGPicInfo("", Integer.parseInt(cid), Integer.parseInt(did), Integer
                                .parseInt(loginID),
                        pid, remoteName, insertPath, type);
            } else {
                res = ChuKuServer.SetInsertPicInfo(WebserviceUtils.WebServiceCheckWord,
                        Integer.parseInt(cid), Integer.parseInt(did), Integer.parseInt(loginID), pid,
                        remoteName, insertPath, type);
            }
            Log.e("zjy", getClass() + "->insertToDb(): ==upload" + insertPath + ",res=" + res);
            if (!"操作成功".equals(res)) {
                throw new IOException("ret=" + res);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            errMsg = "关联图片，输入数据格式异常," + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            errMsg = "其他异常," + e;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            errMsg = "xml异常," + e;
        }
        if (!errMsg.equals("")) {
            throw new IOException(errMsg);
        }
    }
}
