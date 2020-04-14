package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.PkChaidanItem;

import java.util.List;

import utils.adapter.CommonAdapter;
import utils.adapter.ViewHolder;

/**
 * Created by 张建宇 on 2020/4/1.
 */
public class PankuChaidanAdapter extends CommonAdapter<PkChaidanItem> {
    public PankuChaidanAdapter(Context context, List<PkChaidanItem> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    public interface DataChangeNotifyListener{
        void onChange();

    }

    public PankuChaidanAdapter(Context context, List<PkChaidanItem> mDatas, int itemLayoutId,
                               DataChangeNotifyListener mListner) {
        super(context, mDatas, itemLayoutId);
        this.mListner = mListner;
    }

    DataChangeNotifyListener mListner;

    static class WatcherWrapper implements TextWatcher {
        String id;
        EditText ed;
        EditText ed2;
        PkChaidanItem item;

        String tempID;
        PankuChaidanAdapter mAdapter;

        public WatcherWrapper(String id, EditText ed) {
            this.id = id;
            this.ed = ed;
        }

        public WatcherWrapper(String id, EditText ed, PkChaidanItem item, PankuChaidanAdapter mAdapter) {
            this.id = id;
            this.ed = ed;
            this.item = item;
            this.mAdapter = mAdapter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (id.equals(item.id)) {
                switch (ed.getId()) {
                    case R.id.item_chaidan_detail_count:
                        try {
                            item.Number = Integer.parseInt(s.toString());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.item_chaidan_detail_pihao:
                        item.BatchNo = s.toString();
                        break;
                }
            }
            //            item.Number = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
//            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mListner != null) {
            mListner.onChange();
        }
    }

    @Override
    public void convert(ViewHolder helper, final PkChaidanItem item) {
        EditText edPihao = helper.getView(R.id.item_chaidan_detail_pihao);
        EditText edCount = helper.getView(R.id.item_chaidan_detail_count);
        Button btnDel = helper.getView(R.id.item_chaidan_detail_del);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatas.remove(item);
                notifyDataSetChanged();
            }
        });
        String id = item.id;
        helper.setText(R.id.item_chaidan_detail_id, "编号:" + id);
        Object tag = edPihao.getTag();

        WatcherWrapper mWrapper1 = new WatcherWrapper(id, edPihao, item, this);
        WatcherWrapper mWrapper2 = null;

        if (tag == null) {
            mWrapper1 =   new WatcherWrapper(id, edPihao, item, this);
            mWrapper2 =  new WatcherWrapper(id, edCount, item, this);
            edPihao.setText(item.BatchNo);
            edCount.setText(item.Number + "");

            edPihao.addTextChangedListener(mWrapper1);
            edCount.addTextChangedListener(mWrapper2);
            edPihao.setTag(mWrapper1);
            edCount.setTag(mWrapper2);
        } else {
            mWrapper1 = (WatcherWrapper) edPihao.getTag();
            mWrapper2 = (WatcherWrapper) edCount.getTag();

            edPihao.removeTextChangedListener(mWrapper1);
            edCount.removeTextChangedListener(mWrapper2);
            edPihao.setText(item.BatchNo);
            edCount.setText(item.Number + "");
            mWrapper1 =new WatcherWrapper(id, edPihao, item, this);
            mWrapper2 =new WatcherWrapper(id, edCount, item, this);
            edPihao.addTextChangedListener(mWrapper1);
            edCount.addTextChangedListener(mWrapper2);
            edPihao.setTag(mWrapper1);
            edCount.setTag(mWrapper2);
            /*String savedId = tag.toString();
            if (id.equals(savedId)) {
                edPihao.addTextChangedListener(mWrapper1);
                edCount.addTextChangedListener(mWrapper2);
            } else {
                edPihao.removeTextChangedListener(mWrapper1);
                edCount.removeTextChangedListener(mWrapper2);


                mWrapper1 = new WatcherWrapper(id, edPihao);
                mWrapper2 = new WatcherWrapper(id, edCount);
                edPihao.addTextChangedListener(mWrapper1);
                edCount.addTextChangedListener(mWrapper2);
            }*/
        }


    }
}
