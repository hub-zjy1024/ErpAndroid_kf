package com.b1b.js.erpandroid_kf.mvcontract;

import android.content.Context;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;
import com.b1b.js.erpandroid_kf.entity.PicUploadInfo;
import com.b1b.js.erpandroid_kf.picupload.FtpUploader;
import com.b1b.js.erpandroid_kf.picupload.PicUploader;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import utils.dbutils.PicUploadDB;
import utils.net.ftp.FTPUtils;
import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2019/3/28.
 */
public class ReUploadContract {
    public interface IView extends BaseView<IPresent> {
        void uploadCounts(List<PicUploadInfo> infos);

        void onUpload(int index, PicUploadInfo info, String msg);

        void showProgress(String msg);

    }

    public interface IPresent {

        void getFailedImgInfo();

        void startUpload(List<PicUploadInfo> infos);

    }

    static class DataProvider extends ProVider {
        public DataProvider(Context mContext) {
            super(mContext);
        }

        @Override
        protected void upload(PicUploadInfo info) throws Exception {
            String locaPath = info.localfilepath;
            String rmPath = info.remotepath;
            String url = info.ftpurl;
            int okcount = info.okcount;
            String cid = info.cid;
            String did = info.did;
            String uid = info.loginID;
            String pid = info.pid;
            String fileName = info.remoteName;
            String filePath = info.insertPath;
            String stypeID = info.tag;
            Log.e("zjy", "ReUploadContract->upload(): startUploadPic==" + filePath);
            boolean upload = false;
            PicUploader mUploader = new FtpUploader(url);
            File f = new File(locaPath);
            if (!f.exists()) {
                throw new IOException("不存在文件，" + locaPath);
            }
            FileInputStream fis = new FileInputStream(f);
            mUploader.upload(pid, fis, rmPath, uid, cid, did, fileName, stypeID, filePath);
        }
    }

    public static class IPresentImpl2 extends IPresentImpl {
        DataProvider mProvider;
        public IPresentImpl2(Context mContext, IView mView) {
            super(mContext, mView);
            mProvider = new DataProvider(mContext);
        }

        @Override
        public void startUpload(final List<PicUploadInfo> infos) {
            mView.showProgress("开始上传,总数：" + infos.size());
            mProvider.startUpload(infos, new ProVider.UploadListner() {
                @Override
                public void callback(int index, PicUploadInfo info, String msg) {
                    String nowProcess = "进度:" + index + "/" + infos.size();
                    mView.showProgress("正在上传:" + nowProcess);
                    mView.onUpload(index, info, msg);
                }
            });
        }
    }
    public static class IPresentImpl implements IPresent {
        private Context mContext;
        IView mView;
        ProVider proVider;

        public IPresentImpl(Context mContext, IView mView) {
            this.mContext = mContext;
            this.mView = mView;
            proVider = new ProVider(mContext);
        }

        @Override
        public void getFailedImgInfo() {
            mView.showProgress("正在查询失败记录");
            proVider.getList(new ProVider.FailLisnter() {
                @Override
                public void callback(List<PicUploadInfo> infos) {
                    mView.uploadCounts(infos);
                }
            });
        }

        @Override
        public void startUpload(final List<PicUploadInfo> infos) {
            mView.showProgress("开始上传,总数：" + infos.size());
            proVider.startUpload(infos, new ProVider.UploadListner() {
                @Override
                public void callback(int index, PicUploadInfo info, String msg) {
                    String nowProcess = "进度:" + index + "/" + infos.size();
                    mView.showProgress("正在上传:" + nowProcess);
                    mView.onUpload(index, info, msg);
                }
            });
        }
    }

    static class ProVider {
        protected Context mContext;
        protected PicUploadDB picDb;
        protected android.os.Handler mHandler = new android.os.Handler();

        interface UploadListner {
            void callback(int index, PicUploadInfo info, String msg);
        }

        interface FailLisnter {
            void callback(List<PicUploadInfo> infos);
        }

        public ProVider(Context mContext) {
            this.mContext = mContext;
            picDb = new PicUploadDB(mContext);
        }

        public void startUpload(List<PicUploadInfo> infos, final UploadListner listner) {
            final int[] ok = {0};
            for (int i = 0; i < infos.size(); i++) {
                final int finalI = i;
                final PicUploadInfo picUploadInfo = infos.get(i);
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        String msg = "异常";
                        try {
                            upload(picUploadInfo);
                            msg = "成功";
                        } catch (Exception e) {
                            e.printStackTrace();
                            msg = "上传失败," + e.getMessage();
                        }
                        synchronized (ok) {
                            ok[0]++;
                            final String finalMsg = msg;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listner.callback(ok[0], picUploadInfo, finalMsg);
                                }
                            });
                        }
                    }
                };
                TaskManager.getInstance().execute(run, mContext);
            }
        }

        protected void upload(PicUploadInfo info) throws Exception {
            String locaPath = info.localfilepath;
            String rmPath = info.remotepath;
            String url = info.ftpurl;
            int okcount = info.okcount;
            String cid = info.cid;
            String did = info.did;
            String uid = info.loginID;
            String pid = info.pid;
            String fileName = info.remoteName;
            String filePath = info.insertPath;
            String stypeID = info.tag;
            Log.e("zjy", "ReUploadContract->upload(): startUploadPic==" + filePath);
            boolean upload = false;
            if (okcount == 0) {
                //没有上传图片
                FTPUtils mClient = FTPUtils.getFtpFromStr(url);
                mClient.login();
                FileInputStream fis = new FileInputStream(locaPath);
                upload = mClient.upload(fis, rmPath);
            } else {
                //上传完成，但是还未关联
                upload = true;
            }
            if (upload) {
                String res = "";
                try {
                    res = ChuKuServer.SetInsertPicInfo("", Integer.parseInt(cid), Integer.parseInt(did),
                            Integer.parseInt(uid), pid, fileName,
                            filePath, stypeID);
                    Log.e("zjy", "ReUploadContract->upload(): Relate==" + res);
                } catch (Exception e) {
                    throw new Exception("关联失败", e);
                }
                if (!res.equals("操作成功")) {
                    throw new Exception("关联失败");
                }
                int count = picDb.deleteData(locaPath);
                Log.e("zjy", "ReUploadContract->upload(): deletRecored==" + count);
                if (count != 1) {
                    MyApp.myLogger.writeError("删除pic上传记录失败");
                }
            } else {
                throw new Exception("上传失败");
            }
        }

        public void getList(final FailLisnter lisnter) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    final List<PicUploadInfo> allRecoder = picDb.getAllRecoder();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            lisnter.callback(allRecoder);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(run);
        }
    }

}
