package com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity;

import java.io.Serializable;

/**
 * Created by 张建宇 on 2019/6/10.
 */
public class BillOrder implements Serializable {
    private BillUserInfo preWaybillDelivery;
    private BillUserInfo preWaybillPickup;
    /**
     * 旧：
     *  serviceMode=10： 当天达,
     serviceMode=20： 表示次日达,
     ，serviceMode=30： 表示隔日达
     ，serviceMode=40： 表示陆运件
     ，serviceMode=50： 表示同城次日
     ，serviceMode=70： 表示同城即日
     ，serviceMode=160： 表示省内次日
     ，serviceMode=170： 表示省内即日
     <br/>
     新：
     ，serviceMode=60： 表示次晨达
     ，serviceMode=80： 表示航空件
     ，serviceMode=90： 表示早班件
     ，serviceMode=100： 表示中班件
     ，serviceMode=110： 表示晚班件
     ，serviceMode=210：表示空运 ，
     serviceMode=220： 表示专运 （传代码）
     */
  private int serviceMode;
    /**
     * 10-寄方付 20-收方付 30-第三方付
     */
    private int payMode;
    private String paymentCustomer;
    /**
     * 托寄物
     */
    private String goodsType;
    private int count;
    private String orderId;
    /**
     * 有无回单	number	必须 10-有，20-无
     */
    private int receiptFlag;
//    private int receiptCount;
    /**
     * 备注
     */
    private String waybillRemark;

    public BillUserInfo getPreWaybillDelivery() {
        return preWaybillDelivery;
    }

    public void setPreWaybillDelivery(BillUserInfo preWaybillDelivery) {
        this.preWaybillDelivery = preWaybillDelivery;
    }

    public BillUserInfo getPreWaybillPickup() {
        return preWaybillPickup;
    }

    public void setPreWaybillPickup(BillUserInfo preWaybillPickup) {
        this.preWaybillPickup = preWaybillPickup;
    }

    public int getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(int serviceMode) {
        this.serviceMode = serviceMode;
    }

    public int getPayMode() {
        return payMode;
    }

    public void setPayMode(int payMode) {
        this.payMode = payMode;
    }

    public String getPaymentCustomer() {
        return paymentCustomer;
    }

    public void setPaymentCustomer(String paymentCustomer) {
        this.paymentCustomer = paymentCustomer;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getReceiptFlag() {
        return receiptFlag;
    }

    public void setReceiptFlag(int receiptFlag) {
        this.receiptFlag = receiptFlag;
    }

    public String getWaybillRemark() {
        return waybillRemark;
    }

    public void setWaybillRemark(String waybillRemark) {
        this.waybillRemark = waybillRemark;
    }
}
