package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PankuLog;

import java.util.List;

import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;

/**
 * Created by 张建宇 on 2020/4/8.
 */
public class PankuLogAdapter extends BaseRvAdapter<PankuLog> {
    public PankuLogAdapter(List<PankuLog> mData, int layoutId, Context mContext) {
        super(mData, layoutId, mContext);
    }

    @Override
    protected void convert(BaseRvViewholder holder, PankuLog item) {
        holder.setText(R.id.item_panku_logs_tv_no, String.format("序号:%s", holder.getAdapterPosition() + 1));
        holder.setText(R.id.item_panku_logs_tv_date, String.format("盘库时间:%s", item.panKuDate));
        holder.setText(R.id.item_panku_logs_tv_name, String.format("盘库人:%s(%s)",
                item.oprName, item.oprId));
    }
}
