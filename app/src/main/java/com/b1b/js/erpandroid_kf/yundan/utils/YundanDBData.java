package com.b1b.js.erpandroid_kf.yundan.utils;

/**
 * Created by 张建宇 on 2019/1/24.
 */
public class YundanDBData {
    private String pid;
    private String jName;
    private String jTel;
    private String jAddress;
    private String jComapany;
    private String payByWho;
    private String pidNotes;
    private String dAddress;
    private String dTel;
    private String dName;
    private String dCompany;
    private String corpID;

    public String getCorpID() {
        return corpID;
    }

    public void setCorpID(String corpID) {
        this.corpID = corpID;
    }

    public String getStorageID() {
        return storageID;
    }

    public void setStorageID(String storageID) {
        this.storageID = storageID;
    }

    private String storageID;

    public YundanDBData() {
    }


    public YundanDBData(String jName, String jTel, String jAddress, String jComapany, String payByWho,
                        String pidNotes, String dAddress, String dTel, String dName, String dCompany) {
        this.jName = jName;
        this.jTel = jTel;
        this.jAddress = jAddress;
        this.jComapany = jComapany;
        this.payByWho = payByWho;
        this.pidNotes = pidNotes;
        this.dAddress = dAddress;
        this.dTel = dTel;
        this.dName = dName;
        this.dCompany = dCompany;
    }

    public String getjName() {
        return jName;
    }

    public void setjName(String jName) {
        this.jName = jName;
    }

    public String getjTel() {
        return jTel;
    }

    public void setjTel(String jTel) {
        this.jTel = jTel;
    }

    public String getjAddress() {
        return jAddress;
    }

    public void setjAddress(String jAddress) {
        this.jAddress = jAddress;
    }

    public String getjComapany() {
        return jComapany;
    }

    public void setjComapany(String jComapany) {
        this.jComapany = jComapany;
    }

    public String getPayByWho() {
        return payByWho;
    }

    public void setPayByWho(String payByWho) {
        this.payByWho = payByWho;
    }

    public String getPidNotes() {
        return pidNotes;
    }

    public void setPidNotes(String pidNotes) {
        this.pidNotes = pidNotes;
    }

    public String getdAddress() {
        return dAddress;
    }

    public void setdAddress(String dAddress) {
        this.dAddress = dAddress;
    }

    public String getdTel() {
        return dTel;
    }

    public void setdTel(String dTel) {
        this.dTel = dTel;
    }

    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
    }

    public String getdCompany() {
        return dCompany;
    }

    public void setdCompany(String dCompany) {
        this.dCompany = dCompany;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
    //    jName = obj.getString("业务员");
//    jTel = obj.getString("寄件电话");
//    jAddress = obj.getString("寄件地址1");
//    jComapany = obj.getString("寄件公司");
//    payByWho = obj.getString("谁付运费");
//    pidNotes = obj.getString("Note");
//    dAddress = obj.getString("收件地址");
//    dTel = obj.getString("收件电话");
//    dName = obj.getString("收件人");
//    dCompany = obj.getString("收件公司");
}
