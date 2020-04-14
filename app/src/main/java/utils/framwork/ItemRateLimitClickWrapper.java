package utils.framwork;

import android.view.View;

/**
 * Created by 张建宇 on 2020/4/14.
 */
public abstract class ItemRateLimitClickWrapper<T> extends ItemClickWrapper<T> {
    private long time1 = System.currentTimeMillis();

    public ItemRateLimitClickWrapper(T data) {
        super(data);
    }

    private int def_limit = 200;

    @Override
    public void allClick(View v, T data) {
        if (System.currentTimeMillis() - time1 < def_limit) {
            allClick(v, data, true);
        } else {
            time1 = System.currentTimeMillis();
            allClick(v, data, false);
        }
    }

    public abstract void allClick(View v, T data, boolean isOverRate);
}
