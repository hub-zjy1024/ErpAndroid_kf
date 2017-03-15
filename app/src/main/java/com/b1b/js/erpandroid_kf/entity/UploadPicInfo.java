package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/22. */

public class UploadPicInfo {
    private String state;
    private String path;

    public UploadPicInfo(String state, String path) {
        this.state = state;
        this.path = path;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "UploadPicInfo{" +
                "state='" + state + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
