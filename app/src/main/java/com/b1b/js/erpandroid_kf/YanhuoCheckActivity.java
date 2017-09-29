package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

import utils.DialogUtils;
import utils.MyToast;
import utils.SoftKeyboardUtils;
import utils.UploadUtils;
import utils.WebserviceUtils;

public class YanhuoCheckActivity extends AppCompatActivity {

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
                if (MyApp.id == null) {
                    MyToast.showToast(YanhuoCheckActivity.this, "登陆人为空，请重启");
                    return;
                }
                String content = getYanhuoStr(MyApp.id, "同意");
                Log.e("zjy", "YanhuoCheckActivity->onClick(): tv.Txt==" + tvPid.getText
                        ().toString());
                yanhuo(tvPid.getText().toString(), "等待入库", content);
            }
        });
        SoftKeyboardUtils.closeInputMethod(edNote, this);
//        btnOk.requestFocus();
        btnFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApp.id == null) {
                    MyToast.showToast(YanhuoCheckActivity.this, "登陆人为空，请重启");
                    return;
                }

                String note = edNote.getText().toString();
                if (note.equals("")) {
                    MyToast.showToast(YanhuoCheckActivity.this, "请输入不通过理由");
                    return;
                }
                String content = getYanhuoStr(MyApp.id, note);
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
                (YanhuoCheckActivity.this);
        builder.setItems(new String[]{"拍照", "从手机选择", "连拍"}, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(YanhuoCheckActivity.this,
                                TakePicActivity.class);
                        intent.putExtra("pid", pid);
                        intent.putExtra("flag", "caigou");
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(YanhuoCheckActivity.this,
                                ObtainPicFromPhone.class);
                        intent.putExtra("pid", pid);
                        intent.putExtra("flag", "caigou");
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(YanhuoCheckActivity.this,
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
                //                UpdateSSCSState
                final LinkedHashMap<String, Object> map = new LinkedHashMap<String,
                        Object>();
                map.put("pid", pid);
                map.put("state", state);
                map.put("chkNote", note);
                Log.e("zjy", "YanhuoCheckActivity->run(): checkNote==" + note);
                SoapObject req = WebserviceUtils.getRequest(map, "UpdateSSCSState");
                try {
                    SoapPrimitive res = WebserviceUtils.getSoapPrimitiveResponse(req,
                            SoapEnvelope.VER11, WebserviceUtils.MartService);
                    Log.e("zjy", "YanhuoCheckActivity->run(): reuslt==" + res.toString());
                    String result = res.toString();
                    if (result.equals("成功")) {
                        MyApp.myLogger.writeInfo("yanhuo-ok:" + pid + "\t" + state);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.getSpAlert(YanhuoCheckActivity.this,
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
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.getSpAlert(YanhuoCheckActivity.this,
                                        "验货失败！！！", "提示").show();
                                DialogUtils.dismissDialog(pd);
                            }
                        });
                    }
                } catch (IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.getSpAlert(YanhuoCheckActivity.this,
                                    "连接服务器失败！！！", "提示").show();
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
