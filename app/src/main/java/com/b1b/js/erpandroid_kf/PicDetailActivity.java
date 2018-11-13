package com.b1b.js.erpandroid_kf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.myview.ZoomImageView;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import utils.HttpUtils;
import utils.MyImageUtls;

public class PicDetailActivity extends AppCompatActivity {

    private ZoomImageView zoomIv;
    private ViewPager mViewPager;
    private List<ZoomImageView> mImgs;
    private List<String> paths;
    private PagerAdapter adapter;
    int pos;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_detail);
        zoomIv = (ZoomImageView) findViewById(R.id.activity_pic_detail_iv);
        mViewPager = (ViewPager) findViewById(R.id.activity_pic_detail_viewpager);
        tv = (TextView) findViewById(R.id.activity_pic_detail_tv);
        String path = getIntent().getStringExtra("path");
        paths = getIntent().getStringArrayListExtra("paths");
        mImgs = new ArrayList<>();
        ZoomImageView zoomImageView = new ZoomImageView(PicDetailActivity.this);
        try {
            Bitmap bitmap = MyImageUtls.getMySmallBitmap(path, 500, 500);
            zoomImageView.setImageBitmap(bitmap);
            zoomImageView.setTag(bitmap);
            mImgs.add(zoomImageView);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        pos = getIntent().getIntExtra("pos", 0);
        FragmentManager mgr = getSupportFragmentManager();
//        paths = new ArrayList<>();
//        paths.add("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1754781758,2069282087&fm=173&app=49&f=JPEG?w=612&h=365&s=5AD2688C42520FD44484788A0300E09C");
//        paths.add("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=2769768633,2874225384&fm=173&app=49&f=JPEG?w=640&h=451&s=FEC7A144062BB7551C5529070300A0C2");
//        paths.add("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=714297440,3631358855&fm=173&app=49&f=JPEG?w=640&h=620&s=082193194C1372DC0839D5D20300D0A1");
//        paths.add("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1790721534,1107844676&fm=173&app=49&f=JPEG?w=640&h=821&s=6F32038D5402FEF984000CF60300D02F");
        adapter = new ImgFragmentAdapter(mgr, paths);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tv.setText((position + 1) + "/" + paths.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tv.setText(("1/" + paths.size()));
        mViewPager.setCurrentItem(pos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((ImgFragmentAdapter) adapter).releasBitmap();
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
            View zoomVIew = (View) object;
            Bitmap tag = (Bitmap) zoomVIew.getTag();
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    static class ImgFragmentAdapter extends FragmentStatePagerAdapter {
        private List<String> imgs = new ArrayList<>();


        private LruCache<String, Bitmap> cacheMap;
        public ImgFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public ImgFragmentAdapter(FragmentManager fm, List<String> imgs) {
            super(fm);
            this.imgs = imgs;

            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            Log.e("zjy", "PicDetailActivity->ImgFragmentAdapter(): MaxMem==" + maxMemory);
            // 取处内存的 1/5 用来当 缓存 大小
            int cachSize = maxMemory / 3;
            Log.e("zjy", "PicDetailActivity->ImgFragmentAdapter(): cacheSize==" + cachSize);
            // 实例化 LruCache
            cacheMap=new LruCache<String, Bitmap>(cachSize) {
                //内部方法sizeOf设置每一张图片的缓存大小
                protected int sizeOf(String key, Bitmap value) {
                    //在每次存入缓存时调用，告诉系统这张缓存图片有多大
                    // 相当于 为每次 要缓存的 资源 分配 大小空间
                    return value.getByteCount();
                }
            };
        }

        @Override
        public Fragment getItem(int position) {
            String s = imgs.get(position);
            Fragment picFrag = ImgFragment.newInstance(s, cacheMap);
            return picFrag;
        }

        @Override
        public int getCount() {
            return imgs.size();
        }

        public void releasBitmap(){
            cacheMap.evictAll();
        }
    }

    public static class ImgFragment extends Fragment {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        String imgPath = "";
        ImageView mImgView;
        Bitmap nBmp =null;
        private LruCache<String, Bitmap> cacheMap;
        public ImgFragment(){

        }
        @SuppressLint("ValidFragment")
        public ImgFragment(String imgPath) {
            this.imgPath = imgPath;
        }
        @SuppressLint("ValidFragment")
        public ImgFragment(String imgPath, LruCache<String, Bitmap> cacheMap) {
            this.imgPath = imgPath;
            this.cacheMap = cacheMap;
        }

        public static ImgFragment newInstance(String path) {
            return new ImgFragment(path);
        }

        public static ImgFragment newInstance(String path, LruCache<String, Bitmap> cacheMap) {
            return new ImgFragment(path, cacheMap);
        }
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
                savedInstanceState) {
            Bundle arguments = getArguments();
            View itemView = inflater.inflate(R.layout.frag_pic_detail, container, false);
            mImgView = (ImageView) itemView.findViewById(R.id.frag_pic_detail_iv);
            Bitmap tempBmp = cacheMap.get(imgPath);
            if (tempBmp != null) {
               // nBmp = tempBmp;
                Log.e("zjy", "PicDetailActivity->onCreateView(): getCacheBitmap==" + tempBmp);
                mImgView.setImageBitmap(tempBmp);
            }else{
                Runnable imgRun = new Runnable() {
                @Override
                public void run() {
                        Log.e("zjy", "PicDetailActivity->run(): Decode==" + imgPath.substring(imgPath
                                .lastIndexOf("/") + 1));
                    final Bitmap decodeBmp = loadImg(imgPath);
                    Log.e("zjy", "PicDetailActivity->run(): BitampSize==" + decodeBmp.getByteCount());
                    cacheMap.put(imgPath, decodeBmp);
                        mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mImgView.setImageBitmap(decodeBmp);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(imgRun);
            }
            return itemView;
        }

        /*@Override
        public void onStop() {
            super.onStop();
            mImgView.setImageBitmap(null);
            Log.e("zjy", "PicDetailActivity->onStop(): --" + imgPath.substring(imgPath.lastIndexOf("/") + 1)
                    + toString());
        }
*/
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.e("zjy", "PicDetailActivity->onDestroyView(): --" + imgPath.substring(imgPath.lastIndexOf("/")+1)
                    + toString());
            mImgView.setImageBitmap(null);
            Bitmap remove = cacheMap.remove(imgPath);
            if (remove != null) {
                remove.recycle();
            }
        }

        Bitmap loadImg(String path) {
            if (path.startsWith("http")||path.startsWith("https")) {
                final Bitmap[] newBitmap = new Bitmap[1];
                HttpUtils.create(path).execute(new HttpUtils.onResult<InputStream>() {
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onOk(InputStream result) {
                        newBitmap[0] = BitmapFactory.decodeStream(result);
                    }
                });
                return newBitmap[0];
            }else {
                return BitmapFactory.decodeFile(imgPath);
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

            Log.e("zjy", "PicDetailActivity->onDestroy(): --" + imgPath.substring(imgPath.lastIndexOf("/")+1)
                    + toString());
        }

   /*

        @Override
        public void onAttach(Context context) {
            Log.e("zjy", "PicDetailActivity->onAttach(): ++" + imgPath.substring(imgPath.lastIndexOf("/")+1)
                    + toString());
            super.onAttach(context);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            Log.e("zjy", "PicDetailActivity->onDetach(): --" + imgPath.substring(imgPath.lastIndexOf("/")+1)+ toString());
           *//* if (nBmp != null) {
                nBmp.recycle();
                nBmp = null;
            }*//*
        }*/
    }
}
