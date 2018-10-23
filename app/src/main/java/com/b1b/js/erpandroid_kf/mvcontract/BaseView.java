package com.b1b.js.erpandroid_kf.mvcontract;

/**
 * Created by 张建宇 on 2018/11/1.
 */
public interface BaseView<T> {
    /**当BaseView为Fragment时，在Activity中初始化Presenter，并传递到Fragment中，
     *
     * @param t
     */
    void setPrinter(T t);
}
