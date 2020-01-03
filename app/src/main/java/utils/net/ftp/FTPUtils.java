package utils.net.ftp;

import android.util.Log;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 张建宇 on 2017/6/29.
 * 基于commons-net3.6.jar
 */

public class FTPUtils {
    private FTPClient mClient = null;
    private String hostname;
    private int port;
    private String username;
    private String password;
    private ProtocolCommandListener listener;
    public static final String DEF_FTP = "172.16.6.22";
    public static final String ftpName = "dyjftp";
    public static final String ftpPassword = "dyjftp";
    public static String CaigouFTPAddr = "172.16.6.22";
    public static final String DB_HOST = "172.16.6.22";
    public static final String mainAddress = "172.16.6.22";
    public static final int DEFAULT_PORT = 21;
    //    public static final String mainAddress = "210.51.190.36";
    //    public static final int DEFAULT_PORT = 7521;
    public static final String mainName = "NEW_DYJ";
    public static final String mainPwd = "GY8Fy2Gx";
    public static final String LOCAL_NAME = "dyjftp";
    public static final String LOCAL_PWD = "dyjftp";

    public static final String ADMIN_HOST = mainAddress;
    public static final int ADMIN_PORT = DEFAULT_PORT;
    public static final String ADMIN_NAME = mainName;
    public static final String ADMIN_PWD = mainPwd;
    public static final String TEST_FTP_ULR = "192.168.10.66";
    public static FTPUtils getGlobalFTP() {
        return new FTPUtils(mainAddress, mainName, mainPwd);
    }

    public static FTPUtils getAdminFTP() {
        return new FTPUtils(ADMIN_HOST, ADMIN_PORT, ADMIN_NAME, ADMIN_PWD);
    }

    public static FTPUtils getFtpFromStr(String ftpStr) {
        String[] split = ftpStr.split("\\|");
        if (split.length >= 3) {
            String localHost = split[0];
            String userName = split[1];
            String pwd = split[2];
            int port = Integer.parseInt(split[3]);
            return new FTPUtils(localHost, port, userName, pwd);
        } else {
            throw new IllegalArgumentException("ftpStr格式为host|uname|pwd|port");
        }
    }
    public static FTPUtils getTestFTP() {
        String localFTPStr = getTestFTPStr();
        return FTPUtils.getFtpFromStr(localFTPStr);
    }
    public static String getTestFTPStr() {
        String host = TEST_FTP_ULR;
        String user= "zjy";
        String pwd = "123456";
        int port = 21;
        return FTPUtils.getFtpStr(host, user, pwd, port);
    }
    public static String getFtpStr(  String host , String username,String pwd , int port ) {
        return host+"|"+username+"|"+pwd+"|"+port;
    }

    public static String getMainStr() {
        String url = getFtpStr(ADMIN_HOST, ADMIN_NAME, ADMIN_PWD, ADMIN_PORT);
        return url;
    }
    public static String getLocalFTPStr(String localHost) {
        String host = localHost;
        String username = LOCAL_NAME;
        String pwd = LOCAL_PWD;
        int port = 21;
        return host+"|"+username+"|"+pwd+"|"+port;
    }
    public static FTPUtils getLocalFTP(String localHost) {
        return getLocalFTP(localHost, 21);
    }

    public static FTPUtils getLocalFTP(String localHost, int port) {
        String name = LOCAL_NAME;
        String pwd = LOCAL_PWD;
        if (mainAddress.equals(localHost)) {
            Log.w("zjy", FTPUtils.class + "->getLocalFTP():use MainAddress ==");
            name = mainName;
            pwd = mainPwd;
        }
        return new FTPUtils(localHost, port, name, pwd);
    }

    public interface UploadListner {
        void upload();
    }

    private UploadListner mListener;

    public void setListner(UploadListner mListener) {
        this.mListener = mListener;
    }

    /**
     * 调试开关，默认关闭。开启时自动打印ftp命令和回复结果
     */
    private boolean isDebug = false;

    /**
     * 默认不开启调试模式
     *
     * @param hostname
     * @param port
     * @param username
     * @param password
     */
    public FTPUtils(String hostname, int port, String username, String password) {
        this(hostname, port, username, password, false);
    } //登录 /** * FTP登陆 * @throws IOException */

    /**
     * 默认不开启调试模式
     *
     * @param hostname
     * @param username
     * @param password
     */
    public FTPUtils(String hostname, String username, String password) {
        this(hostname, DEFAULT_PORT, username, password, false);
    } //登录 /** * FTP登陆 * @throws IOException */

