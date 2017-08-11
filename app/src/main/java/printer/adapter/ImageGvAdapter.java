package printer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.R;

import java.util.ArrayList;
import java.util.List;

import printer.entity.PrinterItem;


import static printer.adapter.MyImageUtls.bitmapCache;

/**
 * Created by 张建宇 on 2017/6/19.
 */

public class ImageGvAdapter extends MyBaseAdapter<PrinterItem> {
    public ArrayList<PrinterItem> mSelectedImage = new ArrayList<PrinterItem>();
    public ArrayList<String> selectedPath = new ArrayList<String>();
    private android.os.Handler handler = new android.os.Handler();
    public ArrayList<PrinterItem> getmSelectedImage() {
        return mSelectedImage;
    }

    public ImageGvAdapter(List<PrinterItem> data, Context mContext, int itemViewId) {
        super(data, mContext, itemViewId);
    }

    @Override
    protected void initItems(View convertView, MyBasedHolder baseHolder) {
        //        convertView = LayoutInflater.from(mCont
        PrinterHoloder holoder = (PrinterHoloder) baseHolder;
        holoder.iv = (ImageView) convertView.findViewById(R.id.priting_gv_item_iv);

    }

    @Override
    protected void initHolder(final PrinterItem currentData, MyBasedHolder baseHolder) {
        final PrinterHoloder holoder = (PrinterHoloder) baseHolder;
        holoder.iv.setImageResource(R.drawable.pictures_no);
        Bitmap bitmap = bitmapCache.get(currentData.getFile().getAbsolutePath());
        if (bitmap == null) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    final Bitmap newBitmap = MyImageUtls.getSmallBitmap(currentData.getFile()
                            .getAbsolutePath(), 100, 100);
                    bitmapCache.put(currentData.getFile().getAbsolutePath(), newBitmap);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            holoder.iv.setImageBitmap(newBitmap);
                        }
                    });
                }
            }.start();
//            bitmap = BitmapFactory.decodeFile(currentData.getFile()
//                    .getAbsolutePath());
//            Bitmap newBitmap = MyImageUtls.getSmallBitmap(currentData.getFile()
//                    .getAbsolutePath(), 100, 100);
//            bitmapCache.put(currentData.getFile().getAbsolutePath(), newBitmap);
//            holoder.iv.setImageBitmap(newBitmap);
        } else {
            holoder.iv.setImageBitmap(bitmap);
        }
        holoder.iv.setOnClickListener(new View.OnClickListener() {
            //选择，则将图片变暗，反之则反之
            @Override
            public void onClick(View v) {
                // 已经选择过该图片
                if (selectedPath.contains(currentData.getFile().getAbsolutePath())) {
                    selectedPath.remove(currentData.getFile().getAbsolutePath());
                    for (int i = 0; i < mSelectedImage.size(); i++) {
                        if (mSelectedImage.get(i).getFile().getAbsolutePath().equals
                                (selectedPath)) {
                            mSelectedImage.remove(i);
                            return;
                        }
                    }
                    Log.e("zjy", "ImageGvAdapter->onClick(): select img==" +
                            currentData.getFile().getAbsolutePath());
                    holoder.iv.setColorFilter(null);
                } else
                // 未选择该图片
                {
                    selectedPath.add(currentData.getFile().getAbsolutePath());
                    mSelectedImage.add(currentData);
                    holoder.iv.setColorFilter(Color.parseColor("#77000000"));
                }

            }
        });

    }


    @Override
    protected MyBasedHolder getHolder() {
        return new PrinterHoloder();
    }

    private class PrinterHoloder extends MyBasedHolder {
        private ImageView iv;
    }

}
