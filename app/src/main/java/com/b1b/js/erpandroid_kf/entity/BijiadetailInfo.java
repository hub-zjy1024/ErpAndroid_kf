package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/23. */

public class BijiadetailInfo {
    private String startTime;
    private String uid;
    private String userName;
    private String mark;

    @Override
    public String toString() {
        return
                "时间=" + startTime + "\n" +
                        "用户ID=" + uid + "\n" +
                        "用户名=" + userName + "\n" +
                        "备注=" + mark + "\n";

    }


    public BijiadetailInfo() {
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public BijiadetailInfo(String startTime, String uid, String userName, String mark) {
        this.startTime = startTime;
        this.uid = uid;
        this.userName = userName;
        this.mark = mark;
    }
}
