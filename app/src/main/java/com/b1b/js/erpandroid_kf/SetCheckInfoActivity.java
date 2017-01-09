package com.b1b.js.erpandroid_kf;

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

import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WcfUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class SetCheckInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv;

    private Button btnCommit;
    private Button btnFail;
    private EditText edInfo;
    private Button btnAddPhoto;
    String pid;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    MyToast.showToast(SetCheckInfoActivity.this, msg.obj.toString());
                    break;
                case 1:
                    MyToast.showToast(SetCheckInfoActivity.this,"当前网络质量太差");
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

        btnAddPhoto.setOnClickListener(this);
        btnCommit.setOnClickListener(this);
        btnFail.setOnClickListener(this);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        if (pid != null) {
            tv.setText("单据号：" + pid);
        }
    }
//    <xs:element minOccurs="0" name="checkWord" nillable="true" type="xs:string" />
//    <xs:element minOccurs="0" name="t" type="xs:int" />
//    <xs:element minOccurs="0" name="info" nillable="true" type="xs:string" />
//    <xs:element minOccurs="0" name="pid" nillable="true" type="xs:string" />
//    <xs:element minOccurs="0" name="tp" type="xs:int" />
//    <xs:element minOccurs="0" name="uname" nillable="true" type="xs:string" />
//    <xs:element minOccurs="0" name="uid" nillable="true" type="xs:string" />

    /**
     * @param t
     * @param info
     * @param pid
     * @param tp
     * @param uname 允许为""
     * @param uid
     * @throws IOException
     * @throws XmlPullParserException
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
        final SoapObject request = WcfUtils.getRequest(map, "GetSetCheckInfo");
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    SoapPrimitive response = WcfUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WcfUtils.ChuKuServer);
                    Log.e("zjy", "SetCheckInfoActivity.java->run(): response==" + response.toString());
                    Message msg = mHandler.obtainMessage();
                    msg.obj = response.toString();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(0);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }


    @Override
    public void onClick(View v) {
        String info = edInfo.getText().toString().trim();
        if (info.equals("")) {
            MyToast.showToast(SetCheckInfoActivity.this, "请输入审核信息");
            return;
        }
        switch (v.getId()) {
            case R.id.setcheckinfo_fail:
                try {
                    getSetCheckInfo(1, info, pid, 1, "", MyApp.id);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setcheckinfo_commit:
                try {
                    getSetCheckInfo(2, info, pid, 0, "", MyApp.id);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setcheckinfo_photo:
                Intent intent = new Intent(SetCheckInfoActivity.this, TakePicActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
                break;

        }
    }
}
