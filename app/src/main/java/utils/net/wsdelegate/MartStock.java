package utils.net.wsdelegate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class MartStock{
	private static String serverName = WebserviceUtils.MartStock;
//findinfo:q1:FindInfo
//NotFormate Void:GetMartStockListInfo
	@Deprecated
	public static String GetMartStockListInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "GetMartStockListInfo", serverName);
		return res;
	}

//pid:xs:string
		public static String GetMartStockListByPID(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartStockListByPID", serverName);
		return res;
	}

//uid:xs:string
		public static String GetMartStockListDataBind(String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartStockListDataBind", serverName);
		return res;
	}

//id:xs:string
//stype:xs:string
//keyValue:xs:string
		public static String GetMartFormShow(String id,String stype,String keyValue)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("stype", stype);
		properties.put("keyValue", keyValue);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartFormShow", serverName);
		return res;
	}

//uid:xs:string
//sid:xs:int
		public static String GetMartMXInfoByUserID(String uid,int sid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("sid", sid);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartMXInfoByUserID", serverName);
		return res;
	}

//id:xs:int
		public static String GetProviderInfoByID(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetProviderInfoByID", serverName);
		return res;
	}

	public static String GetCaiGouBuInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		 String res=WebserviceUtils.getWcfResult(properties, "GetCaiGouBuInfo",serverName);
		return res;
	}


//maininfo:q2:MartStockInfo
//NotFormate Void:InsertIntoInfo
	@Deprecated
	public static String InsertIntoInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "InsertIntoInfo", serverName);
		return res;
	}

//checkWord:xs:string
//cid:xs:int
//did:xs:int
//uid:xs:int
//pid:xs:string
//filename:xs:string
//filepath:xs:string
//stypeID:xs:string
		public static String InsertSSCGPicInfo(String checkWord,int cid,int did,int uid,String pid,String filename,String filepath,String stypeID)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("checkWord", checkWord);
		properties.put("cid", cid);
		properties.put("did", did);
		properties.put("uid", uid);
		properties.put("pid", pid);
		properties.put("filename", filename);
		properties.put("filepath", filepath);
		properties.put("stypeID", stypeID);
		String res=WebserviceUtils.getWcfResult(properties, "InsertSSCGPicInfo", serverName);
		return res;
	}

//pid:xs:string
//maininfo:q5:MartStockInfo
//NotFormate Void:GetMartStockInfoDetailInfo
	@Deprecated
	public static String GetMartStockInfoDetailInfo(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetMartStockInfoDetailInfo", serverName);
		return res;
	}

//key:xs:string
		public static String GetCJInfo(String key)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("key", key);
		String res=WebserviceUtils.getWcfResult(properties, "GetCJInfo", serverName);
		return res;
	}

//findinfo:q9:FindInfo
//NotFormate Void:GetForenigeStockList
	@Deprecated
	public static String GetForenigeStockList()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "GetForenigeStockList", serverName);
		return res;
	}

//uid:xs:string
		public static String GetBinDingList(String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetBinDingList", serverName);
		return res;
	}

//pid:xs:string
//mainForm:q10:ForStocks
//NotFormate Void:GetForenigeStockInfoByID
	@Deprecated
	public static String GetForenigeStockInfoByID(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetForenigeStockInfoByID", serverName);
		return res;
	}

//uid:xs:string
		public static String IsCorpManagerPass(String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "IsCorpManagerPass", serverName);
		return res;
	}

//sqlwhere:xs:string
		public static String IsSH(String sqlwhere)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("sqlwhere", sqlwhere);
		String res=WebserviceUtils.getWcfResult(properties, "IsSH", serverName);
		return res;
	}

//main:q14:ForStocks
//NotFormate Void:SaveForenigeStockInfo
	@Deprecated
	public static String SaveForenigeStockInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "SaveForenigeStockInfo", serverName);
		return res;
	}

//uid:xs:string
		public static String GetCKListInfo(String uid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		String res=WebserviceUtils.getWcfResult(properties, "GetCKListInfo", serverName);
		return res;
	}

