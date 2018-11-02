package utils.wsdelegate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

import utils.WebserviceUtils;

public class MartService{
	private static String serverName = WebserviceUtils.MartService;
//txt:xs:string
		public static String HelloWorld(String txt)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("txt", txt);
		String res=WebserviceUtils.getWcfResult(properties, "HelloWorld", serverName);
		return res;
	}

//checkWord:xs:string
//userID:xs:string
//passWord:xs:string
//DeviceID:xs:string
//version:xs:string
		public static String AndroidLogin(String checkWord,String userID,String passWord,String DeviceID,String version)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("userID", userID);
		properties.put("passWord", passWord);
		properties.put("DeviceID", DeviceID);
		properties.put("version", version);
		String res=WebserviceUtils.getWcfResult(properties, "AndroidLogin", serverName);
		return res;
	}

//checkWord:xs:string
//buyerID:xs:int
//partNo:xs:string
		public static String GetBillByPartNo(String checkWord,int buyerID,String partNo)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("buyerID", buyerID);
		properties.put("partNo", partNo);
		String res=WebserviceUtils.getWcfResult(properties, "GetBillByPartNo", serverName);
		return res;
	}

//checkWord:xs:string
//buyerID:xs:int
//pid:xs:string
//partNo:xs:string
		public static String GetBillByPartNoAndPid(String checkWord,int buyerID,String pid,String partNo)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("buyerID", buyerID);
		properties.put("pid", pid);
		properties.put("partNo", partNo);
		String res=WebserviceUtils.getWcfResult(properties, "GetBillByPartNoAndPid", serverName);
		return res;
	}

//checkWord:xs:string
//id:xs:int
		public static String GetMartStockInfoByID(String checkWord,int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartStockInfoByID", serverName);
		return res;
	}

//checkWord:xs:string
//userID:xs:int
//myDeptID:xs:int
//providerName:xs:string
		public static String GetMyProvider(String checkWord,int userID,int myDeptID,String providerName)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("userID", userID);
		properties.put("myDeptID", myDeptID);
		properties.put("providerName", providerName);
		String res=WebserviceUtils.getWcfResult(properties, "GetMyProvider", serverName);
		return res;
	}

//checkWord:xs:string
//id:xs:int
//providerID:xs:int
//nofapiao:xs:int
//details:xs:string
//operID:xs:int
//operName:xs:string
//deviceID:xs:string
//storageID:xs:int
		public static String SaveMartStock(String checkWord,int id,int providerID,int nofapiao,String details,int operID,String operName,String deviceID,int storageID)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("id", id);
		properties.put("providerID", providerID);
		properties.put("nofapiao", nofapiao);
		properties.put("details", details);
		properties.put("operID", operID);
		properties.put("operName", operName);
		properties.put("deviceID", deviceID);
		properties.put("storageID", storageID);
		String res=WebserviceUtils.getWcfResult(properties, "SaveMartStock", serverName);
		return res;
	}

//checkWord:xs:string
//partNo:xs:string
		public static String GetBiJiaList(String checkWord,String partNo)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("partNo", partNo);
		String res=WebserviceUtils.getWcfResult(properties, "GetBiJiaList", serverName);
		return res;
	}

//checkWord:xs:string
//mainID:xs:int
		public static String GetBiJiaDetail(String checkWord,int mainID)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("mainID", mainID);
		String res=WebserviceUtils.getWcfResult(properties, "GetBiJiaDetail", serverName);
		return res;
	}

//checkWord:xs:string
//deptID:xs:int
//martStockID:xs:int
//note:xs:string
//price:xs:string
//providerID:xs:int
//tempProviderName:xs:string
//tempProviderPhone:xs:string
//tempProviderLinkMen:xs:string
//payType:xs:string
//jzDate:xs:string
//userID:xs:int
//userName:xs:string
//providerEnableKP:xs:int
//faPiao:xs:int
		public static String InsertCompare(String checkWord,int deptID,int martStockID,String note,String price,int providerID,String tempProviderName,String tempProviderPhone,String tempProviderLinkMen,String payType,String jzDate,int userID,String userName,int providerEnableKP,int faPiao)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("deptID", deptID);
		properties.put("martStockID", martStockID);
		properties.put("note", note);
		properties.put("price", price);
		properties.put("providerID", providerID);
		properties.put("tempProviderName", tempProviderName);
		properties.put("tempProviderPhone", tempProviderPhone);
		properties.put("tempProviderLinkMen", tempProviderLinkMen);
		properties.put("payType", payType);
		properties.put("jzDate", jzDate);
		properties.put("userID", userID);
		properties.put("userName", userName);
		properties.put("providerEnableKP", providerEnableKP);
		properties.put("faPiao", faPiao);
		String res=WebserviceUtils.getWcfResult(properties, "InsertCompare", serverName);
		return res;
	}

//checkWord:xs:string
//userid:xs:int
//filename:xs:string
//image:xs:string
		public static String uploadImage(String checkWord,int userid,String filename,String image)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("userid", userid);
		properties.put("filename", filename);
		properties.put("image", image);
		String res=WebserviceUtils.getWcfResult(properties, "uploadImage", serverName);
		return res;
	}

