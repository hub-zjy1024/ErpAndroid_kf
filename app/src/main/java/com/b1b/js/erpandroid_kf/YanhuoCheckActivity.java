package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.common.UploadUtils;
import utils.framwork.DialogUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.net.wsdelegate.MartService;

public class YanhuoCheckActivity extends SavedLoginInfoActivity {

    private Handler mHandler = new Handler();
    private ProgressDialog pd;
    private TextView tvPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yanhuo_check);
        tvPid = (TextView) findViewById(R.id.activity_yanhuocheck_tvpid);
        final EditText edNote = (EditText) findViewById(R.id.activity_yanhuocheck_ed_mark);
        Button btnTakePic = (Button) findViewById(R.id.activity_yanhuocheck_btn_takepic);
        Button btnOk = (Button) findViewById(R.id.activity_yanhuocheck_btn_ok);
        Button btnFail = (Button) findViewById(R.id.activity_yanhuocheck_btn_fail);
        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在验货");
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginID == null) {
                    showMsgToast( "登陆人为空，请重启");
                    return;
                }
                String content = getYanhuoStr(loginID, "同意");
                Log.e("zjy", "YanhuoCheckActivity->onClick(): tvTotalCount.Txt==" + tvPid.getText
                        ().toString());
                yanhuo(tvPid.getText().toString(), "等待入库", content);
            }
        });
        SoftKeyboardUtils.closeInputMethod(edNote, this);
        btnFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginID == null) {
                    showMsgToast( "登陆人为空，请重启");
                    return;
                }

                String note = edNote.getText().toString();
                if (note.equals("")) {
                    showMsgToast( "请输入不通过理由");
                    return;
                }
                String content = getYanhuoStr(loginID, note);
                yanhuo(tvPid.getText().toString(), "未能入库", content);
            }
        });
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pid = tvPid.getText().toString();

                takePic(pid);
            }
        });
        Intent intent = getIntent();
        tvPid.setText(intent.getStringExtra("pid"));
    }

    public void takePic(final String pid) {
        AlertDialog.Builder builder = new AlertDialog.Builder
                (mContext);
        builder.setItems(new String[]{"拍照", "从手机选择", "连拍"}, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(mContext,
                                TakePicActivity.class);
                        intent.putExtra("pid", pid);
                        intent.putExtra("flag", "caigou");
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(mContext,
                                ObtainPicFromPhone.class);
                        intent.putExtra("pid", pid);
                        intent.putExtra("flag", "caigou");
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(mContext,
                                CaigouTakePic2Activity.class);
                        intent.putExtra("pid", pid);
                        intent.putExtra("flag", "caigou");
                        startActivity(intent);
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void yanhuo(final String pid, final String state, final String note) {

        pd.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    String result = MartService.UpdateSSCSState(pid, state, note);
                    Log.e("zjy", "YanhuoCheckActivity->run(): reuslt==" +result);
                    if (result.equals("成功")) {
                        MyApp.myLogger.writeInfo("yanhuo-ok:" + pid + "\t" + state);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.getSpAlert(mContext,
                                        "验货完成，是否拍照", "提示", new DialogInterface
                                                .OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                final String pid = tvPid.getText()
                                                        .toString();
                                                takePic(pid);
                                            }
                                        }, "是", null, "否").show();

                                DialogUtils.dismissDialog(pd);
                            }
                        });
                    } else {
                        throw new IOException("验货失败," + result);
                    }

                } catch (IOException e) {
                    final String message = e.getMessage();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String msg = "连接服务器失败！！！," +message;
                            showMsgDialog(msg);
                            DialogUtils.dismissDialog(pd);
                        }
                    });
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public String getYanhuoStr(String uid, String content) {
        String s = "库房：" + uid + "\t" + UploadUtils
                .getCurrentAtSS() + " " + content;

        return s;
    }
}
