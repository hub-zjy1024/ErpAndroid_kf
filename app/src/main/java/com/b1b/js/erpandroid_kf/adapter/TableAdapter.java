package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.KucunFBInfo;
import com.b1b.js.erpandroid_kf.myview.WarpLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 Created by 张建宇 on 2017/5/17. */

public class TableAdapter extends BaseAdapter {
    private Context context;
    private List<KucunFBInfo> data;
    private static int dur = 2;
    private int[] len = new int[]{0, 303, 419, 299, 261, 356, 356, 405, 405, 303, 601, 359, 269, 611};

    public TableAdapter(Context context, List<KucunFBInfo> data) {
        this.context = context;
        this.data = data;
        for (int i = 0; i < len.length; i++) {
            len[i] = 0;
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public Object getItem(int position) {

        return data.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        KucunFBInfo fbInfo = data.get(position);
        ArrayList<String> lists = new ArrayList<>();
        lists.add("型号:" + fbInfo.getPartNo());
        lists.add("美金:" + (fbInfo.getDollar().equals("") ? "无" : fbInfo.getDollar()));
        lists.add("合计:" + fbInfo.getTotalInMoney());
        lists.add("封装:" + fbInfo.getFengzhuang());
        lists.add("批号:" + fbInfo.getPihao());
        lists.add("厂家:" + fbInfo.getFactory());
        lists.add("剩余:" + fbInfo.getLeftCount());
        lists.add("发布价格:" + fbInfo.getFabuPrice());
        lists.add("单价:" + fbInfo.getInPrice());
        lists.add("限价:" + fbInfo.getLimitedPrice());
        lists.add("入库:" + fbInfo.getRukuDate());
        lists.add("描述:" + fbInfo.getDescription());
        lists.add("备货部门:" + fbInfo.getIsBeihuo());
        lists.add("备注:" + fbInfo.getNotes());
        if (convertView == null) {
            TableCell[] cells = new TableCell[lists.size()];
            for (int i = 0; i < cells.length; i++) {
                if (i == 0) {
                    int widths = context.getResources().getDisplayMetrics().widthPixels;
                    cells[i] = new TableCell(lists.get(i), widths, LinearLayout.LayoutParams
                            .WRAP_CONTENT, TableCell.STRING);
                } else {
                    cells[i] = new TableCell(lists.get(i), len[i] + dur, 68, TableCell.STRING);
                }
            }
            TableRow tableRow = new TableRow(cells);
            convertView = new WarpLinearLayout(context, tableRow, 0, 17);
        }
        ViewGroup viewGroup = (ViewGroup) convertView;
        measureMax(lists, viewGroup, len);
        TextView tv = (TextView) ((ViewGroup) convertView).getChildAt(0);
        tv.measure(0, 0);
        convertView.setLayoutParams(new ViewGroup.LayoutParams(len[0] + len[1] + len[2] + len[3] + len[4] + len[5] + len[6] +
                len[7] + dur * 6, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (fbInfo.isFabu()) {
            tv.setTextColor(context.getResources().getColor(R.color.kucunfb_isfabu));
        } else {
            tv.setTextColor(Color.BLACK);
        }
        if (position % 2 == 0) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.lv_bg));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    private void measureMax(ArrayList<String> lists, ViewGroup viewGroup, int[] len) {
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
//            Log.e("zjy", "TableAdapter->getView(): len==" + i + ":" + len[i]);
            if (i != 0) {
                tv.setLayoutParams(new ViewGroup.LayoutParams(len[i] + 2, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    /**
     TableRowView 实现表格行的样式
     @author hellogv
     */

    /**
     TableRow 实现表格的行
     @author hellogv
     */
    static public class TableRow {
        private TableCell[] cell;

        public TableRow(TableCell[] cell) {
            this.cell = cell;
        }

        public int getSize() {
            return cell.length;
        }

        public TableCell getCellValue(int index) {
            if (index >= cell.length)
                return null;
            return cell[index];
        }
    }

    /**
     TableCell 实现表格的格单元
     @author hellogv
     */
    static public class TableCell {
        public static final int STRING = 0;
        public static final int IMAGE = 1;
        public Object value;
        public int width;
        public int height;
        private int type;

        public TableCell(Object value, int width, int height, int type) {
            this.value = value;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }
}

