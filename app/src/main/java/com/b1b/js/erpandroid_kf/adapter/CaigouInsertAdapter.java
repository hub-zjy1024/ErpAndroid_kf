package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.InsertDetialInfo;

import java.util.List;

/**
 Created by 张建宇 on 2017/2/16. */

public class CaigouInsertAdapter extends BaseAdapter {
    private List<InsertDetialInfo> data;
    private Context mContext;

    public interface BtnOnClickListener {
        void onClick(int position);
    }

    public CaigouInsertAdapter(List<InsertDetialInfo> data, Context mContext, BtnOnClickListener mListener) {
        this.data = data;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    private BtnOnClickListener mListener;

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
        MyViewHolder mHolder=null;
        if (convertView == null) {
            mHolder = new MyViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.caigouedit_insert_item, parent, false);
            mHolder.pihao = (TextView) convertView.findViewById(R.id.caigouedit_insert_pihao);
            mHolder.money = (TextView) convertView.findViewById(R.id.caigouedit_insert_money);
            mHolder.selType = (Button) convertView.findViewById(R.id.caigouedit_insert_seltype);
            mHolder.partNo = (TextView) convertView.findViewById(R.id.caigouedit_insert_partno);
            mHolder.type = (TextView) convertView.findViewById(R.id.caigouedit_insert_leibie);
//            Log.e("zjy", "CaigouInsertAdapter.java->getView(): new holder");
            convertView.setTag(mHolder);
        } else {
            mHolder = (MyViewHolder) convertView.getTag();
        }
        InsertDetialInfo info = data.get(position);
//        Log.e("zjy", "CaigouInsertAdapter.java->getView(): info_position==" + position);
//        Log.e("zjy", "CaigouInsertAdapter.java->getView(): info_type==" + info.getType());
//        Log.e("zjy", "CaigouInsertAdapter.java->getView(): info_money==" + info.getMoney());
//        Log.e("zjy", "CaigouInsertAdapter.java->getView(): info_pihao==" + info.getPihao());
//        Log.e("zjy", "CaigouInsertAdapter.java->getView(): ====================");
        if (info != null) {
            if (info.getPihao() != null) {
                mHolder.pihao.setText(info.getPihao());
            }
            if (info.getMoney() != null) {
                mHolder.money.setText(info.getMoney());
            }
            if (info.getPartno() != null) {
                mHolder.partNo.setText(info.getPartno());
            }
            if (info.getType() != null) {
                mHolder.type.setText(info.getType());
            }
            final int pos = position;
            mHolder.selType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(pos);
                }
            });
        }
        return convertView;
    }

    class MyViewHolder {
        TextView pihao;
        TextView partNo;
        TextView money;
        TextView type;
        Button selType;
    }
}
