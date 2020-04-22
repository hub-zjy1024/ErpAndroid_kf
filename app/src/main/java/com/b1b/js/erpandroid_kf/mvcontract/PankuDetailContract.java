package com.b1b.js.erpandroid_kf.mvcontract;

import android.os.Handler;
import android.util.Log;

import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.PankuInfo;
import com.b1b.js.erpandroid_kf.entity.PankuLog;
import com.b1b.js.erpandroid_kf.entity.PankuMFC;
import com.b1b.js.erpandroid_kf.mvcontract.callback.DataObj;
import com.b1b.js.erpandroid_kf.mvcontract.callback.RetObject;
import com.b1b.js.erpandroid_kf.picupload.FtpUploader;
import com.b1b.js.erpandroid_kf.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import utils.common.MyFileUtils;
import utils.net.wsdelegate.ChuKuServer;

/**
 * Created by 张建宇 on 2020/4/7.
 */
public class PankuDetailContract {

    public interface IView extends BaseView<Presenter> {

        public void onImageRet(List<FTPImgInfo> list, int code, String msg);

        public void onImageRet2(DataObj<List<FTPImgInfo>> mObj);

        public void onRealInfoRet(PankuInfo minfo, int code, String msg);

        public void onPankuRet(PankuInfo minfo, RetObject retObj);

        public void onGetFactoryRet(List<PankuMFC> minfo, RetObject retObj);

        public void onPankuLogRet(List<PankuLog> minfos, RetObject retObj);

        int loadingWithId(String msg);

        void updateDownProgress(int pIndex, String msg);

        void cancelLoading(int pIndex);
    }

    public static class Presenter {
        IView mView;
        Handler mHandler = new android.os.Handler();

        public Presenter(IView mView) {
            this.mView = mView;
        }


