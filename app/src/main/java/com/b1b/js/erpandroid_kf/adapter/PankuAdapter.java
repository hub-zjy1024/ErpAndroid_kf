package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;

import java.util.HashMap;
import java.util.List;

import utils.adapter.CommonAdapter;

/**
 Created by 张建宇 on 2017/3/17. */

public class PankuAdapter extends CommonAdapter<PankuInfo> implements TextView.OnClickListener {
    private List<PankuInfo> pkList;
    private Context mContext;

    private HashMap<String, String> checker = new HashMap<>();

    public PankuAdapter(List<PankuInfo> pkList, Context mContext) {
        this(mContext, pkList, R.layout.chukudanlist_items);
    }

    public PankuAdapter(Context context, List<PankuInfo> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void notifyDataSetChanged() {
        checker = new HashMap<>();
        super.notifyDataSetChanged();
    }

    static class ClickWrapper<T> implements View.OnClickListener{
        ItemListener<T> mListener;
        T item;
        ItemListener2<T> mListener2;
        View mItemView;

        public ClickWrapper(T item, ItemListener2<T> mListener2, View mItemView) {
            this.item = item;
            this.mListener2 = mListener2;
            this.mItemView = mItemView;
        }

        public ClickWrapper(ItemListener<T> mListener, T item, ItemListener2<T> mListener2,
                            View mItemView) {
            this.mListener = mListener;
            this.item = item;
            this.mListener2 = mListener2;
            this.mItemView = mItemView;
        }

        public ClickWrapper(ItemListener<T> mListener, T item) {
            this.mListener = mListener;
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.itemClick(v.getId(), item);
            }
            if (mListener2 != null) {
                mListener2.itemClick(mItemView, v, item);
            }
        }
    }
    @Override
    public void onClick(View v) {

    }

    public interface ItemListener<T>{
        public void itemClick(int id, T mInfo);
    }

    public interface ItemListener2<T> {
        public void itemClick(View itemView, View nowView, T mInfo);
    }

    ItemListener<PankuInfo> mListener;
    ItemListener2<PankuInfo> mListener2;

 /*   @Override
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
    }*/

 public void addListener( ItemListener<PankuInfo> mListener){
     this.mListener = mListener;
     notifyDataSetChanged();
 }

    public void addListener2(ItemListener2<PankuInfo> mListener) {
        this.mListener2 = mListener;
        notifyDataSetChanged();
    }
 public static class CheckClass{
     public CheckClass(PankuInfo mInfo, String isCheckd) {
         this.mInfo = mInfo;
         this.isCheckd = isCheckd;
     }

     PankuInfo mInfo;
     String isCheckd;

 }
    @Override
    public void convert(utils.adapter.ViewHolder helper, PankuInfo item) {
        View view1 = helper.getView(R.id.chukudan_items_tv);
        TextView mContent = (TextView) view1;
        ClickWrapper<PankuInfo> wrapper = new ClickWrapper<>(mListener, item, mListener2,
                helper.getConvertView());
        View view = helper.getView(R.id.item_pk_btn_rprint);
        View btnTakepic = helper.getView(R.id.item_pk_btn_takepic);

        TextView tvMore = (TextView) helper.getView(R.id.chukudan_items_tvMore);
        TextView tvPid = helper.getView(R.id.item_lv_pk_tv_pid);
        //盘库过的PID修改文字颜色
        if (item.getHasFlag().equals("0")) {
            tvPid.setTextColor(Color.BLACK);
        } else {
            tvPid.setTextColor(Color.RED);
        }
        helper.setText(R.id.item_lv_pk_tv_pid, item.getPid());
        helper.setText(R.id.item_lv_pk_tv_partno, item.getPartNo());
        helper.setText(R.id.item_lv_pk_tv_detailId, item.getDetailId());
        helper.setText(R.id.item_lv_pk_tv_count, item.getLeftCounts());
        helper.setText(R.id.item_lv_pk_tv_pihao, item.getPihao());
        helper.setText(R.id.item_lv_pk_tv_rktime, item.getRukuDate());

        Object tag = tvMore.getTag();
        CheckClass checkClass;
        if (tag == null) {
            checkClass = new CheckClass(item, "");
            tvMore.setTag(checkClass);
        }else{
            checkClass = (CheckClass) tag;
        }

        if (checkClass.mInfo.getDetailId().equals( item.getDetailId()) ) {
            if ("1".equals(checkClass.isCheckd)) {
                tvMore.setVisibility(View.GONE);
                mContent.setText(item.toExtraString());
                mContent.setVisibility(View.VISIBLE);
            } else {
                tvMore.setVisibility(View.VISIBLE);
                mContent.setText("");
                mContent.setVisibility(View.GONE);
            }
        }else{
            checkClass.mInfo = item;
            tvMore.setTag(checkClass);
            tvMore.setVisibility(View.VISIBLE);
            mContent.setText("");
            mContent.setVisibility(View.GONE);
        }
        view.setOnClickListener(wrapper);
        btnTakepic.setOnClickListener(wrapper);
    }

    class ViewHolder {
        TextView tv;
    }
}
