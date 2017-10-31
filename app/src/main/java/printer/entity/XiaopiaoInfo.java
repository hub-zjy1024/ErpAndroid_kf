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
    private String belowCode;

    public XiaopiaoInfo(String partNo, String topID, String time, String deptNo, String counts, String factory, String
            produceFrom, String pihao, String fengzhuang, String description, String place, String note, String flag, String
            codeStr, String belowCode) {
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
        this.belowCode = belowCode;
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
        return belowCode;
    }

    public void setBelowCode(String belowCode) {
        this.belowCode = belowCode;
    }
}
