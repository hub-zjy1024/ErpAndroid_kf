package utils.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;

/**
 * Created by 张建宇 on 2019/2/26.
 */
public interface IHolder<R extends IHolder> {

    public <T extends View> T getView(@IdRes int resId);

    public R setBitmap(@IdRes int resId, Bitmap bitmap);

    public R setOnclick(@IdRes int resId, View.OnClickListener listener);

    public R setText(@IdRes int resId, String text);

    public R setText(@IdRes int resId, @StringRes int strId);

    public R setImageResource(int viewId, int drawableId);

    public R setImageDrawable(int viewId, Drawable drawable);

}
