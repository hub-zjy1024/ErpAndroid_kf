package com.b1b.js.erpandroid_kf.utils;

import android.util.Log;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 Created by 张建宇 on 2017/6/29.
 基于commons-net3.6.jar */

public class DownUtils {
    private FTPClient mClient = null;
    private String hostname;
    private int port;
    private String username;
    private String password;
    private ProtocolCommandListener listener;
    public static final String DEF_FTP="172.16.6.22";
    /**
     调试开关，默认关闭。开启时自动打印ftp命令和回复结果
     */
    private boolean isDebug = false;

    /**
     默认不开启调试模式
     @param hostname
     @param port
     @param username
     @param password
     */
    public DownUtils(String hostname, int port, String username, String password) {
        this(hostname, port, username, password, false);
    } //登录 /** * FTP登陆 * @throws IOException */

    /**
     可选调试模式
     @param hostname
     @param port
     @param username
     @param password
     @param isDebug  调试模式
     */
    public DownUtils(String hostname, int port, String username, String password, boolean isDebug) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        mClient = new FTPClient();
        this.isDebug = isDebug;
        if (this.isDebug) {
            listener = new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent event) {
                    Log.e("zjy", "DownUtils->protocolCommandSent(): sendMsg==" + event.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent event) {
                    int replyCode = event.getReplyCode();
                    Log.e("zjy", "DownUtils->protocolReplyReceived(): replyCode==" + event.getMessage());
                }
            };
            mClient.addProtocolCommandListener(listener);
        }
    }


    /**
     登录FTP
     @throws IOException
     */
    public synchronized void login() throws IOException {
        login(15);
    }
    /**
     登录FTP
     @throws IOException
     */
    public synchronized void login(int timeout) throws IOException {
        Log.e("zjy", "DownUtils->login(): start login==");
        //ftp传输超时
        mClient.setDataTimeout(timeout * 1000);
        //ftp命令响应超时
        mClient.setDefaultTimeout(timeout * 1000);
        //连接超时
        mClient.setConnectTimeout(timeout * 1000);
        mClient.connect(hostname, port);
//        只能在连接成功之后使用
        //ftp命令响应超时
//        mClient.setSoTimeout(timeout * 1000);
        mClient.setControlEncoding("UTF-8");
        mClient.setControlKeepAliveTimeout(2);
        if (!mClient.login(username, password))
            throw new IOException("FTP登陆失败，请检测登陆用户名和密码是否正确!");
        //只能在登陆成功后设置下面的才有效果
        //根据服务器的设置更改
        mClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        //必须设置
        mClient.enterLocalPassiveMode();
    }

    /**
     得到配置 * @return
     */
    private FTPClientConfig getFTPClientConfig() { /* 创建配置对象*/
        FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
        conf.setServerLanguageCode("zh");
        return conf;
    }

    public synchronized boolean makeDir(String path) throws IOException { /* 创建配置对象*/
        return mClient.makeDirectory(path);
    }

    /**
     退出FTP服务器
     */
    public synchronized void exitServer() {
        try {
            mClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     链接是否已经打开 * @return
     */
    public synchronized boolean serverIsOpen() {
        try {
            boolean ok = mClient.changeWorkingDirectory("/");
            return ok;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     列表FTP文件 * @param regEx * @return
     */
    public String[] listFiles(String regEx) {
        String[] names;
        try {
            names = mClient.listNames(regEx);
            if (names == null)
                return new String[0];
            return names;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     取得FTP操作类的句柄 *
     @return
     */
    public synchronized FTPClient getmClient() {
        return mClient;
    }

    /**
     上传 * @throws Exception
     @param localFilePath
     @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     @return
     @throws Exception
     */
    public boolean upload(String localFilePath, String remoteFilePath) throws Exception {
        boolean state = false;
        File localFile = new File
                (localFilePath);
        state = upload(localFile, remoteFilePath);
        return state;
    }

    /**
     上传 * @throws Exception
     @param localFile
     @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     @return
     @throws Exception
     */
    public boolean upload
    (File localFile, String remoteFilePath) throws Exception {
        boolean state = false;
        if (!localFile.isFile() || localFile.length() == 0) {
            return state;
        }
        FileInputStream localIn = new
                FileInputStream(localFile);
        return upload(localIn, remoteFilePath);
    }

    /**
     上传 *
     @param localIn        等待上传的文件流
     @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     @return
     @throws IOException
     */
    public synchronized boolean upload(InputStream localIn, String remoteFilePath) throws IOException {
        int last = remoteFilePath.lastIndexOf("/");
        if (last != 0 && last != -1) {
            String path = remoteFilePath.substring(0, last);
            //            toTargetDir(path);
            boolean dirExists = fileExists(path);
            if (!dirExists) {
                mClient.makeDirectory(path);
                Log.e("zjy", "DownUtils->upload(): makeDir==" + path);
            }
        }
        return mClient.storeFile(remoteFilePath, localIn);
    }

    /**
     删除文件 * @param remoteFilePath
     */
    public synchronized boolean delFile(String remoteFilePath) {
        try {
            return mClient.deleteFile
                    (remoteFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     下载 * @throws Exception
     */
    public void download(String localFilePath, String remoteFilePath) throws IOException {
        OutputStream localOut = new
                FileOutputStream(localFilePath);
        this.download(localOut, remoteFilePath);
        localOut.close();
    }

    public synchronized boolean fileExists(String remoteFilePath) {
        try {
            FTPFile[]  ftpFiles = mClient.listFiles(remoteFilePath);
            if (ftpFiles != null && ftpFiles.length > 0) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     下载 * @throws
     Exception
     */
    public void download(OutputStream localOut, String remoteFilePath) throws IOException {
        boolean result =
                mClient.retrieveFile(remoteFilePath, localOut);
        if (!result) {
            throw new IOException("文件下载失败!");
        }
    }
}
