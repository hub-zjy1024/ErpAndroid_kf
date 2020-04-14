package utils.common;

public class Myuuid {
	public static String create(int lastLen){
		String s = System.currentTimeMillis() + create2(lastLen);
		return s;
	}
	public static String create2(int lastLen){
		String str = String.valueOf(Math.random());
		int start = 2;
		String s = "";
		if (start + lastLen > str.length()) {
			int leftLen = str.length() - start - lastLen;
			String mChar = create2(leftLen);
			mChar.substring(2,2+lastLen);
			s += mChar;
		}else{
			s += str.substring(start, start + lastLen);
		}
		return s;
	}
}
