package kyeexpress;

import java.io.Serializable;

/**
 Created by 张建宇 on 2017/11/6. */

public class YundanJson implements Serializable {
    /**
     客户编码(必)
     */
    public String uuid = KyExpressUtils.uuid;
    /**
     客户密码(必)
     */
    public String key = KyExpressUtils.key;
    //服务方式（必）
    public String col_018 = "";
    /**
     寄件联系人（必）
     */
    public String col_001 = "col_001";
    /**
     寄件联系人（必）
     */
    public String col_004 = "col_004";
    /**
     寄件人手机（必）
     */
    public String col_005 = "col_005";
    /**
     寄件电话（必）
     */
    public String col_003 = "col_003";
    //寄件地址
    public String col_002 = "";

    /**
     寄件地区号(必)
     */
    public String col_011 = "col_011";
    //收件公司（必）
    public String col_006 = "col_006";
    //收件联系人（必）
    public String col_010 = "col_010";
    //收件地址（必）
    public String col_007 = "col_007";
    //    收件手机（必）
    public String col_009 = "col_009";
    //收件电话（必）
    public String col_008 = "col_008";
    public String sjTelFJH = "sjTelFJH";

    //件数(非)
    public String col_021 = "col_021";
    //托寄物（必）
    public String col_019 = "col_019";
    //    收件电话区号（非）
    public String sjTelQH = "sjTelQH";
    //寄件人区号（非）
    public String jjTelQH = "jjTelQH";
    //月结卡号
    public String col_037 = "col_037";
    //付款方式（必）,寄付，到付，寄付月结，到付月结，转第三方付款，预存运费（填文字）
    public String col_013 = "寄付";
    //代收金额（必，默认0）
    public String col_031 = "0";
    //是签回单(必)传1：签回单（面单上打印’签回单’）传0：不签回单（面单上面不用打印）
    public String col_028 = "col_028";
    //备注（非）
    public String col_034 = "col_034";
    //付款账号（非）
    public String payCardNo = "payCardNo";
    //保价值
    public String col_027 = "col_027";
    //保费（非）
    public String bfAmount = "bfAmount";
    //唯品会入库号，col_006为唯品会时必填（必）
    public String vipshopCode = "vipshopCode";


    public YundanJson(String uuid, String key, String col_001, String col_004, String col_005, String col_003, String col_011,
                      String col_006, String col_010, String col_007, String col_009, String col_008, String sjTelFJH, String
                              col_021, String col_019, String sjTelQH, String jjTelQH, String col_037, String col_013, String
                              col_031, String col_028, String col_034, String payCardNo, String col_027, String bfAmount,
                      String vipshopCode) {
        this.uuid = uuid;
        this.key = key;
        this.col_001 = col_001;
        this.col_004 = col_004;
        this.col_005 = col_005;
        this.col_003 = col_003;
        this.col_011 = col_011;
        this.col_006 = col_006;
        this.col_010 = col_010;
        this.col_007 = col_007;
        this.col_009 = col_009;
        this.col_008 = col_008;
        this.sjTelFJH = sjTelFJH;
        this.col_021 = col_021;
        this.col_019 = col_019;
        this.sjTelQH = sjTelQH;
        this.jjTelQH = jjTelQH;
        this.col_037 = col_037;
        this.col_013 = col_013;
        this.col_031 = col_031;
        this.col_028 = col_028;
        this.col_034 = col_034;
        this.payCardNo = payCardNo;
        this.col_027 = col_027;
        this.bfAmount = bfAmount;
        this.vipshopCode = vipshopCode;
    }

    public YundanJson() {
    }
}
