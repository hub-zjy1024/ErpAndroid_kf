package com.b1b.js.erpandroid_kf.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileViewerFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "mfilePath";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mfilePath;
    private String mParam2;

    public FileViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FileViewerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileViewerFragment newInstance(String param1, String param2) {
        FileViewerFragment fragment = new FileViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static FileViewerFragment newInstance(String mfilePath) {
        FileViewerFragment fragment = new FileViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mfilePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mfilePath = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_file_viewer, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view == null) {
            return;
        }
        Log.e("zjy", "FileViewerFragment->onActivityCreated(): ==" + mfilePath);

        final PDFView pdfView = view.findViewById(R.id.fileviwer_pdf);
        File file = new File(mfilePath);
        pdfView.fromFile(file) //设置pdf文件地址
                //设置翻页监听
                .showMinimap(true) //pdf放大的时候，是否在屏幕的右上角生成小地图
                // pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .enableSwipe(true)//是否允许翻页，默认是允许翻页
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        int pageCount = pdfView.getPageCount();
                        String msg = String.format("正在加载%s/%s", nbPages, pageCount);
                        Activity mAc = getActivity();
                        if (mAc instanceof BaseMActivity) {
                            BaseMActivity mActivity = (BaseMActivity) mAc;
                            mActivity.showMsgToast(msg);
                        }

                    }
                })//是否允许翻页，默认是允许翻页
                .load();
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}