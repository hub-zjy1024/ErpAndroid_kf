package com.b1b.js.erpandroid_kf.bussiness;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.b1b.js.erpandroid_kf.FileViewerActivity;
import com.b1b.js.erpandroid_kf.PankuDetailActivity;
import com.b1b.js.erpandroid_kf.PicDetailActivity;
import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张建宇 on 2020/4/20.
 */

public class PicViewer {

    protected Context mContext;

    public PicViewer(Context mContext) {
        this.mContext = mContext;
    }

    PankuDetailActivity.RvImageAdapter rvImageAdapter;
    List<FTPImgInfo> imgsData;
    PopupWindow mPicPopWindow;
    Dialog mPicDialog;
    View contentView;
    public void init() {
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

        AlertDialog.Builder mBUilder = new AlertDialog.Builder(mContext, R.style.dialog_bottom_full);
        mBUilder.setView(contentView);
        mBUilder.setTitle("查看图片");
        mBUilder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPicDialog != null) {
                    mPicDialog.cancel();
                }
            }
        });
        mPicDialog = mBUilder.create();
        Window window = mPicDialog.getWindow();      // 得到dialog的窗体
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
//        mPicDialog = dialog;
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
    public void show(){
        if (mPicDialog != null) {
            mPicDialog.show();
        }
    }

    /**
     * 获取View，findViewById
     * @param id
     * @param <T>
     * @return
     */
    public <T extends View> T getViewInContent(int id) {
        return (T) contentView.findViewById(id);
    }
    public View getContentView() {
        View mView=LayoutInflater.from(mContext).inflate(R.layout.popup_panku_detail_imgs, null);
        return mView;
    }

    public void reFreshImages(List<FTPImgInfo> retImages) {
        imgsData.clear();
        imgsData.addAll(retImages);
        rvImageAdapter.notifyDataSetChanged();
        show();
    }

    public void jumpPicDetail(FTPImgInfo item, int position, ArrayList<String> paths) {
        if (item != null) {
            Intent mIntent = new Intent(mContext, FileViewerActivity.class);
            mIntent.putExtra(PicDetailActivity.ex_Path, item.getImgPath());

            mIntent.putStringArrayListExtra(PicDetailActivity.ex_Paths, paths);
            mIntent.putExtra("pos", position);
            mContext.startActivity(mIntent);
        }
    }

}
