package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.activity.base.SavedLoginInfoActivity;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import utils.net.wsdelegate.ChuKuServer;

public class SetCheckInfoActivity extends SavedLoginInfoActivity implements View.OnClickListener {

    private TextView tv;
    private Button btnCommit;
    private Button btnFail;
    private EditText edInfo;
    private Button btnAddPhoto;
    private String pid;
    private Button btnViewPic;
    private Handler mHandler = new Handler() ;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_check_info);
        tv = (TextView) findViewById(R.id.setcheckinfo_pid);
        btnCommit = (Button) findViewById(R.id.setcheckinfo_commit);
        btnFail = (Button) findViewById(R.id.setcheckinfo_fail);
        edInfo = (EditText) findViewById(R.id.setcheckinfo_ed_info);
        btnAddPhoto = (Button) findViewById(R.id.setcheckinfo_photo);
        btnViewPic = (Button) findViewById(R.id.setcheckinfo_viewpic);
        btnViewPic.setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);
        btnCommit.setOnClickListener(this);
        btnFail.setOnClickListener(this);
        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        if (pid != null) {
            tv.setText("单据号：" + pid);
        }
        mContext =this;
    }

    /**
     @param t
     @param info
     @param pid
     @param tp
     @param uname 允许为""
     @param uid
     @throws IOException
     @throws XmlPullParserException
     */
    public void getSetCheckInfo(final int t, final String info, final String pid, final int tp, final String uname, final String uid) throws IOException, XmlPullParserException {

        Runnable setCheckInfoThread = new Runnable() {
            @Override
            public void run() {
                try {
                    final String soapRes = ChuKuServer.GetSetCheckInfo("", t, info, pid, tp, uname, uid);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMsgToast( soapRes);
                        }
                    });
                } catch (IOException e) {
                    showError();
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    showError();
                    e.printStackTrace();
                }

            }
        };
        TaskManager.getInstance().execute(setCheckInfoThread, this);
    }

    public void showError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showMsgToast( getResources().getString(R.string.bad_connection));
            }
        });
    }

    @Override
    public void onClick(View v) {
        String info = edInfo.getText().toString().trim();

        switch (v.getId()) {
            case R.id.setcheckinfo_fail:
                try {
                    if ("".equals(info)) {
                        showMsgToast( "请输入不通过原因");
                        return;
                    }
                    getSetCheckInfo(1, info, pid, 1, "", loginID);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setcheckinfo_commit:
                try {
                    if ("".equals(info)) {
                        info = "通过";
                    }
                    getSetCheckInfo(2, info, pid, 0, "", loginID);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setcheckinfo_photo:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final Intent uploadIntent = new Intent();
                uploadIntent.putExtra("pid", pid);
                builder.setItems(getResources().getStringArray(R.array.upload_type), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                uploadIntent.setClass(mContext, TakePicActivity.class);
                                MyApp.myLogger.writeInfo("checkpage-take");
                                break;
                            case 1:
                                uploadIntent.setClass(mContext, ObtainPicFromPhone.class);
                                MyApp.myLogger.writeInfo("checkpage-obtain");
                                break;
                            case 2:
                                uploadIntent.setClass(mContext, TakePic2Activity.class);
                                MyApp.myLogger.writeInfo("checkpage-take2");
                                break;
                        }
                        startActivity(uploadIntent);
                    }
                });
//                builder.create().show();
                builder.setTitle("上传方式（设置中可配置默认）");
                Intent intent1 = new Intent();
                intent1.putExtra("pid", pid);
                SharedPreferences spTk = getSharedPreferences(SettingActivity.PREF_TKPIC, MODE_PRIVATE);
                String style = spTk.getString("style", "");
                if (style.equals(getResources().getString(R.string.upload_nomarl))) {
                    intent1.setClass(mContext, TakePicActivity.class);
                } else if (style.equals(getResources().getString(R.string.upload_fromphone))) {
                    intent1.setClass(mContext, ObtainPicFromPhone.class);
                } else if (style.equals(getResources().getString(R.string.upload_continue))) {
                    intent1.setClass(mContext, TakePic2Activity.class);
                } else {
                    builder.create().show();
                }
                if (intent1.getComponent() != null) {
                    startActivity(intent1);
                }
                break;
            case R.id.setcheckinfo_viewpic:
                Intent intent = new Intent(mContext, ViewPicByPidActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
                break;
        }
    }
}
