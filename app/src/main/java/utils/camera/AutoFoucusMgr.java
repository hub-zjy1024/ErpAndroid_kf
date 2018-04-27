/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

public class AutoFoucusMgr implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFoucusMgr.class.getSimpleName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 700;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    private long autoFuocusTime = AUTO_FOCUS_INTERVAL_MS;


    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>();
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private boolean stopped = false;
    private boolean useAutoFocus;
    private final Camera camera;

    public AutoFoucusMgr(long autoDelay, Camera camera) {
        this.autoFuocusTime = autoDelay;
        this.camera = camera;
    }

    public AutoFoucusMgr(Camera camera) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        //        setAutoFoucs(camera);
        Log.e(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        if (success) {
            theCamera.cancelAutoFocus();
        }
        autoFocusAgainLater();
    }

    private synchronized void autoFocusAgainLater() {
        AutoFocusTask newTask = new AutoFocusTask(this, autoFuocusTime);
        try {
            newTask.execute();
        } catch (RejectedExecutionException ree) {
            Log.w(TAG, "Could not request auto focus", ree);
        }
    }

    public synchronized void start() {
        stopped = false;
        if (useAutoFocus) {
            if (camera != null) {
                try {
                    camera.autoFocus(this);
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+;
                    // continue?
                    Log.w(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }

    public synchronized void start(boolean isPreview) {
        if (useAutoFocus) {
            if (camera != null) {
                try {
                    if (!isPreview) {
                        return;
                    }
                    camera.autoFocus(this);
                } catch (RuntimeException re) {
                    Log.w(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }


    public synchronized void stop() {
        stopped = true;
        try {
            camera.cancelAutoFocus();
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    static class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        private AutoFoucusMgr mgr;
        private long autoFuocusTime;

        public AutoFocusTask(AutoFoucusMgr mgr, long autoFuocusTime) {
            this.mgr = mgr;
            this.autoFuocusTime = autoFuocusTime;
        }

        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(autoFuocusTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!mgr.stopped) {
                mgr.start();
            }
            return null;
        }
    }

}
