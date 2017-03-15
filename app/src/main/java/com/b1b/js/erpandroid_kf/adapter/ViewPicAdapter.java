package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 Created by 张建宇 on 2017/3/2. */

public class ViewPicAdapter extends BaseAdapter {
    private List<FTPImgInfo> list;
    private Context mContext;
    private HashSet<String> imgs;
    private LinkedHashMap<String, Bitmap> bitmaps = new LinkedHashMap<>();

    public ViewPicAdapter(List<FTPImgInfo> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PicHolder mHolder;
        if (convertView == null) {
            mHolder = new PicHolder();
            FTPImgInfo info = (FTPImgInfo) getItem(position);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_viewpicbypid, parent, false);
            mHolder.iv = (ImageView) convertView.findViewById(R.id.item_viewpic_iv);
            mHolder.iv.setTag(info.getImgPath());
            convertView.setTag(mHolder);
        } else {
            mHolder = (PicHolder) convertView.getTag();
        }
        String realPath = list.get(position).getImgPath();
        Bitmap bitmap;
        if (!bitmaps.containsKey(realPath)) {
            try {
                bitmap = MyImageUtls.getMySmallBitmap(realPath, 200, 200);
                bitmaps.put(realPath, bitmap);
                mHolder.iv.setImageBitmap(bitmap);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        } else {
            bitmap = bitmaps.get(realPath);
            mHolder.iv.setImageBitmap(bitmap);
        }
        return convertView;
    }

    class PicHolder {
        ImageView iv;
    }
}
