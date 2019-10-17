package com.b1b.js.erpandroid_kf.entity;

import java.util.List;

/**
 * Created by 张建宇 on 2019/7/30.
 */
public class ChukuInfo {
    public List<ChukuDetail> details;
    public String StateNow;
    public String PID;
    public String makeName;
    public String pidStat;
    public String ckStorName;
    public String fhKuqu;
    public int isDiaobo;

    public String makePidTime;
    public String kpType;
    public String kpCompany;
    public String fhType;
    public String comp;
    public String partName;
    public String notes;
    public String preChukuPrint;
    public String chukuResult;
    public String yundanID;
    public String flag;

    @Override
    public String toString() {
        return
                "PID='" + PID + '\'' + "\t" + "单据状态='" + pidStat + '\'' + "\n" +
                        "制单人='" + makeName + '\'' + "\n" +
                        "出库库房='" + ckStorName + '\'' + "\n" +
                        "制单日期='" + makePidTime + '\'' + "\n" +
                        "增值税票='" + kpType + '\'' + "\n" +
                        "开票公司='" + kpCompany + '\'' + "\n" +
                        "发货类型='" + fhType + '\'' + "\n" +
                        "公司='" + comp + '\'' + "\n" +
                        "部门='" + partName + '\'' + "\n" +
                        "运单号='" + yundanID + '\'' + "\n" +
                        "备注='" + notes + '\'' + "\n" +
                        "预出库打印='" + preChukuPrint + '\'';
    }
    //    StateNow: "0",
//    PID: 1391430,
//    制单人: "韩亚萌",
//    单据类型: "正常销售",
//    出库库房: "深圳赛格",
//    制单日期: "2019-07-30T10:43:55.19",
//    开票类型: "增值税票",
//    开票公司: "上海比亿电子技术有限公司",
//    发货类型: "客户自取",
//    公司: "远大创新分公司",
//    部门: "廊坊-代购部",
//    员工: "韩亚萌",
//    备注: "原厂包装 不换标签 带标签 客户自取 带中转库",
//    单据状态: "已出库,完成",
//    预出库打印: "I",
//    出库结果: "已经扣税!付款凭证ID为:786374 成功出库!生成的出库单PID为:1505706 库房：商庆房(2306)2019-7-30 14:17:35 一次复核:黎璐[3954] 2019/7/30 11:11:40 预出库:赵兴[3977]已经预出库 2019/7/30 10:54:41",
}
