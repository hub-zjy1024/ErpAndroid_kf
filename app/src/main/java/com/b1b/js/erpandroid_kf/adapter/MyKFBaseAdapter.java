package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 Created by 张建宇 on 2017/2/23. */

public abstract class MyKFBaseAdapter<T> extends BaseAdapter {
    protected List<T> data;
    protected Context mContext;
    protected int itemView;
    protected int[] itemIds;
    public MyKFBaseAdapter(List<T> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    public abstract void getMyView();

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return null;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
