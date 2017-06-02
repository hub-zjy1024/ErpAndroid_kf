package com.b1b.js.erpandroid_kf.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.adapter.TableAdapter;

/**
 Created by 张建宇 on 2017/5/18. */

public class RowView extends LinearLayout {
    private TableAdapter.TableRow tableRow;
    private int textColor = 0;
    private int textSize = 15;
    public RowView(Context context) {
        super(context);
    }

    public RowView(Context context, TableAdapter.TableRow tableRow, int textColor, int textSize) {
        super(context);
        this.tableRow = tableRow;
        this.textColor = textColor;
        this.textSize = textSize;
        if (tableRow!= null) {
            for (int i = 0; i < tableRow.getSize(); i++) {//逐个格单元添加到行
                TableAdapter.TableCell tableCell = tableRow.getCellValue(i);
                if (tableCell != null) {
                    TextView textCell = new TextView(context);
                    textCell.setText(tableCell.value.toString());
                    if (this.textColor != 0) {
                        textCell.setTextColor(this.textColor);
                    }
                    textCell.setTextSize(this.textSize);
                    addView(textCell);
                    LinearLayout.LayoutParams layoutParams = (LayoutParams) textCell.getLayoutParams();
                    layoutParams.setMargins(0, 0, 1, 1);//预留空隙制造边框
                    layoutParams.width = tableCell.width;
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    textCell.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RowView(Context context, TableAdapter.TableRow tableRow) {
        super(context);
        this.tableRow = tableRow;
        this.setOrientation(LinearLayout.HORIZONTAL);
        if (tableRow!= null) {
            for (int i = 0; i < tableRow.getSize(); i++) {//逐个格单元添加到行
                TableAdapter.TableCell tableCell = tableRow.getCellValue(i);
                TextView textCell = new TextView(getContext());
                textCell.setText(tableCell.value.toString());
                if (textColor != 0) {
                    textCell.setTextColor(textColor);
                }
                textCell.setTextSize(textSize);
                addView(textCell);
                LinearLayout.LayoutParams layoutParams = (LayoutParams) textCell.getLayoutParams();
                layoutParams.setMargins(0, 0, 1, 1);//预留空隙制造边框
                layoutParams.width = tableCell.width;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                textCell.setLayoutParams(layoutParams);
            }
        }
    }

    public TableAdapter.TableRow getTableRow() {
        return tableRow;
    }

    public void setTableRow(TableAdapter.TableRow tableRow) {
        this.tableRow = tableRow;
        invalidate();
    }
}
