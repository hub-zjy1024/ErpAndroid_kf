package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.b1b.js.erpandroid_kf.mvcontract.callback.IDataListCallback;

import java.util.List;

/**
 * Created by 张建宇 on 2019/7/26.
 */
public abstract class AbsListDataContract<T> {
    public AbsListDataContract() {
    }

    public static abstract class BasePresenter<F> {
        IView<F> iView;
        Context mContext;
        IProvider<F> mProvider;

        public BasePresenter(IView<F> iView, Context mContext) {
            this.iView = iView;
            this.mContext = mContext;
            this.mProvider = intProVider();
        }

        abstract IProvider<F> intProVider();

        public abstract void getData(final Object[] params);
    }


    public interface IView<T> {
        void fillList(List<T> infos);

        void loading(String msg);

        void cancelLoading();

        void alert(String msg);
    }


    public interface IProvider<T> {
        void getData(IDataListCallback<T> callback, Object[] param);
    }

    static class BaseProvider<T> implements IProvider<T> {
        protected Handler mHandler = new Handler(Looper.getMainLooper());
        protected Context  mContext;

        public BaseProvider(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void getData(IDataListCallback<T> callback, Object[] param) {

        }
    }
}
