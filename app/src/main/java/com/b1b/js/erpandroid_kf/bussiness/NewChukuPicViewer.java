package com.b1b.js.erpandroid_kf.bussiness;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.entity.PankuLog;
import com.b1b.js.erpandroid_kf.entity.PankuMFC;
import com.b1b.js.erpandroid_kf.mvcontract.PankuDetailContract;
import com.b1b.js.erpandroid_kf.mvcontract.callback.DataObj;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.picupload.FtpUploader;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import utils.common.MyFileUtils;
import utils.net.ftp.FTPUtils;
import utils.net.http2.DyjInterface2;

/**
 * Created by 张建宇 on 2020/4/21.
 */

public class NewChukuPicViewer extends BottomPicViewer {
    public NewChukuPicViewer(Context mContext) {
        super(mContext);
    }

    private PankuDetailContract.IView mView;
    static class NewPicViewer implements PankuDetailContract.IView{

        @Override
        public void onImageRet(List<FTPImgInfo> list, int code, String msg) {

        }

        @Override
        public void onImageRet2(DataObj<List<FTPImgInfo>> mObj) {

        }

        @Override
        public void onRealInfoRet(PankuInfo minfo, int code, String msg) {

        }

        @Override
        public void onPankuRet(PankuInfo minfo, RetObject retObj) {

        }

        @Override
        public void onGetFactoryRet(List<PankuMFC> minfo, RetObject retObj) {

        }

        @Override
        public void onPankuLogRet(List<PankuLog> minfos, RetObject retObj) {

        }

        @Override
        public int loadingWithId(String msg) {
            return 0;
        }

        @Override
        public void updateDownProgress(int pIndex, String msg) {

        }

        @Override
        public void cancelLoading(int pIndex) {

        }

        @Override
        public void setPrinter(PankuDetailContract.Presenter presenter) {

        }
    }
    private Handler mHandler = new Handler();

    List<FTPImgInfo> getPicList(String pid) throws IOException {
        String errMsg = "";
        List<FTPImgInfo> mList = new ArrayList<>();
        String result = "";
        try {
            String mPics = DyjInterface2.GetBILL_Pictures(pid);
            result = mPics;
            JSONArray marr = com.alibaba.fastjson.JSONArray.parseArray(mPics);
            for (int i = 0; i < marr.size(); i++) {
                JSONObject tObj = marr.getJSONObject(i);
                String imgName = tObj.getString("pictureName");

                String dir = tObj.getString("dir");
                String PicUrl = tObj.getString("PicUrl");
                String imgUrl = "ftp://" + FTPUtils.mainAddress + "/" + dir + "/" + imgName;
                if (!"".equals(PicUrl) && PicUrl != null) {
                    imgUrl = PicUrl;
                }
                //                Log.e("zjy", getClass() + "->getPicList(): ==" + imgUrl);
                File fileParent = MyFileUtils.getFileParent();
                File file = new File(fileParent, "dyj_img/" + imgName);
                String localPath = file.getAbsolutePath();
                FTPImgInfo fti = new FTPImgInfo();
                fti.setFtp(imgUrl);
                fti.setImgPath(localPath);
                mList.add(fti);
            }
            return mList;
        } catch (DyjInterface2.DyjException e) {
            e.printStackTrace();
            errMsg = e.getMessage();
        } catch (JSONException e) {
            e.printStackTrace();
            errMsg = "查询图片为空，json2异常";
        } catch (IOException e) {
            e.printStackTrace();
            errMsg = "查询图片，IO异常" + e.getMessage();
        }
        throw new IOException(errMsg + ",res=" + result);
    }

