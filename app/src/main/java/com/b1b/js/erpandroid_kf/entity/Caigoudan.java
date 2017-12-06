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
    private boolean isForeignClient;
    private String inPrice;
    private String salePrice;
    private String note;
    private String clientID;
    private String provider;
    private String askPriceMan;
    private String caigouPlace;
    private String counts;
    private String providerID;
    private String corpID;

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public String getCorpID() {
        return corpID;
    }

    public void setCorpID(String corpID) {
        this.corpID = corpID;
    }

    @Override
    public String toString() {
        return
                " PID=" + pid + "   " + createdDate + "\n" +
                        " 型号=" + partNo + "\n" +
                        " 状态=" + state +  " 采购地=" + caigouPlace + "\n" +
                        " 数量=" + counts + "\n" +
                        " 进价=" + inPrice +  " 售价=" + salePrice + "\n" +
                        " 供应商=" + provider + "\n" +
                        " 询价员=" + askPriceMan + "   采购员=" + caigouName + "\n" +
                        " 客户编码=" + clientID  + "   是否国外=" + ((isForeignClient) ? "是" : "否")+ "\n" +
                        " 备注=" + note ;

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

    public boolean isForeignClient() {
        return isForeignClient;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public void setForeignClient(boolean foreignClient) {
        isForeignClient = foreignClient;
    }

    public String getInPrice() {
        return inPrice;
    }

    public void setInPrice(String inPrice) {
        this.inPrice = inPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAskPriceMan() {
        return askPriceMan;
    }

    public void setAskPriceMan(String askPriceMan) {
        this.askPriceMan = askPriceMan;
    }

    public String getCaigouPlace() {
        return caigouPlace;
    }

    public void setCaigouPlace(String caigouPlace) {
        this.caigouPlace = caigouPlace;
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
