package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.sunmi.scanner.ScanController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class HetongActivity extends BaseMActivity {

    private ScanController mScanner;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hetong);
    }

    @Override
    public void init() {
        mScanner = new ScanController(mContext, new ScanController.ScanListener() {
            @Override
            public void onScanResult(String code) {

            }
        });

        Button btn = getViewInContent(R.id.activity_hetong_btn_smscan);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mScanner.scan();
                } catch (Exception e) {
                    showMsgToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setListeners() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanner.release();
    }

    public void heTong() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String strUrl = "http://192.168.10.65:8080";
                String pid = "";
                String proFullName = "";
                // proFullName=new String(proFullName.getBytes("iso-8859-1"),"UTF-8");
                String hetongID = "";
                String proShortName = "";
                String proPhone = "";
                String proAddress = "";
                String proReceiveMan = "";
                String createDate = "";
                String goodInfos = "";
                try {
                    strUrl += "/PrinterServer/HetongServlet?";
                    strUrl += "pid=" + URLEncoder.encode(pid, "UTF-8");
                    strUrl += "&proShortName=" + URLEncoder.encode(proShortName, "UTF-8");
                    strUrl += "&proPhone=" + URLEncoder.encode(proPhone, "UTF-8");
                    strUrl += "&proAddress=" + URLEncoder.encode(proAddress, "UTF-8");
                    strUrl += "&proReceiveMan=" + URLEncoder.encode(proReceiveMan, "UTF-8");
                    strUrl += "&createDate=" + URLEncoder.encode(createDate, "UTF-8");
                    strUrl += "&goodInfos=" + URLEncoder.encode(goodInfos, "UTF-8");
                    URL url = new URL(strUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    InputStream response = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
                    String result = reader.readLine();
                    Log.e("zjy", "HetongActivity->run(): response==" + result);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
