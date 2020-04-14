package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.entity.Scan2Info;
import com.b1b.js.erpandroid_kf.mvcontract.ScanCheckContract;
import com.b1b.js.erpandroid_kf.myview.ScanViewContainer;
import com.b1b.js.erpandroid_kf.scancode.zbar.view.MZbarScannerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;
import utils.framwork.MyDensityUtils;

public class Check2_scan_activity extends ToolbarHasSunmiActivity implements ScanCheckContract.IScanCheckView
        , View.OnClickListener, ZBarScannerView.ResultHandler {

    MZbarScannerView scanView;
    ScanViewContainer mScan3;
    ImageView showCropIV;
    RvAdapter adapter;
    private String pid;
    List<Scan2Info> datas;
    ScanCheckContract.Presenter mPresenter;
    RecyclerView recyclerView;
    public static SoundPool mSound;
    HashMap<String, String> map = new HashMap();
    TextView tvPid;

    Vibrator vibrator;
    private static final long VIBRATE_DURATION = 200L;
    private String uname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disabledToolbar();
        setContentView(R.layout.activity_check2_scan_activity);
        scanView = getViewInContent(R.id.m_scan);
        mScan3 = getViewInContent(R.id.m_scan3);
        if (mSound == null) {
            int maxStream = 10;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes =
                        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
                mSound =
                        new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(maxStream).build();
            } else {
                mSound = new SoundPool(maxStream, AudioManager.STREAM_MUSIC, 0);
            }
            initMusic();
        }
        showCropIV = getViewInContent(R.id.recorder_show);
        mScan3.setResultCallback(new ScanViewContainer.SimpleListener() {
            @Override
            public void getData(byte[] data) {
                if (isShot) {
                    isShot = false;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showCropIV.setImageBitmap(bitmap);
                        }
                    });
                }
            }

            @Override
            public void getCodeStr(String code) {
                if (pid == null) {
                    pid = code;
                    tvPid.setText("单号:" + pid);
                    mPresenter.getData(pid);
                    mPresenter.getPicInfos(pid);
                }
                onCodeCallback(code);
            }
        });

        tvPid = getViewInContent(R.id.activity_check2_pid);

        scanView.setResultHandler(this);
        setOnClickListener(this, R.id.activity_check2_caputure);

        recyclerView = getViewInContent(R.id.activity_check2_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        datas = new ArrayList<>();
        int initId = 123123;
        for (int i = 0; i < 10; i++) {
            Scan2Info info = new Scan2Info();
            String id = String.valueOf(initId + i);
            info.ID = id;
            info.Partno = "partno_" + i;
            datas.add(info);
        }
        adapter = new RvAdapter(datas, R.layout.item_scan_check, this);
        recyclerView.setAdapter(adapter);
        mPresenter = new ScanCheckContract.Presenter(this, this);
//        mPresent = new ParentChukuContract.Presenter(this, this);
        float screenHeight = MyDensityUtils.getScreenHeight(this);
        int mHeight = (int) (screenHeight / 3);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                mHeight));
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        pid = getIntent().getStringExtra(IntentKeys.key_pid);
        if ("debug".equals(BuildConfig.BUILD_TYPE)) {
            pid = "1387526";
        }
        if (pid != null) {
            tvPid.setText("单号:" + pid);
            mPresenter.getData(pid);
            mPresenter.getPicInfos(pid);
        }
        stopSunmiScan();
        SharedPreferences mPref = getSharedPreferences(SettingActivity.PREF_USERINFO, MODE_PRIVATE);
        uname = mPref.getString("oprName", "");
        View.OnClickListener allListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activity_check2_btn_takepic:
                        if (pid == null) {
                            showMsgToast("无单据号，请先返回上一层");
                            return;
                        }
                        Intent mIntent = new Intent(mContext, ChukuTakePicActivity.class);
                        mIntent.putExtra( IntentKeys.key_pid, pid);
                        startActivity(mIntent);
                        break;

                    case R.id.activity_check2_btn_check:
                        mPresenter.UpdateStoreChekerInfo(pid, loginID, "2", uname);
                        break;

                    case R.id.activity_check2_btn_viewpic:
                        Intent minte = new Intent(mContext, ViewPicByPidActivity.class);
                        minte.putExtra( IntentKeys.key_pid, pid);
                        startActivity(minte);
                        break;

                }
            }
        };
        setOnClickListener(allListner, R.id.activity_check2_btn_takepic);
        setOnClickListener(allListner, R.id.activity_check2_btn_check);
        setOnClickListener(allListner, R.id.activity_check2_btn_viewpic);
    }

    @Override
    public void onChangeSuccess(String flag) {
        showMsgDialog("复核完成");
    }

    public void onCodeCallback(String code) {
        String value = map.get(code);
        if (value == null) {
            int poi = adapter.selectItem(code);
            if (poi != -1) {
                recyclerView.scrollToPosition(poi);
                StartMusic(1);
                map.put(code, "1");
                flushFinished();
            }
        }
    }

    @Override
    public void resultBack(String result) {
        //        super.resultBack(result);
        onCodeCallback(result);
    }

    void flushFinished() {
        int fSize = 0;
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).isChecked) {
                fSize++;
            }
        }
        TextView tvStatus = getViewInContent(R.id.activity_check2_left_count);
        tvStatus.setText(String.format("对货进度：%d/%d", fSize, datas.size()));
    }

    /**
     * 播放MP3资源
     *
     * @param resId 资源ID
     */
    private void StartMusic(int resId) {
        /**
         * 第一个参数为播放音频ID
         * 第二个 第三个为音量
         * 第四个为优先级
         * 第五个为是否循环播放
         * 第六个设置播放速度
         * 返回值 不为0即代表成功
         */
        vibrator.vibrate(VIBRATE_DURATION);
        int type = mSound.play(resId, 1.0f, 1.0f, 1000, 0, 1);
    }

    private void initMusic() {
        //分别加入到SoundPool中
        //        mSound.load(this, R.raw.v_pop, 1);// 1
        mSound.load(this, R.raw.scanok, 1);// 1
    }

    @Override
    public void loading(String msg) {
        showProgress(msg);
    }

    @Override
    public int loading2(String msg) {
        return showProgressWithID(msg);
    }

    @Override
    public void cancel2(int id) {
        cancelDialogById(id);
    }

    @Override
    public void picInfoCallback(List<FTPImgInfo> infos) {
        Button tv = getViewInContent(R.id.activity_check2_btn_viewpic);
        tv.setText("查看图片(" +
                "" + infos.size() +
                ")");
    }

    @Override
    public void cancelLoading() {
        cancelProgress();
    }



    @Override
    public void fillList(List<Scan2Info> infos) {
        datas.clear();
        datas.addAll(infos);
        adapter.notifyDataSetChanged();
        map = new HashMap();
    }

    @Override
    public void alert(String msg) {
        showMsgDialog(msg);
    }

    /**
     * 当BaseView为Fragment时，在Activity中初始化Presenter，并传递到Fragment中，
     *
     * @param presenter
     */
    @Override
    public void setPrinter(ScanCheckContract.Presenter presenter) {

    }

    static class RvAdapter extends BaseRvAdapter<Scan2Info> {

        public RvAdapter(List<Scan2Info> mData, int layoutId, Context mContext) {
            super(mData, layoutId, mContext);
        }

        /**
         * 绑定数据
         *
         * @param holder {@link BaseRvViewholder};
         * @param item   item数据
         */
        @Override
        protected void convert(BaseRvViewholder holder, final Scan2Info item) {
            holder.setText(R.id.item_scan_check_tvpid, item.ID);
            holder.setText(R.id.item_scan_check_tv_parno, item.Partno);
            final CheckBox view = holder.getView(R.id.item_scan_check_cbo_check);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (view.isChecked()) {
                        item.isChecked = view.isChecked();
                        notifyDataSetChanged();
                    }
                }
            });
            view.setChecked(item.isChecked);
        }

        public int selectItem(String code) {
            for (int i = 0; i < mData.size(); i++) {
                Scan2Info item = mData.get(i);
                String id = item.ID;
                if (id.equals(code)) {
                    item.isChecked = true;
                    notifyDataSetChanged();
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScan3.startCamera();
        //        scanView.startCamera();
    }

    boolean isShot = false;

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("zjy", getClass() + "->onPause(): ==");
        scanView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScan3.stopCamera();
    }

    @Override
    public void setListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public String setTitle() {
        return "扫码对货";
    }

    /**
     * This method will be invoked when a menu item is clicked if the item itself did
     * not already handle the event.
     *
     * @param item {@link MenuItem} that was clicked
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.e("zjy", "check2Scan=" + String.format("type=%s value=%s,str=%s", rawResult
                .getBarcodeFormat()
                .getName(), rawResult.getContents())); // Prints scan results
        //        Log.e("zjy", getClass() + "->handleResult(): ==Scan RES=" + rawResult);
        scanView.resumeCameraPreview(this);
        scanView.stopCamera();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_check2_caputure:
                isShot = !isShot;
                break;
        }
    }
}
