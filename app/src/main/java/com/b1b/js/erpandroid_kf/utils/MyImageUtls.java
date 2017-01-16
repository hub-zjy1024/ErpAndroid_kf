package com.b1b.js.erpandroid_kf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MyImageUtls {
    /**
     压缩从文件加载的Bitmap
     @param filePath
     @param targetWidth
     @param targetHeight
     @return
     */
    public static Bitmap getSmallBitmap(String filePath, int targetWidth, int targetHeight) {
        Options opt = new Options();
        opt.inJustDecodeBounds = true;

        Bitmap temp = BitmapFactory.decodeFile(filePath, opt);

        int sampleSize = getSimpleSize(opt, targetWidth, targetHeight);
        opt.inSampleSize = sampleSize;
        opt.inJustDecodeBounds = false;
        Bitmap newBitmap = BitmapFactory.decodeFile(filePath, opt);
        Log.e("zjy", "MyImageUtls,getSmallBitmap(): scal==" + opt.inSampleSize);
        if (newBitmap == null) {
            return null;
        }
        int degree = readBitmapDegreeByExif(filePath);
        Bitmap bm = rotateBitmap(newBitmap, degree);
        return newBitmap;
    }

    /**
     计算合适的缩放比例
     @param opt          BitmapFactory.Options
     @param targetWidth  期望的高度
     @param targetHeight 期望的宽度
     */

    private static int getSimpleSize(Options opt, int targetWidth, int targetHeight) {
        float resultScale = 1;
        int defHeight = opt.outHeight;
        int defWidth = opt.outWidth;
        if (defHeight > targetWidth || defWidth > targetHeight) {
            float widthScale = (float) defWidth / targetHeight;
            float heightScale = (float) defHeight / targetWidth;
            resultScale = Math.min(widthScale, heightScale);
        }
        return Math.round(resultScale);
    }

    /**
     @param imagePath
     @param requestWidth  期望的图片宽度
     @param requestHeight 期望的图片高度
     @return
     */
    public static Bitmap decodeBitmapFromFile(String imagePath, int requestWidth, int requestHeight) {
        if (!TextUtils.isEmpty(imagePath)) {
            if (requestWidth <= 0 || requestHeight <= 0) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                return bitmap;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//不加载图片到内存，仅获得图片宽高
            BitmapFactory.decodeFile(imagePath, options);
            Log.e("zjy", "MyImageUtls.java->decodeBitmapFromFile(): original height: " + options.outHeight);
            Log.e("zjy", "MyImageUtls.java->decodeBitmapFromFile(): original width: " + options.outWidth);
            if (options.outHeight == -1 || options.outWidth == -1) {
                try {
                    ExifInterface exifInterface = new ExifInterface(imagePath);
                    int height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.ORIENTATION_NORMAL);//获取图片的高度
                    int width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, ExifInterface.ORIENTATION_NORMAL);//获取图片的宽度
                    Log.e("zjy", "MyImageUtls.java->decodeBitmapFromFile(): exif height: " + options.inSampleSize);
                    Log.e("zjy", "MyImageUtls.java->decodeBitmapFromFile(): exif width: " + options.inSampleSize);
                    options.outWidth = width;
                    options.outHeight = height;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            options.inSampleSize = getSimpleSize(options, requestWidth, requestHeight); //计算获取新的采样率
            Log.e("zjy", "MyImageUtls.java->decodeBitmapFromFile(): insamplesize==" + options.inSampleSize);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(imagePath, options);

        } else {
            return null;
        }
    }

    // 存储图片

    public static void saveBitmap(String path, Bitmap bitmap) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            FileOutputStream fis = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fis);
            fis.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     保存图片到内部存储空间
     @param context
     @param fileName
     @param bitmap
     */
    // 存储图片
    public static void saveBitmapToInternal(Context context, String fileName, Bitmap bitmap) {
        try {
            FileOutputStream fis = context.openFileOutput(fileName, 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fis);
            fis.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     图片质量压缩
     @param orginPath 图片路径
     @param out       压缩后的输出流
     @param size      期望压缩后的大小（MB）
     @return
     */
    public static boolean compressBitmapAtsize(String orginPath, OutputStream out, float size) {
        boolean res = false;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(orginPath);
            if (bitmap != null) {
                int i = 100;
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, i, bao);
                while ((float) (bao.toByteArray().length) / 1024 / 1024 > size) {
                    bao.reset();
                    i -= 10;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, i, bao);
                }
                out.write(bao.toByteArray());
                res = true;
            }
            return res;
        } catch (OutOfMemoryError e) {
            Log.e("zjy", "MyImageUtls.java->compressBitmapAtsize(): oom==" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     获取拍摄图片的旋转角度
     @param path 图片路径
     @return 图片旋转角度
     */
    public static int readBitmapDegreeByExif(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    /**
     缩放Bitmap
     @param src
     @param w
     @param h
     @return
     */
    public static Bitmap scaleWithWH(Bitmap src, float w, float h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = w / width;
            float scaleHeight = h / height;
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }

    /**
     得到透明的bitmap
     @param sourceImg 源图片
     @param number    透明度
     @return
     */
    public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
        number = number * 255 / 100;
        for (int i = 0; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
        }

        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

                .getHeight(), Config.ARGB_8888);

        return sourceImg;
    }
}
