package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by js on 2016/12/27.
 */

public class KaoqinInfo{
    //    "EmployeeID": "100",
//            "员工": "朱强",
//            "考勤年月": "20161101",
//            "考勤状态": "迟到早退",
//            "上班时间": "10:41:01",
//            "下班时间": "10:41:01",
//            "早IP": "172.16.1.102",
//            "晚IP": "172.16.1.102"
    private String empId;
    private String empName;
    private String date;
    private String state;
    private String startIp;
    private String endIp;
    private String startTime;
    private String endTime;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStartIp() {
        return startIp;
    }

    public void setStartIp(String startIp) {
        this.startIp = startIp;
    }

    public String getEndIp() {
        return endIp;
    }

    public void setEndIp(String endIp) {
        this.endIp = endIp;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public KaoqinInfo(String empId, String empName, String date, String state, String startIp, String endIp, String startTime, String endTime) {
        this.empId = empId;
        this.empName = empName;
        this.date = date;
        this.state = state;
        this.startIp = startIp;
        this.endIp = endIp;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public KaoqinInfo() {
    }
}
