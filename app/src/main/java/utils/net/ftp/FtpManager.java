package utils.net.ftp;

/**
 ftp地址、用户名、密码
 Created by 张建宇 on 2016/12/19. */
public class FtpManager {
    public static final String ftpName = "dyjftp";
    public static final String ftpPassword = "dyjftp";
    public static final String mainAddress = FTPUtils.mainAddress;
    public static final String mainName = "NEW_DYJ";
    public static final String mainPwd = "GY8Fy2Gx";
    public static final String TEST_FTP_ULR = "192.168.10.66";
    public static String getTestFTPStr() {
        String host = TEST_FTP_ULR;
        String user= "zjy";
        String pwd = "123456";
        int port = 21;
        return FTPUtils.getFtpStr(host, user, pwd, port);
    }
    public static FTPUtils getTestFTP() {
        String localFTPStr = getTestFTPStr();
        return FTPUtils.getFtpFromStr(localFTPStr);
    }
    public static  String getTestUrl(){
        return TEST_FTP_ULR;
    }

    public static FTPUtils getTestFTPMain() {
        //        return new FTPUtils(FTPUtils.mainAddress, mainName, mainPwd);
        return FTPUtils.getAdminFTP();
    }
}
