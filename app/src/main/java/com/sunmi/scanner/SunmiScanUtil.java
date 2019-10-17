package com.sunmi.scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.b1b.js.erpandroid_kf.entity.SpSettings;

import java.lang.reflect.Method;

import utils.common.EmailSender;

/**
 * Created by 张建宇 on 2019/10/16.
 */
public class SunmiScanUtil {
    //通过反射获取ro.serialno
    public static String getSerialNumber(){

        String serial = null;
        try {
            Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            serial = (String)get.invoke(c, "ro.serialno");
            Log.e("zjy", "SunmiScanUtil->getSerialNumber(): ==" + serial);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;

    }

    public static void EmailList(Context mContext) {
        Log.e("zjy", "->EmailList(): buidle==" + Build.MODEL);
        if ("SUNMI".equals(Build.BRAND)) {
            final SharedPreferences sp = mContext.getSharedPreferences(SpSettings.PREF_FIRSTUSE, 0);
            sp.edit().clear().apply();
            final String serialNumber = getSerialNumber();
            final String mSerial = sp.getString("sn", "");
            if ("".equals(mSerial)) {
                sp.edit().putString("sn", serialNumber).apply();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        EmailSender sender = new EmailSender();
                        sender.sendCommonMail("sn =" + serialNumber);
                    }
                }.start();
            }
        }
    }
}
