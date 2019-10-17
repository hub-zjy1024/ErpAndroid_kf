package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.ChukuDetail;

import java.util.List;

import utils.adapter.CommonAdapter;
import utils.adapter.ViewHolder;

/**
 * Created by 张建宇 on 2019/7/30.
 */
public class PreChukuParentAdapter extends CommonAdapter<ChukuDetail> {

    public PreChukuParentAdapter(Context context, List<ChukuDetail> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, ChukuDetail item) {
        helper.setText(R.id.item_pre_chuku_detail_tv1, item.toString());

//        型号: "53375-0210",
//                数量: 200,
//                进价: 1.5409,
//                售价: 1.7438,
//                厂家: "Molex",
//                描述: "D201907260000000207",
//                封装: "Aa",
//                明细备注: ""
    }
}
