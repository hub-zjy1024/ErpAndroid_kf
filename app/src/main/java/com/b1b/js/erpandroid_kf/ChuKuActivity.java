package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;

import java.util.ArrayList;
import java.util.List;

public class ChuKuActivity extends BaseScanActivity {
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
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        for (String tabTitle : tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(tabTitle));
        }
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

    @Override
    public void resultBack(String result) {
        super.resultBack(result);
        int currentItem = viewPager.getCurrentItem();
        Fragment fragMent = frags.get(currentItem);
        if (fragMent instanceof ChuKudanFragment) {
            ChuKudanFragment frag = ((ChuKudanFragment) fragMent);
            Button btnSearch = frag.getBtnSearch();
            EditText edPid = frag.getEdPid();
            edPid.setText(result);
            frag.onClick(btnSearch);
        }else if(fragMent instanceof ChuKuTongZhiFragment) {
            ChuKuTongZhiFragment frag = ((ChuKuTongZhiFragment) fragMent);
            Button btnSearch = frag.getBtnSearch();
            EditText edPid = frag.getEdPid();
            edPid.setText(result);
            frag.onClick(btnSearch);
        }
    }

    @Override
    public void getCameraScanResult(String result) {
        super.getCameraScanResult(result);
        int currentItem = viewPager.getCurrentItem();
        Fragment fragMent = frags.get(currentItem);
        if (fragMent instanceof ChuKudanFragment) {
            ChuKudanFragment frag = ((ChuKudanFragment) fragMent);
            Button btnSearch = frag.getBtnSearch();
            EditText edPid = frag.getEdPid();
            edPid.setText(result);
            frag.onClick(btnSearch);
        }else if(fragMent instanceof ChuKuTongZhiFragment) {
            ChuKuTongZhiFragment frag = ((ChuKuTongZhiFragment) fragMent);
            Button btnSearch = frag.getBtnSearch();
            EditText edPid = frag.getEdPid();
            edPid.setText(result);
            frag.onClick(btnSearch);
        }
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
