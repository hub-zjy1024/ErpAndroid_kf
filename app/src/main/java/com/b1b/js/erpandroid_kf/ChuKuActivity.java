package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.b1b.js.erpandroid_kf.entity.ChukuTongZhiInfo;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ChuKuActivity extends AppCompatActivity {
    private List<ChukuTongZhiInfo> data;
    private ListView listView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> frags = new ArrayList<>();
    private MyPagerAdapter fragAdapter;
    private FragmentManager fm;
    private FragmentTransaction fts;
    private String[] tabTitles = new String[]{"出库单", "出库通知"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chu_ku);
        listView = (ListView) findViewById(R.id.ck_lv);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("出库单"));
        tabLayout.addTab(tabLayout.newTab().setText("出库通知"));
        viewPager = (ViewPager) findViewById(R.id.ck_viewpager);
        fm = getSupportFragmentManager();
        tabLayout.setupWithViewPager(viewPager);
        Fragment fragChuku = new ChuKudanFragment();
        Fragment fragChukuTongzhi = new ChuKuTongZhiFragment();
        frags.add(fragChuku);
        frags.add(fragChukuTongzhi);
        fragAdapter = new MyPagerAdapter(fm, frags, tabTitles);
        viewPager.setAdapter(fragAdapter);
    }

    public static String getChukuTongZhiInfoList(String checkWord, String uid, String stime, String etime, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("uid", uid);
        properties.put("stime", stime);
        properties.put("etime", etime);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuTongZhiInfoList");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "ChuKuServer.svc");
        return response.toString();
    }

    public static String getGetChuKuInfoList(String checkWord, String uid, String stime, String etime, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("uid", uid);
        properties.put("stime", stime);
        properties.put("etime", etime);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuInfoList");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "ChuKuServer.svc");
        Log.e("zjy", "ChuKuActivity.java->GetChuKuInfoList(): re==" + response.toString());
        return response.toString();
    }

    public static String getChuKuInfo(String checkWord, String pid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("pid", pid);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuInfo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "ChuKuServer.svc");
        Log.e("zjy", "ChuKuActivity.java->GetChuKuInfo(): re==" + response.toString());
        return response.toString();
    }

    public static String getChuKuCheckInfoByTypeIDResult(String checkWord, int typeid, String pid, String partNo) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("checkWord", checkWord);
        properties.put("typeid", typeid);
        properties.put("pid", pid);
        properties.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(properties, "GetChuKuCheckInfoByTypeID");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, "ChuKuServer.svc");
        Log.e("zjy", "ChuKuActivity.java->GetChuKuCheckInfoByTypeID(): re==" + response.toString());
        return response.toString();
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private String[] strings;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments, String[] strings) {
            super(fm);
            this.fragments = fragments;
            this.strings = strings;
        }

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return strings[position];
        }

    }
}
