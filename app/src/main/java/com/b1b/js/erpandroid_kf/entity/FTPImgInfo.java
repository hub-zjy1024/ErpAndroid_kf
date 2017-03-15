package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/3/2. */

public class FTPImgInfo {
    private String ftp;
    private String imgName;
    private String imgDir;
    private String imgPath;

    public FTPImgInfo(String ftp, String imgName, String imgDir, String imgPath) {
        this.ftp = ftp;
        this.imgName = imgName;
        this.imgDir = imgDir;
        this.imgPath = imgPath;
    }

    public FTPImgInfo() {
    }

    public FTPImgInfo(String ftp, String imgName, String imgDir) {
        this.ftp = ftp;
        this.imgName = imgName;
        this.imgDir = imgDir;
    }

    public String getFtp() {
        return ftp;
    }

    public void setFtp(String ftp) {
        this.ftp = ftp;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgDir() {
        return imgDir;
    }

    public void setImgDir(String imgDir) {
        this.imgDir = imgDir;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
