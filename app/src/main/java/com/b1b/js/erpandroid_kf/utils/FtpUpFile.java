package com.b1b.js.erpandroid_kf.utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 ftp上传文件工具类
 Created by 张建宇 on 2016/12/19. */
public class FtpUpFile {
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
    /**
     本地文件
     */
    private File localFile;
    private FTPClient ftpClient = null;
    private boolean isConnected = false;

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     */
    public FtpUpFile(String name, String password, String ftpUrl) {
        this.password = password;
        this.name = name;
        this.ftpUrl = ftpUrl;
        ftpClient = new FTPClient();
    }

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     @param port     端口（默认21可以直接使用不带port参数的构造方法）
     */
    public FtpUpFile(String name, String password, String ftpUrl, int port) {
        this(name, password, ftpUrl);
        this.defaultPort = port;
    }

    private void connectAndLogin(String name, String password, String ftpUrl) throws IOException {
        //        ftpClient.connect(ftpUrl, defaultPort);
        ftpClient.connect(ftpUrl);
        isConnected = ftpClient.login(name, password);
        Log.e("MyError", "FtpUpFile>>connectAndLogin()->>=connectSuccess");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        ftpClient.enterLocalPassiveMode();
    }

    /**
     开线程：传入文件
     @param file
     @param remotePathName
     @param remoteName
     @throws Exception
     */
    public boolean upload(File file, String remotePathName,
                          String remoteName) throws Exception {
        connectAndLogin(name, password, ftpUrl);
        changeDirectory(remotePathName);
        boolean isSuccess = uploadFile(file, remoteName);
        backToRootDirectory();
        ftpClient.disconnect();
        return isSuccess;
    }

    /**
     开线程：传入文件输入流
     @param inputStream
     @param remotePathName
     @param remoteName
     @return
     @throws Exception
     */
    public boolean upload(InputStream inputStream, String remotePathName,
                          String remoteName) throws IOException, RemoteDeleteException {
        connectAndLogin(name, password, ftpUrl);
        changeDirectory(remotePathName);
        boolean isSuccess = uploadFile(inputStream, remoteName);
        backToRootDirectory();
                ftpClient.disconnect();
//        ftpClient.logout();
        return isSuccess;
    }

    private void changeDirectory(String path)
            throws IOException {
        int nextSeperator = path.indexOf("/", 1);
        String currentPath = null;
        if (nextSeperator == -1) {
            currentPath = path.substring(1, path.length());
            createFile(currentPath);
            return;
        } else {
            currentPath = path.substring(1, nextSeperator);
            createFile(currentPath);
            changeDirectory(path.substring(nextSeperator));
        }
    }

    private void createFile(String path)
            throws IOException {
        if (!ftpClient.changeWorkingDirectory(path)) {
            ftpClient.makeDirectory(path);
        }
        ftpClient.changeWorkingDirectory(path);
    }

    private void backToRootDirectory() throws IOException {
        ftpClient.changeWorkingDirectory("/");
    }

    public class RemoteDeleteException extends Exception {

    }

    private boolean uploadFile(File file, String remoteName) throws IOException, RemoteDeleteException {
        if (file == null || !file.exists()) {
            return false;
        } else {
            InputStream is = new FileInputStream(file);
            return uploadFile(is, remoteName);
        }

        //        String localPathName = file.getAbsolutePath();
        //        FTPFile[] files = ftpClient.listFiles(remoteName);
        //        if (files.length == 1) {
        //            if (!ftpClient.deleteFile(remoteName)) {
        //                throw new RemoteDeleteException();
        //            }
        //        }
        //        File f = new File(localPathName);
        //        InputStream is = new FileInputStream(f);
        //        boolean success = ftpClient.storeFile(remoteName, is);
        //        is.close();
    }

    private boolean uploadFile(InputStream inputStream, String
            remoteName) throws IOException, RemoteDeleteException {
        if (inputStream == null) {
            return false;
        }
        FTPFile[] files = ftpClient.listFiles(remoteName);
        if (files != null) {
            if (!ftpClient.deleteFile(remoteName))
                throw new RemoteDeleteException();
        }
        boolean success = ftpClient.storeFile(remoteName, inputStream);
        inputStream.close();
        return success;
    }
}
