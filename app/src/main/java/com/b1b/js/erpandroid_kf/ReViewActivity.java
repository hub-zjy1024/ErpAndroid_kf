package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.utils.FtpUpFile;
import com.b1b.js.erpandroid_kf.utils.MyToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReViewActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private Button btn_commit;
    private Button btn_commitOrigin;

    private String orginPath;
    private String afterPath;
    FtpUpFile ftp;
    private Bitmap bitmap;
    private ProgressDialog pd;
    File file;

    //更新progressDialog
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    pd.setMessage("上传成功");
                    handler.sendEmptyMessageDelayed(0, 1500);
                    break;
                case 0:
                    pd.dismiss();
                    break;
                case 3:
                    pd.dismiss();
                    MyToast.showToast(ReViewActivity.this, "上传失败");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_view);
        btn_commitOrigin = (Button) findViewById(R.id.review_commitorigin);
        btn_commit = (Button) findViewById(R.id.review_commit);
        btn_commit.setOnClickListener(this);
        btn_commitOrigin.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.review_iv);
        try {
            InputStream is = openFileInput("compress.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap == null) {
                MyToast.showToast(this, "加载出现问题，请返回重新拍摄");
                imageView.setImageResource(R.mipmap.imageerror);
            } else {
                imageView.setImageBitmap(bitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRomoteName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        return sdf.format(new Date());
    }

    @Override
    public void onClick(View v) {

        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle("提示");
        pd.setMessage("正在上传");
        pd.setCancelable(false);
        Log.e("zjy", "ReViewActivity.java->onClick(): date==" + getRomoteName());
        pd.show();

        switch (v.getId()) {
            case R.id.review_commit:
                try {
                    commit(openFileInput("compress.jpg"), getRomoteName(), "/test1");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.review_commitorigin:
                break;
        }
    }

    /**
     * @param is
     * @param remoteName 不带后缀名，默认后缀名为jpg
     * @param remoteDir
     */
    private void commit(final InputStream is, final String remoteName, final String remoteDir) {
        new Thread() {
            @Override
            public void run() {
                try {
                    ftp = FtpUpFile.getFtpUpFile("NEW_DYJ", "GY8Fy2Gx", "172.16.6.22", 21);
//                  ftp = new FtpUpFile("zjy", "123", "192.168.25.53", 21);
                    boolean isSuccess = ftp.upload(is, remoteDir, remoteName + ".jpg");
                    if (isSuccess) {
                        handler.sendEmptyMessage(1);
                    } else {
                        handler.sendEmptyMessage(3);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(3);

                }
            }
        }.start();
    }
}
