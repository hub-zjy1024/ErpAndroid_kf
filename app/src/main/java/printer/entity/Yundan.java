package printer.entity;

/**
 * Created by 张建宇 on 2017/6/26.
 */

public class Yundan {
    private String pid;
    private String createDate;
    private String state;
    private String deptID;
    private String saleMan;
    private String storageName;
    private String customer;
    private String recieveBackNo;
    private String print;
    private String shouHuiDan;
    private String partNo;
    private String counts;
    private String pihao;
    private String type;

    @Override
    public String toString() {
        return
                "单据号='" + pid + "\n" +
                        "制单日期='" + createDate + "'\n" +
                        "单据状态='" + state +  "'\n" +
                        "部门号='" + deptID +  "'\n" +
                        "销售员='" + saleMan +  "'\n" +
                        "仓库名='" + storageName + "'\n" +
                        "客户='" + customer +  "'\n" +
                        "回执号='" + recieveBackNo +  "'\n" +
                        "打印次数='" + print +  "'\n" +
                        "收回单='" + shouHuiDan +  "'\n" +
                        "型号='" + partNo + "'\n" +
                        "数量='" + counts +  "'\n" +
                        "批号='" + pihao
                ;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDeptID() {
        return deptID;
    }

    public void setDeptID(String deptID) {
        this.deptID = deptID;
    }

    public String getSaleMan() {
        return saleMan;
    }

    public void setSaleMan(String saleMan) {
        this.saleMan = saleMan;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getRecieveBackNo() {
        return recieveBackNo;
    }

    public void setRecieveBackNo(String recieveBackNo) {
        this.recieveBackNo = recieveBackNo;
    }

    public String getPrint() {
        return print;
    }

    public void setPrint(String print) {
        this.print = print;
    }

    public String getShouHuiDan() {
        return shouHuiDan;
    }

    public void setShouHuiDan(String shouHuiDan) {
        this.shouHuiDan = shouHuiDan;
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

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
