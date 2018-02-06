package com.b1b.js.erpandroid_kf;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ChuKuActivity extends AppCompatActivity {
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
