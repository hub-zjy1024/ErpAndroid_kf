package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/3/16. */

public class PankuInfo {
    //    "单据号": "16731",
    //            "明细ID": "429",
    //            "型号": "OPA111AM",
    //            "剩余数量": "83",
    //            "厂家": "BB",
    //            "描述": "NEW-C",
    //            "封装": "TO",
    //            "批号": "98+",
    //            "位置": ".2338940",
    //            "入库日期": "2002/1/1 0:00:00"
    //            "仓库": "云岗库房"
    //            "PanKuFlag": "0"
    private String pid;
    private String detailId;
    private String partNo;
    private String leftCounts;
    private String factory;
    private String description;
    private String fengzhuang;
    private String pihao;
    private String placeId;
    private String rukuDate;
    private String storageName;
    private String hasFlag;
    private String minBz;
    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    private String mark;
    public PankuInfo() {
    }

    public String getMinBz() {
        return minBz;
    }

    public void setMinBz(String minBz) {
        this.minBz = minBz;
    }

    @Override
    public String toString() {
        return
                " 单据号=" + pid + "\n" +
                " 明细id=" + detailId + "\n" +
                " 型号=" + partNo + "\n" +
                " 剩余数量=" + leftCounts + "\n" +
                " 厂家=" + factory + "\n" +
                " 描述=" + description + "\n" +
                " 封装=" + fengzhuang + "\n" +
                " 批号=" + pihao + "\n" +
                " 放置位置=" + placeId + "\n" +
                " 入库时间=" + rukuDate + "\n" +
                " 仓库名=" + storageName + "\n" +
                " PankuFlag=" + hasFlag;
    }

    public PankuInfo(String pid, String detailId, String partNo, String leftCounts, String factory, String description, String fengzhuang, String pihao, String placeId, String rukuDate, String storageName, String hasFlag) {
        this.pid = pid;
        this.detailId = detailId;
        this.partNo = partNo;
        this.leftCounts = leftCounts;
        this.factory = factory;
        this.description = description;
        this.fengzhuang = fengzhuang;
        this.pihao = pihao;
        this.placeId = placeId;
        this.rukuDate = rukuDate;
        this.storageName = storageName;
        this.hasFlag = hasFlag;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public void setLeftCounts(String leftCounts) {
        this.leftCounts = leftCounts;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFengzhuang(String fengzhuang) {
        this.fengzhuang = fengzhuang;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setRukuDate(String rukuDate) {
        this.rukuDate = rukuDate;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public void setHasFlag(String hasFlag) {
        this.hasFlag = hasFlag;
    }

    public String getPid() {
        return pid;
    }

    public String getDetailId() {
        return detailId;
    }

    public String getPartNo() {
        return partNo;
    }

    public String getLeftCounts() {
        return leftCounts;
    }

    public String getFactory() {
        return factory;
    }

    public String getDescription() {
        return description;
    }

    public String getFengzhuang() {
        return fengzhuang;
    }

    public String getPihao() {
        return pihao;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getRukuDate() {
        return rukuDate;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getHasFlag() {
        return hasFlag;
    }


}
