package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/5/16. */

public class KucunFBInfo extends BaseFileds {

    private String pid;
    private String detailID;
    private boolean isFabu;
    private String fabuPrice;
    private String limitedPrice;
    private String inPrice;
    private String dollar;
    private String leftCount;
    private String totalInMoney;
    private String deptID;
    private String storageName;
    private String rukuID;
    private String rukuDate;
    private String place;
    private String forcePrice;
    private String pidType;
    private String kaipiaoType;
    private String kaipiaoCompany;
    private String xiadanCompany;
    private String notes;
    private String pankuFlag;
    private String pankuDate;
    private String prePartNo;
    private String preCounts;
    private String pankuPartNo;
    private String pankuCounts;
    private String pankuFactory;
    private String pankuDescription;
    private String pankuPack;
    private String minPack;
    private String operID;
    private String operName;
    private String published;
    private String updateTime;
    private String isBeihuo;

    @Override
    public String toString() {
        return
                "单据号=" + pid + "\n" +
                        "型号=" + prePartNo + "\n" +
                        "是否发布=" + isFabu + "\n" +
                        "发布价格=" + fabuPrice + "\n" +
                        "限价=" + limitedPrice + "\n" +
                        "进价=" + inPrice + "\n" +
                        "美金=" + dollar + "\n" +
                        super.toString() +
                        "剩余数量=" + leftCount + "\n" +
                        "金额=" + totalInMoney + "\n" +
                        "部门号=" + deptID + "\n" +
                        "仓库=" + storageName + "\n" +
                        "强限价=" + forcePrice + "\n" +
                        "备注=" + notes + "\n" +
                        "pankuFlag=" + pankuFlag + "\n" +
                        "发布时间=" + published + "\n" +
                        "是备货部门=" + isBeihuo + "\n" +
                        "更新时间=" + updateTime + "\n"
                ;
    }

    public String toString2() {
        return
                "单据号=" + pid + "\n" +
                        "明细ID=" + detailID + "\n" +
                        "是否发布=" + isFabu + "\n" +
                        "发布价格=" + fabuPrice + "\n" +
                        "限价=" + limitedPrice + "\n" +
                        "进价=" + inPrice + "\n" +
                        "美金=" + dollar + "\n" +
                        super.toString() +
                        "剩余数量=" + leftCount + "\n" +
                        "金额=" + totalInMoney + "\n" +
                        "部门号=" + deptID + "\n" +
                        "仓库=" + storageName + "\n" +
                        "入库编号=" + rukuID + "\n" +
                        "入库日期=" + rukuDate + "\n" +
                        "位置=" + place + "\n" +
                        "强限价=" + forcePrice + "\n" +
                        "单据类型=" + pidType + "\n" +
                        "开票类型=" + kaipiaoType + "\n" +
                        "开票公司=" + kaipiaoCompany + "\n" +
                        "下单公司=" + xiadanCompany + "\n" +
                        "备注=" + notes + "\n" +
                        "pankuFlag=" + pankuFlag + "\n" +
                        "盘库日期=" + pankuDate + "\n" +
                        "盘库前型号=" + prePartNo + "\n" +
                        "盘库前数量=" + preCounts + "\n" +
                        "盘库型号=" + pankuPartNo + "\n" +
                        "盘库数量=" + pankuCounts + "\n" +
                        "盘库厂家=" + pankuFactory + "\n" +
                        "盘库描述=" + pankuDescription + "\n" +
                        "PKPack=" + pankuPack + "\n" +
                        "MinPack=" + minPack + "\n" +
                        "操作人ID=" + operID + "\n" +
                        "操作人=" + operName + "\n" +
                        "发布时间=" + published + "\n" +
                        "是备货部门=" + isBeihuo + "\n" +
                        "更新时间=" + updateTime + "\n"
                ;
    }

    public KucunFBInfo() {
    }

