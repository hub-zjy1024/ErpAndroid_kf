package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/16. */

public class InsertDetialInfo {
    private String money;
    private String pihao;
    private String partno;
    private String type;
    private String detailId;
    private Double checkPrice;
    private String typeId;

    public InsertDetialInfo() {
    }

    @Override
    public String toString() {
        return "InsertDetialInfo{" +
                "money='" + money + '\'' +
                ", pihao='" + pihao + '\'' +
                ", partno='" + partno + '\'' +
                ", type='" + type + '\'' +
                ", detailId='" + detailId + '\'' +
                ", checkPrice=" + checkPrice +
                ", typeId='" + typeId + '\'' +
                '}';
    }

    public InsertDetialInfo(String money, String pihao, String partno, String type, String detailId, Double checkPrice, String typeId) {
        this.money = money;
        this.pihao = pihao;
        this.partno = partno;
        this.type = type;
        this.detailId = detailId;
        this.checkPrice = checkPrice;
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Double getCheckPrice() {
        return checkPrice;
    }

    public void setCheckPrice(Double checkPrice) {
        this.checkPrice = checkPrice;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    public String getPartno() {
        return partno;
    }

    public void setPartno(String partno) {
        this.partno = partno;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }
}
