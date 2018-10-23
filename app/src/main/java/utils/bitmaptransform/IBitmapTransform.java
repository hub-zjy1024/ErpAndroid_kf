package utils.bitmaptransform;

import android.graphics.Bitmap;

/**
 * Created by 张建宇 on 2019/1/11.
 */
public interface IBitmapTransform {
    Bitmap getTransFormedBitmap(Bitmap inputBitmap);
}
