package utils.net.http2;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.PrivilegedActionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import utils.common.MyFileUtils;
import utils.net.HttpUtils;

/**
 * Created by 张建宇 on 2019/7/16.
 */
public class DyjInterface2 {
    public static final String iKey = "10162105300";
        public static final String HOST = "http://210.51.190.36:810";
//    public static final String HOST = "http://192.168.10.117:8090/";

    public static class DyjException extends Exception {
        /**
         * Constructs a new exception with {@code null} as its detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         */
        public DyjException() {
        }

        /**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public DyjException(String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified detail message and
         * cause.  <p>Note that the detail message associated with
         * {@code cause} is <i>not</i> automatically incorporated in
         * this exception's detail message.
         *
         * @param message the detail message (which is saved for later retrieval
         *                by the {@link #getMessage()} method).
         * @param cause   the cause (which is saved for later retrieval by the
         *                {@link #getCause()} method).  (A <tt>null</tt> value is
         *                permitted, and indicates that the cause is nonexistent or
         *                unknown.)
         * @since 1.4
         */
        public DyjException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause and a detail
         * message of <tt>(cause==null ? null : cause.toString())</tt> (which
         * typically contains the class and detail message of <tt>cause</tt>).
         * This constructor is useful for exceptions that are little more than
         * wrappers for other throwables (for example, {@link
         * PrivilegedActionException}).
         *
         * @param cause the cause (which is saved for later retrieval by the
         *              {@link #getCause()} method).  (A <tt>null</tt> value is
         *              permitted, and indicates that the cause is nonexistent or
         *              unknown.)
         * @since 1.4
         */
        public DyjException(Throwable cause) {
            super(cause);
        }

    }

    ///YuChuKu/GetChuKuTongZhiInfoToString?pid=&key=
    //    {
    //        "status":"", "message":"", "isSuccess":true, "isPage":false, "pageInfo":{
    //        "counts":1, "pagesize":50, "pages":1, "currentpage":1
    //    },"list":[{
    //        "PID":1386778, "ID":"I", "OutStorageType":0, "CreateDate":"2019-07-16T14:21:53.217", "CorpID":
    //        6500, "DeptID":6534, "EmployeeID":2934, "StorageID":123, "ClientID":202775, "IsNewClient":
    //        "", "DestStorageID":0, "ConsignAddress":"北京市海淀区中关村大街32号新中发F4110", "PayCarriage":"收货方",
    // "Receiver":
    //        "A", "ZipAndPhone":"A", "InvoiceType":1, "ConsignDate":"2019-07-16T14:19:08.577",
    // "GatheringType":
    //        "现金", "GatheringBank":"", "State":"等待出库", "ApproveInfo":
    //        "总公司：苏海玲(2309)2019-7-16 14:23:59 \r此客户过期", "Note":"", "BuyCorpID":0, "BuyDeptID":0,
    // "BuyEmployeeID":
    //        0, "GeneragedBillID":"", "Killed":false, "BuyTotal":0.00, "SellTotal":2.50, "lvTotal":
    //        "100.00%", "InvoiceCorp":32, "SendPhone":"A", "SendAddress":"北京市海淀区彩和坊路10号1号楼503室",
    // "TaxLvTotal":
    //        "100.00%", "YunDanID":"", "ClientNote":"", "MakerID":2934, "MakerName":"王忠强", "FaHuoType":
    //        "客户自取", "DeptSell":false, "CancelSellID":0, "TakerID":0, "OneChecker":0, "SecondChecker":
    //        0, "YunDanPrint":"", "YunDanBackID":"", "SongHuoDan":"2019-7-16/1386778_142135133.doc",
    // "PhotoYunDan":
    //        0, "FaHuoKuQu":"主库区", "NeedDiaoBo":0, "IsXianHuoXianJie":true, "ClientFax":"", "ClientCountry":
    //        "", "SPCheck":null
    //    }]}

