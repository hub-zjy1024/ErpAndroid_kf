package com.b1b.js.erpandroid_kf.utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 ftp上传文件工具类
 Created by 张建宇 on 2016/12/19. */
public class FtpManager2 {
    private static FtpManager2 ftpManager;

    private FtpManager2() {

    }

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   ftp地址
     @param port     ftp端口
     @return
     */
    public static FtpManager2 getFtpManager(String name, String password, String ftpUrl, int port) {
        if (ftpManager == null) {
            ftpManager = new FtpManager2(name, password, ftpUrl, port);
            Log.e("zjy", "FtpManager.java->getFtpManager(): first create ftp");
        } else {
            Log.e("zjy", "FtpManager.java->getFtpManager(): use last ftp");
        }
        return ftpManager;
    }

    public static void storeNull() {
        ftpManager = null;
    }

    /**
     ftp默认端口
     */
    private int defaultPort = 21;
    /**
     ftp路径
     */
    private String ftpUrl;
    /**
     ftp用户名
     */
    private String name;
    /**
     ftp密码
     */
    private String password;
    private FTPClient zClient = null;
    private boolean isConnected = false;

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     */
    public FtpManager2(String name, String password, String ftpUrl) {
        this.password = password;
        this.name = name;
        this.ftpUrl = ftpUrl;
        zClient = new FTPClient();
    }

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     @param port     端口（默认21可以直接使用不带port参数的构造方法）
     */
    public FtpManager2(String name, String password, String ftpUrl, int port) {
        this(name, password, ftpUrl);
        this.defaultPort = port;
    }

    public void connectAndLogin() throws IOException {
        zClient.connect(ftpUrl, defaultPort);
        isConnected = zClient.login(name, password);
        Log.e("zjy", "FtpManager.java->connectAndLogin(): connSuccess");
        zClient.setFileType(FTP.BINARY_FILE_TYPE);
        zClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        zClient.enterLocalPassiveMode();
    }

    /**
     开线程：传入文件
     @param file
     @param remotePathName 上传路径，以"/"开头,如果不存在会自动创建
     @param remoteName     上传后的文件名（带后缀）
     @throws Exception
     */
    public boolean upload(File file, String remotePathName,
                          String remoteName) throws Exception {
        changeDirectory(remotePathName);
        FileInputStream fis = new FileInputStream(file);
        boolean isSuccess = uploadFile(fis, remoteName);
        backToRootDirectory();
        return isSuccess;
    }

    public void exit() throws IOException {
        //需要先判断是否连接，然后logout，然后再disconnect
        if (zClient.isConnected()) {
            zClient.logout();
            zClient.disconnect();
            Log.e("zjy", "FtpManager.java->FTP exit");
        }
    }

    /**
     开线程：传入文件输入流
     @param inputStream
     @param remotePathName 目录路径，不存在则创建
     @param remoteName     上传后的文件名（带后缀）
     @return
     @throws Exception
     */
    public boolean upload(InputStream inputStream, String remotePathName,
                          String remoteName) throws IOException {
        changeDirectory(remotePathName);
        boolean isSuccess = uploadFile(inputStream, remoteName);
        backToRootDirectory();
        return isSuccess;
    }

    private void changeDirectory(String path)
            throws IOException {
        int nextSeperator = path.indexOf("/", 1);
        String currentPath = null;
        if (nextSeperator == -1) {
            currentPath = path.substring(1, path.length());
            createDir(currentPath);
            return;
        } else {
            currentPath = path.substring(1, nextSeperator);
            createDir(currentPath);
            changeDirectory(path.substring(nextSeperator));
        }
    }

    private void createDir(String path)
            throws IOException {
        if (!zClient.changeWorkingDirectory(path)) {
            zClient.makeDirectory(path);
            zClient.changeWorkingDirectory(path);
        }
    }

    private void backToRootDirectory() throws IOException {
        zClient.changeWorkingDirectory("/");
    }

    private boolean uploadFile(File file, String remoteName) throws IOException {
        if (file == null || !file.exists()) {
            return false;
        } else {
            InputStream is = new FileInputStream(file);
            return uploadFile(is, remoteName);
        }
    }

    private boolean uploadFile(InputStream inputStream, String
            remoteName) throws IOException {
        if (inputStream == null) {
            return false;
        }
        boolean success = zClient.storeFile(remoteName, inputStream);
        inputStream.close();
//        OutputStream outputStream = zClient.storeFileStream(remoteName);
//        int len;
//        byte[] buf = new byte[1024];
//        while ((len = inputStream.read(buf)) != -1) {
//            outputStream.write(buf, 0, len);
//        }
//        inputStream.close();
//        outputStream.close();
//        zClient.completePendingCommand();
        return success;
    }

    public boolean downLoadFile(String remoteName, String remoteDir, String savePath) throws IOException {
        FileOutputStream localOutStream = new FileOutputStream(savePath);
        //retrieveFile方法中的remote为完整的文件路径，以"/"开头
        changeDirectory(remoteDir);
        if (zClient.changeWorkingDirectory(remoteDir)) {
            zClient.retrieveFile(remoteDir + remoteName, localOutStream);
            localOutStream.close();
            return true;
        } else {
            return false;
        }
    }
}
