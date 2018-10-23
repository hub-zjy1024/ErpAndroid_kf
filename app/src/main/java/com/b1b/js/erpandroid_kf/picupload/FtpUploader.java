package com.b1b.js.erpandroid_kf.picupload;

import java.io.IOException;
import java.io.InputStream;

import utils.net.ftp.FTPUtils;

/**
 Created by 张建宇 on 2019/5/10. */
public class FtpUploader extends PicUploader {

    public FtpUploader(String url) {
        super(url);
    }

    @Override
    void uploadPic(String pid, InputStream in, String path, String uid, String cid, String did, String remoteName,
                   String insertType,
                   String insertPath, String sig) throws IOException {
        FTPUtils mUitls = FTPUtils.getLocalFTP(sig);
        try {
            mUitls.login();
            boolean upload = mUitls.upload(in, path);
            if (!upload) {
                throw new IOException("ret=false");
            }
        } catch (IOException e) {
            e.printStackTrace();
            mUitls.exitServer();
            throw new IOException("图片上传ftp失败," + e.getMessage());
        }
    }
}
