package com.b1b.js.erpandroid_kf.task;

/**
 Created by 张建宇 on 2017/8/17. */

public interface WebCallback<T> {
    public void errorCallback(Throwable e);

    public void okCallback(T obj);

    public void otherCallback(Object obj);

}
