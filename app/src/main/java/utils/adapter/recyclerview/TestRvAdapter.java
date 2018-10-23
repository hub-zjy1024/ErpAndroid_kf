package utils.adapter.recyclerview;

import android.content.Context;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;

import java.util.List;

/**
 * Created by 张建宇 on 2019/2/26.
 */
public class TestRvAdapter extends BaseRvAdapter<ChuKuDanInfo> {

    public TestRvAdapter(List<ChuKuDanInfo> mData, int layoutId, Context mContext) {
        super(mData, layoutId, mContext);
    }

    @Override
    protected void convert(BaseRvViewholder holder, ChuKuDanInfo item) {
        holder.setText(R.id.chukudan_items_tv , item.toStringSmall());
    }
}
