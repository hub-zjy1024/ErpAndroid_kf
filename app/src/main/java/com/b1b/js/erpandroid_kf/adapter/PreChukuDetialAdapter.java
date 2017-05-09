package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/5/8. */

public class PreChukuDetialAdapter extends MyBaseAdapter<PreChukuDetailInfo> {
    public PreChukuDetialAdapter(List<PreChukuDetailInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        PreChukuDetailHolder holder = (PreChukuDetailHolder) baseHolder;
        holder.tv = (TextView) convertView.findViewById(R.id.item_pre_chuku_detail_tv);
    }

    @Override
    protected void initHolder(PreChukuDetailInfo currentData, MyBasedHolder baseHolder) {
        PreChukuDetailHolder holder = (PreChukuDetailHolder) baseHolder;
        holder.tv.setText(currentData.toString());
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new PreChukuDetailHolder();
    }

    class PreChukuDetailHolder extends MyBasedHolder {
        TextView tv;
    }
}
