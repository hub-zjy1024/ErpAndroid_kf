package com.b1b.js.erpandroid_kf.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utils.common.MyImageUtls;
import utils.framwork.BitmapLruCache;
import utils.framwork.StatubarHider;
import utils.net.HttpUtils;

/**
 * Created by 张建宇 on 2019/8/29.
 */
public class Img2Adapter extends FragmentStatePagerAdapter {

    private List<String> imgs = new ArrayList<>();
    LinkedList<ImgFragment> mFragCache = new LinkedList<>();
    private BitmapLruCache<String> cacheMap;
    FragmentManager mFragmentManagerfMgr;
    FragmentTransaction fragmentTransaction;
    private int destroyPoi = 0;
    private int initPoi = 0;

    public Img2Adapter(FragmentManager fm, List<String> imgs) {
        super(fm);
        this.mFragmentManagerfMgr = fm;
        this.imgs = imgs;
        int totalSize = (int) Runtime.getRuntime().maxMemory();
        int maxMemory = (int) Runtime.getRuntime().freeMemory();
        float rate = 1024 * 1024;
        //            Log.e("zjy", "PicDetailActivity->ImgFragmentAdapter(): MaxMem==" + totalSize / rate +
        //                    "MB");
        //            Log.e("zjy",
        //                    "PicDetailActivity->ImgFragmentAdapter(): freeSize==" + maxMemory / rate +
        //                    "MB");
        // 取处内存的 1/5 用来当 缓存 大小
        int cachSize = maxMemory / 2;
        Log.d("zjy", "PicDetailActivity->ImgFragmentAdapter(): cacheSize==" + cachSize / rate);
        // 实例化 LruCache
        cacheMap = new BitmapLruCache<>(cachSize);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (this.fragmentTransaction == null) {
            Log.d("zjy", getClass() + "->destroyItem():++++++++++++ fragmentTransaction==null");
            this.fragmentTransaction = this.mFragmentManagerfMgr.beginTransaction();
        }
        fragmentTransaction.remove((Fragment) object);
        destroyPoi = position;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (this.fragmentTransaction == null) {
            this.fragmentTransaction = this.mFragmentManagerfMgr.beginTransaction();
        }
        ImgFragment o = (ImgFragment) getItem(position);
        if (o.isAdded()) {
            Log.d("zjy", getClass() + "->instantiateItem(): ==o Added" + o.toString());
        }
        fragmentTransaction.add(container.getId(), o);
        initPoi = position;
        return o;
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (this.fragmentTransaction != null) {
            try {
                this.fragmentTransaction.commitNowAllowingStateLoss();
            } catch (Exception e) {
                Log.d("zjy", getClass() + "->finishUpdate():ERROR ==", e);
            }
            Log.d("zjy", getClass() + "->finishUpdate(): ==mSize" + mFragCache.size());
            this.fragmentTransaction = null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        String imgUrl = imgs.get(position);
        ImgFragment picFrag = null;
        if (mFragCache.size() < 5) {
            picFrag = ImgFragment.newInstance(cacheMap, mFragCache);
            Log.d("zjy", getClass() + "->getItem():first create ==" + imgUrl);
            picFrag.setImgPath(imgUrl);
            mFragCache.offer(picFrag);
        } else {
//            if (destroyPoi >= position) {
//                picFrag = mFragCache.pollLast();
//            } else {
//                picFrag = mFragCache.poll();
//            }
            picFrag = mFragCache.pollLast();
        }
        picFrag.setImgPath(imgUrl);
        return picFrag;
    }

    @Override
    public int getCount() {
        return imgs.size();
    }

    public void releasBitmap() {
        cacheMap.evictAll();
    }

    public static class ImgFragment extends Fragment {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        String imgPath = "";
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
            tag = toString();
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
        private View mItemView;

        public static ImgFragment newInstance(BitmapLruCache<String> cacheMap,
                                              LinkedList<ImgFragment> mFragCache) {
            return new ImgFragment(cacheMap, mFragCache);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Log.d("zjy", "->onAttach(): ==" + tag);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("zjy", "->onCreate(): ==" + tag);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);

        }

        @Override
        public void onStart() {
            super.onStart();
            Log.d("zjy", "->onStart(): ==" + tag);
        }

        @Override
        public void onResume() {
            super.onResume();
            Rect frame = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            Log.d("zjy", getClass() + "->onResume(): titleHeight==VisiablDisplay" + "\t" + frame);
            Log.d("zjy", "->onResume(): ==" + tag);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
                savedInstanceState) {
            Log.d("zjy", "->onCreateView(): ==" + tag);
            String imgName = imgPath;
            if (imgPath != null) {
                int i = imgPath.lastIndexOf("/");
                if (i >= 0) {
                    imgName = imgPath.substring(imgPath
                            .lastIndexOf("/") + 1);
                }
            }
            Log.d("zjy", getClass() + "->onCreateView(): ==UsePic=" +imgName );
            if (mItemView == null) {
                mItemView = inflater.inflate(R.layout.frag_pic_detail, container, false);
            }
            View itemView = mItemView;
            mImgView = (ImageView) itemView.findViewById(R.id.frag_pic_detail_iv);
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

            heightPixels = displayMetrics.heightPixels;
            widthPixels = displayMetrics.widthPixels;
            FragmentActivity activity = getActivity();
//            ActionBar actionBar = activity.getActionBar();
//            int height = actionBar.getHeight();
            int statusBarHeight = StatubarHider.getStatusBarHeight(activity);
            int statusBarHeight2 = StatubarHider.getStatusBarHeightReflect(activity);
            int realHeight2 = StatubarHider.getRealHeight(activity);
            int titleHeight = StatubarHider.getTitleHeight(activity);

            Log.d("zjy", getClass() + "->onCreateView(): h1 -h2-realheight="+ statusBarHeight2 + "\t" + realHeight2 + "\t" + titleHeight);

            Log.d("zjy",
                    getClass() + "->onCreateView():statusBar+height==" +( statusBarHeight + displayMetrics.heightPixels) );
            Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics2 = new DisplayMetrics();
            defaultDisplay.getMetrics(displayMetrics2);
            Log.d("zjy",
                    getClass() + "->onCreateView(): displayMetrics2==" + displayMetrics2.heightPixels + "\t" + displayMetrics2.widthPixels);
            Log.d("zjy", getClass() + "->onCreateView(): height-width==" + heightPixels + "\t" + widthPixels);
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
            Log.d("zjy", "->onPause(): ==" + tag);
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d("zjy", "->onStop(): ==" + tag);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            //            if (mFragCache != null) {
            //                mFragCache.offer(this);
            //            }
            Log.d("zjy", "onDestroyView(): --" + tag);
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
                Bitmap mBitmap;
                try {
                    //                    long memoSize = MyImageUtls.getMemoSize(imgPath, widthPixels,
                    //                    heightPixels);
                    mBitmap = MyImageUtls.getSmallBitmap(imgPath, widthPixels, heightPixels);
                    BitmapFactory.decodeResource(getActivity().getResources(),
                            R.drawable.ic_pic_placeholder);
                } catch (Throwable e) {
                    e.printStackTrace();
                    mBitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                            R.drawable.ic_pic_placeholder);
                }
                if (mBitmap != null) {
                    Log.d("zjy",
                            tag + ": BitampSize==" + mBitmap.getByteCount() / 1024f / 1024);
                }
                return mBitmap;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            try {
                mImgView.setImageBitmap(null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Log.d("zjy", "PicDetailActivity->onDestroy(): --" + tag);
        }

    }
}