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
 最好使用单例模式，保证不出错
 Created by 张建宇 on 2016/12/19. */
public class FtpUpFile {
    private static FtpUpFile ftpUpFile;

    private FtpUpFile() {

    }

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   ftp地址
     @param port     ftp端口
     @return
     */
    public static FtpUpFile getFtpUpFile(String name, String password, String ftpUrl, int port) {
        if (ftpUpFile == null) {
            ftpUpFile = new FtpUpFile(name, password, ftpUrl, port);
            Log.e("zjy", "FtpUpFile.java->getFtpUpFile(): first create ftp");
        } else {
            Log.e("zjy", "FtpUpFile.java->getFtpUpFile(): use last ftp");
        }
        return ftpUpFile;
    }

    public static void storeNull() {
        ftpUpFile = null;
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
    private FTPClient ftpClient = null;
    private boolean isConnected = false;

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     */
    private FtpUpFile(String name, String password, String ftpUrl) {
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
    private FtpUpFile(String name, String password, String ftpUrl, int port) {
        this(name, password, ftpUrl);
        this.defaultPort = port;
    }

    private void connectAndLogin(String name, String password, String ftpUrl) throws IOException {
        ftpClient.connect(ftpUrl, defaultPort);
        isConnected = ftpClient.login(name, password);
        Log.e("zjy", "FtpUpFile.java->connectAndLogin(): connSuccess");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        ftpClient.enterLocalPassiveMode();
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
        connectAndLogin(name, password, ftpUrl);
        changeDirectory(remotePathName);
        boolean isSuccess = uploadFile(file, remoteName);
        backToRootDirectory();
        //需要先logout，然后再disconnect
        ftpClient.logout();
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
        return isSuccess;
    }

    /**
     开线程：传入文件输入流
     @param inputStream
     @param remotePathName
     @param remoteName     上传后的文件名（带后缀）
     @return
     @throws Exception
     */
    public boolean upload(InputStream inputStream, String remotePathName,
                          String remoteName) throws IOException, RemoteDeleteException {
        connectAndLogin(name, password, ftpUrl);
        changeDirectory(remotePathName);
        boolean isSuccess = uploadFile(inputStream, remoteName);
        backToRootDirectory();
        //需要先logout，然后再disconnect
        ftpClient.logout();
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
            Log.e("zjy", "FtpUpFile.java->upload(): exit");
        }
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
