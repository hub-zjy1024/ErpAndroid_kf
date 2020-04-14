package com.b1b.js.erpandroid_kf.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 张建宇 on 2020/4/8.
 */
public class ShechduleMgr {
    ScheduledExecutorService scheduledExecutorService;

    static class InnerSigler{
        static ShechduleMgr ShechduleMgr = new ShechduleMgr();
    }
    private ShechduleMgr(){
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
        hashSet = new HashSet<>();
    }

    public Set<String> hashSet;

    public static ShechduleMgr getInstance() {
        return InnerSigler.ShechduleMgr;
    }

    public void executeByDur(Runnable mRun, long time) {
        if(hashSet.contains(mRun.toString())){
            return;
        }
        scheduledExecutorService.scheduleWithFixedDelay(mRun, 0, time, TimeUnit.SECONDS);
        hashSet.add(mRun.toString());
    }
    public void close() {
        hashSet.clear();
        scheduledExecutorService.shutdown();
        scheduledExecutorService = null;
    }
}
