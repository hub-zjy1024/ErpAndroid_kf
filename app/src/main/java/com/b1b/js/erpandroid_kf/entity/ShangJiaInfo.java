package com.b1b.js.erpandroid_kf.entity;

import printer.entity.XiaopiaoInfo;

/**
 Created by 张建宇 on 2018/4/19. */
public class ShangJiaInfo extends XiaopiaoInfo {
    private String shangjiaID;
    private String kuqu;
    private String status;

    public ShangJiaInfo(String partNo, String topID, String time, String deptNo, String counts, String factory, String
            produceFrom, String pihao, String fengzhuang, String description, String place, String note, String flag, String
            codeStr, String storageID, String company) {
        super(partNo, topID, time, deptNo, counts, factory, produceFrom, pihao, fengzhuang, description, place, note, flag,
                codeStr, storageID, company);
    }
    public String getShangjiaID() {
        return shangjiaID;
    }

    public void setShangjiaID(String shangjiaID) {
        this.shangjiaID = shangjiaID;
    }

    public String getKuqu() {
        return kuqu;
    }

    public void setKuqu(String kuqu) {
        this.kuqu = kuqu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
