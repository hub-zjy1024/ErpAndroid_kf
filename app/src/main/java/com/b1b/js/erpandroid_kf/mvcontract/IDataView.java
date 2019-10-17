package com.b1b.js.erpandroid_kf.mvcontract;

import java.util.List;

/**
 * Created by 张建宇 on 2019/8/6.
 */
public interface IDataView<T, F> extends BaseView<F> {

    void onDataBack(List<T> infos);

    void loading(String msg);

    void cancelLoading();

    void alert(String msg);

    @Override
    void setPrinter(F o);
}
