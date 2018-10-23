package com.b1b.js.erpandroid_kf.entity;

import android.content.SharedPreferences;

import java.lang.reflect.Field;

/**
 * Created by 张建宇 on 2019/5/30.
 */
public class UserInfoPref {
    public int cid;
    public int did;
    public String name;
    public String oprName;
    public String pwd;
    public String ftp;
    public String debugPwd;

    SharedPreferences sharedPreferences;

    public UserInfoPref(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        Field[] declaredFields = getClass().getDeclaredFields();
        for (Field mField : declaredFields) {
            Class<?> type = mField.getType();
            String fName = mField.getName();
            String strType = type.toString();
//            Log.e("zjy", getClass() + "->UserInfoPref(): ==" + strType + " name=" + fName);
            mField.setAccessible(true);
            try {
            if (type.toString().endsWith("java.lang.String")) {
                mField.set(this, sharedPreferences.getString(fName, ""));
            } else if (type.toString().endsWith("long")) {
                mField.set(this, sharedPreferences.getLong(fName, -1L));
            } else if (type.toString().endsWith("float")) {
                mField.set(this, sharedPreferences.getFloat(fName, -1f));
            } else if (type.toString().endsWith("int")) {
                mField.set(this, sharedPreferences.getInt(fName, -1));
            }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
//            cid = sp.getInt("cid", -1);
//       did = sp.getInt("did", -1);
//        ftp = sp.getString("ftp", "");
//        uname = sp.getString("name", "");
//        pwd = sp.getString("pwd", "");
//        debugPwd = sp.getString("pwdDebug", "");
    }

    public void saved() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Field[] declaredFields = getClass().getDeclaredFields();
        for (Field mField : declaredFields) {
            Class<?> type = mField.getType();
            String fName = mField.getName();
            String strType = type.toString();
            //            Log.e("zjy", getClass() + "->UserInfoPref(): ==" + strType + " name=" + fName);
            mField.setAccessible(true);
            try {
                if (type.toString().endsWith("java.lang.String")) {
                    edit.putString(fName, mField.get(this).toString());
                } else if (type.toString().endsWith("long")) {
                    edit.putLong(fName, mField.getLong(this));
                } else if (type.toString().endsWith("float")) {
                    edit.putFloat(fName, mField.getFloat(this));
                } else if (type.toString().endsWith("int")) {
                    edit.putInt(fName, mField.getInt(this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        edit.commit();
    }
}
