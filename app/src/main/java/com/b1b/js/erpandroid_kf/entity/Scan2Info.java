package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2019/7/16.
 */
public class Scan2Info {
    public boolean isChecked = false;
    public String ID;
    public String Partno;
    public String Quantity;
    public String InstorageMakerID;
    public String CWFlag;
    public String BelongCorpID;
    public String RalateBillID;
    public String PMainID;
    public String Note;
    public int ChildStorageID;
    public int InvoiceType;
    public int InvoiceCorp;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPartno() {
        return Partno;
    }

    public void setPartno(String partno) {
        Partno = partno;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getInstorageMakerID() {
        return InstorageMakerID;
    }

    public void setInstorageMakerID(String instorageMakerID) {
        InstorageMakerID = instorageMakerID;
    }

    public String getCWFlag() {
        return CWFlag;
    }

    public void setCWFlag(String CWFlag) {
        this.CWFlag = CWFlag;
    }

    public String getBelongCorpID() {
        return BelongCorpID;
    }

    public void setBelongCorpID(String belongCorpID) {
        BelongCorpID = belongCorpID;
    }

    public String getRalateBillID() {
        return RalateBillID;
    }

    public void setRalateBillID(String ralateBillID) {
        RalateBillID = ralateBillID;
    }

    public String getPMainID() {
        return PMainID;
    }

    public void setPMainID(String PMainID) {
        this.PMainID = PMainID;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public int getChildStorageID() {
        return ChildStorageID;
    }

    public void setChildStorageID(int childStorageID) {
        ChildStorageID = childStorageID;
    }

    public int getInvoiceType() {
        return InvoiceType;
    }

    public void setInvoiceType(int invoiceType) {
        InvoiceType = invoiceType;
    }

    public int getInvoiceCorp() {
        return InvoiceCorp;
    }

    public void setInvoiceCorp(int invoiceCorp) {
        InvoiceCorp = invoiceCorp;
    }
}
