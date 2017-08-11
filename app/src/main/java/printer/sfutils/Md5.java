package printer.sfutils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 张建宇 on 2017/6/22.
 */

public class Md5 {
    public static String getMD5(String source) {
        String s = null;
        // 用来将字节转换成 16 进制表示的字符
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F'};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source.getBytes("UTF-8"));
            // MD5 的计算结果是一个 128 位的长整数，
            byte tmp[] = md.digest();
            // 用字节表示就是 16 个字节
            // 每个字节用 16 进制表示的话，使用两个字符，
            // 所以表示成 16 进制需要 32 个字符
            char str[] = new char[16 * 2];
            // 表示转换结果中对应的字符位置
            int k = 0;
            // 从第一个字节开始，对 MD5 的每一个字节
            // 转换成 16 进制字符的转换
            for (int i = 0; i < 16; i++) {
                // 取第 i 个字节
                byte byte0 = tmp[i];
                // 取字节中高 4 位的数字转换,>>> 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                // 取字节中低 4 位的数字转换
                str[k++] = hexDigits[byte0 & 0xf];
            }
            // 换后的结果转换为字符串
            s = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    // 方法二
    public static String getMD5Second(String str) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes("UTF-8"));
            for (byte b : md5.digest()) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * @param str 待加密的字符串
     * @return 返回加密后的字节数组
     *
     */
    // 方法二
    public static byte[] getMD5Bytes(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes("UTF-8"));
            return md5.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
