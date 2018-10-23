package com.b1b.js.erpandroid_kf.contract;

import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;

import java.util.List;

public interface CkBaseInterface<T> {
    void updateList(List<T> list, String msg);
}
