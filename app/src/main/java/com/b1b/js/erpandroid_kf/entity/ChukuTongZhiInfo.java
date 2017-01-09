package com.b1b.js.erpandroid_kf.entity;

import com.b1b.js.erpandroid_kf.utils.T;

/**
 * Created by js on 2016/12/28.
 */

public class ChukuTongZhiInfo extends T {
    private String pid;
    private String pDate;
    private String company;
    private String deptName;
    private String uName;
    private String byName;
    private String pType;
    private String state;
    private String fhType;
    private String goodNo;
    private String counts;
    private String inPrice;
    private String outPrice;
    private String basicPrice;
    private String sellCounts;
    private String factory;
    private String fengzhuang;
    private String description;


    //            "PID": "1112898"
//            "制单日期": "2016/12/1 10:02:23"
//            "公司": "总公司采购部"
//            "部门": "北京采购部"
//            "员工": "苏海玲"
//            "制单人": "苏海玲"
//            "单据类型": "内部销售"
//            "单据状态": "已出库完成"
//            "发货类型": "库房发货"
//            "型号": "GRM32ER71H106KA12L"
//            "数量": "5000"
//            "进价": "0.5299"
//            "售价": "0.5299"
//            "成本": "2649.5000"
//            "销售额": "2649.5000"
//            "厂家": "murata"
//            "封装": "1210"
//            "描述": "10uF_±10%_50V_X7R_1210"
    @Override
    public String toString() {
        return "单据号='" + pid + '\'' +
                "\n制单日期='" + pDate + '\'' +
                "\n公司='" + company + '\'' +
                "\n部门='" + deptName + '\'' +
                "\n员工='" + uName + '\'' +
                "\n制单人='" + byName + '\'' +
                "\n单据类型='" + pType + '\'' +
                "\n单据状态='" + state + '\'' +
                "\n发货类型='" + fhType + '\'' +
                "\n型号='" + goodNo + '\'' +
                "\n数量='" + counts + '\'' +
                "\n进价='" + inPrice + '\'' +
                "\n售价='" + outPrice + '\'' +
                "\n成本='" + basicPrice + '\'' +
                "\n销售额='" + sellCounts + '\'' +
                "\n厂家='" + factory + '\'' +
                "\n封装='" + fengzhuang + '\'' +
                "\n描述='" + description + '\'';
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getpDate() {
        return pDate;
    }

    public void setpDate(String pDate) {
        this.pDate = pDate;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
    }

    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFhType() {
        return fhType;
    }

    public void setFhType(String fhType) {
        this.fhType = fhType;
    }

    public String getGoodNo() {
        return goodNo;
    }

    public void setGoodNo(String goodNo) {
        this.goodNo = goodNo;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public String getInPrice() {
        return inPrice;
    }

    public void setInPrice(String inPrice) {
        this.inPrice = inPrice;
    }

    public String getOutPrice() {
        return outPrice;
    }

    public void setOutPrice(String outPrice) {
        this.outPrice = outPrice;
    }

    public String getBasicPrice() {
        return basicPrice;
    }

    public void setBasicPrice(String basicPrice) {
        this.basicPrice = basicPrice;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
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

    public String getSellCounts() {
        return sellCounts;
    }

    public void setSellCounts(String sellCounts) {
        this.sellCounts = sellCounts;
    }
}
