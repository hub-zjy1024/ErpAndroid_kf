package com.b1b.js.erpandroid_kf.mvcontract;

/**
 * Created by 张建宇 on 2020/4/8.
 */
public interface IViewWithLoading<T> extends BaseView<T> {
    int loadingWithId(String msg);

    void cancelLoading(int pIndex);
}
