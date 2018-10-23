package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.printer.entity.XiaopiaoInfo;

import java.util.List;

import utils.adapter.CommonAdapter;


/**
 Created by 张建宇 on 2017/11/9. */

public class XiaopiaoAdapter extends CommonAdapter<XiaopiaoInfo> {
    public XiaopiaoAdapter(Context context, List<XiaopiaoInfo> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(utils.adapter.ViewHolder helper, XiaopiaoInfo item) {
        TextView view = helper.getView(R.id.item_rukutag_tv);
        view.setText(item.toString());
    }
}
