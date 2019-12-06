package utils.net.wsdelegate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class RKServer{
	private static String serverName = WebserviceUtils.RKServer;
	public static String GetApplyCustomInfo(String codes)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("codes", codes);
		return WebserviceUtils.getWcfResult(properties, "GetApplyCustomInfo", serverName);
	}
	public static String GetShangJiaInfo(String codes)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("codes", codes);
		return WebserviceUtils.getWcfResult(properties, "GetShangJiaInfo", serverName);
	}

	public static String SetCustomsRuKu(String json)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("json", json);
		return WebserviceUtils.getWcfResult(properties, "SetCustomsRuKu", serverName);
	}

	public static String ShangJia(String id,String place,String kuQu,String userId)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("place", place);
		properties.put("kuQu", kuQu);
		properties.put("userId", userId);
		return WebserviceUtils.getWcfResult(properties, "ShangJia", serverName);
	}

	public static String SetApplyCustomRuKu(String json)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("json", json);
		return WebserviceUtils.getWcfResult(properties, "SetApplyCustomRuKu", serverName);
	}


	public static String ChaiDan(int instorageMainID, int instorageDetailID, String json,
								 String loginID) throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("instorageMainID", instorageMainID);
		properties.put("instorageDetailID", instorageDetailID);
		//		BatchNo  Number
		properties.put("json", json);
		properties.put("loginID", loginID);
		return WebserviceUtils.getWcfResult(properties, "ChaiDan", serverName);
	}

	public static String ChaiDanLocal(int instorageMainID, int instorageDetailID, String json,
									  String loginID) throws IOException, XmlPullParserException {

		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("instorageMainID", instorageMainID);
		properties.put("instorageDetailID", instorageDetailID);
		//		BatchNo  Number
		properties.put("json", json);
		properties.put("loginID", loginID);
		return WebserviceUtils.getLocalWcf(properties, "ChaiDan", serverName);
	}

	public static String CommitSplit(String data, String pid, String detailId) throws IOException,
			XmlPullParserException {
		return null;
	}
}