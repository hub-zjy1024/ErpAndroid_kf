package com.b1b.js.erpandroid_kf;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.b1b.js.erpandroid_kf.myview.ZoomImageView;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import utils.common.MyImageUtls;
import utils.framwork.BitmapLruCache;
import utils.net.HttpUtils;

public class PicDetailActivity extends BaseMActivity {

    private ZoomImageView zoomIv;
    private ViewPager mViewPager;
    private List<ZoomImageView> mImgs;
    private List<String> paths;
    protected PagerAdapter adapter;
    public static final String ex_Path = "path";
    public static final String ex_Paths = "paths";
    int pos;
    TextView tvPageNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_detail);
        zoomIv = (ZoomImageView) findViewById(R.id.activity_pic_detail_iv);
        mViewPager = (ViewPager) findViewById(R.id.activity_pic_detail_viewpager);
        tvPageNo = (TextView) findViewById(R.id.activity_pic_detail_tv);
        final String path = getIntent().getStringExtra(ex_Path);
        paths = getPaths();
        pos = getIntent().getIntExtra("pos", 0);
        adapter = getPagerAdapter(paths);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                chagePageTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int position = pos;
        chagePageTitle(position);
        mViewPager.setCurrentItem(pos);
    }

    void chagePageTitle(int position) {
        tvPageNo.setText((position + 1) + "/" + paths.size());
        TextView viewInContent = getViewInContent(R.id.activity_pic_detail_fname);
        if (position < paths.size()) {
            String tempPath = paths.get(position);
            String fname = tempPath.substring(tempPath.lastIndexOf("/") + 1);
            viewInContent.setText(fname);
        }else {
            viewInContent.setText("");
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void setListeners() {

    }

    public List<String> getPaths() {
        List<String> paths = getIntent().getStringArrayListExtra(ex_Paths);
        if (paths == null) {
            paths = new ArrayList<>();
        }
        return paths;
    }

    public PagerAdapter getPagerAdapter(List<String> paths) {
        FragmentManager mgr = getSupportFragmentManager();
        PagerAdapter mAdapter = new ImgFragmentAdapter(mgr, paths);
        return mAdapter;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter instanceof ImgFragmentAdapter) {
            try {
                ((ImgFragmentAdapter) adapter).releasBitmap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ImgFragmentAdapter extends FragmentPagerAdapter {
        private List<String> imgs;
        private LinkedHashMap<String, ImgFragment> mFrags = new LinkedHashMap<>();
        private BitmapLruCache<String> cacheMap;
        FragmentManager mFragmentManagerfMgr;

        public ImgFragmentAdapter(FragmentManager fm, List<String> imgs) {
            super(fm);
            this.mFragmentManagerfMgr = fm;
            this.imgs = imgs;
            int totalSize = (int) Runtime.getRuntime().maxMemory();
            int maxMemory = (int) Runtime.getRuntime().freeMemory();
            float rate = 1024 * 1024;
//            Log.e("zjy", "PicDetailActivity->ImgFragmentAdapter(): MaxMem==" + totalSize / rate +
//                    "MB");
//            Log.e("zjy",
//                    "PicDetailActivity->ImgFragmentAdapter(): freeSize==" + maxMemory / rate + "MB");
            // 取处内存的 1/5 用来当 缓存 大小
            int cachSize = maxMemory / 2;
            Log.d("zjy", "PicDetailActivity->ImgFragmentAdapter(): cacheSize==" + cachSize / rate);
            // 实例化 LruCache
            cacheMap = new BitmapLruCache<>(cachSize);
        }


        @Override
        public Fragment getItem(int position) {
            String imgUrl = imgs.get(position);
            ImgFragment picFrag = mFrags.get(imgUrl);

            if (picFrag == null) {
                Log.d("zjy", getClass() + "->getItem():new Instatce ==" + imgUrl);
                picFrag = ImgFragment.newInstance(imgUrl, cacheMap);
                mFrags.put(imgUrl, picFrag);
            }
            Log.d("zjy", getClass() + "->getItem(): ==" + picFrag.toString());
            return picFrag;
        }

        @Override
        public int getCount() {
            return imgs.size();
        }

        public void releasBitmap() {
            cacheMap.evictAll();
        }
    }

    public static class ImgFragment extends Fragment {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        String imgPath = "";
        private View itemView;
        ImageView mImgView;
        Bitmap nBmp = null;
        private BitmapLruCache<String> cacheMap;

        public ImgFragment() {

        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
            if (imgPath != null) {
                int i = imgPath.lastIndexOf("/");
                if (i >= 0) {
                    tag = imgPath.substring(imgPath
                            .lastIndexOf("/") + 1);
                }
            }
            tag = toString() + "/" + tag;
        }

        LinkedList<ImgFragment> mFragCache;

        @SuppressLint("ValidFragment")
        private ImgFragment(BitmapLruCache<String> cacheMap, LinkedList<ImgFragment> mFragCache) {
            this.cacheMap = cacheMap;
            this.mFragCache = mFragCache;
        }

        int heightPixels = 500;
        int widthPixels = 100;
        private String tag = "未知";

        @SuppressLint("ValidFragment")
        public ImgFragment(String imgPath) {
            this.imgPath = imgPath;
        }

        @SuppressLint("ValidFragment")
        public ImgFragment(String imgPath, BitmapLruCache<String> cacheMap) {
            this.imgPath = imgPath;
            this.cacheMap = cacheMap;
            if (imgPath != null) {
                int i = imgPath.lastIndexOf("/");
                if (i >= 0) {
                    tag = imgPath.substring(imgPath
                            .lastIndexOf("/") + 1);
                }
            }
            tag = toString() + "/" + tag;
        }

        public static ImgFragment newInstance(String path) {
            return new ImgFragment(path);
        }

        public static ImgFragment newInstance(String path, BitmapLruCache<String> cacheMap) {
            return new ImgFragment(path, cacheMap);
        }

        public static ImgFragment newInstance(BitmapLruCache<String> cacheMap, LinkedList<ImgFragment> mFragCache) {
            return new ImgFragment(cacheMap, mFragCache);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
                savedInstanceState) {
            Log.d("zjy", "->onCreateView(): ==" + tag);
            //            itemView = getView();
            if (itemView == null) {
                itemView = inflater.inflate(R.layout.frag_pic_detail, container, false);
            } else {
                Log.d("zjy", "initView->onCreateView():useCacheView ==" + tag);
            }
            ViewParent parent = itemView.getParent();
            if (parent != null) {
                ViewGroup view = (ViewGroup) parent;
                view.removeView(itemView);
                Log.d("zjy", "initView->onCreateView(): ==hasView" + tag);
            } else {
                Log.d("zjy", "initView->onCreateView():noParent ==" + tag);
            }
            mImgView = (ImageView) itemView.findViewById(R.id.frag_pic_detail_iv);
           /* TextView tvTitle = (TextView) itemView.findViewById(R.id.frag_pic_detail_title);
            if (imgPath != null) {
                String fName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
                tvTitle.setText(fName);
            }*/
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            heightPixels = displayMetrics.heightPixels;
            widthPixels = displayMetrics.widthPixels;
            if (imgPath == null) {
                mImgView.setImageBitmap(null);
            } else {
                Bitmap tempBmp = cacheMap.get(imgPath);
                if (tempBmp != null) {
                    // nBmp = tempBmp;
                    Log.d("zjy", "PicDetailActivity->onCreateView(): getCacheBitmap==" + tempBmp);
                    mImgView.setImageBitmap(tempBmp);
                } else {
                    Runnable imgRun = new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap decodeBmp = loadImg(imgPath);
                            if (decodeBmp != null) {
                                if (decodeBmp.isRecycled()) {
                                    Log.d("zjy", "->run(): recycle img==" + imgPath);
                                    return;
                                }
                                cacheMap.put(imgPath, decodeBmp);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mImgView.setImageBitmap(decodeBmp);
                                    }
                                });
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mImgView.setImageResource( R.drawable.ic_pic_placeholder);
                                    }
                                });
                                Log.d("zjy", "-load bitmap Failed ,key=" + tag);
                                MyApp.myLogger.writeBug("load bitmap Failed ,key=" + imgPath);
                            }
                        }
                    };
                    TaskManager.getInstance().execute(imgRun);
                }
            }
            return itemView;
        }


        @Override
        public void onDetach() {
            super.onDetach();
            if (mFragCache != null) {
                mFragCache.offer(this);
            }
            Log.d("zjy", "->onDetach(): ==" + tag);
        }

        @Override
        public void onPause() {
            super.onPause();
            itemView = getView();
            Log.d("zjy", "->onPause(): ==" + tag);
        }


        Bitmap loadImg(String path) {
            if (path.startsWith("http") || path.startsWith("https")) {
                Bitmap mBitmap;
                try {
                    InputStream inputStream = HttpUtils.create(path).getInputStream();
                    mBitmap = MyImageUtls.getSmallBitmap(inputStream, widthPixels, heightPixels);
                } catch (IOException e) {
                    e.printStackTrace();
                    mBitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                            R.drawable.ic_pic_placeholder);
                }
                return mBitmap;
            } else {
                Bitmap mBitmap = null;
                try {
                    //                    long memoSize = MyImageUtls.getMemoSize(imgPath, widthPixels, heightPixels);
                    mBitmap = MyImageUtls.getSmallBitmap(imgPath, widthPixels, heightPixels);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (mBitmap != null) {
                    Log.d("zjy",
                            tag + ": BitampSize==" + mBitmap.getByteCount() / 1024f / 1024);
                }
                return mBitmap;
            }
        }
    }
}
