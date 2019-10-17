package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2019/7/26.
 */
public class ChukuInfoNew {
    private String StateNow;
    private String ID;
    private String PID;
    private String CreateDate;
    private String State;
    private String ApproveInfo;
    private String YunDanID;
    private String MakerName;
    private String MakerID;

    public String getStateNow() {
        return StateNow;
    }

    public void setStateNow(String stateNow) {
        StateNow = stateNow;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getApproveInfo() {
        return ApproveInfo;
    }

    public void setApproveInfo(String approveInfo) {
        ApproveInfo = approveInfo;
    }

    public String getYunDanID() {
        return YunDanID;
    }

    public void setYunDanID(String yunDanID) {
        YunDanID = yunDanID;
    }

    public String getMakerName() {
        return MakerName;
    }

    public void setMakerName(String makerName) {
        MakerName = makerName;
    }

    public String getMakerID() {
        return MakerID;
    }

    public void setMakerID(String makerID) {
        MakerID = makerID;
    }

    @Override
    public String toString() {
        return
                "StateNow='" + StateNow + '\'' + "\n" +
                        "ID='" + ID + '\'' + "\n" +
                        "PID='" + PID + '\'' + "\n" +
                        "CreateDate='" + CreateDate + '\'' + "\n" +
                        "State='" + State + '\'' + "\n" +
                        "ApproveInfo='" + ApproveInfo + '\'' + "\n" +
                        "YunDanID='" + YunDanID + '\'' + "\n" +
                        "MakerName='" + MakerName + '\'' + "\n" +
                        "MakerID='" + MakerID + '\'' + "\n" +
                        '}';
    }
}