//checkWord:xs:string
//code:xs:string
		public static String BarCodeLogin(String checkWord,String code)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("code", code);
		String res=WebserviceUtils.getWcfResult(properties, "BarCodeLogin", serverName);
		return res;
	}

//sid:xs:string
		public static String GetFtpPhoneIP(String sid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("sid", sid);
		String res=WebserviceUtils.getWcfResult(properties, "GetFtpPhoneIP", serverName);
		return res;
	}

	public static String GetClientIP()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetClientIP", serverName);
		return res;
	}


//pid:xs:int
//filepath:xs:string
		public static String UpdateHeTongFileInfo(int pid,String filepath)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("filepath", filepath);
		String res=WebserviceUtils.getWcfResult(properties, "UpdateHeTongFileInfo", serverName);
		return res;
	}

//pid:xs:int
		public static String GetHeTongFileInfo(int pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetHeTongFileInfo", serverName);
		return res;
	}

//id:xs:int
		public static String GetPriviteInfo(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetPriviteInfo", serverName);
		return res;
	}

//id:xs:int
		public static String GetInvoiceCorpInfo(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetInvoiceCorpInfo", serverName);
		return res;
	}

//pid:xs:string
//partNo:xs:string
//buyerID:xs:string
		public static String GetOLDMartStockView_ok(String pid,String partNo,String buyerID)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("partNo", partNo);
		properties.put("buyerID", buyerID);
		String res=WebserviceUtils.getWcfResult(properties, "GetOLDMartStockView_ok", serverName);
		return res;
	}

//pid:xs:string
		public static String GetOLDMartStockView_mx(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetOLDMartStockView_mx", serverName);
		return res;
	}

//ip:xs:string
		public static String GetChildStorageIDByIP(String ip)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("ip", ip);
		String res=WebserviceUtils.getWcfResult(properties, "GetChildStorageIDByIP", serverName);
		return res;
	}

//selValue:xs:string
		public static String GetXinHaoManageInfo(String selValue)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("selValue", selValue);
		String res=WebserviceUtils.getWcfResult(properties, "GetXinHaoManageInfo", serverName);
		return res;
	}

//pid:xs:string
//uid:xs:string
//strValue:xs:string
		public static String SetSC_BD_PartTypeInfoInfo(String pid,String uid,String strValue)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("uid", uid);
		properties.put("strValue", strValue);
		String res=WebserviceUtils.getWcfResult(properties, "SetSC_BD_PartTypeInfoInfo", serverName);
		return res;
	}

//id:xs:string
		public static String GetProviderPartInfo(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetProviderPartInfo", serverName);
		return res;
	}

//objid:xs:string
		public static String GetProviderPartInfoByObjID(String objid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("objid", objid);
		String res=WebserviceUtils.getWcfResult(properties, "GetProviderPartInfoByObjID", serverName);
		return res;
	}

//id:xs:string
		public static String DeleteProviderPartInfo(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "DeleteProviderPartInfo", serverName);
		return res;
	}

//checkWord:xs:string
//userID:xs:int
//myDeptID:xs:int
//providerName:xs:string
		public static String GetMyProviderInfoByName(String checkWord,int userID,int myDeptID,String providerName)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("userID", userID);
		properties.put("myDeptID", myDeptID);
		properties.put("providerName", providerName);
		String res=WebserviceUtils.getWcfResult(properties, "GetMyProviderInfoByName", serverName);
		return res;
	}

//objid:xs:string
//parentid:xs:string
//objname:xs:string
//objvalue:xs:string
//objtype:xs:string
//objexpress:xs:string
		public static String SetProviderPartInfoAdd(String objid,String parentid,String objname,String objvalue,String objtype,String objexpress)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("objid", objid);
		properties.put("parentid", parentid);
		properties.put("objname", objname);
		properties.put("objvalue", objvalue);
		properties.put("objtype", objtype);
		properties.put("objexpress", objexpress);
		String res=WebserviceUtils.getWcfResult(properties, "SetProviderPartInfoAdd", serverName);
		return res;
	}

//partno:xs:string
//pid:xs:string
		public static String GetSSCGInfoByDDYH(String partno,String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("partno", partno);
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetSSCGInfoByDDYH", serverName);
		return res;
	}

//partno:xs:string
//pid:xs:string
		public static String GetSSCGInfoByDDYHList(String partno,String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("partno", partno);
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetSSCGInfoByDDYHList", serverName);
		return res;
	}

//pid:xs:string
		public static String GetSSCGInfoByDDYHByID(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetSSCGInfoByDDYHByID", serverName);
		return res;
	}

//pid:xs:string
//state:xs:string
//chkNote:xs:string
		public static String UpdateSSCSState(String pid,String state,String chkNote)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("state", state);
		properties.put("chkNote", chkNote);
		String res=WebserviceUtils.getWcfResult(properties, "UpdateSSCSState", serverName);
		return res;
	}

