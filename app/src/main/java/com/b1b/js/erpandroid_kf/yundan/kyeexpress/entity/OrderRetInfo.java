package com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity;

import java.io.Serializable;

/**
 * Created by 张建宇 on 2019/6/14.
 */
public class OrderRetInfo implements Serializable {
    public String waybillNumber;
    public String areaCode;

    public String getWaybillNumber() {
        return waybillNumber;
    }

    public void setWaybillNumber(String waybillNumber) {
        this.waybillNumber = waybillNumber;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}
