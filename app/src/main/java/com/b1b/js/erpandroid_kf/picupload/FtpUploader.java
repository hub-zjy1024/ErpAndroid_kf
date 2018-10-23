package com.b1b.js.erpandroid_kf.picupload;

import java.io.IOException;
import java.io.InputStream;

import utils.net.ftp.FTPUtils;

/**
 * Created by 张建宇 on 2019/5/10.
 */
public class FtpUploader extends PicUploader {

    public FtpUploader(String url) {
        super(url);
    }

    @Override
    void uploadPic(String pid, InputStream in, String path, String uid, String cid, String did, String
            remoteName,
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

    public void download(String picUrl, String localPath) throws IOException {
        String urlNoShema = picUrl.substring("ftp://".length());
        int endIndex = urlNoShema.indexOf("/");
        String remoteAbsolutePath = urlNoShema.substring(endIndex);
        String imgFtp = urlNoShema.substring(0, endIndex);
        int index = imgFtp.indexOf(":");
        String finalHost = imgFtp;
        int port = 21;
        if (index != -1) {
            String tp = imgFtp.substring(index + 1);
            finalHost = imgFtp.substring(0, index);
            try {
                port = Integer.parseInt(tp);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        FTPUtils mUitls = FTPUtils.getLocalFTP(sig);
        if (FTPUtils.DB_HOST.equals(sig)) {
            mUitls = FTPUtils.getGlobalFTP();
        }
        try {
            mUitls.login();
            mUitls.download(localPath, remoteAbsolutePath);
        } catch (IOException e) {
            e.printStackTrace();
            mUitls.exitServer();
            throw new IOException(remoteAbsolutePath + "下载失败," + e.getMessage());
        }
    }
}
