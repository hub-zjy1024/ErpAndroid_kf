package utils.common.log;

import android.content.Context;
import android.os.Handler;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import utils.common.UploadUtils;
import utils.net.ftp.FTPUtils;

/**
 * Created by 张建宇 on 2019/5/28.
 */
public class LogUploader {
    public static final String remoteDir = "/Zjy/log_kf/";
    public static final String remotePath =  remoteDir+ UploadUtils.getyyMM() + "/";
    public static final int LOG_UPLOAD_SUCCESS = 20;
    public static final int LOG_UPLOAD_FAIL = 21;
    private Context mContext;
    public LogUploader(Context mContext) {
        this.mContext = mContext;
    }


    public  void upload(final Handler mHandler) {
        final String remotePath = LogUploader.remotePath;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                MyApp.myLogger.close();
                File file = MyApp.myLogger.getlogFile();
                try {
                    Date nameDate = new Date();
                    SimpleDateFormat sdfName = new SimpleDateFormat("dd_HHmmssSSS");
                    SimpleDateFormat sdfName2 = new SimpleDateFormat("dd");
                    String tag = sdfName2.format(nameDate);
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
    public String getRemoteName(String dd) {
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
