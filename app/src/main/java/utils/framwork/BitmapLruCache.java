package utils.framwork;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by 张建宇 on 2019/8/19.
 */
public class BitmapLruCache<T> extends LruCache<T, Bitmap> {
    /**
     *
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    public static final int maxCacheSize = (int) (Runtime.getRuntime().maxMemory() / 4);

    @Override
    protected int sizeOf(T key, Bitmap value) {
        return value.getByteCount();
    }

//    @Override
//    protected void entryRemoved(boolean evicted, T key, Bitmap oldValue, Bitmap newValue) {
//        super.entryRemoved(evicted, key, oldValue, newValue);
//        if (oldValue != null) {
//            oldValue.recycle();
//        }
//    }
}
