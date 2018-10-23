package utils.common;

import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.entity.ChuKuDanInfo;
import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;
import com.b1b.js.erpandroid_kf.entity.KaoqinInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by js on 2016/12/28.
 */

public class MyJsonUtils {

    //"EmployeeID": "100",
//"员工": "朱强",
//"考勤年月": "20161101",
//"考勤状态": "迟到早退",
//"上班时间": "10:41:01",
//"下班时间": "10:41:01",
//"早IP": "172.16.1.102",
//"晚IP": "172.16.1.102"
    public static List<KaoqinInfo> getKaoQinList(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        JSONArray array = object.getJSONArray("表");
        List<KaoqinInfo> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            KaoqinInfo kqi = new KaoqinInfo();
            kqi.setEmpId(obj.getString("EmployeeID"));
            kqi.setEmpName(obj.getString("员工"));
            kqi.setDate(obj.getString("考勤年月"));
            kqi.setState(obj.getString("考勤状态"));
            kqi.setStartTime(obj.getString("上班时间"));
            kqi.setEndTime(obj.getString("下班时间"));
            kqi.setStartIp(obj.getString("早IP"));
            kqi.setEndIp(obj.getString("晚IP"));
            list.add(kqi);
        }
        return list;
    }

    public static List<ChukuTongZhiInfo> getCKTZList(String json) throws JSONException {
        if (json == null) {
            return null;
        }
        List<ChukuTongZhiInfo> list = new ArrayList<>();
        JSONObject object = new JSONObject(json);
        JSONArray ob = object.getJSONArray("表");
//        "PID": "1112898",
//                "制单日期": "2016/12/1 10:02:23",
//                "公司": "总公司采购部",
//                "部门": "北京采购部",
//                "员工": "苏海玲",
//                "制单人": "苏海玲",
//                "单据类型": "内部销售",
//                "单据状态": "已出库,完成",
//                "发货类型": "库房发货",
//                "型号": "GRM32ER71H106KA12L",
//                "数量": "5000",
//                "进价": "0.5299",
//                "售价": "0.5299",
//                "成本": "2649.5000",
//                "销售额": "2649.5000",
//                "厂家": "murata",
//                "封装": "1210",
//                "描述": "10uF_±10%_50V_X7R_1210"
        for (int i = 0; i < ob.length(); i++) {
            JSONObject obj = ob.getJSONObject(i);
            ChukuTongZhiInfo cktz = new ChukuTongZhiInfo();
            cktz.setPid(obj.getString("PID"));
            cktz.setpDate(obj.getString("制单日期"));
            cktz.setCompany(obj.getString("公司"));
            cktz.setDeptName(obj.getString("部门"));
            cktz.setuName(obj.getString("员工"));
            cktz.setByName(obj.getString("制单人"));
            cktz.setpType(obj.getString("单据类型"));
            cktz.setState(obj.getString("单据状态"));
            cktz.setFhType(obj.getString("发货类型"));
            cktz.setGoodNo(obj.getString("型号"));
            cktz.setCounts(obj.getString("数量"));
            cktz.setInPrice(obj.getString("进价"));
            cktz.setOutPrice(obj.getString("售价"));
            cktz.setBasicPrice(obj.getString("成本"));
            cktz.setSellCounts(obj.getString("销售额"));
            cktz.setFactory(obj.getString("厂家"));
            cktz.setFengzhuang(obj.getString("封装"));
            cktz.setDescription(obj.getString("描述"));
            list.add(cktz);
        }
        return list;
    }

    public static List<ChuKuDanInfo> getCKDList(String json) throws JSONException {

        if (json == null) {
            return null;
        }
        List<ChuKuDanInfo> list = new ArrayList<>();
        JSONObject obj = new JSONObject(json);
        JSONArray jarray = obj.getJSONArray("表");
        for (int i = 0; i < jarray.length(); i++) {
            JSONObject temp = jarray.getJSONObject(i);
            ChuKuDanInfo ckdi = new ChuKuDanInfo();
            ckdi.setPid(temp.getString("PID"));
            ckdi.setRepository(temp.getString("仓库"));
            ckdi.setDeptName(temp.getString("部门"));
            ckdi.setDeptNo(temp.getString("部门号"));
            ckdi.setCkType(temp.getString("出库类型"));
            ckdi.setPdate(temp.getString("制单日期"));
            ckdi.setTotalBasicPrice(temp.getString("总成本"));
            ckdi.setTotalSellPrice(temp.getString("总销售额"));
            ckdi.setProfit(temp.getString("毛利"));
            ckdi.setUname(temp.getString("业务员"));
            ckdi.setCustomer(temp.getString("客户"));
            ckdi.setCname(temp.getString("客户名称"));
            ckdi.setHeyueDate(temp.getString("合约日期"));
            ckdi.setBillingType(temp.getString("开票类型"));
            ckdi.setBillingCompany(temp.getString("开票公司"));
            ckdi.setPartNo(temp.getString("型号"));
            ckdi.setCounts(temp.getString("数量"));
            ckdi.setInPrice(temp.getString("进价"));
            ckdi.setOutPrice(temp.getString("售价"));
            ckdi.setSellCounts(temp.getString("销售额"));
            ckdi.setFactory(temp.getString("厂家"));
            ckdi.setRemarks(temp.getString("备注"));
            list.add(ckdi);
        }

        return list;
    }
