package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.MyMenuItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import utils.adapter.CommonAdapter;
import utils.adapter.ViewHolder;

/**
 Created by 张建宇 on 2018/1/30. */

public class MenuGvAdapter extends CommonAdapter<MyMenuItem> {
    public MenuGvAdapter(Context context, List<MyMenuItem> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(final ViewHolder helper, MyMenuItem item) {
        final ImageView mv = helper.getView(R.id.item_menu_gv_img);
        final int resId = item.imgResId;
        final String nowId = resId + "";
//        mv.setTag(nowId);
//        Picasso.with(mContext).load(item.imgResId).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                String urlTag = (String) mv.getTag();
//                if (nowId.equals(urlTag)) {
//                    helper.setImageBitmap(R.id.item_menu_gv_img, bitmap);
//                }
//            }
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//                Log.e("zjy", getClass() + "->onBitmapFailed(): ==" + nowId);
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
////                Log.e("zjy", getClass() + "->onPrepareLoad(): ==" + nowId);
//            }
//        });
        Picasso.with(mContext).load(item.imgResId).into(mv);

//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), item.imgResId);
//        helper.setImageBitmap(R.id.item_menu_gv_img,bitmap );
        helper.setText(R.id.item_menu_gv_tvtitle, item.content);
    }
}
