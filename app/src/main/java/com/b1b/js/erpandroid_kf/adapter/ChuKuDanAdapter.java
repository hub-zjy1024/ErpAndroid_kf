package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;

import java.util.List;

import zhy.utils.CommonAdapter;

/**
 * Created by js on 2016/12/29.
 */

public class ChuKuDanAdapter extends CommonAdapter<ChuKuDanInfo> {
    private List<ChuKuDanInfo> list ;
    private LayoutInflater inflater;

    public ChuKuDanAdapter(List<ChuKuDanInfo> list, Context context, int layoutId) {
        super(context, list, layoutId);
    }
  /*  @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chukudanlist_items, parent, false);
            mHolder = new ViewHolder();
            mHolder.tv = (TextView) convertView.findViewById(R.id.chukudan_items_tv);
            mHolder.tvMore = (TextView) convertView.findViewById(R.id.chukudan_items_tvMore);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        if (getCount() >=position) {
            ChuKuDanInfo info = list.get(position);
            mHolder.tv.setText(info.toStringSmall());
        }
        mHolder.tvMore.setVisibility(View.VISIBLE);
        return convertView;
    }*/

    @Override
    public void convert(zhy.utils.ViewHolder helper, ChuKuDanInfo item) {
        helper.setText(R.id.chukudan_items_tv, item.toStringSmall());
    }

    class ViewHolder {
        TextView tv;
        TextView tvMore;
    }
}
