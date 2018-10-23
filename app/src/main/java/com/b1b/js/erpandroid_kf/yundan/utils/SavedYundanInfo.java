package com.b1b.js.erpandroid_kf.yundan.utils;

/**
 * Created by 张建宇 on 2019/1/24.
 */
public class SavedYundanInfo {

    private String orderID  ;
    private String destcode  ;
    private String exName  ;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public SavedYundanInfo() {
    }

    public SavedYundanInfo(String orderID, String destcode, String exName) {
        this.orderID = orderID;
        this.destcode = destcode;
        this.exName = exName;
    }

    public String getDestcode() {
        return destcode;
    }

    public void setDestcode(String destcode) {
        this.destcode = destcode;
    }

    public String getExName() {
        return exName;
    }

    public void setExName(String exName) {
        this.exName = exName;
    }
    //    String orderID  t.getString("objvalue");
//    String destcode  t.getString("objexpress");
//    final String exName  t.getString("objtype");
}
