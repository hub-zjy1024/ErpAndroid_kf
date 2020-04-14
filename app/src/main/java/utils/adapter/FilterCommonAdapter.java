package utils.adapter;

import android.content.Context;
import android.widget.Filterable;

import java.util.List;

/**
 * Created by 张建宇 on 2020/4/10.
 */
public abstract class FilterCommonAdapter<T> extends CommonAdapter<T> implements Filterable {

    public FilterCommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }
}
