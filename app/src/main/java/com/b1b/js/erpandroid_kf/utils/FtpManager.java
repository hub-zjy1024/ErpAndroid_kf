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
public class FtpManager {
    private static FtpManager ftpManager;
    public static String ftpName = "dyjftp";
    public static String ftpPassword = "dyjftp";
    public static String mainAddress ="172.16.6.22";

    private FtpManager() {

    }

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   ftp地址
     @param port     ftp端口
     @return
     */
    public static FtpManager getFtpManager(String name, String password, String ftpUrl, int port) {
        if (ftpManager == null) {
            ftpManager = new FtpManager(name, password, ftpUrl, port);
            Log.e("zjy", "FtpManager->getFtpManager(): initftpUrl==" + ftpUrl);
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
    private FTPClient ftpClient = null;
    private boolean isConnected = false;

    /**
     @param name     用户名
     @param password 密码
     @param ftpUrl   Ftp地址
     */
    private FtpManager(String name, String password, String ftpUrl) {
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
    private FtpManager(String name, String password, String ftpUrl, int port) {
        this(name, password, ftpUrl);
        this.defaultPort = port;
    }

    public synchronized void connectAndLogin(int timeout) throws IOException {
        ftpClient.setDefaultTimeout(timeout*1000);
        ftpClient.setDataTimeout(timeout*1000);
        ftpClient.setConnectTimeout(timeout*1000);
        ftpClient.setControlEncoding("UTF-8");
        Log.e("zjy", "FtpManager->connectAndLogin(): ftp==" + ftpUrl);
        ftpClient.connect(ftpUrl, defaultPort);
        isConnected = ftpClient.login(name, password);
        Log.e("zjy", "FtpManager.java->connectAndLogin(): connSuccess="+isConnected);
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        ftpClient.enterLocalPassiveMode();
    }
    public void connectAndLogin() throws IOException {
        connectAndLogin(20);
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
        InputStream is = new FileInputStream(file);
        return upload(is, remotePathName, remoteName);
    }

    public synchronized void exit() throws IOException {
        //需要先判断是否连接，然后logout，然后再disconnect
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
            Log.e("zjy", "FtpManager.java->FTP exit");
        }
    }

    public synchronized boolean  isConn() {
        boolean isChange = false;
        try {
            isChange = ftpClient.changeWorkingDirectory("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isChange;
    }

    /**
     开线程：传入文件输入流
     @param inputStream
     @param remotePathName 上传路径，以"/"开头,如果不存在会自动创建
     @param remoteName     上传后的文件名（带后缀）
     @return
     @throws Exception
     */
    public synchronized boolean upload(InputStream inputStream, String remotePathName,
                          String remoteName) throws IOException {
//        toTargetDir(remotePathName);
        ftpClient.makeDirectory(remotePathName);
        boolean isSuccess = ftpClient.storeFile(remoteName, inputStream);
        inputStream.close();
        Log.e("zjy", "FtpManager.java->uploadFile():upSuccess==" + isSuccess);
        backToRootDirectory();
        return isSuccess;
    }

    /**
     开线程：传入文件输入流
     @param remotePathName 上传路径，以"/"开头,如果不存在会自动创建
     @param remoteName     上传后的文件名（带后缀）
     @return
     @throws Exception
     */
    public boolean upload(String localPath, String remotePathName,
                          String remoteName) throws Exception {
        toTargetDir(remotePathName);
        File file = new File(localPath);
        return upload(file, remotePathName, remoteName);
    }

    /**
     开线程：传入文件输入流
     @param inputStream
     @param remotePath  上传后的文件的完整路径（带后缀），以"/"开头,如果不存在会自动创建
     @return
     @throws Exception
     */
    public synchronized boolean uploadByPath(InputStream inputStream, String remotePath) throws IOException {
        int last = remotePath.lastIndexOf("/");
        if (last != 0 && last != -1) {
            String path = remotePath.substring(0, last);
//            toTargetDir(path);
            ftpClient.makeDirectory(path);
        }
        String fileName = remotePath.substring(last, remotePath.length());
        Log.e("zjy", "FtpManager->uploadByPath(): remote==" + remotePath);
        boolean isSuccess = ftpClient.storeFile(remotePath, inputStream);
        inputStream.close();
        Log.e("zjy", "FtpManager.java->uploadByPath():upSuccess==" + isSuccess);
        backToRootDirectory();
        return isSuccess;
    }

    /**
     开线程：传入文件输入流
     @param remotePath 上传后的文件的完整路径（带后缀），以"/"开头,如果不存在会自动创建
     @return
     @throws Exception
     */
    public boolean uploadByPath(String localPath, String remotePath) throws IOException {
        File file = new File(localPath);
        return uploadByPath(file, remotePath);
    }

    /**
     开线程：传入文件输入流
     @param remotePath 上传后的文件的完整路径（带后缀），以"/"开头,如果不存在会自动创建
     @return
     @throws Exception
     */
    public boolean uploadByPath(File file, String remotePath) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return uploadByPath(fis, remotePath);
    }

    /**
     @param path 路径必须以“/”开头
     @throws IOException
     */
    private void toTargetDir(String path)
            throws IOException {
        int nextSeperator = path.indexOf("/", 1);
        String currentPath = null;
        if (nextSeperator == -1) {
            currentPath = path.substring(1, path.length());
            createDir(currentPath);
        } else {
            currentPath = path.substring(1, nextSeperator);
            createDir(currentPath);
            toTargetDir(path.substring(nextSeperator));
        }
    }

    private void createDir(String path)
            throws IOException {
        if (!ftpClient.changeWorkingDirectory(path)) {
            ftpClient.makeDirectory(path);
            ftpClient.changeWorkingDirectory(path);
        }
    }

    private void backToRootDirectory() throws IOException {
        ftpClient.changeWorkingDirectory("/");
    }

    public synchronized void downLoadFile(String remoteName, String remoteDir, String savePath) throws IOException {
        FileOutputStream localOutStream = new FileOutputStream(savePath);
        //retrieveFile方法中的remote为完整的文件路径，以"/"开头
        toTargetDir(remoteDir);
        ftpClient.retrieveFile(remoteDir + remoteName, localOutStream);
        localOutStream.close();
    }
}
