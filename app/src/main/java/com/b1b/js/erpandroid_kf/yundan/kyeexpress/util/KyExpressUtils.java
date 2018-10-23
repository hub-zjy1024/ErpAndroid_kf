package com.b1b.js.erpandroid_kf.yundan.kyeexpress.util;

import android.util.Log;

import com.b1b.js.erpandroid_kf.yundan.kyeexpress.entity.YundanJson;
import com.b1b.js.erpandroid_kf.yundan.utils.Md5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import utils.net.MySSLProtocolSocketFactory;

/**
 Created by 张建宇 on 2017/11/6. */

public class KyExpressUtils {
    private static String kye = "10127";
    //private static String rootUrl = "http://testapi.ky-express.com/kyeopenapi";
    private static String rootUrl = "https://openapi.ky-express.com/kyeopenapi";
    private static String orderURL = rootUrl + "/CustomerWaybillPrint";
    private static String searchURL =rootUrl+ "/Find_WEB_LogisticsYD_Tracking_V2";
    private static String accesskey = "5BB18CC3DFB4D254A862DA066DE2DE43";
    private static String charSet = "utf-8";
    private static boolean DEBUG = false;

//    /**
//     客户编码(必)
//     */
//    public static String uuid = "01083729273";
//    /**
//     客户密码(必)
//     */
//    public static String key = "562ADCB11ED28887BD0F1A6E5E39E4A5";
    /**
     客户编码(必)
     */
    public static String uuid = "075517225569";
    /**
     客户密码(必)
     */
    public static String key = "2CD23B03D80B97CDE43E7904DACB6C6E";

    public static String sendPostRequest(YundanJson infos) throws IOException {
//        String json = buildJson(infos);
//        Log.e("zjy", "KyExpressUtils->sendPostRequest(): json==" + json);
        String strPV = "";
        Class cla = infos.getClass();
        Field[] fields = cla.getFields();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field temp = fields[i];
            if (temp.getName().equals("serialVersionUID") || temp.getName().equals("$change")) {
                continue;
            }
            String name = temp.getName();
            names.add(name);
        }
        Collections.sort(names);
        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<names.size();i++) {
            try {
                Field field = cla.getField(names.get(i));
                Object value = field.get(infos);
                String fvalue = "";
                if (value != null) {
                     fvalue = value.toString();
                }
                jsonObject.put(names.get(i), fvalue);
                if ("".equals(fvalue)) {
                    continue;
                }
                strPV += field.getName() + fvalue;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String json = jsonObject.toString();
        String accessToken = Md5.getMD5(accesskey + strPV);
        URL url = new URL(orderURL);
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MySSLProtocolSocketFactory.TrustAnyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("kye", kye);
        conn.setRequestProperty("access-token", accessToken);
        conn.setRequestProperty("Content-type", "application/json");
        conn.setConnectTimeout(30 * 1000);
        conn.setDoOutput(true);
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(json.getBytes(charSet));
        InputStream response = conn.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(response, charSet));
        String s = "";
        StringBuilder builder = new StringBuilder();
        for (; (s = read.readLine()) != null; ) {
            builder.append(s);
        }
        if (DEBUG) {
            Log.e("zjy", "KyExpressUtils->sendPostRequest(): json2==" + json);
            Log.e("zjy", "KyExpressUtils->sendPostRequest(): fv==" + strPV);
            Log.e("zjy", "KyExpressUtils->sendPostRequest(): toke1==" + accessToken);
            Log.e("zjy", "KyExpressUtils->sendPostRequest(): response==" + builder.toString());
        }
        return builder.toString();
    }

    public static String searchWuliuInfo(String yundanID) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("uuid", uuid);
            obj.put("key", key);
            obj.put("ydNumber", yundanID);
            String json = obj.toString();
            Log.e("zjy", "KyExpressUtils->searchWuliuInfo(): searchJson==" + json);
            String strKV = "key" + key + "uuid" + uuid + "ydNumber" + yundanID;
            Log.e("zjy", "KyExpressUtils->searchWuliuInfo(): strKV==" + strKV);
            URL url = new URL(searchURL);
            try {
                SSLContext sc  = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new MySSLProtocolSocketFactory.TrustAnyTrustManager()}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }catch (KeyManagementException e) {
                e.printStackTrace();
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("kye", kye);
            String accessToken = Md5.getMD5(accesskey + strKV);
            conn.setRequestProperty("access-token", accessToken);
            conn.setRequestProperty("Content-type", "application/json");
            conn.setConnectTimeout(30 * 1000);
            conn.setDoOutput(true);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(json.getBytes(charSet));
            InputStream response = conn.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(response, charSet));
            String s = "";
            StringBuilder builder = new StringBuilder();
            for (; (s = read.readLine()) != null; ) {
                builder.append(s);
            }
            Log.e("zjy", "KyExpressUtils->sendPostRequest(): response==" + builder.toString());
            return builder.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
