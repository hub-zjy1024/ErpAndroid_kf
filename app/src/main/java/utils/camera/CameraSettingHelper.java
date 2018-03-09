package utils.camera;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Surface;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 Created by 张建宇 on 2018/3/1. */
public class CameraSettingHelper {
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该旋转的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该旋转的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        return degree;
    }

    /**
     默认使用最大的预览尺寸，以便于获取最清晰的预览画面(测试发现有些不兼容)
     @param parameters
     */
    public static Point getSuitablePreviewSize(Camera.Parameters parameters, int screenW, int screenH) {
        Camera.Size defSize = parameters.getPreviewSize();
        if (defSize.width == screenH && defSize.height == screenW) {
            return null;
        }
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int px1 = lhs.width * lhs.height;
                int px2 = rhs.width * rhs.height;
                if (px1 > px2) {
                    return -1;
                } else if (px1 == px2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        int tWidth = 0;
        int tHeight = 0;
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Camera.Size tSize = supportedPreviewSizes.get(i);
            if (screenH == tSize.width && tSize.height == screenW) {
                tWidth = tSize.width;
                tHeight = tSize.height;
                return new Point(tWidth, tHeight);
            } else if (screenH > tSize.width) {
                float rate = tSize.width / (float) tSize.height;
                float screenRate = screenH / (float) screenW;
                float res = Math.abs(rate - screenRate);
                if (res < 0.23) {
                    tWidth = tSize.width;
                    tHeight = tSize.height;
                    break;
                }
            }
        }
        if (tWidth == 0 && tHeight == 0) {
            return null;
        }
        return new Point(tWidth, tHeight);
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
