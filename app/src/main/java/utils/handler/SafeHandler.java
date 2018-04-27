package utils.handler;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 Created by 张建宇 on 2017/11/23. */

public class SafeHandler<T> extends Handler {
    protected WeakReference<T> mRefer;

    public SafeHandler(T mRefer) {
        this.mRefer = new WeakReference<>(mRefer);
    }

    public T getActivity() {
        return mRefer.get();
    }
}
