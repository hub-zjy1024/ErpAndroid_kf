package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/7. */

public class Caigoudan {
    //    "单据编号": "808826",
    //            "制单日期": "2017/2/6 14:46:55",
    //            "单据状态": "等待采购",
    //            "业务员": "管理员",
    //            "采购员": "管理员",
    //            "型号": "TEST20170206003【10】"
    private String state;
    private String pid;
    private String createdDate;
    private String yewuName;
    private String caigouName;
    private String partNo;

    @Override
    public String toString() {
        return
                " 单据号=" + pid + "\n" +
                        " 单据状态=" + state + "\n" +
                        " 制单时间=" + createdDate + "\n" +
                        " 业务员=" + yewuName + "\n" +
                        " 采购员=" + caigouName + "\n" +
                        " 型号=" + partNo;
    }

    public Caigoudan() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Caigoudan(String pid, String createdDate, String yewuName, String caigouName, String partNo) {
        this.pid = pid;
        this.createdDate = createdDate;
        this.yewuName = yewuName;
        this.caigouName = caigouName;
        this.partNo = partNo;
    }

    public Caigoudan(String state, String pid, String createdDate, String yewuName, String caigouName, String partNo) {
        this.state = state;
        this.pid = pid;
        this.createdDate = createdDate;
        this.yewuName = yewuName;
        this.caigouName = caigouName;
        this.partNo = partNo;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getYewuName() {
        return yewuName;
    }

    public void setYewuName(String yewuName) {
        this.yewuName = yewuName;
    }

    public String getCaigouName() {
        return caigouName;
    }

    public void setCaigouName(String caigouName) {
        this.caigouName = caigouName;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
}
