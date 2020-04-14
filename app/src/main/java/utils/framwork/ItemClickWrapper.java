package utils.framwork;

import android.view.View;

/**
 * Created by 张建宇 on 2020/4/1.
 */
public abstract class ItemClickWrapper<T> implements View.OnClickListener{
    public T data;

    public ItemClickWrapper(T data) {
        this.data = data;
    }

    public ItemClickWrapper() {

    }

    public abstract void allClick(View v, T data) ;
    @Override
    public void onClick(View v) {
        allClick(v, data);
    }
}
