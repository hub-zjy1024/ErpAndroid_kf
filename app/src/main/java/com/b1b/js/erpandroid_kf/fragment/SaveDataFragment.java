package com.b1b.js.erpandroid_kf.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoActivity;
import com.b1b.js.erpandroid_kf.config.ExtraParams;

/**
 * Created by 张建宇 on 2019/12/3.
 */
public class SaveDataFragment extends BaseFragment {
    private String mLoginId = "";
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        SavedLoginInfoActivity mAc = (SavedLoginInfoActivity) mParent;
        mLoginId = mAc.getLoginID();
        outState.putString(ExtraParams.NM_LOGIN_ID, mLoginId);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mLoginId = savedInstanceState.getString(ExtraParams.NM_LOGIN_ID);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof SavedLoginInfoActivity) {
            mParent = (SavedLoginInfoActivity) getActivity();
        } else {
            throw new IllegalStateException("此fragment父类必须继承" + SavedLoginInfoActivity.class.getName());
        }
    }
}
