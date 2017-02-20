package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/9. */

public class CaigouGoodType {
    //    "StrValue": "43",
    //            "Title": "型号",
    //            "StrText": "集成电路"
    private int index;
    private String StrValue;
    private String Title;
    private String StrText;

    public CaigouGoodType(int index, String strValue, String title, String strText) {
        this.index = index;
        StrValue = strValue;
        Title = title;
        StrText = strText;
    }

    public CaigouGoodType(String strValue, String title, String strText) {
        StrValue = strValue;
        Title = title;
        StrText = strText;
    }

    public String getStrValue() {
        return StrValue;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setStrValue(String strValue) {
        StrValue = strValue;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getStrText() {
        return StrText;
    }

    public void setStrText(String strText) {
        StrText = strText;
    }
}