    public static String GetChKuTongZhiDetailInfoToString(String pid) throws DyjException, IOException {
        //        /YuChuKu/GetChKuTongZhiDetailInfoToString?pid=&key=

        String url = HOST + "/YuChuKu/GetChKuTongZhiDetailInfoToString?pid=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            //            Log.e("zjy", DyjInterface2.class.getClass() + "->GetChKuTongZhiDetailInfoToString
            //            (): ==" +
            //                    bodyString);

            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                String list = mobjJSONArray.toJSONString();
                return list;
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
            //            {
            //                "status":"400", "message":"Key参数为空", "isSuccess":false, "isPage":false,
            // "pageInfo":{
            //                "counts":0, "pagesize":50, "pages":1, "currentpage":1
            //            },"list":null
            //            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    public static String GetChuKuTongZhiInfoByPIDToString(String pid) throws DyjException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String sDate = "2019-05-13 09:30:11.6";
        try {
            Date mdate = sdf.parse(sDate);
            Log.d("zjy",
                    DyjInterface2.class.getClass() + "->GetChuKuTongZhiInfoByPIDToString(): data==" + mdate.toLocaleString());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String url = HOST + "/YuChuKu/GetChuKuTongZhiInfoByPIDToString?pid=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            //            Log.e("zjy", DyjInterface2.class.getClass() + "->GetChuKuTongZhiInfoByPIDToString
            //            (): ==" + bodyString);

            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                String list = mobjJSONArray.toJSONString();
                return list;
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
            //            {
            //                "PID":1362658, "制单人":"王雪", "单据类型":"正常销售", "出库库房":"北京中转库", "制单日期":
            //                "2019-05-13T09:30:11.6", "开票类型":"增值税票", "开票公司":"北京航天新兴科技开发有限责任公司",
            //                "发货类型":"自行拿货", "公司":
            //                "航天新兴分公司", "部门":"新威利龙直销部", "员工":"王雪", "备注":"", "型号":"127-1-0210-8002-310",
            //                "数量":2000, "进价":
            //                0.1221, "售价":0.1500, "厂家":"Nextron", "描述":"1021100907", "封装":"A", "明细备注":"",
            //                "客户":
            //                "北京和利康源医疗科技有限公司", "客户编码":"2615.84", "客户电话":"010 57637211", "合同编号":"111",
            //                "收款方式":"电汇", "收件人":
            //                "李敏", "收款日期":"2019-08-09T09:28:38", "调入库房":null, "运单状态":"", "运单号":"",
            //                "回单号":"", "出库结果":
            //                "已经扣税!付款凭证ID为:759090\r成功出库!生成的出库单PID为:1476721\r库房：姚振博(3702)2019-5-13 14:12:43
            //                " +
            //                        "\r二次复核:[3702]2019-05-13 10:10:04,通过 \\r总公司：苏海玲(2309)2019-5-13
            //                        9:36:35 \r此客户过期",
            //                        "需要调拨":
            //                0, "特殊审核":null
            //            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    /**
     * StateNow=1等待调拨
     * StateNow=2等待预出库
     * StateNow=3 一次复核
     * StateNow=4 二次复核
     * StateNow=5 等待特殊审批
     * StateNow=6 等待打印
     *
     * @param pid
     * @return
     * @throws DyjException
     * @throws IOException
     */
    public static String GetChuKuTongZhiInfoToString(String pid) throws DyjException, IOException {
        String url = HOST + "/YuChuKu/GetChuKuTongZhiInfoToString?pid=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->GetChuKuTongZhiInfoToString(): ==" + bodyString);

            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                String list = mobjJSONArray.toJSONString();
                return list;
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
            //            {
            //                "status":"400", "message":"Key参数为空", "isSuccess":false, "isPage":false,
            // "pageInfo":{
            //                "counts":0, "pagesize":50, "pages":1, "currentpage":1
            //            },"list":null
            //            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    /**
     * StateNow=1等待调拨
     * StateNow=2等待预出库
     * StateNow=3 一次复核
     * StateNow=4 二次复核
     * StateNow=5 等待特殊审批
     * StateNow=6 等待打印
     *
     * @param pid
     * @return
     * @throws DyjException
     * @throws IOException
     */
    public static String GetChuKuTongZhiInfoToString2ByIP(String pid, String ip) throws DyjException,
            IOException {
        String url = HOST + "/YuChuKu/GetChuKuTongZhiInfoByPIDToStrings?pid=%s&ip=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, URLEncoder.encode(ip, "utf-8"), iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy",
                    DyjInterface2.class.getClass() + "->GetChuKuTongZhiInfoByPIDToStrings(): ==" + bodyString);

            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                String list = mobjJSONArray.toJSONString();
                return bodyString;
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg + ",url=" + url);
            }
            //            {
            //                "status":"400", "message":"Key参数为空", "isSuccess":false, "isPage":false,
            // "pageInfo":{
            //                "counts":0, "pagesize":50, "pages":1, "currentpage":1
            //            },"list":null
            //            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    /**
     * StateNow=1等待调拨
     * StateNow=2等待预出库
     * StateNow=3 一次复核
     * StateNow=4 二次复核
     * StateNow=5 等待特殊审批
     * StateNow=6 等待打印
     *
     * @param pid
     * @return
     * @throws DyjException
     * @throws IOException
     */
    public static String GetChuKuTongZhiInfoToString2(String pid) throws DyjException,
            IOException {
        String url = HOST + "/YuChuKu/GetChuKuTongZhiInfoByPIDToStrings?pid=%s&ip=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy",
                    DyjInterface2.class.getClass() + "->GetChuKuTongZhiInfoByPIDToStrings(): ==" + bodyString);

            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                String list = mobjJSONArray.toJSONString();
                return bodyString;
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg + ",url=" + url);
            }
            //            {
            //                "status":"400", "message":"Key参数为空", "isSuccess":false, "isPage":false,
            // "pageInfo":{
            //                "counts":0, "pagesize":50, "pages":1, "currentpage":1
            //            },"list":null
            //            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    /**
     * 拍照上传图片
     *
     * @param pid
     * @param folder
     * @param flag
     * @param uid
     * @return
     * @throws DyjException
     * @throws IOException
     */
    public static boolean UpdateCKPhoto(String pid, String folder, String flag, String picname, String uid,
                                        String insertPath) throws DyjException,
            IOException {
        //        http://172.16.6.160:810/YuChuKu/UpdateCKPhoto?cktzID=&folder=&processID=&picName=&makerID
        //        =&key=
        //        http://172.16.6.160:810/YuChuKu/UpdateCKPhotos?cktzID=&folder=&processID=&picName
        //        =&makerID=&picUrl=&key=
        String url = HOST + "/YuChuKu/UpdateCKPhotos?cktzID=%s&folder=%s&processID=%s&picName=%s&makerID=%s" +
                "&picUrl=%s" +
                "&key=%s";
        Log.d("zjy", DyjInterface2.class + "->UpdateCKPhoto():insertPath ==" + insertPath);
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, folder, flag, picname, uid, URLEncoder.encode(insertPath, "utf-8"),
                iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->UpdateCKPhoto(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                //                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                //                String list = mobjJSONArray.toJSONString();
                return true;
            } else {
                String errrMsg = mobj.getString("message");
                Log.w("zjy", DyjInterface2.class.getClass() + "->UpdateCKPhoto():false ==" + url);

                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    public static boolean UpdateStoreChekerInfo(String pid, String uid, String uname, String flag) throws DyjException,
            IOException {
        //        /UpdateStoreChekerInfo?pid=&flag=&checker=&checkerName&key=
        String url = HOST + "/YuChuKu/UpdateStoreChekerInfo?pid=%s&checker=%s&checkerName=%s&flag=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, uid, URLEncoder.encode(uname, "utf-8"), flag, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->UpdateStoreChekerInfo(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                return true;
            } else {
                String errrMsg = mobj.getString("message");
                Log.w("zjy", DyjInterface2.class + "->UpdateStoreChekerInfo(): url==" + url);

                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage() + "," + url);
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    public static String SpCheckInfo(String pid, String userID) throws DyjException,
            IOException {
        String url = HOST + "/YuChuKu/SpCheckInfo?pid=%s&userID=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, userID, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->SpCheckInfo(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                return "1";
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    public static String GetphotosByCKTZIDToString(String pid) throws DyjException,
            IOException {
        String url = HOST + "/YuChuKu/GetphotosByCKTZIDToString?id=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        Log.d("zjy", DyjInterface2.class + "->GetphotosByCKTZIDToString(): url==" + url);

        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->GetphotosByCKTZIDToString(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                return mobjJSONArray.toJSONString();
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }

    }

    public static String GetBILL_ResourcePic(String pid) throws DyjException,
            IOException {
        String pics = GetBILL_PictureRelatenfoByIDToString(pid);

        com.alibaba.fastjson.JSONArray mArray = com.alibaba.fastjson.JSONArray.parseArray
                (pics);
        com.alibaba.fastjson.JSONArray picArray = new JSONArray();
        for (int i = 0; i < mArray.size(); i++) {
            JSONObject tObj = mArray.getJSONObject(i);
            String imgName = tObj.getString("PicName");
            if (MyFileUtils.isImage(imgName)) {
                picArray.add(tObj);
            }
        }
        return picArray.toJSONString();
    }

    public static String GetBILL_Pictures(String pid) throws DyjException,
            IOException {
        String pics = GetphotosByCKTZIDToString(pid);
        //        com.alibaba.fastjson.JSONArray mArray = JSONObject.parseObject(pics).getJSONArray("list");
        com.alibaba.fastjson.JSONArray mArray = com.alibaba.fastjson.JSONArray.parseArray
                (pics);
        com.alibaba.fastjson.JSONArray picArray = new JSONArray();
        for (int i = 0; i < mArray.size(); i++) {
            JSONObject tObj = mArray.getJSONObject(i);
            String imgName = tObj.getString("PicName");
            String dir = tObj.getString("Folder");
            String PicUrl = tObj.getString("PicUrl");
            JSONObject nObj = new JSONObject();
            nObj.put("pictureName", imgName);
            nObj.put("dir", dir);
            nObj.put("PicUrl", PicUrl);
            picArray.add(nObj);
        }
        return picArray.toJSONString();
    }

    public static String GetBILL_PictureRelatenfoByIDToString(String pid) throws DyjException,
            IOException {
        String url = HOST + "/YuChuKu/GetBILL_PictureRelatenfoByIDToString?pid=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy",
                    DyjInterface2.class.getClass() + "->GetBILL_PictureRelatenfoByIDToString(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                //                pictureName: "and_1387526_20190718102615_3366.jpg",
                //                        pictureURL: "ftp://192.168.9
                //                        .244/2019_7_18/and_1387526_20190718102615_3366.jpg"
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                return mobjJSONArray.toJSONString();
            } else {
                String errrMsg = mobj.getString("message");
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage());
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }

    public static boolean SetChuKuTongZhiChuKu(String pid, String uid, String uname, String flag) throws IOException, DyjException {
        //        SetChuKuTongZhiChuKu?pid=&userid=&userName&key=
        String url = HOST + "/YuChuKu/SetChuKuTongZhiChuKu?pid=%s&userid=%s&userName=%s&key=%s";
        String mName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        url = String.format(url, pid, uid, URLEncoder.encode(uname, "utf-8"), iKey);
        String bodyString = "";
        try {
            bodyString = HttpUtils.create(url).getBodyString();
            JSONObject mobj = JSONObject.parseObject(bodyString);
            Log.d("zjy", DyjInterface2.class.getClass() + "->UpdateStoreChekerInfo(): ==" + bodyString);
            boolean isSuccess = mobj.getBoolean("isSuccess");
            if (isSuccess) {
                JSONArray mobjJSONArray = mobj.getJSONArray("list");
                return true;
            } else {
                String errrMsg = mobj.getString("message");
                Log.w("zjy", DyjInterface2.class + "->UpdateStoreChekerInfo(): url==" + url);
                throw new DyjException(mName + ",接口异常," + errrMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("网络异常," + e.getMessage() + "," + url);
        } catch (JSONException e) {
            throw new IOException("数据格式异常,json=" + bodyString);
        }
    }
    //    GetBILL_PictureRelatenfoByIDToString
}
