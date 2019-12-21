package utils.common.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.config.SpSettings;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import utils.common.UpdateClient;
import utils.common.UploadUtils;
import utils.net.ftp.FTPUtils;

/**
 * Created by 张建宇 on 2019/5/28.
 */
public class LogUploader {
    public static final String remoteDir = "/Zjy/log_kf/";
    public String saveDir;
    public static final int LOG_UPLOAD_SUCCESS = 20;
    public static final int LOG_UPLOAD_FAIL = 21;
    private Context mContext;

    final String targeUrl = UpdateClient.logCheckURL;
    public static final String logFileName ="dyj_log.txt";

    private  final String tagStr = "date";
    private int startTime = 9;
    private SharedPreferences sp;
    private int endTime = 20;
    private long fileSize = 0L;
    private int count = 0;
    private long lastUpTime;

    public LogUploader(Context mContext) {
        this.mContext = mContext;
        sp = mContext.getSharedPreferences(SpSettings.PREF_LOGUPLOAD, Context.MODE_PRIVATE);
        saveDir = remoteDir + UploadUtils.getyyMM() + "/";
    }

    private boolean checkDate() {
        int h = UploadUtils.getNowHour();
        return h < startTime || h > endTime;
    }

    public void ScheduelUpload() {
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                Log.e("zjy", "LogUploader->ScheduelUpload run(): start UploadLog==");
                if (checkDate()) {
                    return;
                }
                lastUpTime = sp.getLong("lasttime", 0);
                float timeDur = (System.currentTimeMillis() - lastUpTime) / 1000f / 60;
                MyApp.myLogger.writeInfo("upload dur :" + timeDur);
                if (timeDur < 120) {
                    return;
                }
                Log.e("zjy", "LogUploader->ScheduelUpload run(): time ok==");
                final File root = Environment.getExternalStorageDirectory();
                final File log = new File(root, logFileName);
                final String date = sp.getString(tagStr, "");
                String remoteName = getRemoteName(date);
                fileSize = sp.getLong("logsize", 0);
                final String current = UploadUtils.getDD(new Date());
                if (!date.equals(current)) {
                    fileSize = 0;
                }
                if (!log.exists()) {
                    sp.edit().putString(tagStr, current).apply();
                } else {
                    final String remotePath = saveDir + remoteName;
                    if (fileSize < log.length()) {
                        boolean b = uploadLogFile(targeUrl, current, date, remotePath, log);
                        if (b) {
                            sp.edit().putLong("logsize", log.length())
                                    .putLong("lasttime", System.currentTimeMillis()).
                                    commit();
                        }
                    }
                }
            }
        };
        TaskManager.getInstance().execute(mRun);
    }


    private boolean uploadLogFile(String targeUrl, String current, String date, String remotePath, File log
    ) {
        boolean upOK = false;
        HashMap<String, String> map = new HashMap<>();
        try {
            URL urll = new URL(targeUrl);
            HttpURLConnection conn = (HttpURLConnection) urll
                    .openConnection();
            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(15 * 1000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(is));
                String len = reader.readLine();
                StringBuilder stringBuilder = new StringBuilder();
                while (len != null) {
                    String[] line = len.split("=");
                    if (line.length < 2) {
                        map.put(line[0], "");
                    } else {
                        map.put(line[0], line[1]);
                    }
                    stringBuilder.append(len);
                    len = reader.readLine();
                }
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String checkid = map.get("checkid");
        String deviceID = map.get("deviceID");
        String localID = UploadUtils.getDeviceID(mContext.getApplicationContext());
        boolean nomarlUpload = true;
        if ("1".equals(checkid)) {
            nomarlUpload = false;
        }
        if (!date.equals(current)) {
            try {
                upload(log, remotePath);
                upOK = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (upOK) {
                Log.e("zjy", "LogUploadService->uploadLogFile(): UploadLog Finish==");
                sp.edit().putString(tagStr, current).apply();
                MyApp.myLogger.close();
                MyApp.myLogger.init(false);
                MyApp.myLogger.writeInfo("new Logger");
            }
        } else if (nomarlUpload) {
            return true;
        } else {
            if ("all".equals(deviceID) || localID.equals(deviceID)) {
                try {
                    upload(log, remotePath);
                    upOK = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (upOK) {
                    sp.edit().putString(tagStr, current).apply();
                    MyApp.myLogger.close();
                    MyApp.myLogger.init(true);
                    MyApp.myLogger.writeInfo("speUpload Logger");
                }
            }
        }
        return upOK;
    }
    boolean isSpeDev() {
        return false;
    }


    public  void upload(final Handler mHandler) {
        final String remotePath = saveDir;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                MyApp.myLogger.close();
                File file = MyApp.myLogger.getlogFile();
                try {
                    Date nameDate = new Date();
                    String tag = UploadUtils.getDD(nameDate);
                    String rmName = getRemoteName(tag);
                    String remoteFile = remotePath + rmName;
                    upload(file, remoteFile);
                    MyApp.myLogger.init(true);
                    if (mHandler != null) {
                        mHandler.obtainMessage(LOG_UPLOAD_SUCCESS).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mHandler != null) {
                        mHandler.obtainMessage(LOG_UPLOAD_FAIL, "" + e.getMessage()).sendToTarget();
                    }
                }
            }
        };
        TaskManager.getInstance().execute(mRun);
    }

    private String getRemoteName(String dd) {
        String phoneCode = UploadUtils.getPhoneCode(mContext.getApplicationContext());

        String remoteName = dd + "_" + phoneCode + "_log.txt";
        return remoteName;
    }
    static void upload(File log, String remotePath) throws IOException {
        FTPUtils utils = FTPUtils.getGlobalFTP();
        boolean upOK = false;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(log);
            utils.login();
            upOK = utils.upload(fis, remotePath);
            if (!upOK) {
                throw new IOException("logUploader=False");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("上传log，" + "" + log.getName() +
                    "到FTP失败," + e.getMessage());
        }finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            utils.exitServer();
        }
    }
}
