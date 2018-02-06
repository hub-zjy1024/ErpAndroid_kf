package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;

import java.util.List;

import zhy.utils.CommonAdapter;
import zhy.utils.ViewHolder;

/**
 Created by 张建宇 on 2018/1/30. */

public class MenuGvAdapter extends CommonAdapter<MyMenuItem> {
    public MenuGvAdapter(Context context, List<MyMenuItem> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, MyMenuItem item) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), item.imgResId);
        helper.setImageBitmap(R.id.item_menu_gv_img,bitmap );
        helper.setText(R.id.item_menu_gv_tvtitle, item.content);
    }
}
