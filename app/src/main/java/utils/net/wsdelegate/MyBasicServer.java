package utils.net.wsdelegate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class MyBasicServer{
	private static String serverName = WebserviceUtils.MyBasicServer;
//month:xs:string
//uid:xs:string
//checkWord:xs:string
		public static String GetMyKaoQinInfoJson(String month,String uid,String checkWord)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("month", month);
		properties.put("uid", uid);
		properties.put("checkWord", checkWord);
		String res=WebserviceUtils.getWcfResult(properties, "GetMyKaoQinInfoJson", serverName);
		return res;
	}

//uid:xs:string
//checkWord:xs:string
		public static String GetMyPicInfo(String uid,String checkWord)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("checkWord", checkWord);
		String res=WebserviceUtils.getWcfResult(properties, "GetMyPicInfo", serverName);
		return res;
	}

//id:xs:string
//checkWord:xs:string
		public static String GetMyPicByID(String id,String checkWord)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("checkWord", checkWord);
		String res=WebserviceUtils.getWcfResult(properties, "GetMyPicByID", serverName);
		return res;
	}

}