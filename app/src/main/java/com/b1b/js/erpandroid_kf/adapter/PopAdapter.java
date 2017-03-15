package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.ProviderInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/2/23. */

public class PopAdapter extends BaseAdapter {

    private List<ProviderInfo> data;
    private Context mContext;

    public PopAdapter(List<ProviderInfo> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_simple_item, null);
            mHolder = new MyViewHolder();
            mHolder.tv = (TextView) convertView.findViewById(R.id.spinner_item_tv);
            convertView.setTag(mHolder);
        } else {
            mHolder = ((MyViewHolder) convertView.getTag());
        }
        if (position < data.size()) {
            ProviderInfo info = data.get(position);
            mHolder.tv.setText(info.getName());
        }

        return convertView;
    }

    class MyViewHolder {
        TextView tv;
    }
}
