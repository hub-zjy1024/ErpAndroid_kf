package com.b1b.js.erpandroid_kf;

import com.b1b.js.erpandroid_kf.picupload.PicUploader;
import com.b1b.js.erpandroid_kf.picupload.TomcatTransferUploader;

import utils.common.UploadUtils;

/**
 * Created by 张建宇 on 2019/5/25.
 */
public class HongkongChukuTakpic extends TakePic2Ac {
    @Override
    protected PicUploader getUpLoader() {
        String sig = UploadUtils.getDeviceID(mContext);
        return new TomcatTransferUploader(sig);
    }
}
