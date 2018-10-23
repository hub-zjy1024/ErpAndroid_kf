package utils.adapter.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by 张建宇 on 2019/2/26.
 */
public abstract class BaseRvAdapter<T> extends RecyclerView.Adapter<BaseRvViewholder> {
    protected List<T> mData;
    protected int layoutId;
    protected Context mContext;

    public BaseRvAdapter(List<T> mData, int layoutId, Context mContext) {
        this.mData = mData;
        this.layoutId = layoutId;
        this.mContext = mContext;
    }

    @Override
    public BaseRvViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(mContext).inflate(layoutId, null);
        return new BaseRvViewholder(item);
    }

    @Override
    public void onBindViewHolder(BaseRvViewholder holder, int position) {
        T tData = mData.get(position);
        convert(holder, tData);
    }


    /**
     * 绑定数据
     *
     * @param holder {@link utils.adapter.recyclerview.BaseRvViewholder};
     *
     * @param item   item数据
     */
    protected abstract void convert(BaseRvViewholder holder, T item);

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }
}
