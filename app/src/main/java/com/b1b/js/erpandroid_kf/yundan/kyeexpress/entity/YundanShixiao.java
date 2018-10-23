package com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity;

/**
 * Created by 张建宇 on 2018/7/24.
 */
public class YundanShixiao {
    private String name;
    private String description;

    public YundanShixiao() {
    }

    public YundanShixiao(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}
