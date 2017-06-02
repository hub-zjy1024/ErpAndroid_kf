package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PreChukuInfo;
import com.b1b.js.erpandroid_kf.myview.WarpLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 Created by 张建宇 on 2017/6/1. */

public class PreChukuAdapter2 extends MyBaseAdapter<PreChukuInfo> {
    public PreChukuAdapter2(List<PreChukuInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {

    }

    @Override
    protected void initHolder(PreChukuInfo currentData, MyBasedHolder baseHolder) {

    }

    @Override
    protected MyBasedHolder getHolder() {
        return null;
    }

    public static int dur = 2;
    private int len[] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PreChukuInfo preChukuInfo = data.get(position);
        ArrayList<String> lists = new ArrayList<>();
        lists.add("型号:" + preChukuInfo.getPartNo());
        lists.add("单据号:" + preChukuInfo.getPid());
        lists.add("出库类型:" + preChukuInfo.getChukuType());
        lists.add("委托人:" + preChukuInfo.getWeituo());
        lists.add("仓库:" + preChukuInfo.getStorageID());
        lists.add("发货库区:" + preChukuInfo.getFahuoPart());
        lists.add("调入仓库:" + preChukuInfo.getDiaoruKf());
        lists.add("数量:" + preChukuInfo.getCouts());
        lists.add("打印次数:" + preChukuInfo.getPrintCounts());
        lists.add("批号:" + preChukuInfo.getPihao());
        lists.add("厂家:" + preChukuInfo.getFactory());
        lists.add("库区:" + preChukuInfo.getKuqu());
        lists.add("委托公司:" + preChukuInfo.getWeituoCompanyID());
        lists.add("位置:" + preChukuInfo.getPlacedID());
        lists.add("制单日期:" + preChukuInfo.getCreateDate());
        if (convertView == null) {
            TableAdapter.TableCell[] cells = new TableAdapter.TableCell[lists.size()];
            for (int i = 0; i < cells.length; i++) {
                if (i == 0) {
                    int widths = mContext.getResources().getDisplayMetrics().widthPixels;
                    cells[i] = new TableAdapter.TableCell(lists.get(i), LinearLayout.LayoutParams
                            .MATCH_PARENT, LinearLayout.LayoutParams
                            .WRAP_CONTENT, TableAdapter.TableCell.STRING);
                } else {
                    cells[i] = new TableAdapter.TableCell(lists.get(i), len[i] + dur, 68, TableAdapter.TableCell.STRING);
                }
            }
            TableAdapter.TableRow tableRow = new TableAdapter.TableRow(cells);
            convertView = new WarpLinearLayout(mContext, tableRow, 0, 17);
        }

        convertView.setPadding(20, 2, 20, 2);
        ViewGroup viewGroup = (ViewGroup) convertView;
        measureMax(lists, viewGroup, len, dur);
        TextView tv = (TextView) ((ViewGroup) convertView).getChildAt(0);
        tv.measure(0, 0);
        tv.setTextColor(mContext.getResources().getColor(R.color.color_black));
        if (position % 2 == 0) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lv_bg));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;

    }

    private void measureMax(ArrayList<String> lists, ViewGroup viewGroup, int[] len, int dur) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            TextView tv = (TextView) viewGroup.getChildAt(i);
            tv.setText(lists.get(i));
            tv.measure(0, 0);
            if (len[i] < tv.getMeasuredWidth()) {
                len[i] = tv.getMeasuredWidth();
            }
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            TextView tv = (TextView) viewGroup.getChildAt(i);
            if (i != 0) {
                tv.setLayoutParams(new ViewGroup.LayoutParams(len[i] + dur, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }
}
