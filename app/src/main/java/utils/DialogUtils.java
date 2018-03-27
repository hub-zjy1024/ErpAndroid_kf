package utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 Created by 张建宇 on 2017/8/1. */

public class DialogUtils {
    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static void cancelDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
    }

    public static Dialog getSpAlert(Context mContext, String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(true);
        return builder.create();
    }

    public static void safeShowDialog(Context mContext, Dialog dialog) {
        boolean finishing = ((Activity) mContext).isFinishing();
        if (!finishing) {
            dialog.show();
        }
    }

    public static Dialog getSpAlert(Context mContext, String msg, String title, Dialog.OnClickListener ll, String lStr,
                                    DialogInterface
                                            .OnClickListener rl, String rStr) {
        AlertDialog dialog = (AlertDialog) getSpAlert(mContext, msg, title);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, lStr, ll);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, rStr, rl);
        return dialog;
    }

    public void setWindowAlpha(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 1.0f;
        activity.getWindow().setAttributes(lp);
    }
}
