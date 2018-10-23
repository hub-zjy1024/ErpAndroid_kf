package utils.common;

import android.util.Log;
import android.view.View;

/**
 * Created by 张建宇 on 2019/7/24.
 */
public class ViewDebugUtils {
    void checkMMode(int mSpec, String tag) {
        int mode2 = View.MeasureSpec.getMode(mSpec);

        if (mode2 == View.MeasureSpec.EXACTLY) {

            Log.e("zjy",
                    getClass() + "->checkMMode() mode2 " +
                            "" + tag +
                            "" +
                            " ,EXACTLY);");
        } else if (mode2 == View.MeasureSpec.AT_MOST) {
            Log.e("zjy",
                    getClass() + "->checkMMode() mode2 " +
                            "" + tag +
                            "" +
                            " ,AT_MOST);");
        } else if (mode2 == View.MeasureSpec.UNSPECIFIED) {

            Log.e("zjy",
                    getClass() + "->checkMMode() mode2 " +
                            "" + tag +
                            "" +
                            " ,UNSPECIFIED);");
        } else {
            Log.e("zjy",
                    getClass() + "->checkMMode() mode2 " +
                            "" + tag +
                            "" +
                            " ,Other);");
        }
    }
}
