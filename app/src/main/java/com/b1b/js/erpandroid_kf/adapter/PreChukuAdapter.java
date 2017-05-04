package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/5/3. */

public class PreChukuAdapter extends MyBaseAdapter<PreChukuInfo> {
    public PreChukuAdapter(List<PreChukuInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        PrechukuHolder holder = (PrechukuHolder) baseHolder;
        holder.tv = (TextView) convertView.findViewById(R.id.item_caigou_tv);
    }

    @Override
    protected void initHolder(PreChukuInfo currentData, MyBasedHolder baseHolder) {
        PrechukuHolder holder = (PrechukuHolder) baseHolder;
        holder.tv.setText(currentData.toString());
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new PrechukuHolder();
    }

    public class PrechukuHolder extends MyBasedHolder {
        TextView tv;
    }
}