//findinfo:q17:FindInfo
//NotFormate Void:GetCKTZListInfo
	@Deprecated
	public static String GetCKTZListInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "GetCKTZListInfo", serverName);
		return res;
	}

//id:xs:string
		public static String GetClientInfo(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetClientInfo", serverName);
		return res;
	}

//id:xs:int
		public static String GetModuleInfoClientByID(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetModuleInfoClientByID", serverName);
		return res;
	}

//id:xs:int
		public static String GetModuleBD_EmployeeInfoByID(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetModuleBD_EmployeeInfoByID", serverName);
		return res;
	}

//id:xs:string
		public static String GetInvoiceCorpInfo(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetInvoiceCorpInfo", serverName);
		return res;
	}

//pid:xs:string
//main:q18:ChuKuTongZhiInfo
//NotFormate Void:GetCKTZInfoByID
	@Deprecated
	public static String GetCKTZInfoByID(String pid)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("pid", pid);
		String res=WebserviceUtils.getWcfResult(properties, "GetCKTZInfoByID", serverName);
		return res;
	}

//partNo:xs:string
//storageID:xs:string
//corpID:xs:string
//manageStoreRoomID:xs:string
//onlyShowKaiPiao:xs:boolean
		public static String GetXingHaoInfo(String partNo,String storageID,String corpID,String manageStoreRoomID,boolean onlyShowKaiPiao)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("partNo", partNo);
		properties.put("storageID", storageID);
		properties.put("corpID", corpID);
		properties.put("manageStoreRoomID", manageStoreRoomID);
		properties.put("onlyShowKaiPiao", onlyShowKaiPiao);
		String res=WebserviceUtils.getWcfResult(properties, "GetXingHaoInfo", serverName);
		return res;
	}

//ip:xs:string
		public static String OnlySearchKaiPiao(String ip)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("ip", ip);
		String res=WebserviceUtils.getWcfResult(properties, "OnlySearchKaiPiao", serverName);
		return res;
	}

//id:xs:int
		public static String GetEmployeeForeigNameByID(int id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetEmployeeForeigNameByID", serverName);
		return res;
	}

//typeID:xs:int
//limitInvoiceCorpStr:xs:string
		public static String GetInvoiceCorpGuoWai(int typeID,String limitInvoiceCorpStr)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("typeID", typeID);
		properties.put("limitInvoiceCorpStr", limitInvoiceCorpStr);
		String res=WebserviceUtils.getWcfResult(properties, "GetInvoiceCorpGuoWai", serverName);
		return res;
	}

//id:xs:string
		public static String GetInvoiceCorpGuoWaiByID(String id)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		String res=WebserviceUtils.getWcfResult(properties, "GetInvoiceCorpGuoWaiByID", serverName);
		return res;
	}

//id:xs:string
//country:xs:string
//address:xs:string
//phone:xs:string
//fax:xs:string
		public static String SetUpdateInvoiceCorp(String id,String country,String address,String phone,String fax)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("country", country);
		properties.put("address", address);
		properties.put("phone", phone);
		properties.put("fax", fax);
		String res=WebserviceUtils.getWcfResult(properties, "SetUpdateInvoiceCorp", serverName);
		return res;
	}

//id:xs:int
//name:xs:string
		public static String UpdateUserYWName(int id,String name)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("name", name);
		String res=WebserviceUtils.getWcfResult(properties, "UpdateUserYWName", serverName);
		return res;
	}

//id:xs:int
//account:xs:string
		public static String UpdateClientAccountNo(int id,String account)throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put("id", id);
		properties.put("account", account);
		String res=WebserviceUtils.getWcfResult(properties, "UpdateClientAccountNo", serverName);
		return res;
	}

//find:q22:FindInfo
//NotFormate Void:GetPictureList
	@Deprecated
	public static String GetPictureList()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "GetPictureList", serverName);
		return res;
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
//main:q23:ChuKuTongZhiInfo
//NotFormate Void:SetChuKuTongZhiInfo
	@Deprecated
	public static String SetChuKuTongZhiInfo()throws IOException, XmlPullParserException {
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		String res=WebserviceUtils.getWcfResult(properties, "SetChuKuTongZhiInfo", serverName);
		return res;
	}

}