package utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 Created by 张建宇 on 2017/6/7. */

public class SoftKeyboardUtils {
    public static void closeInputMethod(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void closeInputMethod2(View view, Context mContext) {

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        Log.e("zjy", "SoftKeyboardUtils->closeInputMethod2(): isActive==" + imm.isActive());
        // 如果开启
//        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
//                InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                0);

    }
    /**
     * 必须在setContentView之前使用！！！！
     *
     * @param activity
     */
    public static void hideKeyBoard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
