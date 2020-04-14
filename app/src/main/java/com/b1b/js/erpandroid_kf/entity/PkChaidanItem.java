package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2020/4/1.
 */
public class PkChaidanItem {
    public String id;
    public String BatchNo;
    public int Number;

    public PkChaidanItem(String partno, int count) {
        this.BatchNo = partno;
        this.Number = count;
    }

    public PkChaidanItem() {
    }
}
