package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;

import java.util.List;

/**
 * Created by js on 2016/12/27.
 */

public class KqAdapter extends BaseAdapter {
    private Context context;
    private List<KaoqinInfo> list;

    public KqAdapter(Context context, List<KaoqinInfo> list) {
        this.context = context;
        this.list = list;
    }

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
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.kaoqin_lvitems, parent, false);
            holder = new ViewHolder();
            holder.id = (TextView) convertView.findViewById(R.id.kqitems_id);
            holder.name = (TextView) convertView.findViewById(R.id.kqitems_name);
            holder.date = (TextView) convertView.findViewById(R.id.kqitems_date);
            holder.endIp = (TextView) convertView.findViewById(R.id.kqitems_endIp);
            holder.startIp = (TextView) convertView.findViewById(R.id.kqitems_startIp);
            holder.startTime = (TextView) convertView.findViewById(R.id.kqitems_startTime);
            holder.endTime = (TextView) convertView.findViewById(R.id.kqitems_endTime);
            holder.state = (TextView) convertView.findViewById(R.id.kqitems_state);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //记得检查list集合中是否有数据
        if (getCount() != 0) {
            KaoqinInfo kqi = list.get(position);

            holder.id.setText("id:" + kqi.getEmpId());
            holder.name.setText("姓名:" + kqi.getEmpName());
            holder.date.setText("日期:" + kqi.getDate());
            holder.state.setText("考勤状态:" + kqi.getState());
            holder.startTime.setText("上班时间:" + kqi.getStartTime());
            holder.endTime.setText("下班时间:" + kqi.getEndTime());
            holder.startIp.setText("上班ip:" + kqi.getStartIp());
            holder.endIp.setText("下班ip:" + kqi.getEndIp());
            if (!kqi.getState().equals("正常")) {
                holder.state.setTextColor(Color.RED);
            } else {
                holder.state.setTextColor(Color.BLUE);
            }
        }

        return convertView;
    }

    class ViewHolder {
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
