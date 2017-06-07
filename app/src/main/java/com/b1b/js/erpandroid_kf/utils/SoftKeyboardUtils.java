package com.b1b.js.erpandroid_kf.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by 张建宇 on 2017/6/7.
 */

public class SoftKeyboardUtils {
    public static void closeInputMethod(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager
                    .HIDE_NOT_ALWAYS);
        }
    }
}
