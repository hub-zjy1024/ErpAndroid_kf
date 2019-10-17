package com.b1b.js.erpandroid_kf.entity;

/**
 * Created by 张建宇 on 2019/7/30.
 */
public class ChukuDetail extends BaseFileds {
    //    型号: 200,
    //    数量: 200,
    //    厂家: "Molex",
    //    描述: "D201907260000000207",
    //    封装: "Aa",
    //    明细备注: ""
    private int counts;
    private String dNotes;

    public String getdNotes() {
        return dNotes;
    }

    public void setdNotes(String dNotes) {
        this.dNotes = dNotes;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    @Override
    public String toString() {
        return super.toString() +
                "数量='" + counts + '\'' + "\n" +
                "明细备注='" + dNotes + '\'' + "\n"
                ;
    }
}
