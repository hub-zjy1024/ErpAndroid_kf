package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.adapter.UploadPicAdapter;
import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.task.TaskManager;
import com.b1b.js.erpandroid_kf.task.UpLoadPicRunable;
import com.b1b.js.erpandroid_kf.task.UploadPicRunnable2;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import utils.FTPUtils;
import utils.FtpManager;
import utils.ImageWaterUtils;
import utils.MyImageUtls;
import utils.MyToast;
import utils.UploadUtils;
import utils.WebserviceUtils;
import zhy.imageloader.MyAdapter;
import zhy.imageloader.PickPicActivity;

public class ObtainPicFromPhone extends AppCompatActivity implements View.OnClickListener {

    protected Button btn_commit;
    private Button btn_commitOrigin;
    private ProgressDialog pd;
    private boolean isFirst;
    private int onclickPosition;
    protected GridView gv;
    private int currentIndex = 0;
    private final int PICUPLOAD_SUCCESS = 1;
    private final int PICLISTUPLOAD_SUCCESS = 4;
    private final int PICUPLOAD_ERROR = 2;
    private final int PIC_OOM = 3;
    private final int FTP_ERROR = 8;
    private final int PICUPLOAD_MULTI = 3;
    protected String pid;
    private static final Object lock = new Object();
    protected String failPid;
    private MaterialDialog resultDialog;
    protected Context mContext = ObtainPicFromPhone.this;
    //更新progressDialog
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICUPLOAD_SUCCESS:
                    showFinalDialog("上传成功");
                    int nfId = getIntent().getIntExtra("nfId", 0);
                    UploadPicInfo upInfo = uploadPicInfos.get(onclickPosition);
                    if (nfId != 0) {
                        String path = upInfo.getPath();
                        String name = path.substring(path.lastIndexOf("/") + 1);
                        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        nManager.cancel(nfId);
                    }
                    upInfo.setState("1");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case PICUPLOAD_ERROR:
                    showFinalDialog("上传失败");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case 6:
                    pd.setMessage("上传了" + (currentIndex + 1) + "/" + uploadPicInfos.size());
                    UploadPicInfo up1 = uploadPicInfos.get(currentIndex);
                    up1.setState("1");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case PIC_OOM:
                    showFinalDialog("上传失败,图片过大，超出可用内存");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case PICLISTUPLOAD_SUCCESS:
                    showFinalDialog("批量上传成功");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case FTP_ERROR:
                    MyToast.showToast(mContext, "连接ftp失败，请检查网络");
                    mGvAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    int count = 0;
    private String uploadResult = "";
    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int arg1 = msg.arg1;
            Object obj = msg.obj;
            UploadPicInfo now = uploadPicInfos.get(arg1);
            String err = "上传失败";;
            int size = (int) TaskManager.getInstance().getExecutor().getActiveCount();
            int empty = TaskManager.getInstance().getExecutor().getQueue().size();
            Log.e("zjy", "ObtainPicFromPhone->handleMessage(): queueSize==" + empty);
            Log.e("zjy", "ObtainPicFromPhone->handleMessage(): TaskCounts==" + size);
            switch (msg.what) {
                case PICUPLOAD_SUCCESS:
                    err = "上传成功";
                    uploadResult += "图片" + arg1 + ":" + err + "\n";
                    showFinalDialog(uploadResult);
                    UploadPicInfo upInfo = uploadPicInfos.get(arg1);
                    upInfo.setState("1");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case PICUPLOAD_ERROR:
                    if (obj != null) {
                        err = obj.toString();
                    }
                    uploadResult += "图片" + arg1 + ":" + err + "！！！\n";
                    break;
                case PICUPLOAD_MULTI:
                    now.setState("1");
                    showFinalDialog(err);
                    mGvAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    protected int cid;
    protected int did;

    protected void showFinalDialog(String message) {
        pd.cancel();
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    private EditText edName;
    protected List<UploadPicInfo> uploadPicInfos;
    protected UploadPicAdapter mGvAdapter;
    protected EditText edPid;
    protected String failPath;

    private SharedPreferences userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_view);
        isFirst = true;
        btn_commitOrigin = (Button) findViewById(R.id.review_getFromPhone);
        btn_commit = (Button) findViewById(R.id.review_commit);
        edName = (EditText) findViewById(R.id.review_name);
        edPid = (EditText) findViewById(R.id.review_pid);
        btn_commit.setOnClickListener(this);
        btn_commitOrigin.setOnClickListener(this);
        gv = (GridView) findViewById(R.id.review_gv);
        uploadPicInfos = new ArrayList<>();
        pd = new ProgressDialog(this);
        //初始化结果对话框
        resultDialog = new MaterialDialog(mContext);
        resultDialog.setTitle("提示");
        resultDialog.setPositiveButton("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
                finish();
            }
        });
        resultDialog.setCanceledOnTouchOutside(true);
        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        failPid = intent.getStringExtra("failPid");
        if (failPid != null) {
            edPid.setText(failPid);
        }
        if (pid != null) {
            edPid.setText(pid);
        }
        //初始化ftp
        userInfo= getSharedPreferences("UserInfo", 0);
        SharedPreferences sp = userInfo;
        cid = sp.getInt("cid", -1);
        did = sp.getInt("did", -1);
        mGvAdapter = new UploadPicAdapter(mContext, uploadPicInfos, new UploadPicAdapter.OnItemBtnClickListener() {
            @Override
            public void onClick(View v, final int position) {
                final UploadPicInfo uploadPicInfo = uploadPicInfos.get(position);
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(mContext, pid, 5))
                    return;
                if (uploadPicInfo.getState().equals("-1")) {
                    Button btn = (Button) v;
                    btn.setText("正在上传");
                    onclickPosition = position;
                    showProgressDialog();
                    nUpload(position, uploadPicInfo);
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            super.run();
//                            try {
//                                if (commitImage(uploadPicInfo, cid, did, pid)) {
//                                    handler.sendEmptyMessage(PICUPLOAD_SUCCESS);
//                                    MyApp.myLogger.writeInfo("obtainpic ok:" + pid);
//                                } else {
//                                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
//                                }
//                            } catch (OutOfMemoryError e) {
//                                handler.sendEmptyMessage(PIC_OOM);
//                                int[] wh = MyImageUtls.getBitmapWH(uploadPicInfo.getPath());
//                                MyApp.myLogger.writeError("obtainpic oom:" + wh[0] + "X" + wh[1]+"-memory:"+Runtime.getRuntime().freeMemory()+"/"+Runtime.getRuntime().maxMemory());
//                                e.printStackTrace();
//                            } catch (FileNotFoundException e) {
//                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
//                                e.printStackTrace();
//                            } catch (XmlPullParserException e) {
//                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                MyApp.myLogger.writeError("obtainpic IO:" + e.getMessage());
//                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
//                                e.printStackTrace();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();

                } else {
                    MyToast.showToast(mContext, "当前图片已经上传完成");
                }
            }
        });
        gv.setAdapter(mGvAdapter);
    }

    private void nUpload(final int position, final UploadPicInfo uploadPicInfo){
        final String intentFlag = getIntent().getStringExtra("flag");
        String insertPath = "";
        String remoteName = UploadUtils.getChukuRemoteName(pid) + ".jpg";
        String remotePath = "/" + UploadUtils.getCurrentDate() + "/";
        String mUrl = MyApp.ftpUrl;
        FTPUtils ftpUtil =null;
        UpLoadPicRunable runable = null;
        if (intentFlag != null && intentFlag.equals("caigou")) {
            mUrl = CaigouActivity.ftpAddress;
            ftpUtil = new FTPUtils(mUrl, CaigouActivity.username, CaigouActivity.password);
            remoteName = UploadUtils.createSCCGRemoteName(pid);
            remoteName = getRemarkName(remoteName, true);
            remotePath = UploadUtils.getCaigouRemoteDir(remoteName + ".jpg");
        }else {
            mUrl = MyApp.ftpUrl;
            ftpUtil = new FTPUtils(mUrl, FtpManager.ftpName,FtpManager.ftpPassword);
            remotePath = UploadUtils.getChukuRemotePath(pid);
        }
        if ("101".equals(MyApp.id)) {
            mUrl = FtpManager.mainAddress;
            remotePath = UploadUtils.getTestPath(pid);
            ftpUtil = new FTPUtils(mUrl, FtpManager.mainName, FtpManager.mainPwd);
        }
        insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
        runable = new UploadPicRunnable2(remotePath, insertPath, ftpUtil) {

            @Override
            public void onResult(int code, String err) {
                Message msg = nHandler.obtainMessage();
                msg.arg1 = position;
                if (code == SUCCESS) {
                    msg.what = PICUPLOAD_SUCCESS;
                } else {
                    msg.what = PICUPLOAD_ERROR;
                    msg.obj = err;
                }
                nHandler.sendMessage(msg);
            }

            @Override
            public boolean getInsertResult() throws Exception {
                String remoteName = getRemoteName();
                String insertPath = getInsertpath();
                Log.e("zjy", "ObtainPicFromPhone->getInsertResult(): insertpath==" + insertPath);
                if ("101".equals(MyApp.id)) {
                    return true;
                }
                String res = "";
                if ("caigou".equals(intentFlag)) {
                    res = setSSCGPicInfo("", cid,
                            did, Integer.parseInt(MyApp.id), pid, remoteName, insertPath, "SCCG");
                } else {
                    res = setInsertPicInfo("", cid,
                            did, Integer.parseInt(MyApp.id), pid, remoteName, insertPath, "CKTZ");
                }
                return res.equals("操作成功");
            }

            @Override
            public InputStream getInputStream() throws Exception {
                String fPath = uploadPicInfo.getPath();
                return getTransferedImg(fPath);
            }
        };
        TaskManager.getInstance().execute(runable);
    }

    public InputStream getTransferedImg(String filePath) throws IOException {
        String fPath = filePath;
        FileInputStream inputStream = new FileInputStream(fPath);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Bitmap waterBitmap = null;
        if (bitmap.getWidth() >= 1080 && bitmap.getHeight() > 1080) {
            waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
        } else {
            waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_small);
        }
        Bitmap textBitmap = ImageWaterUtils.drawTextToRightTop(mContext, bitmap, pid, (int) (bitmap
                .getWidth() * 0.015), Color.RED, 20, 20);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        Bitmap compressImage = ImageWaterUtils.createWaterMaskRightBottom(mContext, textBitmap,
                waterBitmap, 0, 0);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        MyImageUtls.compressBitmapAtsize(compressImage, bao, 0.4f);
        ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
        MyImageUtls.releaseBitmap(waterBitmap);
        MyImageUtls.releaseBitmap(textBitmap);
        MyImageUtls.releaseBitmap(compressImage);
        return bai;
    }


    public String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath,
                                   String stypeID) throws IOException, XmlPullParserException {
        String str = "";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("cid", cid);
        map.put("did", did);
        map.put("uid", uid);
        map.put("pid", pid);
        map.put("filename", fileName);
        map.put("filepath", filePath);
        map.put("stypeID", stypeID);//标记，固定为"CKTZ"
        SoapObject request = WebserviceUtils.getRequest(map, "SetInsertPicInfo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request,  WebserviceUtils
                .ChuKuServer);
        str = response.toString();
        Log.e("zjy", "ObtainPicFromPhone.java->setInsertPicInfo(): insertRes==" + str);
        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_commit:
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(mContext, pid, 5))
                    return;
                if (uploadPicInfos.size() == 0) {
                    MyToast.showToast(mContext, "请先添加一张图片");
                    return;
                }
                showProgressDialog();
                count = 0;
                uploadResult = "";
                for (int i = 0; i < uploadPicInfos.size(); i++) {
                    UploadPicInfo item = uploadPicInfos.get(i);
                    if (!item.getState().equals("1")) {
                        nUpload(i, item);
                    } else {
                        MyToast.showToast(mContext, "图片" + i + "已上传完成");
                        pd.cancel();
                    }
                }
