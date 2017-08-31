package utils;

import java.util.Random;

public class UuidUtils {
	public static String getIDWithDate(int len) {
		Random ran = new Random();
		double d = Math.random();
		String str = String.valueOf(d);
		String dest = str.substring(2, 2+len);
		return dest;
	}
}
