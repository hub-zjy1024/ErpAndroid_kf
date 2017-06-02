package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/6/2. */

public class MyMenuItem {
    public int imgResId;
    public String content;
    public String description;

    public MyMenuItem(int imgResId, String content, String description) {
        this.imgResId = imgResId;
        this.content = content;
        this.description = description;
    }

    public MyMenuItem(int imgResId, String content) {
        this.imgResId = imgResId;
        this.content = content;
    }
}
