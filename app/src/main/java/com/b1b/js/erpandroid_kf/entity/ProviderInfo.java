package com.b1b.js.erpandroid_kf.entity;

/**
 Created by 张建宇 on 2017/2/15. */

public class ProviderInfo {
    private String id;
    private String name;
    /**
     * 0为不能开票，1为可以开票
     */
    private String hasKaipiao;

    public ProviderInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHasKaipiao() {
        return hasKaipiao;
    }

    public void setHasKaipiao(String hasKaipiao) {
        this.hasKaipiao = hasKaipiao;
    }

    public ProviderInfo(String id, String name, String hasKaipiao) {

        this.id = id;
        this.name = name;
        this.hasKaipiao = hasKaipiao;
    }
}
