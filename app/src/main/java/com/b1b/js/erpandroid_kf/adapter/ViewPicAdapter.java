package com.b1b.js.erpandroid_kf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import utils.framwork.BitmapLruCache;

/**
 * Created by 张建宇 on 2017/3/2.
 */

public class ViewPicAdapter extends MyBaseAdapter<FTPImgInfo> {

    private LinkedHashMap<String, Bitmap> bitmaps = new LinkedHashMap<>();

    private static LruCache<String, Bitmap> mCache = new BitmapLruCache<>(BitmapLruCache.maxCacheSize);

    public ViewPicAdapter(List<FTPImgInfo> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        PicHolder mHolder = (PicHolder) baseHolder;
        mHolder.iv = (ImageView) convertView.findViewById(R.id.item_viewpic_iv);
    }

    @Override
    protected void initHolder(FTPImgInfo currentData, MyBasedHolder baseHolder) {
        PicHolder mHolder = (PicHolder) baseHolder;
        final String realPath = currentData.getImgPath();
//        Bitmap bitmap;
//        if (!bitmaps.containsKey(realPath)) {
//            try {
//                bitmap = MyImageUtls.getMySmallBitmap(realPath, 200, 200);
//                bitmaps.put(realPath, bitmap);
//                mHolder.iv.setImageBitmap(bitmap);
//            } catch (OutOfMemoryError error) {
//                error.printStackTrace();
//            }
//        } else {
//            bitmap = bitmaps.get(realPath);
//            mHolder.iv.setImageBitmap(bitmap);
//        }
        if (realPath == null) {
            Log.e("zjy", getClass() + "->initHolder(): ==picUrl=null");
            return;
        }
//        bitmap = mCache.get(realPath);
//        if (bitmap == null) {
//            bitmap = MyImageUtls.getMySmallBitmap(realPath, 200, 200);
//            mCache.put(realPath, bitmap);
//        }
//        mHolder.iv.setImageBitmap(bitmap);
        final ImageView finalIv=mHolder.iv;
        finalIv.setTag(realPath);
        Picasso.with(mContext).load(new File(realPath)).resize(200, 200).into(finalIv);
//        Picasso.with(mContext).load(new File(realPath)).resize(200, 200).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
////                if (bitmap != null) {
////                    String realTag = (String) finalIv.getTag();
////                    if (realPath.equals(realTag)) {
////                        finalIv.setImageBitmap(bitmap);
////                    } else {
////                        Log.e("zjy", getClass() + "->onBitmapLoaded()not found tag==");
////                    }
////                } else {
////                    Log.e("zjy", getClass() + "->onBitmapLoaded(): load null==");
////                    finalIv.setImageResource(R.drawable.ic_pic_placeholder);
////                }
//                finalIv.setImageBitmap(bitmap);
//            }
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//                Log.e("zjy", getClass() + "->onBitmapFailed(): load Failed=="+realPath);
//                finalIv.setImageResource(R.drawable.ic_pic_placeholder);
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//                Log.e("zjy", getClass() + "->onPrepareLoad(): ==preLoad"+realPath  );
//                finalIv.setImageResource(R.drawable.ic_pic_placeholder);
//            }
//        });
    }

    public void relaseCache() {
        mCache.evictAll();
    }

    @Override
    protected MyBasedHolder getHolder() {
        return new PicHolder();
    }


    class PicHolder extends MyBaseAdapter.MyBasedHolder {
        ImageView iv;
    }
}
