package com.b1b.js.erpandroid_kf;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.b1b.js.erpandroid_kf.service.PushService;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartSrv;
    private Button btnStopSrv;
    private Button btnBindSrv;
    private Button btnUnbindSrv;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("zjy", "UploadActivity.java->onServiceConnected(): onServiceConnected==");
            PushService.MyBinder mBinder = (PushService.MyBinder) service;
            mBinder.upLoad();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("zjy", "UploadActivity.java->onServiceDisconnected(): srv Disconnected==" + name.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        btnStartSrv = (Button) findViewById(R.id.activity_upload_start_srv);
        btnStopSrv = (Button) findViewById(R.id.activity_upload_stop_srv);
        btnBindSrv = (Button) findViewById(R.id.activity_upload_bind_srv);
        btnUnbindSrv = (Button) findViewById(R.id.activity_upload_unbind_srv);
        btnStartSrv.setOnClickListener(this);
        btnStopSrv.setOnClickListener(this);
        btnBindSrv.setOnClickListener(this);
        btnUnbindSrv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(UploadActivity.this, PushService.class);
        switch (v.getId()) {
            case R.id.activity_upload_start_srv:
                startService(intent);
                break;
            case R.id.activity_upload_stop_srv:
                stopService(intent);
                break;
            case R.id.activity_upload_bind_srv:
                bindService(intent, serviceConn, BIND_AUTO_CREATE);
                break;
            case R.id.activity_upload_unbind_srv:
                unbindService(serviceConn);
                break;

        }
    }

    public class FtpUtil {
        private FTPClient ftpClient = null;
        private String hostname;
        private int port;
        private String username;
        private String password;
        private String remoteDir;

        //构造方法
        public FtpUtil(String hostname, int port, String username, String password, String remoteDir) {
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            this.remoteDir = remoteDir;
            if (remoteDir == null) {
                remoteDir = "/";
            }
        } //登录 /** * FTP登陆 * @throws IOException *//

        public void login() throws Exception {
            ftpClient = new FTPClient();
            ftpClient.configure(getFTPClientConfig());
            ftpClient.setDefaultPort(port);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(hostname);
            if (!ftpClient.login(username, password)) {
                throw new Exception("FTP登陆失败，请检测登陆用户名和密码是否正确!");
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(remoteDir);
        }

        /**
         得到配置 * @return
         */
        private FTPClientConfig getFTPClientConfig() { // 创建配置对象
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
            conf.setServerLanguageCode("zh");
            return conf;
        }

        /**
         关闭FTP服务器
         */
        public void closeServer() {
            try {
                if (ftpClient != null) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         链接是否已经打开 * @return
         */
        public boolean serverIsOpen() {
            if (ftpClient == null) {
                return false;
            }
            return !ftpClient.isConnected();
        }

        /**
         列表FTP文件 * @param regEx * @return
         */
        public String[] listFiles(String regEx) {
            String[] names;
            try {
                names = ftpClient.listNames(regEx);
                if (names == null)
                    return new String[0];
                return names;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String[0];
        }

        /**
         取得FTP操作类的句柄 * @return
         */
        public FTPClient getFtpClient() {
            return ftpClient;
        }

        /**
         上传 * @throws Exception
         */
        public boolean upload(String localFilePath, String remoteFilePath) throws Exception {
            boolean state = false;
            File localFile = new File(localFilePath);
            if (!localFile.isFile() || localFile.length() == 0) {
                return state;
            }
            FileInputStream localIn = new FileInputStream(localFile);
            state = this.upload(localIn, remoteFilePath);
            localIn.close();
            return state;
        }

        /**
         上传 * @throws Exception
         */
        public boolean upload(InputStream localIn, String remoteFilePath) throws Exception {
            this.createDir(new File(remoteFilePath).getParent());
            boolean result = ftpClient.storeFile(remoteFilePath, localIn);
            return result;
        }

        /**
         是否存在FTP目录 * @param dir * @param ftpClient * @return
         */
        public boolean isDirExist(String dir) {
            try {
                int retCode = ftpClient.cwd(dir);
                return FTPReply.isPositiveCompletion(retCode);
            } catch (Exception e) {
                return false;
            }
        }

        /**
         创建FTP多级目录 * @param remoteFilePath * @throws IOException
         */
        public void createDir(String dir) throws IOException {
            if (!isDirExist(dir)) {
                File file = new File(dir);
                this.createDir(file.getParent());
                ftpClient.makeDirectory(dir);
            }
        }

        /**
         删除文件 * @param remoteFilePath
         */
        public boolean delFile(String remoteFilePath) {
            try {
                return ftpClient.deleteFile(remoteFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         下载
         @param localFilePath
         @param remoteFilePath
         @throws Exception
         */
        public void download(String localFilePath, String remoteFilePath) throws Exception {
            OutputStream localOut = new FileOutputStream(localFilePath);
            this.download(localOut, remoteFilePath);
            localOut.close();
        }

        /**
         下载 * @throws Exception
         */
        public void download(OutputStream localOut, String remoteFilePath) throws Exception {
            boolean result = ftpClient.retrieveFile(remoteFilePath, localOut);
            if (!result) {
                throw new Exception("文件下载失败!");
            }
        }
    }
}
