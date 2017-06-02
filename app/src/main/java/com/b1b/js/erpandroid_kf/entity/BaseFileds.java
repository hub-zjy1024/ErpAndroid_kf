package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/5/16. */

public class BaseFileds {
    protected String partNo;
    protected String factory;
    protected String pihao;
    protected String fengzhuang;
    protected String description;

    public BaseFileds() {
    }

    public BaseFileds(String partNo, String factory, String pihao, String fengzhuang, String description) {
        this.partNo = partNo;
        this.factory = factory;
        this.pihao = pihao;
        this.fengzhuang = fengzhuang;
        this.description = description;
    }

    @Override
    public String toString() {
        return
                "型号=" + partNo + "\n" +
                        "厂家=" + factory + "\n" +
                        "批号=" + pihao + "\n" +
                        "封装=" + fengzhuang + "\n" +
                        "描述=" + description + "\n";
    }


    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    public String getFengzhuang() {
        return fengzhuang;
    }

    public void setFengzhuang(String fengzhuang) {
        this.fengzhuang = fengzhuang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
