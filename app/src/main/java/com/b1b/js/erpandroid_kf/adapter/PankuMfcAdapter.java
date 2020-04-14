package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.widget.Filter;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PankuMFC;

import java.util.ArrayList;
import java.util.List;

import utils.adapter.FilterCommonAdapter;
import utils.adapter.ViewHolder;

/**
 * Created by 张建宇 on 2020/4/10.
 */
public class PankuMfcAdapter extends FilterCommonAdapter<PankuMFC> {
    private List<PankuMFC> allData;

    public PankuMfcAdapter(Context context, List<PankuMFC> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
        allData = new ArrayList<>();
    }

    @Override
    public void convert(ViewHolder helper, PankuMFC item) {
        helper.setText(R.id.item_simple_panku_ato, item.getFullName());
    }
    public void setAllData(List<PankuMFC> allData) {
        this.allData = allData;
    }

    public void reset() {
        mDatas.clear();
        mDatas.addAll(allData);
    }

    static class MfcFilter extends Filter {
        PankuMfcAdapter mfcAdapter;

        public MfcFilter(PankuMfcAdapter mfcAdapter) {
            this.mfcAdapter = mfcAdapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<PankuMFC> newData = new ArrayList<>();
            if (constraint != null && !constraint.toString().contains("@")) {
                for (PankuMFC data : mfcAdapter.allData) {
                    if (data.getFullName().contains(constraint)) {
                        newData.add(data);
                    }
                }
            }
            results.values = newData;
            results.count = newData.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mfcAdapter.mDatas.clear();
            mfcAdapter.mDatas.addAll((List<PankuMFC>) results.values);
            mfcAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return new MfcFilter(this);
    }

}
