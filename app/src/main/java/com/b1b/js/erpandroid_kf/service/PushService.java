package com.b1b.js.erpandroid_kf.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.b1b.js.erpandroid_kf.KucunFBActivity;
import com.b1b.js.erpandroid_kf.R;
import utils.UploadUtils;
import utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;

public class PushService extends Service {
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private NotificationManager notifyManager;
    private Thread mWorker;
    int notifyTimes = 0;

    public PushService() {
    }

    @Override
    public void onCreate() {
        //        GetInseorageBalanceInfoToSenderResult
        //        GetInstorageBalanceInfoNew
        //        GetInseorageBalanceInfoToCount
        Log.e("zjy", "PushService->onCreate(): ==");
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mWorker = new Thread() {
            @Override
            public void run() {
                boolean flag = false;
                SharedPreferences sp = getSharedPreferences("notify_flag", MODE_PRIVATE);
                final String loacalDate = sp.getString("date", "");
                String current = UploadUtils.getCurrentDate();
                sp.edit().clear().commit();
                if (!loacalDate.equals(current)) {
                    sp.edit().putString("date", current).putInt("counts", 0).commit();
                    notifyTimes = 0;
                    //                        AlarmManager manager = getSystemService(Context.ALARM_SERVICE);
                }
                notifyTimes = sp.getInt("counts", 0);
                if (notifyTimes < 3) {
                    while (!flag) {
                        Date date = new Date();
                        int hours = date.getHours();
                        int minute = date.getMinutes();
                        if ((hours == 10 && minute < 10) || (hours == 15 && minute < 10)) {
                            SoapObject request2 = WebserviceUtils.getRequest(null, "GetInseorageBalanceInfoToCount");
                            try {
                                SoapPrimitive response2 = WebserviceUtils.getSoapPrimitiveResponse(request2, SoapEnvelope
                                                .VER11,
                                        WebserviceUtils.MartService);
                                Log.e("zjy", "PushService->run(): GetInseorange==" + response2.toString());
                                sp.edit().putString("content", response2.toString()).commit();

                                NotificationCompat.Builder notice = new NotificationCompat.Builder(PushService.this);
                                notice.setContentText("当前有" + response2.toString() + "条库存发布消息需要进行处理");
                                notice.setContentTitle("消息通知");
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.notify_icon_large);
                                notice.setSmallIcon(R.mipmap.notify_icon);
                                notice.setLargeIcon(bitmap);
                                notice.setWhen(System.currentTimeMillis());
                                Intent click = new Intent(PushService.this,
                                        KucunFBActivity.class);
                                PendingIntent pintent = PendingIntent.getActivity(PushService.this, 100, click,
                                        PendingIntent
                                                .FLAG_UPDATE_CURRENT);
                                notice.setContentIntent(pintent);
                                notice.setAutoCancel(true);
                                notice.setVibrate(new long[]{0, 2000});
                                notifyManager.notify(0, notice.build());
                                notifyTimes++;
                                if (notifyTimes >= 2) {
                                    flag = true;
                                }
                                sp.edit().putInt("counts", notifyTimes).commit();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(60 * 5 * 1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(5 * 60 * 1000);
                                Log.e("zjy", "PushService->run(): Thread.id==" + Thread.currentThread().getId());
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
    }

    private void notifyMessage(String id) {
        if (id.equals("101") || id.equals("1415 ") || id.equals("3548")) {
            if (!mWorker.isAlive()) {
                mWorker.start();
            }
        }
    }

    private MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("zjy", "PushService.java->onBind(): ==");
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("zjy", "PushService->onStartCommand(): on==");
        String id = intent.getStringExtra("id");
        notifyMessage(id);
        return START_REDELIVER_INTENT;
    }

    public class MyBinder extends Binder {
        public void upLoad() {
            Log.e("zjy", "PushService.java->upLoad(): ==");
        }
    }
}