    void downLoadPIc(final int pIndex, List<FTPImgInfo> list) {
        int code = 1;
        final List<FTPImgInfo> foundPics = new ArrayList<>();
        final int totalPics = list.size();
        if (totalPics > 0) {
            code = 0;
        }
        String downloadResult = "";
        for (int i = 0; i < totalPics; i++) {
            try {
                FTPImgInfo fti = list.get(i);
                String localPath = fti.getImgPath();
                String imgUrl = fti.getFtp();
                File file = new File(localPath);
                //                            Log.e("zjy", "ViewPicByPidActivity->run(): mfile
                //                            .Len==" + file.getName() + "," + file.length());
                if (!file.exists() || file.length() == 0) {
                    Log.e("zjy", "PankuDetailContract->run(): prepard to DownLoad==" + file);
                    startDownLoad(imgUrl, localPath);
                } else {
                    downloadResult += "第" + (i + 1) + "张,已从手机找到\r\n";
                }
                foundPics.add(fti);
                final int finalI = i;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.updateDownProgress(pIndex,
                                "已下载," + finalI + "/" + totalPics +
                                        "张图片");
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                downloadResult += "第" + (i + 1) + "张,下载失败，" + e.getMessage() + "\r\n";
            }
        }
        if (foundPics.size() != totalPics) {
            code = 1;
        }
        final int finalCode = code;
        final List<FTPImgInfo> finalList1 = foundPics;
        final String finalDownloadResult = downloadResult;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //mView.onImageRet(finalList1, finalCode, finalDownloadResult);
                DataObj<List<FTPImgInfo>> retObj = new DataObj<>();
                retObj.mData = finalList1;
                retObj.errCode = finalCode;
                retObj.errMsg = finalDownloadResult;
                mView.onImageRet2(retObj);
                mView.cancelLoading(pIndex);
            }
        });
    }
    void startDownLoad(String url, String local) throws IOException {
        String imgUrl = url;
        String urlNoShema = imgUrl.substring("ftp://".length());
        int endIndex = urlNoShema.indexOf("/");
        String remoteAbsolutePath = urlNoShema.substring(endIndex);
        String imgFtp = urlNoShema.substring(0, endIndex);
        int index = imgFtp.indexOf(":");
        String finalHost = imgFtp;
        int port = 21;
        if (index != -1) {
            String tp = imgFtp.substring(index + 1);
            finalHost = imgFtp.substring(0, index);
            try {
                port = Integer.parseInt(tp);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        FtpUploader mUploader = new FtpUploader(finalHost);
        mUploader.download(url, local);
    }

    public void viewPic(final String pid) {
        int pdId = 0;
        BaseMActivity mActivity = null;
        if (mContext instanceof BaseMActivity) {
            mActivity = (BaseMActivity) mContext;
        }

        final BaseMActivity finalMActivity = mActivity;
        mView = new  NewPicViewer() {

            @Override
            public void onImageRet2(DataObj<List<FTPImgInfo>> mObj) {
                if (mObj.errCode == 0) {
                    reFreshImages(mObj.mData);
                } else {
                    if (mObj.mData.size() > 0) {
                        reFreshImages(mObj.mData);
                    }
                    if (finalMActivity != null) {
                        finalMActivity.showMsgDialog("图片下载结果:" + mObj.errMsg);
                    }
                }
            }

            @Override
            public void updateDownProgress(int pIndex, String msg) {
                if (finalMActivity == null) {
                    return ;
                }
                ProgressDialog dialogById = (ProgressDialog) finalMActivity.getDialogById(pIndex);
                dialogById.setMessage(msg);
            }

            @Override
            public void cancelLoading(int pIndex) {
                if (finalMActivity == null) {
                    return ;
                }
                finalMActivity.cancelDialogById(pIndex);
            }

            @Override
            public int loadingWithId(String msg) {
                if (finalMActivity == null) {
                    return 0;
                }
                return finalMActivity.showProgressWithID(msg);
            }
        };
        pdId = mView.loadingWithId("正在下载图片");
        final int finalPdId = pdId;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                List<FTPImgInfo> picList = null;
                try {
                    picList = getPicList(pid);
                    downLoadPIc(finalPdId, picList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        TaskManager.getInstance().execute(mRun);
    }
}
