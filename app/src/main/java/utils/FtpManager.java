package utils;

/**
 ftp地址、用户名、密码
 Created by 张建宇 on 2016/12/19. */
public class FtpManager {
    public static final String ftpName = "dyjftp";
    public static final String ftpPassword = "dyjftp";
    public static final String mainAddress = "172.16.6.22";
    public static final String mainName = "NEW_DYJ";
    public static final String mainPwd = "GY8Fy2Gx";

    public static FTPUtils getTestFTP() {
//        return new FTPUtils(FTPUtils.mainAddress, mainName, mainPwd);
        return new  FTPUtils("192.168.10.66", "zjy", "123456");
    }
}
