package utils.framwork;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Method;

/**
 * Created by 张建宇 on 2018/11/6.
 */
public class StatubarHider {
    private static final String lTag = "zjy——StatubarHider";
    public static void setStatubarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        //设置透明状态栏,这样才能让 ContentView 向上
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(statusColor);
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        } else {

        }
    }

    public static void hide2(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(statusColor);
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                ViewCompat.setFitsSystemWindows(mChildView, true);
            }
        }
    }

    public void hide4_4(Activity activity) {
        Window window = activity.getWindow();
        ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        //首先使 ChildView 不预留空间
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }

        int statusBarHeight = getStatusBarHeight(activity);
        //需要设置这个 flag 才能设置状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //避免多次调用该方法时,多次移除了 View
        if (mChildView != null && mChildView.getLayoutParams() != null && mChildView.getLayoutParams()
                .height == statusBarHeight) {
            //移除假的 View.
            mContentView.removeView(mChildView);
            mChildView = mContentView.getChildAt(0);
        }
        if (mChildView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
            //清除 ChildView 的 marginTop 属性
            if (lp != null && lp.topMargin >= statusBarHeight) {
                lp.topMargin -= statusBarHeight;
                mChildView.setLayoutParams(lp);
            }
        }
    }


    /**
     * 标题栏高度
     * @return
     */
    public static int getTitleHeight(Activity activity){
        View contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        Log.e("zjy", "->getTitleHeight(): ==" + contentView.getTop());
        Log.e("zjy", "->getContentBottom(): ==" + contentView.getBottom());
        return  contentView.getTop();
    }
    //获取屏幕原始尺寸高度，包括虚拟功能键高度
    public static int getRealHeight(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(displayMetrics);
        } else {
            @SuppressWarnings("rawtypes")
            Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked")
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, displayMetrics);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dpi = displayMetrics.heightPixels;
        return dpi;
    }

    public static int getStatusBarHeightReflect(Activity activity) {
        int statusHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = activity.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     *
     * 在onWindowFocusChanged（）方法中调用
     *
     * @param activity
     */
    public static void getDimen(Activity activity) {

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e(lTag, "屏幕高:" + dm.heightPixels);

        //应用区域
        Rect outRect1 = new Rect();
        activity. getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        Log.e(lTag, "应用区顶部" + outRect1.top);
        Log.e(lTag, "应用区高" + outRect1.height());

        //View绘制区域
        Rect outRect2 = new Rect();
        activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect2);
        Log.e(lTag, "View绘制区域顶部-错误方法：" + outRect2.top);
        //不能像上边一样由outRect2.top获取，这种方式获得的top是0，可能是bug吧
        int viewTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();   //要用这种方法

        Log.e(lTag, "View绘制区域顶部-正确方法：" + viewTop);
        Log.e(lTag, "View绘制区域高度：" + outRect2.height());
    }

    public int getSbarNowHeight(Activity activity) {
        int height = 0;
        Rect rectangle = new Rect();
       activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        //高度为rectangle.top-0仍为rectangle.top
        height = rectangle.top;
                Log.e(lTag, "状态栏-方法3:" + rectangle.top);
        return height;
    }
}
