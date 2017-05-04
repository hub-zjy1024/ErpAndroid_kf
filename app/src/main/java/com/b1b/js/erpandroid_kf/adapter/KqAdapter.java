package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;

import java.util.List;

/**
 Created by js on 2016/12/27. */

public class KqAdapter extends MyBaseAdapter<KaoqinInfo> {
    public KqAdapter(List<KaoqinInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        ViewHolder tempHolder = (ViewHolder) baseHolder;
        tempHolder.id = (TextView) convertView.findViewById(R.id.kqitems_id);
        tempHolder.name = (TextView) convertView.findViewById(R.id.kqitems_name);
        tempHolder.date = (TextView) convertView.findViewById(R.id.kqitems_date);
        tempHolder.endIp = (TextView) convertView.findViewById(R.id.kqitems_endIp);
        tempHolder.startIp = (TextView) convertView.findViewById(R.id.kqitems_startIp);
        tempHolder.startTime = (TextView) convertView.findViewById(R.id.kqitems_startTime);
        tempHolder.endTime = (TextView) convertView.findViewById(R.id.kqitems_endTime);
        tempHolder.state = (TextView) convertView.findViewById(R.id.kqitems_state);
    }

    @Override
    protected void initHolder(KaoqinInfo currentData, MyBasedHolder baseHolder) {
        ViewHolder holder = (ViewHolder) baseHolder;
        if (getCount() != 0) {
            holder.id.setText("id:" + currentData.getEmpId());
            holder.name.setText("姓名:" + currentData.getEmpName());
            holder.date.setText("日期:" + currentData.getDate());
            holder.state.setText("考勤状态:" + currentData.getState());
            holder.startTime.setText("上班时间:" + currentData.getStartTime());
            holder.endTime.setText("下班时间:" + currentData.getEndTime());
            holder.startIp.setText("上班ip:" + currentData.getStartIp());
            holder.endIp.setText("下班ip:" + currentData.getEndIp());
            if (!currentData.getState().equals("正常")) {
                holder.state.setTextColor(Color.RED);
            } else {
                holder.state.setTextColor(Color.BLUE);
            }
        }
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new ViewHolder();
    }

    class ViewHolder extends MyBaseAdapter.MyBasedHolder {
        TextView id;
        TextView name;
        TextView startIp;
        TextView endIp;
        TextView date;
        TextView startTime;
        TextView endTime;
        TextView state;

    }
}