    /**
     * 可选调试模式
     *
     * @param hostname
     * @param port
     * @param username
     * @param password
     * @param isDebug  调试模式
     */
    public FTPUtils(String hostname, int port, String username, String password, boolean isDebug) {
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
                    Log.d("zjy", "FTPUtils->protocolCommandSent(): sendMsg==" + event.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent event) {
                    int replyCode = event.getReplyCode();
                    Log.d("zjy", "FTPUtils->protocolReplyReceived(): replyCode==" + event.getMessage());
                }
            };
            mClient.addProtocolCommandListener(listener);
        }
    }


    /**
     * 登录FTP
     *
     * @throws IOException
     */
    public synchronized void login() throws IOException {
        login(15);
    }

    /**
     * 登录FTP
     *
     * @throws IOException
     */
    public synchronized void login(int timeout) throws IOException {
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
        //文件名中有中文，下载和删除的过程中
        //                mClient.setControlEncoding("UTF-8");
        //        mClient.setControlKeepAliveTimeout(2);
        if (!mClient.login(username, password))
            throw new IOException(hostname + ",FTP登陆失败，请检测登陆用户名和密码是否正确!");
        //只能在登陆成功后设置下面的才有效果
        //根据服务器的设置更改
        mClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        mClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        //必须设置
        mClient.enterLocalPassiveMode();
    }

    /**
     * 得到配置 * @return
     */
    private FTPClientConfig getFTPClientConfig() { /* 创建配置对象*/
        FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
        conf.setServerLanguageCode("zh");
        return conf;
    }

    /**
     * 退出FTP服务器
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
     * 链接是否已经打开 * @return
     */
    public synchronized boolean serverIsOpen() {
        try {
            mClient.printWorkingDirectory();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 列表FTP文件 * @param regEx * @return
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
     * 取得FTP操作类的句柄 *
     *
     * @return
     */
    public synchronized FTPClient getmClient() {
        return mClient;
    }

    /**
     * 上传 * @throws Exception
     *
     * @param localFilePath
     * @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     * @return
     * @throws Exception
     */
    public boolean upload(String localFilePath, String remoteFilePath) throws IOException {
        boolean state = false;
        File localFile = new File
                (localFilePath);
        state = upload(localFile, remoteFilePath);
        return state;
    }

    /**
     * 上传 * @throws Exception
     *
     * @param localFile
     * @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     * @return
     * @throws Exception
     */
    public boolean upload
    (File localFile, String remoteFilePath) throws IOException {
        if (!localFile.isFile() || localFile.length() == 0) {
            return false;
        }
        FileInputStream localIn = new
                FileInputStream(localFile);
        return upload(localIn, remoteFilePath);
    }

    /**
     * 上传 *
     *
     * @param localIn        等待上传的文件流
     * @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     * @return
     * @throws IOException
     */
    public synchronized boolean upload(InputStream localIn, String remoteFilePath) throws IOException {
        int last = remoteFilePath.lastIndexOf("/");
        if (last >0) {
            String path = remoteFilePath.substring(0, last);
            boolean dirExists = fileExists(path);
            if (!dirExists) {
                Log.d("zjy", "FTPUtils->upload(): makeDir==" + path);
                mkDirs(path);
            }
        }
        return mClient.storeFile(remoteFilePath, localIn);
    }

    /**
     * 上传 *
     *
     * @param localIn        等待上传的文件流
     * @param remoteFilePath 以“/”开头，文件名结尾（带后缀），如：/day/d1/123.txt，目录不存在会自动创建目录
     * @return
     * @throws IOException
     */
    public synchronized boolean uploadByStream(InputStream localIn, String remoteFilePath) throws
            IOException {

        int last = remoteFilePath.lastIndexOf("/");
        if (last != 0 && last != -1) {
            String path = remoteFilePath.substring(0, last);
            boolean dirExists = fileExists(path);
            if (!dirExists) {
                mkDirs(path);
                Log.d("zjy", "FTPUtils->upload(): makeDir==" + path);
            }
        }
        OutputStream outputStream = mClient.storeFileStream(remoteFilePath);
        int b;
        byte[] buf = new byte[1024 * 8];

        while ((b = localIn.read(buf)) != -1) {
            outputStream.write(buf, 0, b);
        }
        outputStream.close();
        localIn.close();
        mClient.completePendingCommand();
        return true;
    }

    /**
     * 删除文件 * @param remoteFilePath
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
     * 下载 * @throws Exception
     */
    public void download(String localFilePath, String remoteFilePath) throws IOException {
        OutputStream localOut = new
                FileOutputStream(localFilePath);
        this.download(localOut, remoteFilePath);
        localOut.close();
    }

    public synchronized boolean fileExists(String remoteFilePath) {
        try {
//            FTPFile[] ftpFiles = mClient.listFiles(remoteFilePath);
//            if (ftpFiles != null && ftpFiles.length > 0) {
//                return true;
//            }
            boolean isOK = mClient.changeWorkingDirectory(remoteFilePath);
            if (isOK) {
                mClient.changeWorkingDirectory("/");
            }
            Log.d("zjy", "FTPUtils->fileExists(): dirExitst==" + isOK + "\t,path=" + remoteFilePath);
            return isOK;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载 * @throws
     * Exception
     */
    public void download(OutputStream localOut, String remoteFilePath) throws IOException {
        boolean result = mClient.retrieveFile(remoteFilePath, localOut);
        if (!result) {
            throw new IOException("文件下载失败!");
        }
    }

    /**
     * 某些FTP服务器不支持一次创建多级目录，所以需要循环创建
     *
     * @param dirPath
     * @throws IOException
     */
    public synchronized void mkDirs(String dirPath) throws IOException {
        int index = dirPath.indexOf("/");
        String nPath = "";
        while (!nPath.equals(dirPath)) {
            index = dirPath.indexOf("/", index + 1);
            if (index == -1 || index == dirPath.length() - 1) {
                nPath = dirPath;
            } else {
                nPath = dirPath.substring(0, index);
            }
            mClient.makeDirectory(nPath);
            if (nPath.equals(dirPath)) {
                break;
            }
        }
    }
}
