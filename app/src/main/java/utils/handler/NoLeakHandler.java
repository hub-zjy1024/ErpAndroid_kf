package utils.handler;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 Created by 张建宇 on 2018/4/4. */
public class NoLeakHandler extends Handler {
    private WeakReference<NoLeakCallback> reference;

    public NoLeakHandler(NoLeakCallback callBack) {
        this.reference = new WeakReference<>(callBack);
        SoftReference s = new SoftReference(null);
        s.enqueue();
    }

    public interface NoLeakCallback {
        void handleMessage(Message msg);
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        NoLeakCallback callback = reference.get();
        if (callback != null) {
            callback.handleMessage(msg);
        }
    }
}
