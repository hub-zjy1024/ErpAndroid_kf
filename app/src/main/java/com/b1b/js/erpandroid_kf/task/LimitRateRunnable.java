package com.b1b.js.erpandroid_kf.task;

import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 张建宇 on 2020/1/2.
 */
public class LimitRateRunnable implements Runnable {
    static HashMap<String, LinkedBlockingQueue<Runnable>> mSet = new HashMap<>();
    private String mTag = "";
    private static int maxTask = 3;
    private Runnable realRun;

    public LimitRateRunnable(String mTag, Runnable mRun) {
        this.mTag = mTag;
        this.realRun = mRun;
        synchronized (LimitRateRunnable.class) {
            LinkedBlockingQueue<Runnable> strings = mSet.get(mTag);
            if (strings ==null){
                strings = new LinkedBlockingQueue<>(maxTask);
                Log.e("zjy", "LimitRateRunnable->LimitRateRunnable(): newLog==" + mTag.hashCode());
                mSet.put(mTag, strings);
            }
            boolean offer = strings.offer(mRun);
            Log.w("zjy",
                    "LimitRateRunnable->LimitRateRunnable(): add==" + offer + "\t,run=" + mRun.toString()+ "\t,tagHash=" + mTag.hashCode());

        }
    }

    @Override

    public void run() {

        LinkedBlockingQueue<Runnable> mRuns;
        synchronized (LimitRateRunnable.class) {
            mRuns = mSet.get(mTag);

            //            Runnable poll = mRuns.poll();
            if (mRuns != null) {
                int size = mRuns.size();
                Log.w("zjy", "LimitRateRunnable->run(): queque size==" + size);
                Runnable poll = mRuns.peek();
                realRun = poll;
//                remove、element、offer 、poll、peek 其实是属于Queue接口。
            } else {
                Log.e("zjy", "LimitRateRunnable->run(): queue is null==" + mTag);
            }
        }
        if (realRun != null) {
            realRun.run();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (LimitRateRunnable.class) {
                if (mRuns != null) {
                    mRuns.remove(realRun);
                }
            }
        }
    }
}
