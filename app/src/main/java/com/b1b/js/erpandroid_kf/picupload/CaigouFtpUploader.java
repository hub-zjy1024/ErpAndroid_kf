package com.b1b.js.erpandroid_kf.picupload;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.net.wsdelegate.MartStock;

/**
 Created by 张建宇 on 2019/5/10. */
public class CaigouFtpUploader extends FtpUploader {

    public CaigouFtpUploader(String url) {
        super(url);
    }

    @Override
    protected void insertToDb(String cid, String did, String loginID, String pid, String remoteName, String insertPath, String
            type) throws IOException {
        String errMsg = "";
        try {
            String sccg = MartStock.InsertSSCGPicInfo("", Integer.parseInt(cid), Integer.parseInt(did),
                    Integer.parseInt(loginID), pid, remoteName, insertPath,
                    picType_SCCG);
            if (!"操作成功".equals(sccg)) {
                throw new IOException("插入采购图片失败，" + sccg);
            }
        }catch (NumberFormatException e) {
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
