package com.b1b.js.erpandroid_kf.bussiness;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.PankuDetailActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;

import java.util.ArrayList;

/**
 * Created by 张建宇 on 2020/4/20.
 */

public class BottomPicViewer extends PicViewer {
    public BottomPicViewer(Context mContext) {
        super(mContext);
    }

    @Override
    public View getContentView() {
        return LayoutInflater.from(mContext).inflate(R.layout.popup_panku_detail_botttom_sheet_dialog, null);
    }

    public void init() {
        Log.e("zjy", "BottomPicViewer->init(): ==");
        imgsData = new ArrayList<>();
        rvImageAdapter = new PankuDetailActivity.RvImageAdapter(imgsData, R.layout.item_panku_rv_gv_pics,
                mContext,
                new PankuDetailActivity.RvImageAdapter.ItemClickWrapperWithPosition<FTPImgInfo>() {
                    @Override
                    public void allClick2(View v, FTPImgInfo data, int poi) {
                        ArrayList<String> paths = new ArrayList<>();
                        for (int i = 0; i < imgsData.size(); i++) {
                            paths.add(imgsData.get(i).getImgPath());
                        }
                        jumpPicDetail(data, poi, paths);
                    }
                });
        contentView = getContentView();
        //        mPicPopWindow  = new PopupWindow(picContent, ViewGroup.LayoutParams.MATCH_PARENT,
        //        ViewGroup.LayoutParams.WRAP_CONTENT, true);
        BottomSheetDialog dialog = new BottomSheetDialog(mContext);

//        R.layout.design_bottom_sheet_dialog
        //setTitle无效
        dialog.setTitle("bottom sheet");
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true);

        mPicDialog = dialog;
        RecyclerView mPicView = contentView.findViewById(R.id.activity_panku_detail_rv_imgs);
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        mPicView.setLayoutManager(manager);
        mPicView.setAdapter(rvImageAdapter);

        Button closeBtn = contentView.findViewById(R.id.popup_panku_detail_imgs_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPicPopWindow.dismiss();
            }
        });
    }

    public void show() {
        if (mPicDialog != null) {
            mPicDialog.show();
            TextView mView = getViewInContent(R.id.activity_panku_detail_tv_pic_title);
            mView.setText("图片列表(" + imgsData.size() +
                    ")");
            int maxHeight = (int) mContext.getResources().getDimension(R.dimen.image_preview_height_w2);
            maxHeight = (int) (maxHeight * (2 + 0.2f));
            WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(metrics);
            maxHeight = (int) (metrics.heightPixels * 0.6f);

            final int height = maxHeight;
            final RecyclerView rvImages = getViewInContent(R.id.activity_panku_detail_rv_imgs);
            contentView.post(new Runnable() {
                @Override
                public void run() {
                    int nowHeight = rvImages.getHeight();
                    Log.e("zjy", getClass() +"->show  rvImages-nowHeight2 ==" + nowHeight);
                    if (nowHeight > height) {
                        rvImages.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                height));
                    }
                    rvImages.scrollToPosition(0);
                }
            });
        }
    }

}
