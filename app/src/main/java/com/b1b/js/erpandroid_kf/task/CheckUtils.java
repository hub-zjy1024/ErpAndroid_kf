package com.b1b.js.erpandroid_kf.task;

import android.content.Context;

import com.b1b.js.erpandroid_kf.MyApp;

import utils.MyToast;

/**
 Created by 张建宇 on 2018/5/7. */
public class CheckUtils {
    public static boolean checkUID(Context mContext) {
        return checkUID(mContext, "程序出现错误，请重启");
    }

    /**
     判断登陆人是否为空
     @param mContext 上下文对象
     @param msg 登陆人为空时打印的信息
     @return true 是，false 否
     */
    public static boolean checkUID(Context mContext, String msg) {
        if (MyApp.id == null) {
            MyToast.showToast(mContext, msg);
            return false;
        }
        return true;
    }

    /**判断是否为管理员
     @return true是，false否
     */
    public static boolean isAdmin() {
        return ("101".equals(MyApp.id));
    }
}
