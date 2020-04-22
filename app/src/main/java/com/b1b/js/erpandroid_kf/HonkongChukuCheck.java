package com.b1b.js.erpandroid_kf;

import android.content.Intent;
import android.os.Bundle;

import com.b1b.js.erpandroid_kf.entity.CheckInfo;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;

/**
 * Created by 张建宇 on 2019/5/25.
 */
public class HonkongChukuCheck extends CheckActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void onItemClickMy(CheckInfo minfo) {
        Intent intent = new Intent(mContext, HongkongChukuTakpic.class);
        intent.putExtra(IntentKeys.key_pid, minfo.getPid());
        startActivity(intent);
    }

    @Override
    public String setTitle() {
        String tittle = getResString(R.string.menu_hk_check);
        return tittle;
    }

    @Override
    public void OnAutoGo(CheckInfo fInfo) {
        onItemClickMy(fInfo);
    }
}
