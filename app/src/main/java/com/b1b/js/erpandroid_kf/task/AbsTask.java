package com.b1b.js.erpandroid_kf.task;

import android.os.Handler;

/**
 * Created by 张建宇 on 2019/5/28.
 */
public class AbsTask {
    private Handler mHandler;

    interface ICallBack<T>{
        void onSuccess(T t);
        void onError(Throwable e);
    }

    interface ICallBack2 extends ICallBack {
        void onFailed(String msg);
    }
    public static class Observerable<T> {
        Observerable<T> mObj;

        void create(MRun run) {
            run.run();
        }

        Observerable map(Observerable<T> mobs) {
            return this;
        }

        void subscribe(ICallBack<T> t) {

        }
    }

    public static class MRun implements Runnable{
        @Override
        public void run() {

        }
    }

    public void startBgtask(Runnable m, ICallBack2 mCallback) {
        TaskManager manager = TaskManager.getInstance();
        manager.execute(m);
    }
}
