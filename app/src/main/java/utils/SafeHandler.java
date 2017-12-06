package utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 Created by 张建宇 on 2017/11/23. */

public  class SafeHandler extends Handler {
    protected WeakReference<Activity> mContext;

    public SafeHandler(Activity mContext) {
        this.mContext = new WeakReference<Activity>(mContext);
    }

    @Override

    public  void handleMessage(Message msg){

    } ;

    public WeakReference<Activity> getmContext() {
        return mContext;
    }

}
