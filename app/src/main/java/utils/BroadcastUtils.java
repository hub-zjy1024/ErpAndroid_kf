package utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by 张建宇 on 2018/11/27.
 */
public class BroadcastUtils {
    private boolean isReg = false;
    private BroadcastReceiver receiver;
    private Context mContext;

    public BroadcastUtils(BroadcastReceiver receiver, Context mContext) {
        this.receiver = receiver;
        this.mContext = mContext;
    }

    public void register(IntentFilter mFilter) {
        if(!isReg){
            mContext.registerReceiver(receiver, mFilter);
            isReg = true;
        }
    }
    public void unRegister(){
        if(isReg){
            mContext.unregisterReceiver(receiver);
            isReg = false;
        }
    }
}
