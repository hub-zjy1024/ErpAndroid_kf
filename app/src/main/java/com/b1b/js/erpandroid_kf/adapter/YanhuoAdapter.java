package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.YanhuoInfo;

import java.util.List;


/**
 * Created by 张建宇 on 2017/8/25.
 */

public class YanhuoAdapter extends MyBaseAdapter2<YanhuoInfo> {

    public YanhuoAdapter(List<YanhuoInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void findChildViews(View convertView, MyBasedHolder baseHolder) {
        CaigouYanhuoHoloder holder = (CaigouYanhuoHoloder) baseHolder;
        holder.tv =(TextView) convertView.findViewById(R.id
                .activity_caigouyanhuo_item_tv);
    }

    @Override
    protected void onBindData(YanhuoInfo currentData, MyBasedHolder baseHolder) {
        CaigouYanhuoHoloder holder = (CaigouYanhuoHoloder) baseHolder;
        holder.tv.setText(currentData.toString());

    }

    @Override
    protected MyBasedHolder getCustomHolder() {
        return new CaigouYanhuoHoloder();
    }

    public class CaigouYanhuoHoloder extends MyBasedHolder{
        TextView tv;
    }
}
