package utils.adapter.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import utils.adapter.IHolder;

/**
 * Created by 张建宇 on 2019/2/26.
 */
public  class BaseRvViewholder extends RecyclerView.ViewHolder implements IHolder<BaseRvViewholder> {
    private SparseArray<View> mItemViews = new SparseArray<>();
    //子布局
    private View itemView;

    ViewGroup parent;
    int viewType;
    Context mContext;
    int layoutId;

    public static BaseRvViewholder getViewHolder(ViewGroup parent, int viewType, int layoutId,
                                                 Context mContext) {
        View itemView = LayoutInflater.from(mContext).inflate(layoutId, null, false);
        BaseRvViewholder baseRvViewholder = new BaseRvViewholder(itemView);
        baseRvViewholder.parent = parent;
        baseRvViewholder.viewType = viewType;
        baseRvViewholder.mContext = mContext;
        baseRvViewholder.layoutId = layoutId;
        return baseRvViewholder;
    }

    public BaseRvViewholder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public View getItemView() {
        return itemView;
    }

    public <T extends View> T getView(@IdRes int resId) {
        View view = mItemViews.get(resId);
        if (view == null) {
            view = itemView.findViewById(resId);
            mItemViews.put(resId, view);
        }
        return (T) view;
    }

    public BaseRvViewholder setBitmap(@IdRes int resId, Bitmap bitmap) {
        ImageView tv = getView(resId);
        tv.setImageBitmap(bitmap);
        return this;
    }

    public BaseRvViewholder setOnclick(@IdRes int resId, View.OnClickListener listener) {
        View tv = getView(resId);
        tv.setOnClickListener(listener);
        return this;
    }

    public BaseRvViewholder setText(@IdRes int resId, String text) {
        TextView tv = getView(resId);
        tv.setText(text);
        return this;
    }

    public BaseRvViewholder setText(@IdRes int resId, @StringRes int strId) {
        TextView tv = getView(resId);
        tv.setText(strId);
        return this;
    }

    public BaseRvViewholder setImageResource(int viewId, int drawableId) {
        ImageView view = getView(viewId);
        view.setImageResource(drawableId);
        return this;
    }

    @Override
    public BaseRvViewholder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }
}
