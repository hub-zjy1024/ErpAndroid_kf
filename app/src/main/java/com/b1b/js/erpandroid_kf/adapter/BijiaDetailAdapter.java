package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.BijiadetailInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/2/23. */

public class BijiaDetailAdapter extends BaseAdapter {

    private List<BijiadetailInfo> list;
    private Context context;
    private LayoutInflater inflater;

    public BijiaDetailAdapter(List<BijiadetailInfo> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chukudanlist_items, parent, false);
            mHolder = new ViewHolder();
            mHolder.tv = (TextView) convertView.findViewById(R.id.chukudan_items_tv);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        if (getCount() > position) {
            BijiadetailInfo info = list.get(position);
            mHolder.tv.setText(info.toString());
        }


        return convertView;
    }

    class ViewHolder {
        TextView tv;
    }
}
