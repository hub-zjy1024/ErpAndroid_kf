package com.b1b.js.erpandroid_kf.yundan.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by 张建宇 on 2017/6/29.
 */

public class Code128C {
    /**
     * 生成Code128C格式条形码（纯数字，偶数位长度）
     *
     * @param content          条码内容（偶数位纯数字内容）
     * @param widthScale(1-10,默认2，)
     * @param height           条码高度(40-200,默认100)
     * @return
     */
    public static Bitmap create128CBitmap(String content, int widthScale, int height) {
        if (content == null || content.length() == 0) {
            return null;
        }
        if (height < 40 || height > 200) {
            height = 100;
        }
        if ((content.length() % 2) == 1) {
            return null;
        }
        for (int i = 0; i < content.length(); i++) {
            char charFlag = content.charAt(i);
            if (charFlag > 57 || charFlag < 48) {
                return null;
            }
        }
        String text = CodeSymbol128C.SYMBOL_START_C.getSymbolValue(); // 获取开始位
        int examine = 105;
        int count = 0;
        while (content.length() != 0) {
            int tempValue = Integer.valueOf(content.subSequence(0, 2).toString());
            text += CodeSymbol128C.getTargetSymbolValue(tempValue);
            examine += tempValue * ++count;
            content = content.substring(2);
        }
        examine = examine % 103; // 获得严效位
        text += CodeSymbol128C.getTargetSymbolValue(examine); // 获取严效位
        text += CodeSymbol128C.SYMBOL_STOP.getSymbolValue(); // 结束位

        return getImageFrom128CSymbols(text, widthScale, height);

    }

    private static Bitmap getImageFrom128CSymbols(String symbolsStr, int barSizeScale,
                                                  int bitmapHeight) {
        if (barSizeScale < 1 || barSizeScale > 8) {
            barSizeScale = 2;
        }
        byte[] symbols = symbolsStr.getBytes();

        int width = 0;
        for (int i = 0; i < symbols.length; i++) {
            width += (symbols[i] - 48) * barSizeScale;
        }
        Bitmap barCodeBitmap = Bitmap.createBitmap(width, bitmapHeight, Bitmap.Config
                .ARGB_8888);
        int offset = 0;
        for (int i = 0; i < symbols.length; i++) {
            int barElementWidth = (symbols[i] - 48) * barSizeScale; // 获取元素宽度
            if (!((i & 1) == 0)) { // 偶数位
                for (int j = 0; j < barElementWidth; j++) {
                    // 图元列循环
                    for (int k = 0; k < bitmapHeight; k++) {
                        // 图元列像素循环
                        barCodeBitmap.setPixel((offset + j), k, Color.WHITE);
                    }
                }
            } else {
                // 奇数位
                for (int j = 0; j < barElementWidth; j++) {
                    // 图元列循环
                    for (int k = 0; k < bitmapHeight; k++) {
                        // 图元列像素循环
                        barCodeBitmap.setPixel((offset + j), k, Color.BLACK);
                    }
                }
            }
            // _Garphics.(_Pen, new Point(_LenEx, 0), new Point(_LenEx,
            // m_Height));
            offset += barElementWidth;
        }
        return barCodeBitmap;
    }

