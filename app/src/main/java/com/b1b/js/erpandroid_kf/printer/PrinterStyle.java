package com.b1b.js.erpandroid_kf.printer;

import android.content.Context;
import android.util.Log;

import com.b1b.js.erpandroid_kf.entity.PreChukuDetailInfo;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.printer.entity.XiaopiaoInfo;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import utils.btprint.MyBluePrinter;
import utils.btprint.MyPrinter;
import utils.btprint.SPrinter;

/**
 Created by 张建宇 on 2017/5/2. */

public class PrinterStyle {
    public static boolean printPreparedChuKu(MyPrinter printer, PreChukuInfo info, String localKuqu) throws IOException {
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
        //        com.b1b.js.erpandroid_kf.printer.printTextLn(info.getSalesman() + "-" + info.getEmployeeID() + "-" + pid.substring(0, 3)+ "\t[VIP]");
        printer.printTextLn(info.getSalesman() + "-" + info.getEmployeeID() + "-" + pid.substring(0, 3) + "\t" + (info.getIsVip
                ().equals("1") ? "[VIP]" : ""));
        printer.setFont(0);
        printer.printTextLn("DeptID:" + getStringAtLength(info.getDeptID(), 8, 7) + "\t" + "Client:" + info.getClient());
        printer.printTextLn("PactID:" + getStringAtLength(info.getPactID(), 8, 7) + "\t" + "oType:" + info.getOutType());
        List<PreChukuDetailInfo> detailInfos = info.getDetailInfos();
        if (detailInfos != null) {
            for (int i = 0; i < detailInfos.size(); i++) {
                PreChukuDetailInfo dInfo = detailInfos.get(i);
                //一行47个字符
                //                com.b1b.js.erpandroid_kf.printer.printTextLn((i + 1) + ".-----------------------------------------");
                // TODO: 2017/7/24 修改打印格式
                String date = dInfo.getInitialDate();
                Date compareDate = new Date(117, 6, 1);
                Log.e("zjy", "PrinterStyle->printPreparedChuKu(): compareDate==" + compareDate.toString());
                boolean isShow = false;
                if (!date.equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date pidDate = null;
                    try {
                        pidDate = sdf.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (pidDate.compareTo(compareDate) < 0) {
                        isShow = true;
                    }
                }
                //                1160454
                if (isShow) {
                    printer.printTextLn((i + 1) + ".---供应商等级:[" + dInfo.getProLevel() + "]---需检测入库时间:" + dInfo.getInitialDate());
                } else {
                    printer.printTextLn((i + 1) + ".---供应商等级:[" + dInfo.getProLevel() + "]-------------------------");
                }
                printer.printTextLn("明细ID:" + getStringAtLength(dInfo.getDetailID(), 8, 7) + "M");
                printer.printTextLn("@@型号:" + getStringAtLength(dInfo.getPartNo(), 20, 0));
                String fz = getStringAtLength(dInfo.getFengzhuang(), 10, 5);
                String ph = getStringAtLength(dInfo.getPihao(), 10, 5);
                String fc = getStringAtLength(dInfo.getFactory(), 10, 5);
                String ms = getStringAtLength(dInfo.getDescription(), 10, 5);
                String place = getStringAtLength(dInfo.getPlace(), 13, 2);
                String bz = getStringAtLength(dInfo.getNotes(), 4, 5);
                String counts = getStringAtLength(dInfo.getCounts(), 10, 5);
                String leftCounts = dInfo.getLeftCounts();
                printer.printTextLn("封装:" + fz + "\t" + "批号:" + ph + "\t" + "厂家:" + fc);
                printer.printTextLn("描述:" + ms + "\t" + "P:" + place + "\t" + "备注:" + bz);
                printer.printTextLn("数量:" + counts + " \t" + "剩余数量:" + leftCounts);
            }
        }
        printer.newLine();
        printer.printTextLn("主备注：" + info.getMainNotes());
        if (!info.getKuqu().equals(localKuqu)) {
            String name = "";
            if (info.getKuqu().equals("0")) {
                name = "次库区";
            } else if (info.getKuqu().equals("1")) {
                name = "主库区";
            }
            printer.printTextLn("本单以上型号，由发" + info.getFahuoPart() + "发货调拨至" + name + ",请认真核实，如果不能发货，请及时归还原库区。");
        } else {
            printer.printTextLn("鉴于工作需要，本人向公司做出如下承诺：");
            printer.printTextLn("1、因业务需要，本人自愿自行取货。");
            printer.printTextLn("2、所取的上述货物由本人负责在30日内收回销货款并上交公司。否则，由本人承担全部的经济责任。");
            printer.printTextLn("3、本签收单由本人签字后即产生法律效力，作为本人欠款的依据。");
            printer.printTextLn("4、本签收单的原件、复印件和传真件具有同等的法律效力。");
        }
        printer.newLine();
        printer.printTextLn("出库员：");
        printer.printTextLn("一次复核：");
        printer.printTextLn("二次复核：");
        printer.printTextLn("承诺人/代理人");
        printer.printTextLn("(请用正楷签收)：");
        return true;
    }

    private static String getStringAtLength(String src, int maxLength, int titleLength) {
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

    public static void printXiaopiao(Context mContext, MyBluePrinter printer) {
        String msg = "12344567979";
        printer.printBarCode(mContext, msg, 50, true);
        printer.printTextLn(msg);
        printer.printTextLn(msg);
    }

    public synchronized static void printXiaopiao2(Context mContext, MyBluePrinter printer, XiaopiaoInfo info) {
        int len[] = new int[]{15, 0};
        printer.printText("\t" + info.getDeptNo() + "_" + info.getTime() + "\t" + info.getStorageCode());
        printer.newLine();
        printer.printText("型号:" + info.getPartNo());
        printer.newLine();
        String[] str = new String[]{"数量:" + info.getCounts(), "产地:" + info.getProduceFrom()};
        printer.printTextByLength(str, len);
        printer.newLine();
        str = new String[]{"厂家:" + info.getFactory(), "批号:" + info.getPihao()};
        printer.printTextByLength(str, len);
        printer.newLine();
        str = new String[]{"封装:" + info.getFengzhuang(), "描述:" + info.getDescription()};
        printer.printTextByLength(str, len);
        printer.newLine();
//        str = new String[]{"位置:" + info.getPlace(), "备注:" + info.getNote()};
        str = new String[]{"位置:" + info.getPlace(), "备注:"};
        printer.printTextByLength(str, len);
        printer.newLine();
        printer.setZiTiSize(0);
        if (info.getFlag().equals("1")) {
            printer.printText("z" + info.getCompany() + "z");
        } else if (info.getFlag().equals("2")) {
            printer.printText("p" + info.getCompany() + "p");
        }
        printer.newLine();
        printer.newLine();
        printer.setZiTiSize(1);
        printer.printBarCodeWithDifferentBelow(mContext, info.getCodeStr(), 50, info.getCodeStr() + "M");
    }

    public synchronized static void printXiaopiao2Test(SPrinter printer, XiaopiaoInfo info) {
        String msg = "这是测试," + new Date().toLocaleString();
        printer.printText(msg);
        String msg2 = "write:" + msg;
        printer.write(msg.getBytes());

    }
    public synchronized static void printXiaopiao2(SPrinter printer, XiaopiaoInfo info) {
        int len[] = new int[]{15, 0};
        printer.initPrinter();
//        printer.newLine();
        printer.printText("\t" + info.getDeptNo() + "_" + info.getTime() + "\t" + info.getStorageCode());
        printer.newLine();
        printer.printText("型号:" + info.getPartNo());
        printer.newLine();
        String[] str = new String[]{"数量:" + info.getCounts(), "产地:" + info.getProduceFrom()};
        printer.printTextByLength(str, len);
        printer.newLine();
        str = new String[]{"厂家:" + info.getFactory(), "批号:" + info.getPihao()};
        printer.printTextByLength(str, len);
        printer.newLine();
        str = new String[]{"封装:" + info.getFengzhuang(), "描述:" + getStringAt(info.getDescription(),8)};
        printer.printTextByLength(str, len);
        printer.newLine();
                str = new String[]{"位置:" + info.getPlace(), "备注:" + info.getNote()};
//        str = new String[]{"位置:" + info.getPlace(), "备注:"};
        printer.printTextByLength(str, len);
        printer.newLine();
        printer.setZiTiSize(0);
        if (info.getFlag().equals("1")) {
            printer.printText("z" + info.getCompany() + "z");
        } else if (info.getFlag().equals("2")) {
            printer.printText("p" + info.getCompany() + "p");
        }else{
            printer.printText("z" + info.getCompany() + "z");
        }
        printer.newLine();
        printer.setZiTiSize(1);
        printer.printBarCode(info.getCodeStr(), 0, 1, 43);
        printer.printText("M" + info.getCodeStr());
        printer.newLine();
        printer.newLine();
        printer.newLine();
        printer.commit();
    }

    public static String getStringAt(String src, int maxLength) {
        int tempLength = 0;
        int index = 0;
        for (int i = 0; i < src.length(); i++) {
            int tmp = (int) src.charAt(i);
            if (tmp > 0 && tmp < 127) {
                tempLength += 1;
            }else{
                tempLength += 2;
            }
            if (tempLength >= maxLength) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            return src;
        }
        return src.substring(0, index);
    }
}