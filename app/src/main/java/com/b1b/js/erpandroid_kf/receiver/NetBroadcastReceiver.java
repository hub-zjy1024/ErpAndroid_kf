package com.b1b.js.erpandroid_kf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by 张建宇 on 2018/7/9.
 */
public class NetBroadcastReceiver extends BroadcastReceiver {
    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    private int lastState = -1;
    private Context nContext;

    public NetBroadcastReceiver(Context nContext, StateCallback evevt) {
        this.nContext = nContext;
        this.evevt = evevt;
    }

    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                    return NETWORK_WIFI;
                } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                    return NETWORK_MOBILE;
                }
            } else {
                return NETWORK_NONE;
            }
        }
        return NETWORK_NONE;
    }

    public interface StateCallback {
        void onNetChange(int state);
    }

    StateCallback evevt;

    public NetBroadcastReceiver(StateCallback evevt) {
        this.evevt = evevt;

    }

    public void getLastState(Context mContext) {
        lastState = 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = getNetWorkState(context);
            // 接口回调传过去状态的类型
            if (lastState == NETWORK_WIFI && netWorkState == NETWORK_NONE) {

            }
            evevt.onNetChange(netWorkState);
            lastState = netWorkState;
        }
    }
}
