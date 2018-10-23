package com.b1b.js.erpandroid_kf.yundan.utils;

/**
 * Created by 张建宇 on 2019/1/24.
 */
public class DHInfo {
    private String from;
    private String to;
    private String name1;
    private String phone1;
    private String address1;
    private String name2;
    private String phone2;
    private String address2;
    private String account;

    public DHInfo() {
    }

    public DHInfo(String from, String to, String name1, String phone1, String address1, String name2,
                  String phone2, String address2, String account) {
        this.from = from;
        this.to = to;
        this.name1 = name1;
        this.phone1 = phone1;
        this.address1 = address1;
        this.name2 = name2;
        this.phone2 = phone2;
        this.address2 = address2;
        this.account = account;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
