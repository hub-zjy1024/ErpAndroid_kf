package com.b1b.js.erpandroid_kf.yundan.kyeexpress;

import java.util.Comparator;

/**
 Created by 张建宇 on 2017/11/6. */

public class CompareInterface implements Comparator<String> {


    @Override
    public int compare(String lhs, String rhs) {
        char[] char1 = lhs.toCharArray();
        char[] char2 = rhs.toCharArray();
        int len = char1.length > char2.length ? char2.length : char1.length;
        for (int i = 0; i < len; i++) {
            int c1 = char1[i];
            int c2 = char2[i];
            if (c1 > c2) {
                return 1;
            } else if (c1 == c2) {
            } else {
                return -1;
            }
        }
        return 0;
    }
}
