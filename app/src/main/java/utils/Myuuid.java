package utils;

public class Myuuid {
	public static String create(int lastLen){
		String s = System.currentTimeMillis() + create2(lastLen);
		return s;
	}
	public static String create2(int lastLen){
		String s=String.valueOf(Math.random()).substring(2,2+lastLen);
		return s;
	}
}
