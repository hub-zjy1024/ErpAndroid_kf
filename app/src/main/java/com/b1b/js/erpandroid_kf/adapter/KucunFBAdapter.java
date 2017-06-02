package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.KucunFBInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/5/17. */

public class KucunFBAdapter  extends MyBaseAdapter<KucunFBInfo>{
    public KucunFBAdapter(List<KucunFBInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        KucunFBHolder holder = (KucunFBHolder) baseHolder;
        holder.tv = (TextView) convertView.findViewById(R.id.kucunfb_item_tv);
    }

    @Override
    protected void initHolder(KucunFBInfo currentData, MyBasedHolder baseHolder) {
        KucunFBHolder holder = (KucunFBHolder) baseHolder;
        holder.tv.setText(currentData.toString());
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new KucunFBHolder();
    }

    class KucunFBHolder extends MyBasedHolder {
        TextView tv;
    }
}
