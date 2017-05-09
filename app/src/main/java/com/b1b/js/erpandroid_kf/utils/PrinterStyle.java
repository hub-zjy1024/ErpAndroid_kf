package com.b1b.js.erpandroid_kf.utils;

import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;

import java.io.IOException;
import java.util.List;

/**
 Created by 张建宇 on 2017/5/2. */

public class PrinterStyle {
    public static boolean printPreparedChuKu(MyPrinter printer, PreChukuInfo info) throws IOException {
        String pid = info.getPid();
        if (info.isXiankuan()) {
            printer.setFont(3);
            printer.printTextLn("现货现结");
            printer.newLine();
            printer.setFont(0);
        }
        printer.printCode(pid, MyPrinter.BARCODE_FLAG_NONE);
        printer.newLine();
        printer.setFont(1);
        printer.printText("出库通知单-" + pid.substring(3) + "\t");
        printer.setCharHeight(2);
        printer.printTextLn(info.getOutType());
        printer.setFont(1);
        printer.printTextLn(info.getSalesman() + "-" + info.getEmployeeID() + "-" + pid.substring(0, 3));
        printer.setFont(0);
        printer.printTextLn("DeptID:" + info.getDeptID() + "\t" + "Client:" + info.getClient());
        printer.printTextLn("PactID:" + info.getPactID() + "\t" + "oType:" + info.getOutType());
        List<PreChukuDetailInfo> detailInfos = info.getDetailInfos();
        if (detailInfos != null) {
            for (int i = 0; i < detailInfos.size(); i++) {
                PreChukuDetailInfo dInfo = detailInfos.get(i);
                //一行47个字符
                printer.printTextLn((i + 1) + ".-----------------------------------------");
                printer.printTextLn("@@型号:" + getStringAtLength(dInfo.getPartNo(), 20,0));
                String fz = getStringAtLength(dInfo.getFengzhuang(), 10,5);
                String ph = getStringAtLength(dInfo.getPihao(), 10,5);
                String fc =  getStringAtLength(dInfo.getFactory(), 10,5);
                String ms = getStringAtLength(dInfo.getDescription(), 10,5);
                String place = getStringAtLength(dInfo.getP(), 13,2);
                String bz = getStringAtLength(dInfo.getNotes(), 10,5);
                String counts = getStringAtLength(dInfo.getCounts(), 10,5);
                String leftCounts = dInfo.getLeftCounts();
                printer.printTextLn("封装:" + fz + "\t" + "批号:" + ph + "\t" + "厂家:"+fc);
                printer.printTextLn("描述:" + ms + "\t" + "P:" + place + "\t" + "备注:" + bz);
                printer.printTextLn("数量:" + counts + " \t" + "剩余数量:" + leftCounts);
            }
        }
        printer.newLine();
        printer.printTextLn("主备注：" + info.getMainNotes());
        printer.printTextLn("鉴于工作需要，本人向公司做出如下承诺：");
        printer.printTextLn("1、因业务需要，本人自愿自行取货。");
        printer.printTextLn("2、所取的上述货物由本人负责在30日内收回销货款并上交公司。否则，由本人承担全部的经济责任。");
        printer.printTextLn("3、本签收单由本人签字后即产生法律效力，作为本人欠款的依据。");
        printer.printTextLn("4、本签收单的原件、复印件和传真件具有同等的法律效力。");
        printer.newLine();
        printer.printTextLn("出库员：");
        printer.printTextLn("一次复核：");
        printer.printTextLn("二次复核：");
        printer.printTextLn("承诺人/代理人");
        printer.printTextLn("(请用正楷签收)：");
        return true;
    }

    public static String getStringAtLength(String src, int maxLength, int titleLength) {
        String newString = "";
        if (src == null) {
            for (int i = 0; i < 8 - titleLength; i++) {
                newString = newString + " ";
            }
            return newString;
        }
        if (src.length() > maxLength) {
            newString = src.substring(0, maxLength);
        } else {
            newString = src;
            int srcLenth = src.length();
            int p = 8 - srcLenth - titleLength;
            if (p > 0) {
                for (int i = 0; i < p; i++) {
                    newString = newString + " ";
                }
            }
        }
        return newString;
    }
}
