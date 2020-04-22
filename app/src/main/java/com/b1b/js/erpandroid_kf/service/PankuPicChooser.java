package com.b1b.js.erpandroid_kf.service;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.ObtainPicPanku;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.RukuTagPrintAcitivity;
import com.b1b.js.erpandroid_kf.TakePicChildPanku;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;

/**
 * Created by 张建宇 on 2020/4/20.
 */

public class PankuPicChooser {

    private Context mContext;

    public PankuPicChooser(Context mContext) {
        this.mContext = mContext;
    }

    private AlertDialog choiceMethodDialog;


    public void openPrintPage(final String detailID) {
        Intent mINten = new Intent(mContext, RukuTagPrintAcitivity.class);
        mINten.putExtra(RukuTagPrintAcitivity.extra_DPID, detailID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mContext instanceof Activity) {
                mContext.startActivity(mINten,
                        ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle()
                );
            } else {
                mContext.startActivity(mINten);
            }
        } else {
            mContext.startActivity(mINten);
        }
    }

    public void openPrintPageWithShared(final String detailID, View sharedView) {
        Intent mINten = new Intent(mContext, RukuTagPrintAcitivity.class);
        mINten.putExtra(RukuTagPrintAcitivity.extra_DPID, detailID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mContext instanceof Activity) {
                mContext.startActivity(mINten,
                        ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,sharedView,mContext.getResources().getString(R.string.transition_ruku_tag_detailPid)).toBundle()
                );
            } else {
                mContext.startActivity(mINten);
            }
        } else {
            mContext.startActivity(mINten);
        }
    }

    public void openTakePic(final String detailID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("关联图片");
        builder.setItems(new String[]{"拍照", "从手机选择"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApp.myLogger.writeInfo("菜单拍照：" + which);
                Intent intent = new Intent();
                intent.putExtra(IntentKeys.key_pid, detailID);
                switch (which) {
                    case 0:
                        intent.setClass(mContext, TakePicChildPanku.class);
                        MyApp.myLogger.writeInfo("takepic_panku");
                        break;
                    case 1:
                        intent.setClass(mContext, ObtainPicPanku.class);
                        MyApp.myLogger.writeInfo("obtain_panku");
                        break;
                    case 2:
                        break;
                }
                mContext.startActivity(intent);
            }
        });
        if (choiceMethodDialog != null && choiceMethodDialog.isShowing()) {
            choiceMethodDialog.cancel();
        }
        choiceMethodDialog = builder.show();
    }
}
