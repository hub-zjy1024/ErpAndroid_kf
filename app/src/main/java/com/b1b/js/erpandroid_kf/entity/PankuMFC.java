package com.b1b.js.erpandroid_kf.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by 张建宇 on 2020/4/10.
 */
public class PankuMFC implements Serializable {
    /**
     * 品牌id
     */
    private String ID;
    /**
     * 品牌简称
     */
    private String Name;
    /**
     * 品牌全名
     */
    private String FullName;
    private String CHIName;
    private String Note;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getCHIName() {
        return CHIName;
    }

    public void setCHIName(String CHIName) {
        this.CHIName = CHIName;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    @NonNull
    @Override
    public String toString() {
        return FullName;
    }

    //    "ID": "1361",
    //            "Name": " Excelitas",
    //            "FullName": "Excelitas",
    //            "CHIName": "",
    //            "URL": "",
    //            "Note": "",
    //            "IsDL": "",
    //            "DLManage": "",
    //            "DLManage_C": "",
    //            "DLCorp": "",
    //            "DLProviderID": "",
    //            "DLProviderName": ""
}
