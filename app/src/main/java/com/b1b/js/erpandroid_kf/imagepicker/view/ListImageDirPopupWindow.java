package com.b1b.js.erpandroid_kf.imagepicker.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.imagepicker.bean.ImageFloder;
import com.b1b.js.erpandroid_kf.imagepicker.utils.CommonAdapter;
import com.b1b.js.erpandroid_kf.imagepicker.utils.ViewHolder;

import java.util.List;


public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFloder> {
    private ListView mListDir;

    public ListImageDirPopupWindow(int width, int height,
                                   List<ImageFloder> datas, View convertView) {
        super(convertView, width, height, true, datas);
    }

    @Override
    public void initViews() {
        mListDir = (ListView) findViewById(R.id.id_list_dir);
        mListDir.setAdapter(new CommonAdapter<ImageFloder>(context, mDatas,
                R.layout.list_dir_item) {
            @Override
            public void convert(ViewHolder helper, ImageFloder item) {
                helper.setText(R.id.id_dir_item_name, item.getName())
                        .setImageByUrl(R.id.id_dir_item_image,
                                item.getFirstImagePath())
                        .setText(R.id.id_dir_item_count, item.getCount() + "张");
            }

        });
    }

    public interface OnImageDirSelected {
        void selected(ImageFloder floder);
    }

    private OnImageDirSelected mImageDirSelected;

    public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected) {
        this.mImageDirSelected = mImageDirSelected;
    }

    @Override
    public void initEvents() {
        mListDir.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (mImageDirSelected != null) {
                    mImageDirSelected.selected(mDatas.get(position));
                }
            }
        });
    }

    @Override
    public void init() {

    }

    @Override
    protected void beforeInitWeNeedSomeParams(Object... params) {
    }

}
