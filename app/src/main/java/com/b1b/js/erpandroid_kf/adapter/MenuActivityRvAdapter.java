package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;

import java.util.List;

import utils.adapter.recyclerview.BaseItemClickListener;
import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;

/**
 * Created by 张建宇 on 2020/1/3.
 */
public class MenuActivityRvAdapter extends BaseRvAdapter<MyMenuItem> {

    private BaseItemClickListener<MyMenuItem> mListener;

    public MenuActivityRvAdapter(List<MyMenuItem> mData, int layoutId, Context mContext,
                                 BaseItemClickListener<MyMenuItem> mListener) {
        super(mData, layoutId, mContext);
        this.mListener = mListener;
    }

    public MenuActivityRvAdapter(List<MyMenuItem> mData, int layoutId, Context mContext) {
        super(mData, layoutId, mContext);
    }

    @Override
    protected void convert(final BaseRvViewholder holder, final MyMenuItem item) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), item.imgResId);
        holder.setBitmap(R.id.item_menu_gv_img, bitmap);
        holder.setText(R.id.item_menu_gv_tvtitle, item.content);
        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(holder, item);
                }
            }
        });
    }
}
