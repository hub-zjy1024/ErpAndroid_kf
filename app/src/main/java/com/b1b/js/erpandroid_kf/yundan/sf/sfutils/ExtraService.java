package com.b1b.js.erpandroid_kf.yundan.sf.sfutils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 张建宇 on 2017/6/23.
 * 顺丰增值服务
 */

public class ExtraService {
    /*1.保价-INSURE
 value为声明价值以原寄地所在区域币种为准，如中国大陆为人民币，香港为港币，保留3位小数。
/*
   2.签收短信通知-MSG
   value为接受短信的号码
*/
    /*3.电子签收-Esign
    value 为图片类型：
     1、身份证，2、军官证，3、护照，4、其他
     Value1 为照片张数*/
 /*   4.拍照验证-Psign 同3
 * */
    public static ArrayList<String> service = new ArrayList<>();

    static {
        service.add("保价");
        service.add("签收短信通知");
        service.add("电子签收");
        service.add("拍照验证");
    }

    public static HashMap<Integer, String> valueType = new HashMap<>();

    public String name;
    public String value;
    public String value1;

    public static ExtraService addBaojia(double price) {
        ExtraService service = new ExtraService();
        service.name = "INSURE";
        service.value = String.valueOf(price);
        return service;
    }

    public static ExtraService addDuanxin(String phone) {
        ExtraService service = new ExtraService();
        service.name = "MSG";
        service.value = phone;
        return service;
    }

    /**
     * @param type   1、身份证，2、军官证，3、护照，4、其他
     * @param counts 为照片张数
     * @return
     */
    public static ExtraService addEsign(int type, int counts) {
        ExtraService service = new ExtraService();
        service.name = "Esign";
        service.value = "2";
        service.value1 = String.valueOf(counts);
        return service;
    }

    /**
     * @param type   1、身份证，2、军官证，3、护照，4、其他
     * @param counts 为照片张数
     * @return
     */
    public static ExtraService addPsign(int type, int counts) {

        ExtraService service = new ExtraService();
        service.name = "Psign";
        service.value =  "4";
        service.value1 = String.valueOf(counts);
        return service;
    }
}
