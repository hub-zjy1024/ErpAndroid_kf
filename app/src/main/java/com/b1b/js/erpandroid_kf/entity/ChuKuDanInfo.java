package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by js on 2016/12/29.
 */

public class ChuKuDanInfo {
    private String pid;

    @Override
    public String toString() {
        return
                "单据号='" + pid + '\'' +
                        "\n仓库='" + repository + '\'' +
                        "\n部门='" + deptName + '\'' +
                        "\n部门号='" + deptNo + '\'' +
                        "\n出库类型='" + ckType + '\'' +
                        "\n制单日期='" + pdate + '\'' +
                        "\n总成本='" + totalBasicPrice + '\'' +
                        "\n总销售额='" + totalSellPrice + '\'' +
                        "\n毛利='" + profit + '\'' +
                        "\n业务员='" + uname + '\'' +
                        "\n客户='" + customer + '\'' +
                        "\n客户名称='" + cname + '\'' +
                        "\n合约日期='" + heyueDate + '\'' +
                        "\n开票类型='" + billingType + '\'' +
                        "\n开票公司='" + billingCompany + '\'' +
                        "\n型号='" + partNo + '\'' +
                        "\n数量='" + counts + '\'' +
                        "\n进价='" + inPrice + '\'' +
                        "\n售价='" + outPrice + '\'' +
                        "\n销售额='" + sellCounts + '\'' +
                        "\n厂家='" + factory + '\'' +
                        "\n备注='" + remarks + '\'';
    }

    private String repository;
    private String deptName;
    private String deptNo;
    private String ckType;
    private String pdate;
    private String totalBasicPrice;
    private String totalSellPrice;
    private String profit;
    private String uname;
    private String customer;
    private String cname;
    private String heyueDate;
    private String billingType;
    private String billingCompany;
    private String partNo;
    private String counts;
    private String inPrice;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getCkType() {
        return ckType;
    }

    public void setCkType(String ckType) {
        this.ckType = ckType;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getTotalBasicPrice() {
        return totalBasicPrice;
    }

    public void setTotalBasicPrice(String totalBasicPrice) {
        this.totalBasicPrice = totalBasicPrice;
    }

    public String getTotalSellPrice() {
        return totalSellPrice;
    }

    public void setTotalSellPrice(String totalSellPrice) {
        this.totalSellPrice = totalSellPrice;
    }

    public String getProfit() {
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getHeyueDate() {
        return heyueDate;
    }

    public void setHeyueDate(String heyueDate) {
        this.heyueDate = heyueDate;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getBillingCompany() {
        return billingCompany;
    }

    public void setBillingCompany(String billingCompany) {
        this.billingCompany = billingCompany;
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

    public String getSellCounts() {
        return sellCounts;
    }

    public void setSellCounts(String sellCounts) {
        this.sellCounts = sellCounts;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    private String outPrice;
    private String sellCounts;
    private String factory;
    private String remarks;

//    "PID": "1212185"
//            "仓库": "深圳赛格"
//            "部门": "北京采购部"
//            "部门号": "6102"
//            "出库类型": "内部销售"
//            "制单日期": "2016/12/1 10:11:35"
//            "总成本": "138.32"
//            "总销售额": "138.32"
//            "毛利": "0.00"
//            "业务员": "苏海玲"
//            "客户": ""
//            "客户名称": ""
//            "合约日期": "2016/11/30 16:00:56"
//            "开票类型": "增值税票"
//            "开票公司": "深圳市创新恒远供应链管理有限公司"
//            "型号": "PCF8563T/5518"
//            "数量": "100"
//            "进价": "1.3832"
//            "售价": "1.3832"
//            "销售额": "138.32"
//            "厂家": "NXP"
//            "备注": ""
}
