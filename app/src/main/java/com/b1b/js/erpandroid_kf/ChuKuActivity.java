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

import com.b1b.js.erpandroid_kf.acview.CktzFragment;
import com.b1b.js.erpandroid_kf.contract.CkdContract;
import com.b1b.js.erpandroid_kf.contract.CktzContract;

import java.util.ArrayList;
import java.util.List;

public class ChuKuActivity extends SavedLoginInfoWithScanActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<ChukuBaseFragment> frags = new ArrayList<>();
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
        ChuKudanFragment fragChuku = new ChuKudanFragment();
        CkdContract.Presenter ckdP = new CkdContract.Presenter(fragChuku);
        fragChuku.addArgLoginId(loginID);
        //ChukuBaseFragment fragChukuTongzhi = new ChuKuTongZhiFragment();
        CktzFragment fragChukuTongzhi = new CktzFragment();
        CktzContract.IcktzPresenter ckPresenter = new CktzContract.CktzPresent(fragChukuTongzhi);
        fragChukuTongzhi.addArgLoginId(loginID);
        frags.add(fragChuku);
        frags.add(fragChukuTongzhi);
        fragAdapter = new MyPagerAdapter(fm, frags, tabTitles);
        viewPager.setAdapter(fragAdapter);
    }

    @Override
    public void resultBack(String result) {
        int currentItem = viewPager.getCurrentItem();
        ChukuBaseFragment fragMent = frags.get(currentItem);
        Button btnSearch = fragMent.getBtnSearch();
        EditText edPid = fragMent.getEdPid();
        edPid.setText(result);
        fragMent.onClick(btnSearch);
    }

    @Override
    public void getCameraScanResult(String result) {
        int currentItem = viewPager.getCurrentItem();
        ChukuBaseFragment fragMent = frags.get(currentItem);
        Button btnSearch = fragMent.getBtnSearch();
        EditText edPid = fragMent.getEdPid();
        edPid.setText(result);
        fragMent.onClick(btnSearch);
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        private List<ChukuBaseFragment> fragments;
        private String[] strings;

        public MyPagerAdapter(FragmentManager fm, List<ChukuBaseFragment> fragments, String[] strings) {
            super(fm);
            this.fragments = fragments;
            this.strings = strings;
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
