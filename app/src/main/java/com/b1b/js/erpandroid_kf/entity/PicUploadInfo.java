package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2019/3/28.
 */
public class PicUploadInfo {
    public String localfilepath;
    public String pid;
    public String tag;
    public String remotepath;
    public String cid;
    public String did;
    public String loginID;
    public String remoteName;
    public String insertPath;
    public String ftpurl;
    public String uid;
    public int okcount;

    public PicUploadInfo(String localfilepath, String pid, String tag, String remotepath, String cid,
                         String did, String loginID, String remoteName, String insertPath) {
        this.localfilepath = localfilepath;
        this.pid = pid;
        this.tag = tag;
        this.remotepath = remotepath;
        this.cid = cid;
        this.did = did;
        this.loginID = loginID;
        this.remoteName = remoteName;
        this.insertPath = insertPath;
    }
}
