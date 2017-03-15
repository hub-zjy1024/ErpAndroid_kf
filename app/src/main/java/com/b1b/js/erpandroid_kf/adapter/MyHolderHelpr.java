package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 Created by 张建宇 on 2017/2/23. */

public class MyHolderHelpr {
    private Context mContext;
    private View itemView;
    private List<View> childVies = new ArrayList<>();

    public static MyHolderHelpr get(int itemViewId, int ids[], Context mContext) {
        View v = LayoutInflater.from(mContext).inflate(itemViewId, null);
        for(int i=0;i<ids.length;i++) {
            View tempView = v.findViewById(ids[i]);

        }
        return null;
    }
}
