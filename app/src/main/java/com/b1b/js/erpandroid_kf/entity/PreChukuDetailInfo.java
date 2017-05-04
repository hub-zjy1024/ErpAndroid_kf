package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/5/3. */

public class PreChukuDetailInfo {
    private String partNo;
    private String fengzhuang;
    private String pihao;
    private String factory;
    private String description;
    private String notes;
    private String p;
    private String counts;
    private String leftCounts;

    public PreChukuDetailInfo() {
    }

    public PreChukuDetailInfo(String partNo, String fengzhuang, String pihao, String factory, String description, String notes, String p, String counts, String leftCounts) {
        this.partNo = partNo;
        this.fengzhuang = fengzhuang;
        this.pihao = pihao;
        this.factory = factory;
        this.description = description;
        this.notes = notes;
        this.p = p;
        this.counts = counts;
        this.leftCounts = leftCounts;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
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

    public String getNotes() {
        if (notes == null) {
            notes = "";
        }
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public String getLeftCounts() {
        return leftCounts;
    }

    public void setLeftCounts(String leftCounts) {
        this.leftCounts = leftCounts;
    }
}
