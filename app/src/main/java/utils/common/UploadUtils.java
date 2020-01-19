package utils.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.b1b.js.erpandroid_kf.MyApp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 Created by 张建宇 on 2017/2/21.
 主要是一些路径的获取 */

public class UploadUtils {
    public static String KF_DIR = "/Zjy/kf/";
    public static String CG_DIR = "/Zjy/caigou/";

    public static String getPankuRemoteName(String id) {
        return "a_" + id + "_" + getTimeYmdhms()+ "_" + getRandomNumber(4);
    }

    public static String getChukuRemoteName(String id) {
        return "and_" + id + "_" + getTimeYmdhms() + "_" + getRandomNumber(4);
    }
    public static String getChukuRemoteNameNew(String id) {
        return "a_ck_" + id + "_" + getTimeYmdhms() + "_" + getRandomNumber(4);
    }

    public static String getTimeYmdhms() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    public static String getRandomNumber(int len) {
        String flag = String.valueOf(Math.random());
        return flag.substring(2, 2 + len);
    }

    public static String getCurrentAtSS() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        String str = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar
                .DAY_OF_MONTH);
        return str;
    }

    public static String getCurrentYearAndMonth() {
        Calendar calendar = Calendar.getInstance();
        String str = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1);
        return str;
    }

    public static String getyyMM() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
        String str = sdf.format(new Date());
        return str;
    }

    public static String getDD(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String str = sdf.format(date);
        return str;
    }

    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        String str = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return str;
    }

    public static String getCaigouRemoteDir(String fileName) {
        return "/" + getCurrentDate() + "/" + fileName;
    }

    public static String getChukuRemotePath(String pid) {
        return getChukuRemotePath(getChukuRemoteName(pid), pid);
    }

    public static String getChukuRemotePath(String nowName, String pid) {
        return "/" + getCurrentDate() + "/" + nowName + ".jpg";
    }

    public static String getTestPath(String pid) {
        return KF_DIR + getChukuRemoteName(pid) + ".jpg";
    }

    /**
     @param ftpUrl
     @param path
     @return 插入到数据库的图片地址
     */
    public static String createInsertPath(String ftpUrl, String path) {
        StringBuilder builder = new StringBuilder();
        builder.append("ftp://");
        builder.append(ftpUrl);
        builder.append(path);
        return builder.toString();
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceID(Context mContext) {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return "service is unable:" + getTimeYmdhms();
        }
        String deviceid=tm.getDeviceId();
        String serial;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serial = android.os.Build.getSerial();
        } else {
            serial = Build.SERIAL;
        }
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            MyApp.myLogger.writeInfo("android-10 inUse");
            //android 10兼容id
            String m_szDevIDShort = "35" +
                    Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                    Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                    Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                    Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                    Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                    Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                    Build.USER.length() % 10; //13 位
            deviceid = "";

            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
            //            getGLESTextureLimitEqualAboveLollipop();
        }
        return deviceid;
    }

    @SuppressLint("MissingPermission")
    public static String getPhoneCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return "service is unable:" + getTimeYmdhms();
        }
        String deviceId = tm.getDeviceId();
        if(deviceId==null){
            deviceId = tm.getSubscriberId();
        }
        if (deviceId == null) {
        }

        String phoneModel = Build.MODEL;
        String phoneName = Build.BRAND;
        StringBuilder phoneId = new StringBuilder();
        phoneId.append(phoneModel);
        phoneId.append("-");
        phoneId.append(phoneName);
        phoneId.append("-");
        phoneId.append(deviceId);
        return phoneId.toString();
    }
    /**
     @return 插入到数据库的图片地址
     */
    public static String createSCCGRemoteName(String pid) {
        StringBuilder builder = new StringBuilder();
        builder.append("SCCG_a_");
        builder.append(pid);
        builder.append("_");
        builder.append(System.currentTimeMillis());
        return builder.toString();
    }

    public static String createSHQD_Rm(String pid) {
        StringBuilder builder = new StringBuilder();
        builder.append("SHQD_");
        builder.append(pid);
        builder.append("_a_");
        builder.append(getRandomNumber(4));
        return builder.toString();
    }

    public static String getSQDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String str = sdf.format(date);
        return str;
    }

    public static String getPkRemotePath(String pid) {
        String remotePath = "/" +getCurrentDate() + "/pk/" +getPankuRemoteName(pid) + ".jpg";
        return remotePath;
    }

    public static String getSampTime() {
        String format = DateFormat.getDateTimeInstance().format(new Date());
        return format;
    }

    public static int getNowHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}
