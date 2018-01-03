package com.android.scan;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dev.ScanBaseActivity;

public class MainActivity extends ScanBaseActivity {

    private static final String TAG = "BarcodeScan";

    private TextView mLog;
    private Button btn_clear;
    private Switch switchPower;
    private Switch switchSanMode;
    private Vibrator mvibrator;
    private boolean g_mvibrator = true;
    private boolean g_sound = true;
    private int test_ts = 0;
    private MediaPlayer mmediaplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);
        mLog = (TextView) findViewById(R.id.log);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        switchPower = (Switch) findViewById(R.id.PowerSwh);
        switchSanMode = (Switch) findViewById(R.id.ScanSwh);
        mvibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayerInit();
        btn_clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLog.setText("");
                setVibratortime(200);
            }
        });
        switchPower.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (scanTool == null) {
                    Toast.makeText(MainActivity.this, "请先按压扫码键,检测是否具有扫码功能", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isChecked) {
                    scanTool.open();
                } else {
                    scanTool.close();
                }

            }
        });
        switchSanMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (scanTool != null) {
                    scanTool.setScanMode(isChecked);
                }
            }
        });


    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_scan_main;
    }

    @Override
    public void resultBack(String result) {
        Log.e("zjy", "MainActivity->resultBack(): null==" + result);
        test_ts++;
        if (test_ts == 15) {
            mLog.setText("");
            test_ts = 0;
        }
        mLog.setText(mLog.getText() + result + "\n");
        setVibratortime(50);
        mediaPlayer();
    }

    private void mediaPlayerInit() {
        mmediaplayer = new MediaPlayer();
        mmediaplayer = MediaPlayer.create(this, R.raw.scanok);
        mmediaplayer.setLooping(false);
    }

    private void mediaPlayer() {
        if (g_sound) {
            mmediaplayer.start();
        }
    }

    private void mediaPlayerfinish() {
        mmediaplayer.stop();
        mmediaplayer.release();
    }

    private void setVibratortime(int times) {
        if (mvibrator.hasVibrator() && g_mvibrator)
            mvibrator.vibrate(times);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayerfinish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            //
            //		case R.id.action_sound:
            //			if (g_sound) {
            //				g_sound = false;
            //				item.setChecked(false);
            //			} else {
            //				g_sound = true;
            //				item.setChecked(true);
            //			}
            //			break;
            //		case R.id.action_Vibrator:
            //			if (g_mvibrator) {
            //				g_mvibrator = false;
            //				item.setChecked(false);
            //			} else {
            //				g_mvibrator = true;
            //				item.setChecked(true);
            //			}
            //
            //			break;
        }
        return true;
    }

}