//PictureName:xs:string
//PictureURL:xs:string
//MakerID:xs:string
//CorpID:xs:string
//DeptID:xs:string
//UserID:xs:string
//billID:xs:string
//billType:xs:string
		public static String InsertPicYHInfo(String PictureName,String PictureURL,String MakerID,String CorpID,String DeptID,String UserID,String billID,String billType)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("PictureName", PictureName);
		properties.put("PictureURL", PictureURL);
		properties.put("MakerID", MakerID);
		properties.put("CorpID", CorpID);
		properties.put("DeptID", DeptID);
		properties.put("UserID", UserID);
		properties.put("billID", billID);
		properties.put("billType", billType);
		String res=WebserviceUtils.getWcfResult(properties, "InsertPicYHInfo", serverName);
		return res;
	}

//pid:xs:string
		public static String GetYHPicInfo(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetYHPicInfo", serverName);
		return res;
	}

//pid:xs:int
//cbtype:xs:string
		public static String GetCheckInfo(int pid,String cbtype)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("cbtype", cbtype);
		String res=WebserviceUtils.getWcfResult(properties, "GetCheckInfo", serverName);
		return res;
	}

//pid:xs:string
//title:xs:string
//uid:xs:string
//note:xs:string
//type:xs:string
		public static String SetCheckInfo(String pid,String title,String uid,String note,String type)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("title", title);
		properties.put("uid", uid);
		properties.put("note", note);
		properties.put("type", type);
		String res=WebserviceUtils.getWcfResult(properties, "SetCheckInfo", serverName);
		return res;
	}

//part:xs:string
//pcount:xs:int
//isbhbm:xs:boolean
		public static String GetInstorageBalanceInfoNew(String part,int pcount,boolean isbhbm)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("part", part);
		properties.put("pcount", pcount);
		properties.put("isbhbm", isbhbm);
		String res=WebserviceUtils.getWcfResult(properties, "GetInstorageBalanceInfoNew", serverName);
		return res;
	}

//detailID:xs:int
//price:xs:decimal
//uid:xs:int
//ip:xs:string
//dogSN:xs:string
		public static String SetPriceInfo(int detailID,float price,int uid,String ip,String dogSN)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("detailID", detailID);
		properties.put("price", price);
		properties.put("uid", uid);
		properties.put("ip", ip);
		properties.put("dogSN", dogSN);
		String res=WebserviceUtils.getWcfResult(properties, "SetPriceInfo", serverName);
		return res;
	}

//detailID:xs:int
//isfb:xs:boolean
//uid:xs:int
//ip:xs:string
//dogSN:xs:string
		public static String SetStypeInfo(int detailID,boolean isfb,int uid,String ip,String dogSN)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("detailID", detailID);
		properties.put("isfb", isfb);
		properties.put("uid", uid);
		properties.put("ip", ip);
		properties.put("dogSN", dogSN);
		String res=WebserviceUtils.getWcfResult(properties, "SetStypeInfo", serverName);
		return res;
	}

	public static String GetBHBMDataInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetBHBMDataInfo", serverName);
		return res;
	}


	public static String GetInseorageBalanceInfoToSender()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetInseorageBalanceInfoToSender", serverName);
		return res;
	}


	public static String GetInseorageBalanceInfoToCount()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetInseorageBalanceInfoToCount", serverName);
		return res;
	}


//pid:xs:string
//uid:xs:string
		public static String SetNHWC_UserIDInfo(String pid,String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "SetNHWC_UserIDInfo", serverName);
		return res;
	}

//uid:xs:string
//pid:xs:string
//type:xs:string
		public static String GetMHWCInfo(String uid,String pid,String type)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("pid", pid);
		properties.put("type", type);
		String res=WebserviceUtils.getWcfResult(properties, "GetMHWCInfo", serverName);
		return res;
	}

//pid:xs:string
//checkinfo:xs:string
		public static String SetNaHuoWanChengInfo(String pid,String checkinfo)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		properties.put("checkinfo", checkinfo);
		String res=WebserviceUtils.getWcfResult(properties, "SetNaHuoWanChengInfo", serverName);
		return res;
	}

	/**
	 * 获取快递时效
	 * @param pid 4:跨越，1：顺丰
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String GetHYTypeInfo(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		return WebserviceUtils.getWcfResult(properties, "GetHYTypeInfo", serverName);
	}
	public static String getSellListDetails(String rq,String ghs,String kpgs)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("rq", rq);
		properties.put("ghs", ghs);
		properties.put("kpgs", kpgs);
		return WebserviceUtils.getWcfResult(properties, "getSellListDetails", serverName);
	}

	public static String getSellList(String rq,String gys,String kpgs,String currentKP)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("rq", rq);
		properties.put("gys", gys);
		properties.put("kpgs", kpgs);
		properties.put("currentKP", currentKP);
		return WebserviceUtils.getWcfResult(properties, "getSellList", serverName);
	}

	public static String InsertPicInfo(String userId,String picName,String picUrl,String note)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("userId", userId);
		properties.put("picName", picName);
		properties.put("picUrl", picUrl);
		properties.put("note", note);
		return WebserviceUtils.getWcfResult(properties, "InsertPicInfo", serverName);
	}
}