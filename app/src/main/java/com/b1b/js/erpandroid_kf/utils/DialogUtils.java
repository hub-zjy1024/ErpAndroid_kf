package com.b1b.js.erpandroid_kf.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 Created by 张建宇 on 2017/8/1. */

public class DialogUtils {
    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static Dialog getSpAlert(Context mContext, String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(true);
        return builder.create();
    }

    public static Dialog getSpAlert(Context mContext, String msg, String title, Dialog.OnClickListener ll, String lStr,
                                    DialogInterface
                                            .OnClickListener rl, String rStr) {
        AlertDialog dialog = (AlertDialog) getSpAlert(mContext, msg, title);
        dialog.setButton2(lStr, ll);
        dialog.setButton(rStr, rl);
        return dialog;
    }
}
