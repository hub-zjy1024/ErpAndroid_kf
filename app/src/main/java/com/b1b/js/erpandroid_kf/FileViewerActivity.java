package com.b1b.js.erpandroid_kf;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import com.b1b.js.erpandroid_kf.fragment.FileViewerFragment;

import java.util.LinkedHashMap;
import java.util.List;

import utils.common.MyFileUtils;
import utils.framwork.BitmapLruCache;

public class FileViewerActivity extends PicDetailActivity {

    @Override
    public PagerAdapter getPagerAdapter(List<String> paths) {
        FragmentManager mgr = getSupportFragmentManager();
        PagerAdapter mAdapter = new FileViewerAdapterextends(mgr, paths);
        return mAdapter;
    }

    class FileViewerAdapterextends extends FragmentPagerAdapter {

        private List<String> imgs;
        private LinkedHashMap<String, Fragment> mFrags = new LinkedHashMap<>();
        private BitmapLruCache<String> cacheMap;
        FragmentManager mFragmentManagerfMgr;

        public FileViewerAdapterextends(FragmentManager fm, List<String> imgs) {
            super(fm);
            this.mFragmentManagerfMgr = fm;
            this.imgs = imgs;
            int totalSize = (int) Runtime.getRuntime().maxMemory();
            int maxMemory = (int) Runtime.getRuntime().freeMemory();
            float rate = 1024 * 1024;
            // 取处内存的 1/5 用来当 缓存 大小
            int cachSize = maxMemory / 2;
            Log.d("zjy", getClass() + "->FileViewerAdapterextends(): cacheSize==" + cachSize / rate);
            // 实例化 LruCache
            cacheMap = new BitmapLruCache<>(cachSize);
        }


        @Override
        public Fragment getItem(int position) {
            String imgUrl = imgs.get(position);
            Fragment picFrag = mFrags.get(imgUrl);
            if (picFrag == null) {
                Log.d("zjy", getClass() + "->getItem():new Instatce ==" + imgUrl);
                if (MyFileUtils.isPdf(imgUrl)) {
                    picFrag = FileViewerFragment.newInstance(imgUrl);
                } else if (MyFileUtils.isImage(imgUrl)) {
                    picFrag = ImgFragment.newInstance(imgUrl, cacheMap);
                } else {
                    picFrag = ImgFragment.newInstance(imgUrl, cacheMap);
                }
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
}