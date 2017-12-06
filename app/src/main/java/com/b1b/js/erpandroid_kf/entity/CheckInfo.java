package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by js on 2017/1/3.
 */

public class CheckInfo {
    //    "PID": "1118648",
//            "制单日期": "2017.01.03",
//            "单据类型": "正常销售",
//            "单据状态": "已出库,完成",
//            "公司": "北方科讯分公司",
//            "部门": "北京NORTH",
//            "员工": "田珂",
//            "型号": "LM2575HVT-ADJ/NOPB",
//            "数量": "200",
//            "出库库房": "深圳赛格",
//            "客户": "Saint  Nation  Co.  Ltd",
//            "发货类型": "库房发货",
//            "开票公司": "61",
//            "预出库打印": ""
    private String pid;
    private String pdate;
    private String ptype;
    private String pstate;
    private String company;
    private String deptName;
    private String uname;
    private String partNo;
    private String counts;
    private String outfrom;
    private String customer;
    private String fhType;
    private String kpCompany;
    private String prePrint;

    @Override
    public String toString() {
        return
                toSmallString() + "\n" +
                        "\n" + "公司=" + company +
                        "\n" + "部门=" + deptName +
                        "\n" + "发货类型=" + fhType +
                        "\n" + "开票公司=" + kpCompany +
                        "\n" + "预出库打印=" + prePrint;
    }
    public String toSmallString(){
        return "单据号=" + pid + "   " + pdate +
                "\n" + "单据类型=" + ptype + "   " + "状态=" + pstate +
                "\n" + "型号=" + partNo +
                "\n" + "数量=" + counts+ "   "+"员工=" + uname+
                "\n"+"出库库房=" + outfrom +
                "\n"+"客户=" + customer ;
    }

    public String getKpCompany() {
        return kpCompany;
    }

    public void setKpCompany(String kpCompany) {
        this.kpCompany = kpCompany;
    }

    public String getPrePrint() {
        return prePrint;
    }

    public void setPrePrint(String prePrint) {
        this.prePrint = prePrint;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getPstate() {
        return pstate;
    }

    public void setPstate(String pstate) {
        this.pstate = pstate;
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

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
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

    public String getOutfrom() {
        return outfrom;
    }

    public void setOutfrom(String outfrom) {
        this.outfrom = outfrom;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getFhType() {
        return fhType;
    }

    public void setFhType(String fhType) {
        this.fhType = fhType;
    }


}
