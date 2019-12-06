package com.b1b.js.erpandroid_kf.entity;

import java.io.Serializable;

/**
 * Created by 张建宇 on 2019/12/3.
 */
public class RukuInfo implements Serializable {
    private String pid;
    private String detailID;
    private String partno;
    private int count;
    private String pihao;

    public RukuInfo() {
    }

    public RukuInfo(RukuInfo mInfo) {
        this.pid = mInfo.pid;
        this.detailID =mInfo. detailID;
        this.partno =mInfo. partno;
        this.count =mInfo. count;
        this.pihao =mInfo. pihao;
    }

    public RukuInfo(String pid, String detailID, String partno, int count, String pihao) {
        this.pid = pid;
        this.detailID = detailID;
        this.partno = partno;
        this.count = count;
        this.pihao = pihao;
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

    public String getPartno() {
        return partno;
    }

    public void setPartno(String partno) {
        this.partno = partno;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPihao() {
        return pihao;
    }

    public void setPihao(String pihao) {
        this.pihao = pihao;
    }

    @Override
    public String toString() {
        return
                        "明细号='" + detailID + '\'' + "\n" +
                        "型号='" + partno + '\'' + "\n" +
                        "数量=" + count  + "\n" +
                        "批号='" + pihao + '\''
                       ;
    }
}
