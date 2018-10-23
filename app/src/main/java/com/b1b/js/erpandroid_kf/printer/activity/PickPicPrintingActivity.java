package com.b1b.js.erpandroid_kf.printer.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.printer.adapter.ImageGvAdapter;
import com.b1b.js.erpandroid_kf.printer.entity.PrinterItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.DialogUtils;

public class PickPicPrintingActivity extends AppCompatActivity {
    private List<PrinterItem> selcetItems;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    DialogUtils.dismissDialog(pdialog);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private int page = 1;
    private int picCount = 30;
    ProgressDialog pdialog ;
    private List<PrinterItem> dataList = new ArrayList<>();
    private List<PrinterItem> total = new ArrayList<>();
    private ImageGvAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_pic_printing);
        final GridView gv = (GridView) findViewById(R.id.activity_pick_pic_printing_gv);
        adapter = new ImageGvAdapter(dataList, this, R.layout.pickpic_printing_gv_items);
        gv.setAdapter(adapter);
        gv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        for (int i = page * picCount; i < (page + 1) * picCount; i++) {
                            if (i < total.size()) {
                                dataList.add(total.get(i));
                            }
                        }
                        page++;
                        mHandler.sendEmptyMessage(0);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        Button btnCommit = (Button) findViewById(R.id
                .activity_pick_pic_printing_btnCommit);
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<PrinterItem> arrayList = adapter.getmSelectedImage();
                Log.e("zjy", "PickPicPrintingActivity->onClick(): arrayList.size==" +
                        arrayList.size());
                if (arrayList.size() == 0) {
                    Toast.makeText(PickPicPrintingActivity.this, "请选择要打印的图片", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                String[] paths = new String[arrayList.size()];
                String[] flags = new String[arrayList.size()];
                for(int i=0;i<arrayList.size();i++) {
                    PrinterItem item = arrayList.get(i);
                    paths[i] = item.getFile().getAbsolutePath();
                    flags[i] = item.getFlag();
                }
                Intent intent = getIntent();
                intent.putExtra("imgPaths", paths);
                intent.putExtra("flags", flags);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        pdialog = new ProgressDialog(this);
        pdialog.setTitle("提示");
        pdialog.setMessage("正在加载图片");
        pdialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (total.size() > 0) {
            page = 1;
            dataList.clear();
            for (int i =0; i <  picCount; i++) {
                if (i < total.size()) {
                    dataList.add(total.get(i));
                }
            }
            mHandler.sendEmptyMessage(0);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String firstImage = null;
                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver mContentResolver = PickPicPrintingActivity.this.getContentResolver();
                    // 只查询jpeg和png的图片
                    Cursor mCursor = mContentResolver.query(mImageUri, null,
                            MediaStore.Images.Media.MIME_TYPE + "=? or "
                                    + MediaStore.Images.Media.MIME_TYPE + "=?",
                            new String[]{"image/jpeg", "image/png"}, MediaStore.Images
                                    .Media.DATE_ADDED + " desc");

                    Log.e("zjy", "PickPicActivity.java->run():searched pic counts==" +
                            mCursor.getCount());
                    int i = 0;
                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            // 获取图片的路径
                            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore
                                    .Images.Media.DATA));
                            PrinterItem item = new PrinterItem();
                            item.setFile(new File(path));
                            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                                item.setFlag("jpg");
                            } else if (path.endsWith(".png")) {
                                item.setFlag("png");
                            }
                            total.add(item);
                            if (i < picCount) {
                                dataList.add(item);

                            }
                            if (i == picCount) {
                                mHandler.sendEmptyMessage(0);
                            }
                            i++;
                        }
                        if (mCursor.getCount() > 0) {
                            // 通知Handler扫描图片完成

                        }
                        mCursor.close();
                    }
                }
            }).start();
        }
    }
}
