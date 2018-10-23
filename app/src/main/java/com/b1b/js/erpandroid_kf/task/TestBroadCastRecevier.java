package com.b1b.js.erpandroid_kf.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.b1b.js.erpandroid_kf.receiver.AlarmRepeatReceive;
import com.b1b.js.erpandroid_kf.receiver.OneShotReceiver;

import java.util.Calendar;

/**
 * Created by 张建宇 on 2019/5/28.
 */
public class TestBroadCastRecevier {

    private Context mContext;

    public TestBroadCastRecevier(Context mContext) {
        this.mContext = mContext;
    }

    public void registerOnshotAlarm(Context mContext) {
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction(mContext.getPackageName() + ".alarm.oneshot");
        mContext.registerReceiver(new OneShotReceiver(), alarmFilter);
    }

    public void setRepeatAlarm() {
        Intent intent = new Intent(mContext,
                AlarmRepeatReceive.class);
        intent.setAction(mContext.getPackageName() + ".alarm.repeat");
        PendingIntent sender = PendingIntent.getBroadcast(
                mContext, 0, intent, 0);
        // We want the alarm to go off 10 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        // Schedule the alarm!
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), 60 * 1000, sender);
        }
    }
    public void oneShotAlarm(){
        if (!CheckUtils.isAdmin()) {
            return;
        }
        Context mContext = this.mContext;
        Intent intent = new Intent(mContext, OneShotReceiver.class);
        intent.setAction(mContext.getPackageName() + ".alarm.oneshot");
        PendingIntent sender = PendingIntent.getBroadcast(
                mContext, 0, intent, 0);
        // We want the alarm to go off 10 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }
}
