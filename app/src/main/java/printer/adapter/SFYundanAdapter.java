package printer.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.adapter.MyBaseAdapter;

import java.util.List;

import printer.entity.Yundan;

/**
 Created by 张建宇 on 2017/10/12. */

public class SFYundanAdapter extends MyBaseAdapter<Yundan> {

    public SFYundanAdapter(List<Yundan> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        YdHolder holder = (YdHolder) baseHolder;
        TextView tv = (TextView) convertView.findViewById(R.id.sf_tv);
        TextView tvFlag = (TextView) convertView.findViewById(R.id.sf_flag);
        TextView tvmore = (TextView) convertView.findViewById(R.id.sf_more);
        holder.tv = tv;
        holder.tvFlag = tvFlag;
        holder.tvmore = tvmore;

    }

    @Override
    protected void initHolder(Yundan currentData, MyBasedHolder baseHolder) {
        YdHolder holder = (YdHolder) baseHolder;
        if (currentData.getType().equals("2")) {
            holder.tvFlag.setText("调货");
            holder.tvFlag.setVisibility(View.VISIBLE);
        } else {
            holder.tvFlag.setText(null);
            holder.tvFlag.setVisibility(View.GONE);
        }
        holder.tv.setText(currentData.toStringSmall());
        holder.tvmore.setVisibility(View.VISIBLE);
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new YdHolder();
    }

    public class YdHolder extends MyBasedHolder{
        public TextView tv;
        public TextView tvFlag;
        public TextView tvmore;

    }
}
