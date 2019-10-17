package com.b1b.js.erpandroid_kf;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

import com.b1b.js.erpandroid_kf.adapter.Img2Adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张建宇 on 2019/8/29.
 */
public class PicDetailActivity2 extends PicDetailActivity {
    @Override
    public List<String> getPaths() {
        List<String> paths = new ArrayList<>();
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397591&di=a4f17e7e3d5de911b6d5fd7544d0a8d0&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201305%2F26%2F20130526140022_5fMJe.jpeg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397590&di=d58ffee2256a4c4abd7e45a405aec025&imgtype=0&src=http%3A%2F%2Fb.zol-img.com.cn%2Fsoft%2F6%2F571%2FcepyVKtIjudo6.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397590&di=8b22e020885376aaa6af59f6d63f5277&imgtype=0&src=http%3A%2F%2Fpic18.nipic.com%2F20111129%2F4155754_234055006000_2.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397590&di=960df0dac5182b30ed10fc20094490db&imgtype=0&src=http%3A%2F%2Fimg.sccnn.com%2Fbimg%2F337%2F31452.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397590&di=0d900756843597d49d7af01a6a9b14a8&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2Fbcaca7ee3601c6e24762946e9b9d313be5cd4369b0f1-CKjm9d_fw658");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397588&di=f9981d6c9810b0d294a1f137cc27100c&imgtype=0&src=http%3A%2F%2Fimg.sccnn.com%2Fbimg%2F337%2F23662.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397588&di=62b5ce4e9d72a81ffd8ba68549fd7034&imgtype=0&src=http%3A%2F%2Fpic1.nipic.com%2F2008-12-05%2F200812584425541_2.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397587&di=340f01cebd5afde8536f188a63a1914c&imgtype=0&src=http%3A%2F%2Fpic37.nipic.com%2F20140110%2F8821914_135241051000_2.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397587&di=7fc84a9593966ec3424ebec27956cd08&imgtype=0&src=http%3A%2F%2Fpic35.nipic.com%2F20131121%2F3822951_144045377000_2.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397587&di=c7e156b5aec9a6d9a87264efe17d2f6a&imgtype=0&src=http%3A%2F%2Fpic8.nipic.com%2F20100626%2F2476235_102059530423_2.jpg");
        paths.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567248397587&di=3e5f12b0382ee9289944a3d7b1bb7893&imgtype=0&src=http%3A%2F%2Fnews.mydrivers.com%2FImg%2F20110307%2F02425314.jpg");
        return paths;
    }

    @Override
    public PagerAdapter getPagerAdapter(List<String> paths) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        return new Img2Adapter(supportFragmentManager, paths);
    }
}
