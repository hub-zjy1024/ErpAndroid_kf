package utils.camera;


import android.hardware.Camera;
import android.os.AsyncTask;

/**
 Created by 张建宇 on 2018/3/28. */
public class CustomAutoFocus implements android.hardware.Camera.AutoFocusCallback {
    public static final long AUTO_INTERVAL = 700L;
    public boolean isStopped = false;

    @Override
    public void onAutoFocus(boolean success, android.hardware.Camera camera) {
        if (isStopped) {
            return;
        }
        new FocusTask(this).execute();
    }

    private Camera camera;

    public CustomAutoFocus(Camera camera) {
        this.camera = camera;
    }

    private void startFocus() {
        try {
            camera.autoFocus(this);
        } catch (RuntimeException e) {
            e.printStackTrace();
            new FocusTask(this).execute();
        }
    }

    static class FocusTask extends AsyncTask<Void, Void, Void> {

        CustomAutoFocus mgr;

        public FocusTask(CustomAutoFocus mgr) {
            this.mgr = mgr;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(AUTO_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mgr.isStopped) {
                return null;
            }
            mgr.startFocus();
            return null;
        }
    }

    public void start() {
        isStopped = false;
        startFocus();
    }

    public void stop() {
        isStopped = true;
    }
}
