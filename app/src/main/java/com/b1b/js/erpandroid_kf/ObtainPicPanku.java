package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.app.ProgressDialog;
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

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class ObtainPicPanku extends AppCompatActivity implements View.OnClickListener {

    private Button btn_commit;
    private Button btn_commitOrigin;
    private ProgressDialog pd;
    private boolean isFirst;
    private int onclickPosition;
    private GridView gv;
    private int currentIndex = 0;
    private final int PICUPLOAD_SUCCESS = 1;
    private final int PICLISTUPLOAD_SUCCESS = 4;
    private final int PICUPLOAD_ERROR = 2;
    private final int PIC_OOM = 3;
    private final int FTP_ERROR = 8;
    private String pid;
    private String failPid;
    private MaterialDialog resultDialog;
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
                        //                        NotificationCompat.Builder builder = new NotificationCompat.Builder
                        // (PankuObtain.this);
                        //                        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap
                        // .notify_icon_large);
                        //                        builder.setContentTitle("上传" + failPid + "图片");
                        //                        builder.setContentText(name + "上传成功").setSmallIcon(R.mipmap.notify_icon)
                        // .setLargeIcon(largeIcon);
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
                    MyToast.showToast(ObtainPicPanku.this, "连接ftp失败，请检查网络");
                    mGvAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void showFinalDialog(String message) {
        pd.cancel();
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    private EditText edName;
    private List<UploadPicInfo> uploadPicInfos;
    private UploadPicAdapter mGvAdapter;
    private EditText edPid;
    private String failPath;

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
        resultDialog = new MaterialDialog(ObtainPicPanku.this);
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
        failPath = intent.getStringExtra("failPath");
        if (failPath != null) {
            uploadPicInfos.add(new UploadPicInfo("-1", failPath));
        }
        if (uploadPicInfos.size() > 0) {
            btn_commit.setEnabled(true);
        }
        failPid = intent.getStringExtra("failPid");
        if (failPid != null) {
            edPid.setText(failPid);
        }
        if (pid != null) {
            edPid.setText(pid);
        }
        //初始化ftp
        mGvAdapter = new UploadPicAdapter(ObtainPicPanku.this, uploadPicInfos, new UploadPicAdapter.OnItemBtnClickListener() {
            @Override
            public void onClick(View v, int position) {
                final UploadPicInfo uploadPicInfo = uploadPicInfos.get(position);
                pid = edPid.getText().toString().trim();
                if (TakePicPankuActivity.checkPid(ObtainPicPanku.this, pid))
                    return;
                if (uploadPicInfo.getState().equals("-1")) {
                    Button btn = (Button) v;
                    btn.setText("正在上传");
                    onclickPosition = position;
                    SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                    final int cid = sp.getInt("cid", -1);
                    final int did = sp.getInt("did", -1);
                    showProgressDialog();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                if (commitImage(uploadPicInfo, cid, did, pid)) {
                                    handler.sendEmptyMessage(PICUPLOAD_SUCCESS);
                                } else {
                                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                                }
                            } catch (OutOfMemoryError e) {
                                handler.sendEmptyMessage(PIC_OOM);
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
                                e.printStackTrace();
                            } catch (IOException e) {
                                handler.sendEmptyMessage(PICUPLOAD_ERROR);
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } else {
                    MyToast.showToast(ObtainPicPanku.this, "当前图片已经上传完成");
                }
            }
        });
        gv.setAdapter(mGvAdapter);
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
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils
                .ChuKuServer);
        str = response.toString();
        Log.e("zjy", "PankuObtain.java->setInsertPicInfo(): insertRes==" + str);
        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_commit:
                pid = edPid.getText().toString().trim();
                if (TakePicPankuActivity.checkPid(ObtainPicPanku.this, pid))
                    return;
                showProgressDialog();
                new Thread() {
                    @Override
                    public void run() {
                        if (commitImages(uploadPicInfos)) {
                            handler.sendEmptyMessage(PICLISTUPLOAD_SUCCESS);
                        }
                    }
                }.start();
                break;
            case R.id.review_getFromPhone:
                Intent intent = new Intent(ObtainPicPanku.this, PickPicActivity.class);
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
                    e.printStackTrace();
                    return success;
                } catch (IOException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    return success;
                } catch (XmlPullParserException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    return success;
                }
            }
        }
        success = true;
        return success;
    }

    private boolean commitImage(UploadPicInfo uploadPicInfo, int cid, int did, String pid) throws IOException,
            XmlPullParserException {
        InputStream inputStream = new FileInputStream(uploadPicInfo.getPath());
        boolean flag = false;
        String fileName = UploadUtils.getPankuRemoteName(pid);
        if (failPid != null) {
            //重新上传失败的文件
            fileName = failPath.substring(failPath.lastIndexOf("/") + 1, failPath.lastIndexOf("."));
            Log.e("zjy", "PankuObtain->commitImage(): MyAppp.id==" + MyApp.id);
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
                Log.e("zjy", "PankuObtain.java->commitImages():image size ==" + bitmap.getWidth() + "\t" + bitmap
                        .getHeight());
                Bitmap textBitmap = ImageWaterUtils.drawTextToRightTop(ObtainPicPanku.this, bitmap, pid, (int) (bitmap
                        .getWidth() * 0.015), Color.RED, 20, 20);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                Bitmap compressImage = ImageWaterUtils.createWaterMaskRightBottom(ObtainPicPanku.this, textBitmap,
                        waterBitmap, 0, 0);
                if (!waterBitmap.isRecycled()) {
                    waterBitmap.recycle();
                }
                if (!textBitmap.isRecycled()) {
                    textBitmap.recycle();
                }
                ByteArrayInputStream bai = new ByteArrayInputStream(MyImageUtls.compressBitmapAtsize(compressImage, 0.4f));
                if (compressImage != null && !compressImage.isRecycled()) {
                    compressImage.recycle();
                }
                flag = uploadFlag(cid, did, pid, bai, fileName);
            }
        }
        return flag;
    }

    private boolean uploadFlag(int cid, int did, String pid, InputStream inputStream, String fileName) throws IOException,
            XmlPullParserException {
        String insertPath;
        boolean flag = false;
        boolean isSuccess;
        //文件名或者目录中有中文需要转码 new String(fileName.getBytes("UTF-8"), "iso-8859-1")
        String remoteName =  fileName + ".jpg";
        String remotePath = "/" + UploadUtils.getCurrentDate() + "/";
        String mUrl = MyApp.ftpUrl;
        FTPUtils ftpUtil =null;
        if ("101".equals(MyApp.id)) {
            mUrl = FtpManager.mainAddress;
            ftpUtil = new FTPUtils(mUrl, 21, FtpManager.mainName,FtpManager.mainPwd);
            //            mUrl= "192.168.10.65";
            //            ftpUtil=  new ftpUtil(mUrl, 21, "zjy", "123456");
            remotePath = UploadUtils.KF_DIR+ "pk/" +remoteName ;
        } else {
            mUrl = MyApp.ftpUrl;
            ftpUtil = new FTPUtils(mUrl, 21,FtpManager.ftpName,
                    FtpManager.ftpPassword);
            remotePath = "/" + UploadUtils.getCurrentDate() + "/pk/" + remoteName;
        }
        insertPath = UploadUtils.createInsertPath(mUrl, remotePath);
        ftpUtil.login();
        isSuccess = ftpUtil.upload(inputStream, new String(remotePath.getBytes("UTF-8"), "iso-8859-1"));
        ftpUtil.exitServer();
        Log.e("zjy", "PankuObtain.java->commitImage(): schemePath==" + insertPath);
        if (isSuccess) {
            String res = setInsertPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid,remoteName, insertPath, "PK");
            Log.e("zjy", "PankuObtain->uploadFlag(): insert res==" + res);
            if (res.equals("操作成功")) {
                flag = true;
            }
        }
        return flag;
    }

    @NonNull
    private String getRemarkName(String fileName, boolean hasSuffix) {
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
            Log.e("zjy", "PankuObtain.java->onActivityResult(): imgPaths==" + returnPaths.size());
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
        Log.e("zjy", "PankuObtain.java->onDestroy(): clear uploadpicinfos");
        MyAdapter.mSelectedImage.clear();
    }
}
