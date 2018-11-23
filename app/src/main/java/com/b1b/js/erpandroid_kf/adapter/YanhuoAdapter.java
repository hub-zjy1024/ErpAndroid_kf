package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.YanhuoInfo;

import java.util.List;

import zhy.utils.CommonAdapter;
import zhy.utils.ViewHolder;


/**
 * Created by 张建宇 on 2017/8/25.
 */

public class YanhuoAdapter extends CommonAdapter<YanhuoInfo> {
    public YanhuoAdapter(Context context, List<YanhuoInfo> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, YanhuoInfo item) {
        helper.setText(R.id.activity_caigouyanhuo_item_tv, item.toString());
    }
}
