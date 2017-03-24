package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.b1b.js.erpandroid_kf.myview.ZoomImageView;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;

import java.util.ArrayList;
import java.util.List;

public class PicDetailActivity extends AppCompatActivity {

    private ZoomImageView zoomIv;
    private ViewPager mViewPager;
    private List<ZoomImageView> mImgs;
    private List<String> paths;
    PicDetailViewPagerAdapter adapter;
    int pos;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (paths != null) {
                        for (int i = 0; i < paths.size(); i++) {
                            ZoomImageView tempView = new ZoomImageView(PicDetailActivity.this);
                            //                imgLayoutParams.width =300;
                            //                imgLayoutParams.height =300;
                            //                zoomImageView.setLayoutParams(imgLayoutParams);
                            if (i != pos) {
                                try {
                                    Bitmap bitmap = MyImageUtls.getMySmallBitmap(paths.get(i), 500, 500);
                                    tempView.setImageBitmap(bitmap);
                                    mImgs.add(tempView);
                                } catch (OutOfMemoryError error) {
                                    error.printStackTrace();
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_detail);
        zoomIv = (ZoomImageView) findViewById(R.id.activity_pic_detail_iv);
        mViewPager = (ViewPager) findViewById(R.id.activity_pic_detail_viewpager);
        String path = getIntent().getStringExtra("path");
        paths = getIntent().getStringArrayListExtra("paths");
        mImgs = new ArrayList<>();
        ZoomImageView zoomImageView = new ZoomImageView(PicDetailActivity.this);
        try {
            Bitmap bitmap = MyImageUtls.getMySmallBitmap(path, 500, 500);
            zoomImageView.setImageBitmap(bitmap);
            mImgs.add(zoomImageView);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        pos = getIntent().getIntExtra("pos", -1);
         adapter = new PicDetailViewPagerAdapter(null, mImgs);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(adapter);
        mHandler.sendEmptyMessageDelayed(0,300);
    }

    class PicDetailViewPagerAdapter extends PagerAdapter {
        public PicDetailViewPagerAdapter(Context mContext, List<ZoomImageView> data) {
            this.mContext = mContext;
            this.data = data;
        }

        private Context mContext;
        private List<ZoomImageView> data;

        @Override

        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ZoomImageView zoomImageView = data.get(position);
            ViewGroup parent = (ViewGroup) zoomImageView.getParent();
            if (parent != null) {
                parent.removeView(zoomImageView);
            }
            container.addView(zoomImageView);
            return zoomImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
