package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.CheckInfo;

import java.util.List;

/**
 * Created by js on 2017/1/3.
 */

public class CheckInfoAdapter extends BaseAdapter {
    private List<CheckInfo> list;
    private Context context;

    public CheckInfoAdapter(List<CheckInfo> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    private LayoutInflater inflater;

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chukudanlist_items, parent, false);
            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.chukudan_items_tv);
            holder.tvMore = (TextView) convertView.findViewById(R.id.chukudan_items_tvMore);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (getCount() != 0) {
            holder.tv.setText(list.get(position).toSmallString());
        }
        holder.tvMore.setVisibility(View.VISIBLE);
        return convertView;
    }

    class ViewHolder {
        TextView tv;
        TextView tvMore;
    }
}
