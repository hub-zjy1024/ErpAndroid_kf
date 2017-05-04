package com.b1b.js.erpandroid_kf.entity;

import java.util.List;

/**
 Created by 张建宇 on 2017/5/2. */

public class PreChukuInfo {
    //        "PID": "966250",
    //            "制单日期": "2016/6/3 10:55:25",
    //            "出库类型": "正常销售",
    //            "委托人": "管理员",
    //            "仓库": "110",
    //            "发货库区": "主库区",
    //            "调入仓库": "",
    //            "打印次数": "I",
    //            "型号": "WWWWW",
    //            "数量": "1",
    //            "批号": "1",
    //            "厂家": "1",
    //    "位置": "12310A000400",
    //            "库区": "0",
    //            "发货类型": "库房发货",
    //            "委托公司": "89"
    //

    @Override
    public String toString() {
        return
                "单据ID=" + pid + "\n" +
                        "制单日期=" + createDate + "\n" +
                        "出库类型=" + chukuType + "\n" +
                        "委托人=" + weituo + "\n" +
                        "仓库=" + storageID + "\n" +
                        "发货库区=" + fahuoPart + "\n" +
                        "调入仓库=" + diaoruKf + "\n" +
                        "打印次数=" + printCounts + "\n" +
                        "型号=" + partNo + "\n" +
                        "数量=" + couts + "\n" +
                        "批号=" + pihao + "\n" +
                        "厂家=" + factory + "\n" +
                        "位置=" + placedID + "\n" +
                        "库区=" + kuqu + "\n" +
                        "发货类型=" + fahuoType + "\n" +
                        "委托公司=" + weituoCompanyID
                ;
    }

    private String pid;
    private String createDate;
    private String chukuType;
    private String weituo;
    private String storageID;
    private String fahuoPart;
    private String diaoruKf;
    private String printCounts;
    private String partNo;
    private String couts;
    private String pihao;
    private String factory;
    private String placedID;
    private String kuqu;
    private String fahuoType;
    private String weituoCompanyID;

    private String deptID;
    private String client;
    private String pactID;
    private String outType;
    private String salesman;
    private String employeeID;
    private String mainNotes;

    public String getDeptID() {
        return deptID;
    }

    public void setDeptID(String deptID) {
        this.deptID = deptID;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getPactID() {
        return pactID;
    }

    public void setPactID(String pactID) {
        this.pactID = pactID;
    }

    public String getOutType() {
        return outType;
    }

    public void setOutType(String outType) {
        this.outType = outType;
    }

    public String getSalesman() {
        return salesman;
    }

    public void setSalesman(String salesman) {
        this.salesman = salesman;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    private List<PreChukuDetailInfo> detailInfos;


    public PreChukuInfo() {
    }

    public PreChukuInfo(String pid, String createDate, String chukuType, String weituo, String storageID, String fahuoPart, String diaoruKf, String printCounts, String partNo, String couts, String pihao, String factory, String placedID, String kuqu, String fahuoType, String weituoCompanyID) {
        this.pid = pid;
        this.createDate = createDate;
        this.chukuType = chukuType;
        this.weituo = weituo;
        this.storageID = storageID;
        this.fahuoPart = fahuoPart;
        this.diaoruKf = diaoruKf;
        this.printCounts = printCounts;
        this.partNo = partNo;
        this.couts = couts;
        this.pihao = pihao;
        this.factory = factory;
        this.placedID = placedID;
        this.kuqu = kuqu;
        this.fahuoType = fahuoType;
        this.weituoCompanyID = weituoCompanyID;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List<PreChukuDetailInfo> getDetailInfos() {
        return detailInfos;
    }

    public void setDetailInfos(List<PreChukuDetailInfo> detailInfos) {
        this.detailInfos = detailInfos;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getChukuType() {
        return chukuType;
    }

    public void setChukuType(String chukuType) {
        this.chukuType = chukuType;
    }

    public String getWeituo() {
        return weituo;
    }

    public void setWeituo(String weituo) {
        this.weituo = weituo;
    }

    public String getStorageID() {
        return storageID;
    }

    public void setStorageID(String storageID) {
        this.storageID = storageID;
    }

    public String getFahuoPart() {
        return fahuoPart;
    }

    public void setFahuoPart(String fahuoPart) {
        this.fahuoPart = fahuoPart;
    }

    public String getDiaoruKf() {
        return diaoruKf;
    }

    public void setDiaoruKf(String diaoruKf) {
        this.diaoruKf = diaoruKf;
    }

    public String getPrintCounts() {
        return printCounts;
    }

    public void setPrintCounts(String printCounts) {
        this.printCounts = printCounts;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getCouts() {
        return couts;
    }

    public void setCouts(String couts) {
        this.couts = couts;
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

    public String getPlacedID() {
        return placedID;
    }

    public void setPlacedID(String placedID) {
        this.placedID = placedID;
    }

    public String getKuqu() {
        return kuqu;
    }

    public void setKuqu(String kuqu) {
        this.kuqu = kuqu;
    }

    public String getFahuoType() {
        return fahuoType;
    }

    public void setFahuoType(String fahuoType) {
        this.fahuoType = fahuoType;
    }

    public String getWeituoCompanyID() {
        return weituoCompanyID;
    }

    public void setWeituoCompanyID(String weituoCompanyID) {
        this.weituoCompanyID = weituoCompanyID;
    }

    public String getMainNotes() {
        return mainNotes;
    }

    public void setMainNotes(String mainNotes) {
        this.mainNotes = mainNotes;
    }
}