    public KucunFBInfo(String partNo, String factory, String pihao, String fengzhuang, String description, String pid, String
            detailID, boolean isFabu, String fabuPrice, String limitedPrice, String inPrice, String dollar, String leftCount,
                       String totalInMoney, String deptID, String storageName, String rukuID, String rukuDate, String place,
                       String forcePrice, String pidType, String kaipiaoType, String kaipiaoCompany, String xiadanCompany,
                       String notes, String pankuFlag, String pankuDate, String prePartNo, String preCounts, String
                               pankuPartNo, String pankuCounts, String pankuFactory, String pankuDescription, String pankuPack,
                       String minPack, String operID, String operName, String published, String updateTime) {
        super(partNo, factory, pihao, fengzhuang, description);
        this.pid = pid;
        this.detailID = detailID;
        this.isFabu = isFabu;
        this.fabuPrice = fabuPrice;
        this.limitedPrice = limitedPrice;
        this.inPrice = inPrice;
        this.dollar = dollar;
        this.leftCount = leftCount;
        this.totalInMoney = totalInMoney;
        this.deptID = deptID;
        this.storageName = storageName;
        this.rukuID = rukuID;
        this.rukuDate = rukuDate;
        this.place = place;
        this.forcePrice = forcePrice;
        this.pidType = pidType;
        this.kaipiaoType = kaipiaoType;
        this.kaipiaoCompany = kaipiaoCompany;
        this.xiadanCompany = xiadanCompany;
        this.notes = notes;
        this.pankuFlag = pankuFlag;
        this.pankuDate = pankuDate;
        this.prePartNo = prePartNo;
        this.preCounts = preCounts;
        this.pankuPartNo = pankuPartNo;
        this.pankuCounts = pankuCounts;
        this.pankuFactory = pankuFactory;
        this.pankuDescription = pankuDescription;
        this.pankuPack = pankuPack;
        this.minPack = minPack;
        this.operID = operID;
        this.operName = operName;
        this.published = published;
        this.updateTime = updateTime;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDetailID() {
        return detailID;
    }

    public void setDetailID(String detailID) {
        this.detailID = detailID;
    }

    public boolean isFabu() {
        return isFabu;
    }

    public void setFabu(boolean fabu) {
        isFabu = fabu;
    }

    public String getFabuPrice() {
        return fabuPrice;
    }

    public void setFabuPrice(String fabuPrice) {
        this.fabuPrice = fabuPrice;
    }

    public String getLimitedPrice() {
        return limitedPrice;
    }

    public void setLimitedPrice(String limitedPrice) {
        this.limitedPrice = limitedPrice;
    }

    public String getInPrice() {
        return inPrice;
    }

    public void setInPrice(String inPrice) {
        this.inPrice = inPrice;
    }

    public String getDollar() {
        return dollar;
    }

    public void setDollar(String dollar) {
        this.dollar = dollar;
    }

    public String getLeftCount() {
        return leftCount;
    }

    public void setLeftCount(String leftCount) {
        this.leftCount = leftCount;
    }

    public String getTotalInMoney() {
        return totalInMoney;
    }

    public void setTotalInMoney(String totalInMoney) {
        this.totalInMoney = totalInMoney;
    }

    public String getDeptID() {
        return deptID;
    }

    public void setDeptID(String deptID) {
        this.deptID = deptID;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getRukuID() {
        return rukuID;
    }

    public void setRukuID(String rukuID) {
        this.rukuID = rukuID;
    }

    public String getRukuDate() {
        return rukuDate;
    }

    public void setRukuDate(String rukuDate) {
        this.rukuDate = rukuDate;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getForcePrice() {
        return forcePrice;
    }

    public void setForcePrice(String forcePrice) {
        this.forcePrice = forcePrice;
    }

    public String getPidType() {
        return pidType;
    }

    public void setPidType(String pidType) {
        this.pidType = pidType;
    }

    public String getKaipiaoType() {
        return kaipiaoType;
    }

    public void setKaipiaoType(String kaipiaoType) {
        this.kaipiaoType = kaipiaoType;
    }

    public String getKaipiaoCompany() {
        return kaipiaoCompany;
    }

    public void setKaipiaoCompany(String kaipiaoCompany) {
        this.kaipiaoCompany = kaipiaoCompany;
    }

    public String getXiadanCompany() {
        return xiadanCompany;
    }

    public void setXiadanCompany(String xiadanCompany) {
        this.xiadanCompany = xiadanCompany;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPankuFlag() {
        return pankuFlag;
    }

    public void setPankuFlag(String pankuFlag) {
        this.pankuFlag = pankuFlag;
    }

    public String getPankuDate() {
        return pankuDate;
    }

    public void setPankuDate(String pankuDate) {
        this.pankuDate = pankuDate;
    }

    public String getPrePartNo() {
        return prePartNo;
    }

    public void setPrePartNo(String prePartNo) {
        this.prePartNo = prePartNo;
    }

    public String getPreCounts() {
        return preCounts;
    }

    public void setPreCounts(String preCounts) {
        this.preCounts = preCounts;
    }

    public String getPankuPartNo() {
        return pankuPartNo;
    }

    public void setPankuPartNo(String pankuPartNo) {
        this.pankuPartNo = pankuPartNo;
    }

    public String getPankuCounts() {
        return pankuCounts;
    }

    public void setPankuCounts(String pankuCounts) {
        this.pankuCounts = pankuCounts;
    }

    public String getPankuFactory() {
        return pankuFactory;
    }

    public void setPankuFactory(String pankuFactory) {
        this.pankuFactory = pankuFactory;
    }

    public String getPankuDescription() {
        return pankuDescription;
    }

    public void setPankuDescription(String pankuDescription) {
        this.pankuDescription = pankuDescription;
    }

    public String getPankuPack() {
        return pankuPack;
    }

    public void setPankuPack(String pankuPack) {
        this.pankuPack = pankuPack;
    }

    public String getOperID() {
        return operID;
    }

    public void setOperID(String operID) {
        this.operID = operID;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getMinPack() {
        return minPack;
    }

    public void setMinPack(String minPack) {
        this.minPack = minPack;
    }

    public String getIsBeihuo() {
        return isBeihuo;
    }

    public void setIsBeihuo(String isBeihuo) {
        this.isBeihuo = isBeihuo;
    }
}
