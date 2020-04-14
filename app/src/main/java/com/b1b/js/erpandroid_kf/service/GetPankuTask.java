package com.b1b.js.erpandroid_kf.service;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.MyPankuListActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2020/4/8.
 */
public class GetPankuTask implements Runnable {

    NotificationManagerCompat manager;

    private Context mContext;

    public static int notifyId = 10000000;

    public GetPankuTask(Context mContext) {
        this.mContext = mContext;
    }


    String uid;

    public GetPankuTask(Context mContext, String uid) {
        this.mContext = mContext;
        this.uid = uid;
    }

    @NonNull
    @Override
    public String toString() {
        return "taks-panku-list";
    }

    public void getTask(final String uid) {
        //        ChuKuServer.GetPanKuLog("");
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                String contentText = "";
                try {
                    String mTask = ChuKuServer.GetPanKuTask(uid);
                    JSONObject mObj = JSONObject.parseObject(mTask);
                    JSONArray mArr = mObj.getJSONArray("表");
                    int mCount = mArr.size();
                    contentText = String.format("还有%s条数据待盘库,点击前往", mCount);
                    sendPankuTaskMsg(contentText);
                } catch (JSONException e) {
                    e.printStackTrace();
                    contentText = "";
                } catch (IOException e) {
                    e.printStackTrace();
                    sendPankuTaskMsg("待办异常," + e.getMessage());
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                    MyApp.myLogger.writeError(e, "get panKuTask error");
                }
            }
        };
        TaskManager.getInstance().execute(mRun);
    }

    public void initNotification() {

    }

    public static final String Notify_G_ID_Other = "其他消息";
    public static final String Notify_Channel_ID_PANKU = "待办事项";

    public void sendPankuTaskMsg(String contentText) {
        int msgImportant = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            msgImportant = NotificationManager.IMPORTANCE_HIGH;
        }
        String channelId =Notify_Channel_ID_PANKU;
        sendMsg("收到新的盘库任务", contentText, channelId, msgImportant, channelId);

    }

    public void sendMsg(String subTitle, String msg, String title, int proprity,
                        String mChannelId) {

        NotificationManager mgr =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent mIntent = new Intent(mContext, MyPankuListActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 300, mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = title;
            String channelName = title;

            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    proprity);
            // 开启指示灯，如果设备有的话
            channel.enableLights(true);
            // 设置指示灯颜色
        //    channel.setLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            channel.setLightColor(Color.WHITE);
            // 是否在久按桌面图标时显示此渠道的通知
            channel.setShowBadge(true);
            // 设置是否应在锁定屏幕上显示此频道的通知
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            // 设置绕过免打扰模式
            channel.setBypassDnd(true);

            ArrayList<NotificationChannelGroup> groups = new ArrayList<>();
            String groupId = "上传消息";
            String groupName = "上传消息";
            NotificationChannelGroup group = new NotificationChannelGroup(groupId, groupName);
            channel.setGroup(groupId);
            groups.add(group);
            String groupDownloadId =Notify_G_ID_Other;
            CharSequence groupDownloadName = "其他消息";
            NotificationChannelGroup group_download =
                    new NotificationChannelGroup(groupDownloadId, groupDownloadName);
            groups.add(group_download);
            mgr.createNotificationChannelGroups(groups);
            mgr.createNotificationChannel(channel);

            builder.setChannelId(mChannelId);
           // builder.setGroup(mGroupId);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.app_logo);
        //                    Notification.Builder mbuider = new Notification.Builder(mContext, "");
        builder/*.addAction(R.mipmap.app_logo, "待办", pIntent)*/.setContentTitle(subTitle).setSmallIcon(R.mipmap.app_logo)
                .setAutoCancel(true).setContentText(msg).setLargeIcon(largeIcon)
                .setContentIntent(pIntent);
        String tag = mContext.getPackageName();
        mgr.notify(tag, notifyId, builder.build());
    }

    @Override
    public void run() {
        getTask(uid);
    }
}
