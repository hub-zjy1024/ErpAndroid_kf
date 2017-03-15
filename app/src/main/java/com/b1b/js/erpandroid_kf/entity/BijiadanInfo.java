package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/22. */

public class BijiadanInfo {
    //    "单据号": "908353
    //            "制单日期": "2017/2/22 15:36:50
    //            "型号": "AON6512
    //            "数量": "3000
    //            "封装": "qfn
    //            "批号": "16
    //            "描述": "data
    //            "厂家": "aos
    //            "比价开始时间": "2017/2/22 15:38:59
    //            "开票公司": "北京美商利华电子技术有限公司
    //            "备注": "1"
    private String pid;
    private String createDate;
    private String partNo;
    private String counts;
    private String fengzhuang;
    private String pihao;
    private String factory;
    private String description;
    private String startTime;
    private String kpCompany;
    private String mark;

    @Override
    public String toString() {
        return
                "单据号=" + pid + "\n" +
                        "比价开始时间=" + startTime + "\n" +
                        "制单日期=" + createDate + "\n" +
                        "型号=" + partNo + "\n" +
                        "数量=" + counts + "\n" +
                        "封装=" + fengzhuang + "\n" +
                        "批号=" + pihao + "\n" +
                        "厂家=" + factory + "\n" +
                        "描述=" + description + "\n" +
                        "开票公司=" + kpCompany + "\n" +
                        "备注=" + mark;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public String getFengzhuang() {
        return fengzhuang;
    }

    public void setFengzhuang(String fengzhuang) {
        this.fengzhuang = fengzhuang;
    }

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getKpCompany() {
        return kpCompany;
    }

    public void setKpCompany(String kpCompany) {
        this.kpCompany = kpCompany;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public BijiadanInfo() {
    }

    public BijiadanInfo(String pid, String createDate, String partNo, String counts, String fengzhuang, String pihao, String factory, String description, String startTime, String kpCompany, String mark) {
        this.pid = pid;
        this.createDate = createDate;
        this.partNo = partNo;
        this.counts = counts;
        this.fengzhuang = fengzhuang;
        this.pihao = pihao;
        this.factory = factory;
        this.description = description;
        this.startTime = startTime;
        this.kpCompany = kpCompany;
        this.mark = mark;
    }
}
