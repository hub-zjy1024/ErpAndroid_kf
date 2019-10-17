package com.b1b.js.erpandroid_kf.picupload;

import java.io.IOException;

import utils.net.http2.DyjInterface2;

/**
 * Created by 张建宇 on 2019/7/30.
 */
public class PrechukuUploader extends FtpUploader{
    public PrechukuUploader(String url) {
        super(url);
    }

    @Override
    protected void insertToDb(String cid, String did, String loginID, String pid, String remoteName,
                              String insertPath, String type) throws IOException {
//        super.insertToDb(cid, did, loginID, pid, remoteName, insertPath, type);
        String folder=cid;
        String picName=remoteName;
        String newFlag=type;
        try {
            boolean b = DyjInterface2.UpdateCKPhoto(pid, folder, newFlag, picName, loginID, insertPath);
            if (!b) {
                throw new IOException("关联返回false");
            }
        } catch (DyjInterface2.DyjException e) {
            e.printStackTrace();
            throw new IOException("关联异常", e);
        }
    }
}