//                new Thread() {
//                    @Override
//                    public void run() {
//                        if (commitImages(uploadPicInfos)) {
//                            handler.sendEmptyMessage(PICLISTUPLOAD_SUCCESS);
//                        }
//                    }
//                }.start();
                break;
            case R.id.review_getFromPhone:
                Intent intent = new Intent(mContext, PickPicActivity.class);
                startActivityForResult(intent, 100);
                break;
        }
    }

    /**
     @param uploadPicInfos
     @throws IOException
     @throws XmlPullParserException
     */
    private boolean commitImages(List<UploadPicInfo> uploadPicInfos) {
        boolean success = false;
        SharedPreferences sp = getSharedPreferences("UserInfo", 0);
        final int cid = sp.getInt("cid", -1);
        final int did = sp.getInt("did", -1);
        for (int i = 0; i < uploadPicInfos.size(); i++) {
            if (uploadPicInfos.get(i).getState().equals("-1")) {
                try {
                    if (commitImage(uploadPicInfos.get(i), cid, did, pid)) {
                        currentIndex = i;
                        handler.sendEmptyMessage(6);
                    } else {
                        handler.sendEmptyMessage(PICUPLOAD_ERROR);
                        return success;
                    }
                } catch (OutOfMemoryError e) {
                    handler.sendEmptyMessage(PIC_OOM);
                    int[] wh = MyImageUtls.getBitmapWH(uploadPicInfos.get(i).getPath());
                    MyApp.myLogger.writeError("obtainpic oom:" + wh[0] + "X" + wh[1]+"-memory:"+Runtime.getRuntime().freeMemory()+"/"+Runtime.getRuntime().maxMemory());
                    e.printStackTrace();
                    return success;
                } catch (IOException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    MyApp.myLogger.writeError("obtainpic IO:" + e.getMessage());
                    return success;
                } catch (XmlPullParserException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    return success;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        success = true;
        return success;
    }

    private boolean commitImage(UploadPicInfo uploadPicInfo, int cid, int did, String pid) throws Exception {
        InputStream inputStream = new FileInputStream(uploadPicInfo.getPath());
        boolean flag = false;
        String fileName = UploadUtils.getChukuRemoteName(pid);
        if (failPid != null) {
            //重新上传失败的文件
            fileName = failPath.substring(failPath.lastIndexOf("/") + 1, failPath.lastIndexOf("."));
            fileName = getRemarkName(fileName, false);
            flag = uploadFlag(cid, did, pid, inputStream, fileName);
        } else {
            fileName = getRemarkName(fileName, true);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap waterBitmap = null;
            if (bitmap != null) {
                if (bitmap.getWidth() > 1080 && bitmap.getHeight() > 1080) {
                    waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                } else {
                    waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_small);
                }
                Bitmap textBitmap = ImageWaterUtils.drawTextToRightTop(mContext, bitmap, pid, (int) (bitmap
                        .getWidth() * 0.015), Color.RED, 20, 20);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                Bitmap compressImage = ImageWaterUtils.createWaterMaskRightBottom(mContext, textBitmap,
                        waterBitmap, 0, 0);
                if (!waterBitmap.isRecycled()) {
                    waterBitmap.recycle();
                }
                if (!textBitmap.isRecycled()) {
                    textBitmap.recycle();
                }
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                MyImageUtls.compressBitmapAtsize(compressImage, bao, 0.4f);
                ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
                if (compressImage != null && !compressImage.isRecycled()) {
                    compressImage.recycle();
                }
                String intentFlag = getIntent().getStringExtra("flag");
                if (intentFlag != null && intentFlag.equals("caigou")) {
                    boolean isSuccess;
                    //文件名或者目录中有中文需要转码 new String(fileName.getBytes("UTF-8"), "iso-8859-1")
                    String insertPath;
                    String remoteName = UploadUtils.createSCCGRemoteName(pid);
                    FTPUtils ftpUtil = new FTPUtils( CaigouActivity.ftpAddress, 21, CaigouActivity
                            .username, CaigouActivity.password);
                    ftpUtil.login();
                    remoteName = getRemarkName(remoteName, false);
                    String remotePath = "";
                    if ("101".equals(MyApp.id)) {
                        remotePath = UploadUtils.CG_DIR + remoteName + ".jpg";
                    } else {
                        remotePath = UploadUtils.getCaigouRemoteDir(remoteName + ".jpg");
                    }
                    Log.e("zjy", "ObtainPicFromPhone->commitImage(): remote==" + remotePath);
                    isSuccess = ftpUtil.upload(bai, new String(remotePath.getBytes("UTF-8"), "iso-8859-1"));
                    bai.close();
                    ftpUtil.exitServer();
                    insertPath = UploadUtils.createInsertPath(CaigouActivity.ftpAddress, remotePath);
                    Log.e("zjy", "ObtainPicFromPhone->commitImage(): SCCGPATH==" + insertPath);
                    if (isSuccess) {
                        String result = setSSCGPicInfo(WebserviceUtils.WebServiceCheckWord, cid, did, Integer
                                .parseInt(MyApp.id), pid, remoteName + ".jpg", insertPath, "SCCG");
                        Log.e("zjy", "ObtainPicFromPhone.java->run(): SCCG==" + result);
                        return "操作成功".equals(result);
                    }
                } else {
                    flag = uploadFlag(cid, did, pid, bai, fileName);
                }
            }
        }
        return flag;
    }

    public synchronized static String setSSCGPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath,
                                 String stypeID) throws IOException, XmlPullParserException {
        String str = "";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("cid", cid);
        map.put("did", did);
        map.put("uid", uid);
        map.put("pid", pid);
        map.put("filename", fileName);
        map.put("filepath", filePath);
        map.put("stypeID", stypeID);//标记，固定为"CKTZ"
        SoapObject request = WebserviceUtils.getRequest(map, "InsertSSCGPicInfo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request,  WebserviceUtils
                    .MartStock);
            str = response.toString();
        return str;
    }

    private boolean uploadFlag(int cid, int did, String pid, InputStream inputStream, String fileName) throws IOException,
            XmlPullParserException {
        String insertPath;
        boolean isSuccess;
        //文件名或者目录中有中文需要转码 new String(fileName.getBytes("UTF-8"), "iso-8859-1")
        String remoteName =fileName + ".jpg";
        String remotePath = "/" + UploadUtils.getCurrentDate() + "/";
        String mUrl = MyApp.ftpUrl;
        FTPUtils ftpUtil =null;
        if ("101".equals(MyApp.id)) {
            mUrl = FtpManager.mainAddress;
            ftpUtil = new FTPUtils(mUrl, FtpManager.mainName,FtpManager.mainPwd);
            remotePath = UploadUtils.KF_DIR + remoteName ;
        } else {
            mUrl = MyApp.ftpUrl;
            ftpUtil = new FTPUtils(mUrl,FtpManager.ftpName,
                    FtpManager.ftpPassword);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/" + remoteName;
        }
        insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
        ftpUtil.login();
        isSuccess = ftpUtil.upload(inputStream, new String(remotePath.getBytes("UTF-8"), "iso-8859-1"));
        ftpUtil.exitServer();
        inputStream.close();
        Log.e("zjy", "ObtainPicFromPhone.java->commitImage(): uploadPath==" + insertPath);
        if (isSuccess) {
            String res = setInsertPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, remoteName, insertPath, "CKTZ");
            return "操作成功".equals(res);
        }
        return false;
    }

    @NonNull
    protected String getRemarkName(String fileName, boolean hasSuffix) {
        String name = fileName;
        String remark = edName.getText().toString().trim();
        //从手机取的图片，文件后缀加"_o"
        String suffix = "_o";
        if (hasSuffix) {
            if (!TextUtils.isEmpty(remark)) {
                name = fileName + "_" + remark + suffix;
            } else {
                name = fileName + suffix;
            }
        } else {
            if (!TextUtils.isEmpty(remark)) {
                name = fileName + "_" + remark;
            }
        }
        return name;
    }

    public void showProgressDialog() {
        pd.setMessage("正在上传");
        if (!pd.isShowing()) {
            pd.show();
        }
        pd.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 & resultCode == RESULT_OK) {
            ArrayList<String> returnPaths = data.getStringArrayListExtra("imgPaths");
            if (returnPaths.size() > 0) {
                btn_commit.setEnabled(true);
            }
            uploadPicInfos.clear();
            Log.e("zjy", "ObtainPicFromPhone.java->onActivityResult(): imgPaths==" + returnPaths.size());
            for (int i = 0; i < returnPaths.size(); i++) {
                UploadPicInfo info = new UploadPicInfo("-1", returnPaths.get(i));
                uploadPicInfos.add(info);
            }
            mGvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyAdapter.mSelectedImage.clear();
    }
}
