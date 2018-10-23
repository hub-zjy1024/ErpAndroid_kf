package utils.net.wsdelegate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class Login{
	private static String serverName = WebserviceUtils.Login;
//uid:xs:string
//pwd:xs:string
//ip:xs:string
//netWorkID:xs:string
//mainBordID:xs:string
//hostName:xs:string
//diskID:xs:string
//diskVolNum:xs:string
//yzinfo:q1:YanZhengInfo
//NotFormate Void:GetUserInfo
	@Deprecated
	public static String GetUserInfo(String uid,String pwd,String ip,String netWorkID,String mainBordID,String hostName,String diskID,String diskVolNum)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("pwd", pwd);
		properties.put("ip", ip);
		properties.put("netWorkID", netWorkID);
		properties.put("mainBordID", mainBordID);
		properties.put("hostName", hostName);
		properties.put("diskID", diskID);
		properties.put("diskVolNum", diskVolNum);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfo", serverName);
		return res;
	}

//uid:xs:string
//pwd:xs:string
//ip:xs:string
//netWorkID:xs:string
//mainBordID:xs:string
//hostName:xs:string
//diskID:xs:string
//diskVolNum:xs:string
		public static String GetUserInfoByID(String uid,String pwd,String ip,String netWorkID,String mainBordID,String hostName,String diskID,String diskVolNum)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("pwd", pwd);
		properties.put("ip", ip);
		properties.put("netWorkID", netWorkID);
		properties.put("mainBordID", mainBordID);
		properties.put("hostName", hostName);
		properties.put("diskID", diskID);
		properties.put("diskVolNum", diskVolNum);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByID", serverName);
		return res;
	}

//uid:xs:string
//pwd:xs:string
//ip:xs:string
//netWorkID:xs:string
//mainBordID:xs:string
//hostName:xs:string
//diskID:xs:string
//diskVolNum:xs:string
		public static String GetUserInfoByIDFordt(String uid,String pwd,String ip,String netWorkID,String mainBordID,String hostName,String diskID,String diskVolNum)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("pwd", pwd);
		properties.put("ip", ip);
		properties.put("netWorkID", netWorkID);
		properties.put("mainBordID", mainBordID);
		properties.put("hostName", hostName);
		properties.put("diskID", diskID);
		properties.put("diskVolNum", diskVolNum);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByIDFordt", serverName);
		return res;
	}

//checker:xs:string
//uid:xs:string
		public static String GetUserInfoByUserID(String checker,String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checker", checker);
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByUserID", serverName);
		return res;
	}

//checker:xs:string
//uid:xs:string
		public static String GetUserInfoByUID(String checker,String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checker", checker);
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByUID", serverName);
		return res;
	}

//uid:xs:string
		public static String GetCCInfoByUserID(String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetCCInfoByUserID", serverName);
		return res;
	}

	public static String GetUserInfoByAllInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByAllInfo", serverName);
		return res;
	}


//id:xs:string
		public static String GetUserInfoByAllInfoByID(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetUserInfoByAllInfoByID", serverName);
		return res;
	}

//uid:xs:string
//pwd:xs:string
//oldpwd:xs:string
		public static String UpdateUserPwd(String uid,String pwd,String oldpwd)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("pwd", pwd);
		properties.put("oldpwd", oldpwd);
		String res=WebserviceUtils.getWcfResult(properties, "UpdateUserPwd", serverName);
		return res;
	}

//Name:xs:string
//DownPath:xs:string
//A_Downpic:xs:string
//A_NDownpic:xs:string
//CC_path:xs:string
//A_bm:xs:string
		public static String SetCCInfo(String Name,String DownPath,String A_Downpic,String A_NDownpic,String CC_path,String A_bm)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("Name", Name);
		properties.put("DownPath", DownPath);
		properties.put("A_Downpic", A_Downpic);
		properties.put("A_NDownpic", A_NDownpic);
		properties.put("CC_path", CC_path);
		properties.put("A_bm", A_bm);
		String res=WebserviceUtils.getWcfResult(properties, "SetCCInfo", serverName);
		return res;
	}

	public static String GetCCInfoAll()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetCCInfoAll", serverName);
		return res;
	}


//pid:xs:string
//filenames:xs:string
//downpath:xs:string
		public static String InsertIntoCC(String pid,String filenames,String downpath)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("filenames", filenames);
		properties.put("downpath", downpath);
		String res=WebserviceUtils.getWcfResult(properties, "InsertIntoCC", serverName);
		return res;
	}

//A_ID:xs:string
		public static String DeleteCCInfo(String A_ID)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("A_ID", A_ID);
		String res=WebserviceUtils.getWcfResult(properties, "DeleteCCInfo", serverName);
		return res;
	}

	public static String GetUserMenuInfoAll()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetUserMenuInfoAll", serverName);
		return res;
	}


	public static String GetUserTreeInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetUserTreeInfo", serverName);
		return res;
	}


//uid:xs:string
//A_ID:xs:string
//st:xs:string
		public static String DeleteUserMenuByUserID(String uid,String A_ID,String st)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("A_ID", A_ID);
		properties.put("st", st);
		String res=WebserviceUtils.getWcfResult(properties, "DeleteUserMenuByUserID", serverName);
		return res;
	}

}