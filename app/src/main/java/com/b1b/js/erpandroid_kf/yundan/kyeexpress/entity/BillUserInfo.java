package com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity;

import java.io.Serializable;

/**
 * Created by 张建宇 on 2019/6/10.
 */
public class BillUserInfo implements Serializable {
    private String companyName;
    private String person;
    private String phone;
    private String mobile;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String address;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    //    	"companyName": "寄件公司001",
//                "person": "张三",
//                "phone": "18379151111",
//                "mobile": "",
//                "provinceName": "广东省",
//                "cityName": "深圳市",
//                "countyName": "宝安区",
//                "address": "福永街道福永二路深翔物流园"

//    "preWaybillDelivery":{
//        this;
//    },
//    "preWaybillPickup":{
//        this;
//    }
//    	,"serviceMode": 20,
//                "payMode": 10,
//                "paymentCustomer": "75568655228",
//                "goodsType": "托寄物",
//                "count": 1,
//                "orderId": "ZY0000001",
//                "receiptFlag": 20,
//                "receiptCount": 13,
//                "waybillRemark": "运单备注00001"
}
