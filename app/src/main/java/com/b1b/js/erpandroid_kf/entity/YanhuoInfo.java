package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2017/8/25.
 */

public class YanhuoInfo {
//    {
//        "PID":"830257", "采购地":"深圳市场", "制单日期":"2017/8/25 9:55:15", "公司":"美商利华分公司", "部门":
//        "北京美商利华国际", "业务员":"谭晓燕", "单据状态":"等待验货", "收款":"现款现货", "客户开票":"普通发票", "供应商开票":
//        "普通发票", "供应商":"深圳市同亨微科技有限公司", "采购员":"郭峰建", "询价员":"郭峰建"
//    }

    @Override
    public String toString() {
        return
                "单号='" + pid +"'\n"+
                "采购地='" + caigouAddress+"'\n"+
                "制单日期='" + pidDate +"'\n"+
                "公司='" + company+"'\n"+
                "部门='" + deptName+"'\n"+
                "业务员='" + saleMan+"'\n"+
                "单据状态='" + pidState+"'\n"+
                "收款='" + payType+"'\n"+
                "客户开票='" + userFapiao+"'\n"+
                "供应商开票='" + companyFapiao+"'\n"+
                "供应商='" + providerName+"'\n"+
                "采购员='" + caigouMan+"'\n"+
                "询价员='" + askPriceBy +"'\n";
    }

    public YanhuoInfo() {
    }

    public YanhuoInfo(String pid, String caigouAddress, String pidDate, String company,
                      String deptName, String saleMan, String pidState, String payType,
                      String userFapiao, String companyFapiao, String providerName,
                      String caigouMan, String askPriceBy) {
        this.pid = pid;
        this.caigouAddress = caigouAddress;
        this.pidDate = pidDate;
        this.company = company;
        this.deptName = deptName;
        this.saleMan = saleMan;
        this.pidState = pidState;
        this.payType = payType;
        this.userFapiao = userFapiao;
        this.companyFapiao = companyFapiao;
        this.providerName = providerName;
        this.caigouMan = caigouMan;
        this.askPriceBy = askPriceBy;
    }

    private String pid;
    private String caigouAddress;
    private String pidDate;
    private String company;
    private String deptName;
    private String saleMan;
    private String pidState;
    private String payType;
    private String userFapiao;
    private String companyFapiao;
    private String providerName;
    private String caigouMan;
    private String askPriceBy;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCaigouAddress() {
        return caigouAddress;
    }

    public void setCaigouAddress(String caigouAddress) {
        this.caigouAddress = caigouAddress;
    }

    public String getPidDate() {
        return pidDate;
    }

    public void setPidDate(String pidDate) {
        this.pidDate = pidDate;
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

    public String getSaleMan() {
        return saleMan;
    }

    public void setSaleMan(String saleMan) {
        this.saleMan = saleMan;
    }

    public String getPidState() {
        return pidState;
    }

    public void setPidState(String pidState) {
        this.pidState = pidState;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getUserFapiao() {
        return userFapiao;
    }

    public void setUserFapiao(String userFapiao) {
        this.userFapiao = userFapiao;
    }

    public String getCompanyFapiao() {
        return companyFapiao;
    }

    public void setCompanyFapiao(String companyFapiao) {
        this.companyFapiao = companyFapiao;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCaigouMan() {
        return caigouMan;
    }

    public void setCaigouMan(String caigouMan) {
        this.caigouMan = caigouMan;
    }

    public String getAskPriceBy() {
        return askPriceBy;
    }

    public void setAskPriceBy(String askPriceBy) {
        this.askPriceBy = askPriceBy;
    }

}
