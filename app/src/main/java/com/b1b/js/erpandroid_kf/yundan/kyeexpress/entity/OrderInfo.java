package com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 张建宇 on 2019/6/10.
 */
public class OrderInfo implements Serializable {

    //75568655228
    private String customerCode;
    //1952A01898073D1E561B9B4F2E42CBD7
    private String platformFlag;

    private List<BillOrder> orderInfos;

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getPlatformFlag() {
        return platformFlag;
    }

    public void setPlatformFlag(String platformFlag) {
        this.platformFlag = platformFlag;
    }

    public List<BillOrder> getOrderInfos() {
        return orderInfos;
    }

    public void setOrderInfos(List<BillOrder> orderInfos) {
        this.orderInfos = orderInfos;
    }
}
