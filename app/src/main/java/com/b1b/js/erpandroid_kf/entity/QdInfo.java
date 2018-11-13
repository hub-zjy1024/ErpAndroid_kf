package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2018/11/5.
 */
public class QdInfo {
    public QdInfo(String tvDate, String tvProId, String tvProName, String tvKpName, String tvCounts, String
            tvNote) {
        this.tvDate = tvDate;
        this.tvProId = tvProId;
        this.tvProName = tvProName;
        this.tvKpName = tvKpName;
        this.tvCounts = tvCounts;
        this.tvNote = tvNote;
    }

    public QdInfo() {

    }

    private String tvDate;
    private String tvProId;
    private String tvProName;
    private String tvKpName;
    private String tvCounts;
    private String tvNote;

    public String getTvDate() {
        return tvDate;
    }

    public void setTvDate(String tvDate) {
        this.tvDate = tvDate;
    }

    public String getTvProId() {
        return tvProId;
    }

    public void setTvProId(String tvProId) {
        this.tvProId = tvProId;
    }

    public String getTvProName() {
        return tvProName;
    }

    public void setTvProName(String tvProName) {
        this.tvProName = tvProName;
    }

    public String getTvKpName() {
        return tvKpName;
    }

    public void setTvKpName(String tvKpName) {
        this.tvKpName = tvKpName;
    }

    public String getTvCounts() {
        return tvCounts;
    }

    public void setTvCounts(String tvCounts) {
        this.tvCounts = tvCounts;
    }

    public String getTvNote() {
        return tvNote;
    }

    public void setTvNote(String tvNote) {
        this.tvNote = tvNote;
    }
}