        public void startPk(final String pkPartNo, final PankuInfo info, final String minpack,
                            final String PKQuantity, final String PKmfc, final String PKDescription,
                            final String PKPack, final String PKBatchNo, final String Note,
                            final String PKPlace, final int OperID, final String OperName,
                            final String DiskID) {
            final int pIndex = mView.loadingWithId("正在盘库中...");
            Runnable panKuRunnable = new Runnable() {
                @Override
                public void run() {


                    final RetObject mRet = new RetObject();
                    try {
                        int MinPack = 0;
                        if (!minpack.equals("")) {
                            MinPack = Integer.valueOf(minpack);
                        }
                        String detailId = info.getDetailId();
                   /* String soapRes = ChuKuServer.PanKu(InstorageDetailID, OldPartNo, OldQuantity,
                   PKPartNo, PKQuantity,
                            PKmfc, PKDescription, PKPack, PKBatchNo, MinPack, OperID, OperName, DiskID,
                            Note, PKPlace);
*/
                        String result = ChuKuServer.PanKu(Integer.parseInt(detailId), info
                                        .getPartNo(), Integer
                                        .parseInt(info.getLeftCounts()), pkPartNo, PKQuantity, PKmfc,
                                PKDescription, PKPack,
                                PKBatchNo, MinPack, OperID, OperName, DiskID, Note, PKPlace);

                        if ("1".equals(result)) {
                            mRet.errCode = 0;
                            mRet.errMsg = "盘库成功";
                        } else {
                            mRet.errMsg = "盘库返回异常,ret=" + result;
                        }
                    } catch (NumberFormatException e) {
                        mRet.errMsg = "输入不合法，请输入数字";
                    } catch (IOException e) {
                        e.printStackTrace();
                        mRet.errMsg = "盘库失败(IO)," + e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        mRet.errMsg = "盘库失败(xml)," + e.getMessage();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.cancelLoading(pIndex);
                            mView.onPankuRet(null, mRet);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(panKuRunnable);
        }

        public void getFactoryList(final String itemName) {
            //            final int pIndex = mView.loadingWithId("获取盘库信息中...");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    final List<PankuMFC> infos = new ArrayList<>();
                    int code = 1;
                    try {
                        String s = ChuKuServer.GetMFCListInfo(itemName);
                        Log.e("zjy",
                                "PankuDetailContract->GetMFCListInfo dataLen==" + (s.getBytes().length / 1024f));
                        com.alibaba.fastjson.JSONObject root = com.alibaba.fastjson.JSONObject.parseObject(s);
                        com.alibaba.fastjson.JSONArray jsonArray = root.getJSONArray("表");
                        if (jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                com.alibaba.fastjson.JSONObject tempJ = jsonArray.getJSONObject(i);
                                PankuMFC mfc = tempJ.toJavaObject(PankuMFC.class);
                                infos.add(mfc);
                            }
                            code = 0;
                            errMsg = "成功";
                        } else {
                            errMsg = "返回数据为空";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = "IO," + e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = "xml," + e.getMessage();
                    } catch (com.alibaba.fastjson.JSONException e) {
                        e.printStackTrace();
                        errMsg = "返回json异常," + e.getMessage();
                    }
                    final int finalCode = code;
                    final String finalErrMsg = errMsg;
                    final RetObject retObj = new RetObject();
                    retObj.errCode = code;
                    retObj.errMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onGetFactoryRet(infos, retObj);
                        }
                    });

                }
            };
            TaskManager.getInstance().execute(mRun);
        }

        public void getPankuLog(final String tempPid, final String detailId) {
            //            final int pIndex = mView.loadingWithId("获取盘库信息中...");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    PankuInfo info = null;
                    int code = 1;
                    final List<PankuLog> minfos = new ArrayList<>();
                    try {
                        /*{
                            "ID":"73345", "InstorageDetailID":"429", "PanKuDate":
                            "2018/3/5 17:44:21", "OldPartNo":"OPA111AM", "OldQuantity":"83", "PKPartNo":
                            "OPA111AM", "PKQuantity":"83", "PKmfc":"BB", "PKDescription":"NEW-C", "PKPack":
                            "TO", "PKBatchNo":"98+", "PKPlace":"10251T10900", "MinPack":"0", "OperID":
                            "101", "OperName":"管理员", "DiskID":"", "Note":""
                        }*/
                        String s = ChuKuServer.GetPanKuLog(detailId);
                        //                        {"表":] }
                        JSONObject root = new JSONObject(s);
                        JSONArray jsonArray = root.getJSONArray("表");
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject tempJ = jsonArray.getJSONObject(i);
                                PankuLog pankuLog = new PankuLog();
                                //                            "PanKuDate": "2018/3/5 17:44:21",
                                //                            "OperID": "101",
                                //                                    "OperName": "管理员",
                                //                                pankuLog.oprId = tempJ.getString("OperID");
                                //                                pankuLog.oprName = tempJ.getString
                                //                                ("OperName");
                                //                                pankuLog.panKuDate = tempJ.getString
                                //                                ("PanKuDate");
                                pankuLog.oprId = tempJ.getString("OperID");
                                pankuLog.oprName = tempJ.getString("盘库人");
                                pankuLog.panKuDate = tempJ.getString("盘库日期");
                                minfos.add(pankuLog);
                            }
                            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                            Collections.sort(minfos, new Comparator<PankuLog>() {
                                @Override
                                public int compare(PankuLog o1, PankuLog o2) {
                                    String d1 = o1.panKuDate;
                                    String d2 = o2.panKuDate;
                                    try {
                                        Date rd1 = sdf.parse(d1);
                                        Date rd2 = sdf.parse(d2);
                                        if (rd1.getTime() > rd2.getTime()) {
                                            return 1;
                                        } else {
                                            return -1;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 0;
                                }
                            });
                        }
                        code = 0;
                        errMsg = "成功";
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = "获取盘库日志失败," + e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = "获取盘库日志失败," + e.getMessage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //                        errMsg = "获取盘库日志失败," + e.getMessage();
                        errMsg = "盘库日志为空";
                    }
                    final int finalCode = code;
                    final String finalErrMsg = errMsg;
                    final PankuInfo finalInfo = info;
                    final RetObject mObj = new RetObject();
                    mObj.errCode = code;
                    mObj.errMsg = errMsg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onPankuLogRet(minfos, mObj);
                        }
                    });
                }
            };
            TaskManager.getInstance().execute(mRun);
        }

        public void getRealInfo(final PankuInfo item) {
            final int pIndex = mView.loadingWithId("获取盘库信息中...");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    PankuInfo info = null;
                    int code = 1;
                    try {
                        String s = ChuKuServer.GetPauKuDataInfoByID(item.getHasFlag());
                        String tempPid = item.getPid();
                        JSONObject root = new JSONObject(s);
                        JSONArray jsonArray = root.getJSONArray("表");
                        if (jsonArray.length() > 0) {
                            JSONObject tempJ = jsonArray.getJSONObject(0);
                            String detailId = tempJ.getString("InstorageDetailID");
                            String PKPartNo = tempJ.getString("PKPartNo");
                            String PKQuantity = tempJ.getString("PKQuantity");
                            String PKmfc = tempJ.getString("PKmfc");
                            String PKDescription = tempJ.getString("PKDescription");
                            String PKPack = tempJ.getString("PKPack");
                            String PKBatchNo = tempJ.getString("PKBatchNo");
                            String MinPack = tempJ.getString("MinPack");
                            String Mark = tempJ.getString("Note");
                            String PKPlace = tempJ.getString("PKPlace");
                            String flag = tempJ.getString("ID");
                            info = new PankuInfo(tempPid, detailId, PKPartNo, PKQuantity, PKmfc,
                                    PKDescription, PKPack, PKBatchNo,
                                    PKPlace, "", "", flag);
                            info.setMinBz(MinPack);
                            info.setMark(Mark);
                         /*   Message message = mHandler.obtainMessage(GET_PANKUINFO, info);
                            mHandler.sendMessage(message);*/
                            code = 0;
                            errMsg = "成功";
                        } else {
                            errMsg = "返回数据为空";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errMsg = "IO," + e.getMessage();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        errMsg = "xml," + e.getMessage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errMsg = "返回json异常," + e.getMessage();
                    }
                    final int finalCode = code;
                    final String finalErrMsg = errMsg;
                    final PankuInfo finalInfo = info;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onRealInfoRet(finalInfo, finalCode, finalErrMsg);
                            mView.cancelLoading(pIndex);
                        }
                    });

                }
            };
            TaskManager.getInstance().execute(mRun);
        }

        public void getNormalInfo(final String detailId) {
            final int pIndex = mView.loadingWithId("获取盘库信息中...");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    String errMsg = "";
                    PankuInfo info = null;
                    int code = 1;
                    try {
                        String soapRes = ChuKuServer.GetDataListForPanKu(detailId, "");
                        JSONObject jObj = new JSONObject(soapRes);
                        JSONArray jsonArray = jObj.getJSONArray("表");
                        if (jsonArray.length() > 0) {
                            JSONObject tempJobj = jsonArray.getJSONObject(0);
                            String pid = tempJobj.getString("单据号");
                            String mxId = tempJobj.getString("明细ID");
                            String sPartno = tempJobj.getString("型号");
                            String leftCounts = tempJobj.getString("剩余数量");
                            String factory = tempJobj.getString("厂家");
                            String description = tempJobj.getString("描述");
                            String fengzhuang = tempJobj.getString("封装");
                            String pihao = tempJobj.getString("批号");
                            String placeId = tempJobj.getString("位置");
                            String rkDate = tempJobj.getString("入库日期");
                            String storageName = tempJobj.getString("仓库");
                            String pkFlag = tempJobj.getString("PanKuFlag");
                            PankuInfo pkInfo = new PankuInfo(pid, mxId, sPartno, leftCounts, factory,
                                    description,
                                    fengzhuang, pihao, placeId,
                                    rkDate, storageName, pkFlag);
                            if (!"0".equals(pkFlag)) {
                                try {
                                    Log.e("zjy", "PankuDetailContract->run(): use detail==");
                                    String pkNow = ChuKuServer.GetPauKuDataInfoByID(pkFlag);
                                    String tempPid = pid;
                                    JSONObject root = new JSONObject(pkNow);
                                    JSONArray jsonArray2 = root.getJSONArray("表");
                                    if (jsonArray2.length() > 0) {
                                        JSONObject tempJ = jsonArray2.getJSONObject(0);
                                        String detailId = tempJ.getString("InstorageDetailID");
                                        String PKPartNo = tempJ.getString("PKPartNo");
                                        String PKQuantity = tempJ.getString("PKQuantity");
                                        String PKmfc = tempJ.getString("PKmfc");
                                        String PKDescription = tempJ.getString("PKDescription");
                                        String PKPack = tempJ.getString("PKPack");
                                        String PKBatchNo = tempJ.getString("PKBatchNo");
                                        String MinPack = tempJ.getString("MinPack");
                                        String Mark = tempJ.getString("Note");
                                        String PKPlace = tempJ.getString("PKPlace");
                                        String flag = tempJ.getString("ID");
                                        info = new PankuInfo(tempPid, detailId, PKPartNo, PKQuantity, PKmfc,
                                                PKDescription, PKPack, PKBatchNo,
                                                PKPlace, "", "", flag);
                                        info.setMinBz(MinPack);
                                        info.setMark(Mark);
                         /*   Message message = mHandler.obtainMessage(GET_PANKUINFO, info);
                            mHandler.sendMessage(message);*/
                                        code = 0;
                                        errMsg = "成功";
                                    } else {
                                        errMsg = "返回数据为空";
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    errMsg = "IO," + e.getMessage();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                    errMsg = "xml," + e.getMessage();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    errMsg = "返回json异常," + e.getMessage();
                                }
                            } else {
                                code = 0;
                                errMsg = "成功";
                                info = pkInfo;
                            }
                        }
                    } catch (Exception e) {
                        errMsg = exHandler(e, "获取盘库信息失败");
                    }
                    final int finalCode = code;
                    final String finalErrMsg = errMsg;
                    final PankuInfo finalInfo = info;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.onRealInfoRet(finalInfo, finalCode, finalErrMsg);
                            mView.cancelLoading(pIndex);
                        }
                    });

                }
            };
            TaskManager.getInstance().execute(mRun);
        }


        public String exHandler(Exception e, String prefix) {
            String errmsg = "";
            if (e != null) {
                if (e instanceof IOException) {
                    errmsg = "连接失败," + e.getMessage();
                }else if(e instanceof XmlPullParserException) {
                    errmsg = "接口解析异常," + e.getMessage();
                }else if(e instanceof JSONException) {
                    errmsg = "返回json异常," + e.getMessage();
                }else{
                    errmsg = "未知异常," + e.getMessage();
                }
            }
            errmsg = prefix + "," + errmsg;
            return errmsg;
        }
        public void getImages(final String pid) {
            final int pIndex = mView.loadingWithId("正在查询图片");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    int code = 1;
                    int downCounts = 0;
                    List<FTPImgInfo> list = new ArrayList<>();
                    String errmsg = "";
                    String downloadResult = "";
                    try {
                        list = getPicList(pid);
                        final List<FTPImgInfo> finalList = list;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.updateDownProgress(pIndex, "查找到" + finalList.size() + "张图片，开始下载");
                            }
                        });
                        downloadResult = "总共查询到" + list.size() + "张图片\r\n";
                    } catch (IOException e) {
                        e.printStackTrace();
                        errmsg = e.getMessage();
                        downloadResult = errmsg;
                    } catch (Exception e) {
                        errmsg = "其他," + e.getMessage();
                        e.printStackTrace();
                        downloadResult = errmsg;
                    }
                    final List<FTPImgInfo> foundPics = new ArrayList<>();
                    final int totalPics = list.size();
                    if (totalPics > 0) {
                        code = 0;
                    }
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
            };
            TaskManager.getInstance().execute(mRun);
        }

        List<FTPImgInfo> getPicList(String pid) throws IOException, JSONException {
            String errMsg = "";
            String result = "";
            try {
                result = ChuKuServer.GetBILL_PictureRelatenfoByID("", pid);
                JSONObject root = new JSONObject(result);
                final JSONArray array = root.getJSONArray("表");
                List<FTPImgInfo> list = new ArrayList<>();
                int searchSize = array.length();
                for (int i = 0; i < searchSize; i++) {
                    JSONObject tObj = array.getJSONObject(i);
                    String imgName = tObj.getString("pictureName");
                    String imgUrl = tObj.getString("pictureURL");
                    File fileParent = MyFileUtils.getFileParent();
                    File file = new File(fileParent, "dyj_img/" + imgName);
                    String localPath = file.getAbsolutePath();
                    FTPImgInfo fti = new FTPImgInfo();
                    fti.setFtp(imgUrl);
                    fti.setImgPath(localPath);
                    list.add(fti);
                }
                if (list.size() == 0) {
                    throw new IOException("图片数量为0");
                }
                return list;
            } catch (IOException e) {
                errMsg = "查询图片异常," + e.getMessage();
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                errMsg = "xml异常," + e.getMessage();
            } catch (JSONException e) {
                if ("{\"表\":] }".equals(result)) {
                    throw new IOException("图片数量为0");
                }
                errMsg = "json异常," + e.getMessage();
                e.printStackTrace();
            }
            throw new IOException(errMsg + ",res=" + result);
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
    }


}
