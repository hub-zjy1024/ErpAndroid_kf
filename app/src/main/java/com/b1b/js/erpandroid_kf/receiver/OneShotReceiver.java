package com.b1b.js.erpandroid_kf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by 张建宇 on 2018/11/26.
 */
public class OneShotReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("zjy", "OneShotReceiver->onReceive(): I'm  oneshot alarm==");
    }
}
