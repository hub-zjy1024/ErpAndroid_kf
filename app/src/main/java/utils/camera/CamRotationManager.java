package utils.camera;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

/**
 * Created by 张建宇 on 2018/8/2.
 */
public class CamRotationManager {
    private int rotation = 0;
    private OrientationEventListener mOrientationListener = null;

    public CamRotationManager(Context mContext) {
        mOrientationListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotation = getProperRotation(orientation);
            }
        };
    }

    /**
     * 此方法配合OrientationEventListener使用
     *
     * @param rot 传感器的角度
     * @return 成像图片应该旋转的角度
     */
    private int getProperRotation(int rot) {
        int degree = 0;
        //根据传感器的方向获取拍照成像的方向
        if (rot > 240 && rot < 300) {
            degree = 270;
        } else if (rot > 60 && rot < 120) {
            degree = 90;
        }
        return degree;
    }

    /**
     * 添加屏幕旋转监听
     */
    public void attachToSensor() {
        if (mOrientationListener != null) {
            mOrientationListener.enable();
        }
    }
    public void disable(){
        mOrientationListener.disable();
    }

    public int getRotation() {
        return rotation;
    }
}
