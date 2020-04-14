package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.imagepicker.utils.ImageLoader;

import java.io.File;
import java.util.List;

/**
 Created by 张建宇 on 2017/2/22. */

public class UploadPicAdapter extends BaseAdapter {
    private Context mContext;
    private List<UploadPicInfo> data;
    private OnItemBtnClickListener listener;

    public UploadPicAdapter(Context mContext, List<UploadPicInfo> data, OnItemBtnClickListener listener) {
        this.mContext = mContext;
        this.data = data;
        this.listener = listener;
    }

    @Override

    public int getCount() {
        return data != null ? data.size() : 0;
    }

    public interface OnItemBtnClickListener {
        void onClick(View v, int position);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        MyViewHolder mHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.uploadpic_item, parent, false);
            mHolder = new MyViewHolder();
            mHolder.iv = (ImageView) convertView.findViewById(R.id.uploadpic_iv);
            mHolder.btn = (Button) convertView.findViewById(R.id.uploadpic_btn);
            mHolder.btn.setTag(data.get(position));
            convertView.setTag(mHolder);
        } else {
            mHolder = (MyViewHolder) convertView.getTag();
        }
        if (position < data.size()) {
            UploadPicInfo uploadPicInfo = data.get(position);
            String filePath = uploadPicInfo.getPath();
            File file = new File(filePath);
            Log.e("zjy", "UploadPicAdapter->getView(): mPath==" + filePath);
//            Picasso.with(mContext).load(file).placeholder(R.drawable.ic_pic_placeholder).resize(200, 200).into(mHolder.iv);
            ImageLoader.getInstance().loadImage(filePath, mHolder.iv);
            if (uploadPicInfo.getState().equals("1")) {
                mHolder.btn.setText("上传完成");
                mHolder.btn.setTextColor(Color.GREEN);
            } else if (uploadPicInfo.getState().equals("-1")) {
                mHolder.btn.setText("等待上传");
                mHolder.btn.setTextColor(Color.RED);
            }
        }
        mHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v, position);
            }
        });
        return convertView;
    }

    class MyViewHolder {
        ImageView iv;
        Button btn;
    }
}
