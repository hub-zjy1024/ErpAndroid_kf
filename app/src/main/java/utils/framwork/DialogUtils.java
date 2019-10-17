package utils.framwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.myview.ScrollViewHasMaxHeight;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 张建宇 on 2017/8/1.
 */

public class DialogUtils {
    private SparseArray<Dialog> mPds = new SparseArray<>();
    private static AtomicInteger mit = new AtomicInteger(0);
    private Context mContext;

    public DialogUtils(Context mContext) {
        this.mContext = mContext;
    }

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

    public int showAlertWithId(String msg) {
        Dialog proDialog = getDialog(mContext).showCommMsgDialog(msg);
        int andIncrement = mit.getAndIncrement();
        mPds.put(andIncrement, proDialog);
        return andIncrement;
    }

    public void cancelAll() {
        for (int i = 0; i < mPds.size(); i++) {
            int i1 = mPds.keyAt(i);
            cancelDialogById(i1);
        }
        mPds.clear();
    }

    public int showProgressWithID(String msg) {
        ProgressDialog proDialog = new ProgressDialog(mContext);
        proDialog.setTitle("请稍后");
        proDialog.setMessage(msg);
        proDialog.show();
        Window window = proDialog.getWindow();
        if (window != null) {
            proDialog.show();
        } else {
            MyApp.myLogger.writeError("showProgressWithID  no Window==");
            Log.e("zjy", getClass() + "->showProgressWithID(): no Window==" + msg);
        }
        int andIncrement = mit.getAndIncrement();
        mPds.put(andIncrement, proDialog);
        return andIncrement;
    }

    public void cancelDialogById(int pdId) {
        ProgressDialog proDialog = (ProgressDialog) mPds.get(pdId);
        if (proDialog != null) {
            proDialog.cancel();
            mPds.remove(pdId);
        }
    }

    public static Builder getDialog(Context mContext) {
        return new Builder(mContext);
    }

    public static class Builder {
        private Context mContext;
        private String msg;
        private String btn1;
        private String btn2;
        private DialogInterface.OnClickListener btn1L;
        private DialogInterface.OnClickListener btn2L;
        private View itemView;
        private boolean cancelAble = true;
        AlertDialog alertDialog;

        private float tvHeightRate = 0.6f;
        float maxHeight;
        public Builder(Context mContext) {
            this.mContext = mContext;
            Resources resources = mContext.getResources();
            alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.getWindow().setBackgroundDrawable(resources.getDrawable(R.drawable.material_dialog_bg));
            View mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_common_contentview, null,
                    false);
            itemView = mView;
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            maxHeight= tvHeightRate * heightPixels;
        }

        public Builder setBtn1(String msg) {
            this.btn1 = msg;
            return this;
        }

        public Builder setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setCancelable(boolean cancelAble) {
            this.btn1 = msg;
            return this;
        }

        public Builder setBtn2(String msg) {
            this.btn2 = msg;
            return this;
        }

        public Dialog showCommMsgDialog(String msg) {
            Dialog dialog =
                    setBtn1("确认").setMsg(msg).create();
            return dialog;
        }
        public Dialog create() {
            View mView = itemView;
            final TextView mContent = (TextView) mView.findViewById(R.id.dialog_cm_tv);
            Button btnLeft = (Button) mView.findViewById(R.id.dialog_cm_btn_left);
            btnLeft.setText(btn1);
            Button btnRight = (Button) mView.findViewById(R.id.dialog_cm_btn_right);
            ScrollViewHasMaxHeight sroller = (ScrollViewHasMaxHeight) mView.findViewById(R.id.dialog_cm_scrollview);
            sroller.setMaxHeight((int) maxHeight);
            btnRight.setText(btn2);
            LinearLayout btnContainers = (LinearLayout) mView.findViewById(R.id.dialog_cm_btns);
            TextView tvDivider2 = (TextView) mView.findViewById(R.id.dialog_cm_divider2);
            if (btn2 == null) {
                tvDivider2.setVisibility(View.GONE);
                btnRight.setVisibility(View.GONE);
            }
            btnLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                    if (btn1L != null) {
                        btn1L.onClick(alertDialog, 1);
                    }
                }
            });
            alertDialog.setCancelable(cancelAble);
            btnRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                    if (btn2L != null) {
                        btn2L.onClick(alertDialog, 2);
                    }
                }
            });
            mContent.setText(msg);
            mContent.setTextColor(Color.BLACK);
            alertDialog.show();
            alertDialog.setContentView(mView);
            return alertDialog;
        }

        public Builder setBtn1L(DialogInterface.OnClickListener btn1L) {

            this.btn1L = btn1L;
            return this;
        }

        public Builder setBtn2L(DialogInterface.OnClickListener btn2L) {
            this.btn2L = btn2L;
            return this;
        }
    }
    public void createCommDialog(String msg, String btn1, final DialogInterface.OnClickListener listener1,
                                 String btn2, final DialogInterface.OnClickListener listener2) {
        Resources resources = mContext.getResources();
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        //        ViewParent parent = mView.getParent();
        //        if (parent != null) {
        //            ViewGroup mp = (ViewGroup) (parent);
        //            mp.removeView(mView);
        //        }
        alertDialog.setCancelable(true);
        alertDialog.getWindow().setBackgroundDrawable(resources.getDrawable(R.drawable.material_dialog_bg));
        View mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_common_contentview, null, false);
        TextView mContent = (TextView) mView.findViewById(R.id.dialog_cm_tv);
        Button btnLeft = (Button) mView.findViewById(R.id.dialog_cm_btn_left);
        btnLeft.setText(btn1);
        Button btnRight = (Button) mView.findViewById(R.id.dialog_cm_btn_right);
        btnRight.setText(btn2);
        LinearLayout btnContainers = (LinearLayout) mView.findViewById(R.id.dialog_cm_btns);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener1.onClick(alertDialog, 1);
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener2.onClick(alertDialog, 2);
            }
        });
        mContent.setText(msg);
        mContent.setTextColor(resources.getColor(R.color.color_green));
        alertDialog.setContentView(mView);
        alertDialog.show();
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

    public static Dialog getSpAlert(Context mContext, String msg, String title, Dialog.OnClickListener ll,
                                    String lStr,
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
