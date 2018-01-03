package utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * Created by 张建宇 on 2016/12/16.
 */

public class MyToast {
    private static Toast mToast;

    /**
     * 显示Toast
     *
     * @param context 上下文路径
     * @param msg     要显示的消息
     */
    public static void showToast(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

    public static boolean checkNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    private MyToast() {
    }
}
