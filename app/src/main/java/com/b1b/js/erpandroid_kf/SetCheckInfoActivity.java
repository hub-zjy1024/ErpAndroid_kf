package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

import utils.MyToast;
import utils.WebserviceUtils;

public class SetCheckInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv;
    private Button btnCommit;
    private Button btnFail;
    private EditText edInfo;
    private Button btnAddPhoto;
    private String pid;
    private Button btnViewPic;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    MyToast.showToast(SetCheckInfoActivity.this, msg.obj.toString());
                    break;
                case 1:
                    MyToast.showToast(SetCheckInfoActivity.this, "当前网络质量太差");
                    break;
            }
        }
    };


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
    public void getSetCheckInfo(int t, String info, String pid, int tp, String uname, String uid) throws IOException, XmlPullParserException {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", "");
        map.put("t", t);
        map.put("info", info);
        map.put("pid", pid);
        map.put("tp", tp);
        map.put("uname", uname);
        map.put("uid", uid);
        final SoapObject request = WebserviceUtils.getRequest(map, "GetSetCheckInfo");
        Runnable setCheckInfoThread = new Runnable() {
            @Override
            public void run() {
                try {
                    SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request,
                            WebserviceUtils.ChuKuServer);
                    Log.e("zjy", "SetCheckInfoActivity.java->run(): response==" + response.toString());
                    Message msg = mHandler.obtainMessage();
                    msg.obj = response.toString();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }

            }
        };
        TaskManager.getInstance().execute(setCheckInfoThread, this);
    }


    @Override
    public void onClick(View v) {
        String info = edInfo.getText().toString().trim();

        switch (v.getId()) {
            case R.id.setcheckinfo_fail:
                try {
                    if ("".equals(info)) {
                        MyToast.showToast(SetCheckInfoActivity.this, "请输入不通过原因");
                        return;
                    }
                    getSetCheckInfo(1, info, pid, 1, "", MyApp.id);
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
                    getSetCheckInfo(2, info, pid, 0, "", MyApp.id);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setcheckinfo_photo:
                AlertDialog.Builder builder = new AlertDialog.Builder(SetCheckInfoActivity.this);
                builder.setItems(getResources().getStringArray(R.array.upload_type), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent1 = new Intent(SetCheckInfoActivity.this, TakePicActivity.class);
                                intent1.putExtra("pid", pid);
                                startActivity(intent1);
                                MyApp.myLogger.writeInfo("checkpage-take");
                                break;
                            case 1:
                                Intent intent2 = new Intent(SetCheckInfoActivity.this, ObtainPicFromPhone.class);
                                intent2.putExtra("pid", pid);
                                startActivity(intent2);
                                MyApp.myLogger.writeInfo("checkpage-obtain");

                                break;
                            case 2:
                                Intent intent3 = new Intent(SetCheckInfoActivity.this, TakePic2Activity.class);
                                intent3.putExtra("pid", pid);
                                startActivity(intent3);
                                MyApp.myLogger.writeInfo("checkpage-take2");
                                break;
                        }
                    }
                });
                builder.create().show();
                break;
            case R.id.setcheckinfo_viewpic:
                Intent intent = new Intent(SetCheckInfoActivity.this, ViewPicByPidActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
                break;
        }
    }
}
