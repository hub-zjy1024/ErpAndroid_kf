package utils.adapter.recyclerview;

/**
 * Created by 张建宇 on 2020/1/3.
 */
public interface BaseItemClickListener<T> {
    void onItemClick(BaseRvViewholder holder, T item);
}
