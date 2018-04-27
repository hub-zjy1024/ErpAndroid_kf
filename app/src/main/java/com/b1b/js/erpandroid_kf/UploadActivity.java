package com.b1b.js.erpandroid_kf;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.b1b.js.erpandroid_kf.service.PushService;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartSrv;
    private Button btnStopSrv;
    private Button btnBindSrv;
    private Button btnUnbindSrv;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("zjy", "UploadActivity.java->onServiceConnected(): onServiceConnected==");
            PushService.MyBinder mBinder = (PushService.MyBinder) service;
            mBinder.upLoad();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("zjy", "UploadActivity.java->onServiceDisconnected(): srv Disconnected==" + name.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        btnStartSrv = (Button) findViewById(R.id.activity_upload_start_srv);
        btnStopSrv = (Button) findViewById(R.id.activity_upload_stop_srv);
        btnBindSrv = (Button) findViewById(R.id.activity_upload_bind_srv);
        btnUnbindSrv = (Button) findViewById(R.id.activity_upload_unbind_srv);
        btnStartSrv.setOnClickListener(this);
        btnStopSrv.setOnClickListener(this);
        btnBindSrv.setOnClickListener(this);
        btnUnbindSrv.setOnClickListener(this);
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, 2000, 2000);
        Picasso.with(this).load("").into(new ImageButton(this));
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(UploadActivity.this, PushService.class);
        switch (v.getId()) {
            case R.id.activity_upload_start_srv:
                startService(intent);
                break;
            case R.id.activity_upload_stop_srv:
                stopService(intent);
                break;
            case R.id.activity_upload_bind_srv:
                bindService(intent, serviceConn, BIND_AUTO_CREATE);
                break;
            case R.id.activity_upload_unbind_srv:
                unbindService(serviceConn);
                break;
        }
    }
}
