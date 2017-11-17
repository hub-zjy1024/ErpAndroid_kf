package printer.entity;

/**
 Created by 张建宇 on 2017/10/31. */

public class XiaopiaoInfo {
    private String partNo;
    private String topID;
    private String time;
    private String deptNo;
    private String counts;
    private String factory;
    private String produceFrom;
    private String pihao;
    private String fengzhuang;
    private String description;
    private String place;
    private String note;
    private String flag;
    private String codeStr;
    private String storageID;
    private String company;
    private String pid;

    public XiaopiaoInfo(String partNo, String topID, String time, String deptNo, String counts, String factory, String
            produceFrom, String pihao, String fengzhuang, String description, String place, String note, String flag, String
            codeStr, String storageID, String company) {
        this.partNo = partNo;
        this.topID = topID;
        this.time = time;
        this.deptNo = deptNo;
        this.counts = counts;
        this.factory = factory;
        this.produceFrom = produceFrom;
        this.pihao = pihao;
        this.fengzhuang = fengzhuang;
        this.description = description;
        this.place = place;
        this.note = note;
        this.flag = flag;
        this.codeStr = codeStr;
        this.storageID = storageID;
        this.company = company;
    }

    public XiaopiaoInfo(String partNo, String topID, String time, String deptNo, String counts, String factory, String
            produceFrom, String pihao, String fengzhuang, String description, String place, String note, String flag, String
            codeStr, String storageID) {
        this.partNo = partNo;
        this.topID = topID;
        this.time = time;
        this.deptNo = deptNo;
        this.counts = counts;
        this.factory = factory;
        this.produceFrom = produceFrom;
        this.pihao = pihao;
        this.fengzhuang = fengzhuang;
        this.description = description;
        this.place = place;
        this.note = note;
        this.flag = flag;
        this.codeStr = codeStr;
        this.storageID = storageID;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getTopID() {
        return topID;
    }

    public void setTopID(String topID) {
        this.topID = topID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getProduceFrom() {
        return produceFrom;
    }

    public void setProduceFrom(String produceFrom) {
        this.produceFrom = produceFrom;
    }

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCodeStr() {
        return codeStr;
    }

    public void setCodeStr(String codeStr) {
        this.codeStr = codeStr;
    }

    public String getBelowCode() {
        return storageID;
    }

    public void setBelowCode(String belowCode) {
        this.storageID = belowCode;
    }

    @Override
    public String toString() {
        return "单据号='" + pid + '\'' + "\n" +
                "型号='" + partNo + '\'' + "\n" +
                "topID='" + topID + '\'' + "\n" +
                "制单日期='" + time + '\'' + "\n" +
                "部门号='" + deptNo + '\'' + "\n" +
                "数量='" + counts + '\'' + "\n" +
                "厂家='" + factory + '\'' + "\n" +
                "产地='" + produceFrom + '\'' + "\n" +
                "批号='" + pihao + '\'' + "\n" +
                "封装='" + fengzhuang + '\'' + "\n" +
                "描述='" + description + '\'' + "\n" +
                "位置='" + place + '\'' + "\n" +
                "备注='" + note + '\'' + "\n" +
                "表示='" + flag + '\'' + "\n" +
                "明细ID='" + codeStr + '\'' + "\n" +
                "库房ID='" + storageID + '\'' + "\n" +
                "开票公司='" + company + '\'' + "\n" ;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
