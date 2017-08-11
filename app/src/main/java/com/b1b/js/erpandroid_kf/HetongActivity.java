package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class HetongActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hetong);

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
