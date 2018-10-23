package utils;

import android.support.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Created by 张建宇 on 2018/2/27. */
public class PicUploadThreadPool {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    private static final int defaultCapcity = 50;


    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "PicUploadThreadPool #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(defaultCapcity);

    private PicUploadThreadPool() {
    }

    /**
     An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    static {
        THREAD_POOL_EXECUTOR.prestartAllCoreThreads();
    }

    public static void execute(@NonNull Runnable command) throws RejectedExecutionException {
        try {
            THREAD_POOL_EXECUTOR.execute(command);
        } catch (RejectedExecutionException e) {
            throw new RejectedExecutionException(e);
        }
    }

    public static synchronized int getTaskCounts() {
        return (int) THREAD_POOL_EXECUTOR.getTaskCount();
    }

    public static synchronized int getPoolSize() {
        return THREAD_POOL_EXECUTOR.getPoolSize();
    }

    public static synchronized int getMaximumPoolSize() {
        return THREAD_POOL_EXECUTOR.getMaximumPoolSize();
    }
}
