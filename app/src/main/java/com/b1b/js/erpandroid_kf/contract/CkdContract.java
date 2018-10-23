package com.b1b.js.erpandroid_kf.contract;

import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;
import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;

public class CkdContract {

    public interface ICkdView extends CkBaseInterface<ChuKuDanInfo>{
        void finishSearch(String msg);
        void searchBefore();
    }
}
