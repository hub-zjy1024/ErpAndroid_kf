package com.b1b.js.erpandroid_kf.task;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Created by 张建宇 on 2018/2/6. */

public class TaskManager {
    private ThreadPoolExecutor threadPoolExecutor;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int DEFAULT_CORE = 5;
    private static final int DEFAULT_MAX = 9;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 60;
    private static final int defaultCapcity = 50;

    private static final ThreadFactory mTaskFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "TaskManager #" + mCount.getAndIncrement());
        }
    };
    private static BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(defaultCapcity);
    private static TaskManager tm;

    private TaskManager(int core, int max, int maxTasks) {
        Log.e("zjy", "TaskManager->TaskManager(): newInstance==");
        sPoolWorkQueue = new LinkedBlockingQueue<>(maxTasks);
        threadPoolExecutor = new ThreadPoolExecutor(core, max,
                KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, mTaskFactory);
        threadPoolExecutor.prestartAllCoreThreads();
    }

    public static TaskManager getInstance() {
        return getInstance(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE);
    }

    public static TaskManager getInstance(int core, int max) {
        return getInstance(core, max, defaultCapcity);
    }

    /**
     单例：双重非空校验，并且线程安全
     @param core
     @param max
     @param maxTasks
     @return
     */
    public static TaskManager getInstance(int core, int max, int maxTasks) {
        if(tm==null){
            synchronized (TaskManager.class){
                if(tm==null){
                    tm=new TaskManager(core, max, maxTasks);
                }
            }
        }
        return tm;
    }

    public boolean execute(Runnable runnable) {
        boolean addSuccess = false;
        try {
            threadPoolExecutor.execute(runnable);
            addSuccess = true;
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
        return addSuccess;
    }

    public boolean execute(Runnable runnable, Context mContext) {
        boolean addSuccess = false;
        try {
            threadPoolExecutor.execute(runnable);
            addSuccess = true;
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(mContext, "当前任务达到上限，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }
        return addSuccess;
    }

    public <T> Future<T> submit(Callable<T> callable) {
        Future<T> result = null;
        try {
            result = threadPoolExecutor.submit(callable);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }

}
