package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/3/17. */

public class PankuAdapter extends BaseAdapter {
    private List<PankuInfo> pkList;
    private Context mContext;

    public PankuAdapter(List<PankuInfo> pkList, Context mContext) {
        this.pkList = pkList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return pkList != null ? pkList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return pkList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chukudanlist_items, parent, false);
            mHolder = new PankuAdapter.ViewHolder();
            mHolder.tv = (TextView) convertView.findViewById(R.id.chukudan_items_tv);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        if (getCount() > position) {
            PankuInfo info = pkList.get(position);
            if (info.getHasFlag().equals("0")) {
                mHolder.tv.setTextColor(Color.BLACK);
            } else {
                mHolder.tv.setTextColor(Color.RED);
            }
            mHolder.tv.setText(info.toString());
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv;
    }
}
