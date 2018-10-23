package com.b1b.js.erpandroid_kf.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
/**
 Created by 张建宇 on 2019/4/29. */
public class SpSettings {

    public static final String NAME = "kfName";
    public static final String KYACCOUNT = "kyAccount";
    public static final String KYUUID = "kyUuid";
    public static final String KYKEY = "kyKey";
    public static final String PRINTERSERVER = "printerServer";
    public static final String FTPSERVER = "ftpAddress";
    public static final String SFACCOUNT = "sfAccount";
    public static final String KY_SMSNUM = "smsNum";
    public static final String CONFIG_JSON = "configJson";
    public static final String CHUKU_PRINTER = "chukuPrinter";
    public static final String PREF_KF = "pref_kf";
    public static final String PREF_USERINFO = "UserInfo";
    public static final String PREF_TKPIC = "pref_takepic_style";
    public static final String PREF_CAMERA_INFO = "cameraInfo";
    public static final String PREF_LOGUPLOAD = "uploadlog";
    public static final String PREF_EXPRESS = "prefExpress";

    public static final String Ruku_storageKey = "storageID";
    public static final String storageKey = "storageID";

    /**
     * 反射获取所有SharedPreferences的名称，即以"PREF_"开头的静态变量，并进行清理
     * @param mContext
     */
    public static void clearAllSp(Context mContext) {
        Class<SpSettings> spSettingsClass = SpSettings.class;
        Field[] declaredFields = spSettingsClass.getDeclaredFields();
        for (Field field : declaredFields) {
            String fName = field.getName();
            if (field.getType().toString().endsWith("java.lang.String") && Modifier.isStatic(field
                    .getModifiers()) && fName.startsWith("PREF_")) {
                try {
                    String spName = field.get(spSettingsClass).toString();
                    SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
                    sp.edit().clear().apply();
                    Log.e("zjy", spSettingsClass.getName() + "->clearAllSp():clear ==" + field.getName() +
                            " , "
                            + spName);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