    private enum CodeSymbol128C {
        SYMBOL_00(0, "212222"),
        SYMBOL_01(1, "222122"),
        SYMBOL_02(2, "222221"),
        SYMBOL_03(3, "121223"),
        SYMBOL_04(4, "121322"),
        SYMBOL_05(5, "131222"),
        SYMBOL_06(6, "122213"),
        SYMBOL_07(7, "122312"),
        SYMBOL_08(8, "132212"),
        SYMBOL_09(9, "221213"),
        SYMBOL_10(10, "221312"),
        SYMBOL_11(11, "231212"),
        SYMBOL_12(12, "112232"),
        SYMBOL_13(13, "122132"),
        SYMBOL_14(14, "122231"),
        SYMBOL_15(15, "113222"),
        SYMBOL_16(16, "123122"),
        SYMBOL_17(17, "123221"),
        SYMBOL_18(18, "223211"),
        SYMBOL_19(19, "221132"),
        SYMBOL_20(20, "221231"),
        SYMBOL_21(21, "213212"),
        SYMBOL_22(22, "223112"),
        SYMBOL_23(23, "312131"),
        SYMBOL_24(24, "311222"),
        SYMBOL_25(25, "321122"),
        SYMBOL_26(26, "321221"),
        SYMBOL_27(27, "312212"),
        SYMBOL_28(28, "322112"),
        SYMBOL_29(29, "322211"),
        SYMBOL_30(30, "212123"),
        SYMBOL_31(31, "212321"),
        SYMBOL_32(32, "232121"),
        SYMBOL_33(33, "111323"),
        SYMBOL_34(34, "131123"),
        SYMBOL_35(35, "131321"),
        SYMBOL_36(36, "112313"),
        SYMBOL_37(37, "132113"),
        SYMBOL_38(38, "132311"),
        SYMBOL_39(39, "211313"),
        SYMBOL_40(40, "231113"),
        SYMBOL_41(41, "231311"),
        SYMBOL_42(42, "112133"),
        SYMBOL_43(43, "112331"),
        SYMBOL_44(44, "132131"),
        SYMBOL_45(45, "113123"),
        SYMBOL_46(46, "113321"),
        SYMBOL_47(47, "133121"),
        SYMBOL_48(48, "313121"),
        SYMBOL_49(49, "211331"),
        SYMBOL_50(50, "231131"),
        SYMBOL_51(51, "213113"),
        SYMBOL_52(52, "213311"),
        SYMBOL_53(53, "213131"),
        SYMBOL_54(54, "311123"),
        SYMBOL_55(55, "311321"),
        SYMBOL_56(56, "331121"),
        SYMBOL_57(57, "312113"),
        SYMBOL_58(58, "312311"),
        SYMBOL_59(59, "332111"),
        SYMBOL_60(60, "314111"),
        SYMBOL_61(61, "221411"),
        SYMBOL_62(62, "431111"),
        SYMBOL_63(63, "111224"),
        SYMBOL_64(64, "111422"),
        SYMBOL_65(65, "121124"),
        SYMBOL_66(66, "121421"),
        SYMBOL_67(67, "141122"),
        SYMBOL_68(68, "141221"),
        SYMBOL_69(69, "112214"),
        SYMBOL_70(70, "112412"),
        SYMBOL_71(71, "122114"),
        SYMBOL_72(72, "122411"),
        SYMBOL_73(73, "142112"),
        SYMBOL_74(74, "142211"),
        SYMBOL_75(75, "241211"),
        SYMBOL_76(76, "221114"),
        SYMBOL_77(77, "413111"),
        SYMBOL_78(78, "241112"),
        SYMBOL_79(79, "134111"),
        SYMBOL_80(80, "111242"),
        SYMBOL_81(81, "121142"),
        SYMBOL_82(82, "121241"),
        SYMBOL_83(83, "114212"),
        SYMBOL_84(84, "124112"),
        SYMBOL_85(85, "124211"),
        SYMBOL_86(86, "411212"),
        SYMBOL_87(87, "421112"),
        SYMBOL_88(88, "421211"),
        SYMBOL_89(89, "212141"),
        SYMBOL_90(90, "214121"),
        SYMBOL_91(91, "412121"),
        SYMBOL_92(92, "111143"),
        SYMBOL_93(93, "111341"),
        SYMBOL_94(94, "131141"),
        SYMBOL_95(95, "114113"),
        SYMBOL_96(96, "114311"),
        SYMBOL_97(97, "411113"),
        SYMBOL_98(98, "411311"),
        SYMBOL_99(99, "113141"),
        // 以下为标志位
        SYMBOL_START_A(103, "211412"),
        SYMBOL_START_B(104, "211214"),
        SYMBOL_START_C(105, "211232"),
        SYMBOL_STOP(106, "2331112");

        private int codeValue;
        private String symbolValue;

        private CodeSymbol128C(int codeValue, String symbolValue) {
            this.codeValue = codeValue;
            this.symbolValue = symbolValue;
        }

        public String getSymbolValue() {
            return this.symbolValue;
        }

        public static String getTargetSymbolValue(int codeValue) {
            return CodeSymbol128C.valueOf("SYMBOL_" + (codeValue < 10 ? "0" : "") +
                    codeValue).getSymbolValue();
        }

    }
}
