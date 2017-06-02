package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;

import java.util.List;

/**
 Created by 张建宇 on 2017/6/2. */

public class MenuAdapter extends MyBaseAdapter<MyMenuItem> {
    public MenuAdapter(List<MyMenuItem> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        MenuHolder holder = (MenuHolder) baseHolder;
        holder.tv = (TextView) convertView.findViewById(R.id.menu_item_content);
        holder.iv = (ImageView) convertView.findViewById(R.id.menu_item_img);
        holder.tvDes = (TextView) convertView.findViewById(R.id.menu_item_detail);
        holder.tvDes.scrollBy(0, 20);
    }

    @Override
    protected void initHolder(MyMenuItem currentData, MyBasedHolder baseHolder) {
        MenuHolder holder = (MenuHolder) baseHolder;
        holder.tv.setText(currentData.content);
        holder.iv.setImageResource(currentData.imgResId);
        holder.tvDes.setText(currentData.description);
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new MenuHolder();
    }

    class MenuHolder extends MyBasedHolder {
        ImageView iv;
        TextView tv;
        TextView tvDes;
    }
}
