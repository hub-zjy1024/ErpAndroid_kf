package com.b1b.js.erpandroid_kf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MyFileUtils {
    public static void saveImg(String name, Bitmap bitmap, Context context) {
        File file;
        if (isMonuted()) {
            file = new File(Environment.getDataDirectory(), name);
        } else {
            file = new File(context.getCacheDir(), name);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            BufferedOutputStream fio = new BufferedOutputStream(new FileOutputStream(file));
//            FileOutputStream fio = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, fio);
            bitmap.recycle();
            Bitmap newBitmap = MyImageUtls.getSmallBitmap(file.getAbsolutePath(), 800, 480);
            newBitmap.compress(CompressFormat.JPEG, 100, fio);
            Log.e("length", "" + file.length());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean isMonuted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


}
