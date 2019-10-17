package com.b1b.js.erpandroid_kf;

import com.b1b.js.erpandroid_kf.picupload.PicUploader;
import com.b1b.js.erpandroid_kf.picupload.PrechukuUploader;

import utils.net.ftp.FTPUtils;

/**
 * Created by 张建宇 on 2019/8/1.
 */
public class YundanPicActivity extends ChukuTakePicActivity {

    @Override
    protected PicUploader getUpLoader() {
        return new PrechukuUploader(FTPUtils.mainAddress);
    }
}