//    "PID": "1212185",
//            "仓库": "深圳赛格",
//            "部门": "北京采购部",
//            "部门号": "6102",
//            "出库类型": "内部销售",
//            "制单日期": "2016/12/1 10:11:35",
//            "总成本": "138.32",
//            "总销售额": "138.32",
//            "毛利": "0.00",
//            "业务员": "苏海玲",
//            "客户": "",
//            "客户名称": "",
//            "合约日期": "2016/11/30 16:00:56",
//            "开票类型": "增值税票",
//            "开票公司": "深圳市创新恒远供应链管理有限公司",
//            "型号": "PCF8563T/5,518",
//            "数量": "100",
//            "进价": "1.3832",
//            "售价": "1.3832",
//            "销售额": "138.32",
//            "厂家": "NXP",
//            "备注": ""

    public static List<CheckInfo> getCheckInfo(String json) throws JSONException {
        if (json == null) {
            return null;
        }
        List<CheckInfo> list = new ArrayList<>();
        JSONObject obj = new JSONObject(json);
        JSONArray jarray = obj.getJSONArray("表");
//        "PID": "1118648",
//                "制单日期": "2017.01.03",
//                "单据类型": "正常销售",
//                "单据状态": "已出库,完成",
//                "公司": "北方科讯分公司",
//                "部门": "北京NORTH",
//                "员工": "田珂",
//                "型号": "LM2575HVT-ADJ/NOPB",
//                "数量": "200",
//                "出库库房": "深圳赛格",
//                "客户": "Saint  Nation  Co.  Ltd",
//                "发货类型": "库房发货",
//                "开票公司": "61",
//                "预出库打印": ""
        for (int i = 0; i < jarray.length(); i++) {
            CheckInfo ci = new CheckInfo();
            JSONObject temp = jarray.getJSONObject(i);
            ci.setPid(temp.getString("PID"));
            ci.setPdate(temp.getString("制单日期"));
            ci.setPtype(temp.getString("单据类型"));
            ci.setPstate(temp.getString("单据状态"));
            ci.setCompany(temp.getString("公司"));
            ci.setDeptName(temp.getString("部门"));
            ci.setUname(temp.getString("员工"));
            ci.setPartNo(temp.getString("型号"));
            ci.setCounts(temp.getString("数量"));
            ci.setOutfrom(temp.getString("出库库房"));
            ci.setCustomer(temp.getString("客户"));
            ci.setFhType(temp.getString("发货类型"));
            ci.setKpCompany(temp.getString("开票公司"));
            ci.setPrePrint(temp.getString("预出库打印"));
            list.add(ci);
        }
        return list;
    }
}
