package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.b1b.js.erpandroid_kf.entity.ChukuInfoNew;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.Scan2Info;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IBoolCallback;
import com.b1b.js.erpandroid_kf.mvcontract.callback.IDataListCallback;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.common.MyFileUtils;
import utils.net.http2.DyjInterface2;
import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2019/7/16.
 */
public class ScanCheckContract {
    public interface IScanCheckView extends BaseView<Presenter> {
        void fillList(List<Scan2Info> infos);

        void picInfoCallback(List<FTPImgInfo> infos);

        void loading(String msg);

        int loading2(String msg);

        void cancel2(int id);

        void cancelLoading();

        void alert(String msg);

        void onChangeSuccess(String flag);
    }

    public static class Presenter {
        IScanCheckView iView;
        Context mContext;

        DataProvider mProvider;

        @MainThread
        public Presenter(IScanCheckView iView, Context mContext) {
            this.iView = iView;
            this.mContext = mContext;
            mProvider = new DataProvider();
        }

        public void getPicInfos(final String pid) {
            final int pdId = iView.loading2("正在查询关联图片信息");
            mProvider.getPicInfos(pid, new IDataListCallback<FTPImgInfo>() {
                @Override
                public void callback(List<FTPImgInfo> msg) {
                    iView.cancel2(pdId);
                    iView.picInfoCallback(msg);
                }

                @Override
                public void onError(String msg) {
                    iView.cancel2(pdId);
                    iView.alert("查询不到" + pid + "关联图片，" + msg);
                }
            });

        }
        public void getData(final String pid) {
            final int pdId = iView.loading2("正在查询信息");
            mProvider.getData(pid, new DataProvider.IDataCallback() {
                @Override
                public void callback(final List<Scan2Info> minfos) {
                    iView.cancel2(pdId);
                    iView.fillList(minfos);
                }

                @Override
                public void onError(final String msg) {
                    iView.cancel2(pdId);
                    iView.alert("查询" + pid + "信息失败，" + msg);
                }
            });
        }
        public void UpdateStoreChekerInfo(final String pid,final String uid,  final String flag, String uname) {
            //            /UpdateStoreChekerInfo?pid=&flag=&checker=&checkerName&key=
            iView.loading("正在更新单据状态");
            mProvider.UpdateStoreChekerInfo(pid, uid,flag, uname,new IBoolCallback() {

                @Override
                public void callback(Boolean result) {
                    iView.cancelLoading();
                    iView.onChangeSuccess(flag);
                }

                @Override
                public void onError(String msg) {
                    iView.cancelLoading();
                    iView.alert("更新" + pid + "状态失败，" + msg);
                }
            });

        }
    }

    static class DataProvider {
        private Handler mHandler = new Handler();
        static abstract class IDataCallback implements IDataListCallback<Scan2Info> {
        }

        void getPicInfos(final String pid, final IDataListCallback<FTPImgInfo> callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<FTPImgInfo> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
                        //                        String dataJson = DyjInterface2.GetBILL_Pictures(pid);
                        //                        Log.e("zjy", getClass() + "->GetBILL_Pictures(): ==" +
                        //                        dataJson);
                        //                        com.alibaba.fastjson.JSONArray mArray = com.alibaba
                        //                        .fastjson.JSONArray.parseArray
                        //                                (dataJson);
                        String dataJson = ChuKuServer.GetBILL_PictureRelatenfoByID("", pid);
                        com.alibaba.fastjson.JSONArray mArray = JSONObject.parseObject(dataJson).getJSONArray(
                                "表");
                        for (int i = 0; i < mArray.size(); i++) {
                            JSONObject tObj = mArray.getJSONObject(i);
                            String imgName = tObj.getString("pictureName");
                            String imgUrl = tObj.getString("pictureURL");
                            String urlNoShema = imgUrl.substring("ftp://".length());
                            int endIndex = urlNoShema.indexOf("/");
                            String imgFtp = urlNoShema.substring(0, endIndex);
                            int index = imgFtp.indexOf(":");
                            String finalHost = imgFtp;
                            String remoteAbsolutePath = urlNoShema.substring(endIndex);
                            File fileParent = MyFileUtils.getFileParent();
                            File file = new File(fileParent, "dyj_img/" + imgName);
                            String localPath = file.getAbsolutePath();
                            FTPImgInfo info = new FTPImgInfo();
                            info.setImgName(imgName);
                            info.setFtp(finalHost);
                            info.setImgPath(localPath);
                            minfos.add(info);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(minfos);
                            }
                        });
                        return;
                        //                    } catch (DyjInterface2.DyjException e) {
                        //                        e.printStackTrace();
                        //                        errMsg = e.getMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errMsg = "数据格式有误，关联图片数量未知" + e.getMessage();
                    }
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(finalErrMsg);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }
        void getData(final String pid, final IDataCallback callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<Scan2Info> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
                        String dataJson = DyjInterface2.GetChKuTongZhiDetailInfoToString(pid);
                        com.alibaba.fastjson.JSONArray mArray = com.alibaba.fastjson.JSONArray.parseArray
                                (dataJson);
                        List<Scan2Info> scan2Infos = JSONArray.parseArray(dataJson, Scan2Info.class);
                        minfos.addAll(scan2Infos);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(minfos);
                            }
                        });
                        return;
                    } catch (DyjInterface2.DyjException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    }
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(finalErrMsg);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }

        void UpdateStoreChekerInfo(final String pid, final String uid, final String flag, final String uname,
                                   final IBoolCallback callback) {
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    final List<ChukuInfoNew> minfos = new ArrayList<>();
                    String errMsg = "";
                    try {
                        final boolean dataJson = DyjInterface2.UpdateStoreChekerInfo(pid, uid, uname, flag);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(dataJson);
                            }
                        });
                        return;
                    } catch (DyjInterface2.DyjException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = e.getMessage();
                    }
                    final String finalErrMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(finalErrMsg);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }
    }

}


