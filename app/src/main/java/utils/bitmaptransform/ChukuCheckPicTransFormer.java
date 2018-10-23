package utils.bitmaptransform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import utils.ImageWaterUtils;

/**
 * Created by 张建宇 on 2019/1/11.
 */
public class ChukuCheckPicTransFormer implements IBitmapTransform{
    private Bitmap wtBmp;
    private Context mContext;
    private String pid;

    public ChukuCheckPicTransFormer(Bitmap wtBmp, Context mContext, String pid) {
        this.wtBmp = wtBmp;
        this.mContext = mContext;
        this.pid = pid;
    }

    @Override
    public Bitmap getTransFormedBitmap(Bitmap inputBitmap) {
        Bitmap waterBitmap = ImageWaterUtils.createWaterMaskRightBottom(mContext, inputBitmap,
                wtBmp);
        Bitmap TextBitmap = ImageWaterUtils.drawTextToRightTop(mContext, waterBitmap, pid,
                (int) (inputBitmap.getWidth() * 0.015), Color.RED, 20, 20);
        if (inputBitmap != null) {
            inputBitmap.recycle();
            inputBitmap = null;
        }
        if (waterBitmap != null) {
            waterBitmap.recycle();
            inputBitmap = null;
        }
        return TextBitmap;
    }
}
