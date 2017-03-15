package com.b1b.js.erpandroid_kf;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.adapter.UploadPicAdapter;
import com.b1b.js.erpandroid_kf.entity.UploadPicInfo;
import com.b1b.js.erpandroid_kf.utils.FtpManager;
import com.b1b.js.erpandroid_kf.utils.ImageWaterUtils;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.UploadUtils;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

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
import zhy.imageloader.MyAdapter;
import zhy.imageloader.PickPicActivity;

public class ObtainPicFromPhone extends AppCompatActivity implements View.OnClickListener {

    private Button btn_commit;
    private Button btn_commitOrigin;
    private FtpManager ftp;
    private Bitmap compressImage;
    private ProgressDialog pd;
    private boolean isFirst;
    private int onclickPosition;
    private GridView gv;
    private int currentIndex = 0;
    private final int PICUPLOAD_SUCCESS = 1;
    private final int LISTPICUPLOAD_SUCCESS = 4;
    private final int PICUPLOAD_ERROR = 2;
    private final int PIC_OOM = 3;
    private final int FTP_ERROR = 8;
    private String pid;
    private MaterialDialog resultDialog;
    //更新progressDialog
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICUPLOAD_SUCCESS:
                    showFinalDialog("上传成功");
                    int nfId = getIntent().getIntExtra("nfId", 0);
                    String failPid = getIntent().getStringExtra("failPid");
                    UploadPicInfo upInfo = uploadPicInfos.get(onclickPosition);
                    String path = upInfo.getPath();
                    String name = path.substring(path.lastIndexOf("/") + 1);
                    if (nfId != 0) {
                        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(ObtainPicFromPhone.this);
                        builder.setContentTitle("上传" + failPid + "图片");
                        builder.setContentText(name + "上传成功");
                        nManager.notify(nfId, builder.build());
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
                case LISTPICUPLOAD_SUCCESS:
                    showFinalDialog("批量上传成功");
                    mGvAdapter.notifyDataSetChanged();
                    break;
                case FTP_ERROR:
                    MyToast.showToast(ObtainPicFromPhone.this, "连接ftp失败，请检查网络");
                    connFTP(handler, FTP_ERROR);
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
    private Uri imageUri;
    private String path;

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
        resultDialog = new MaterialDialog(ObtainPicFromPhone.this);
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
        path = intent.getStringExtra("failPath");
        if (path != null) {
            Log.e("zjy", "ObtainPicFromPhone.java->onCreate(): sent path==" + path);
            uploadPicInfos.add(new UploadPicInfo("-1", path));
        }
        if (pid != null) {
            edPid.setText(pid);
        }
        //初始化ftp
        if ("".equals(MyApp.ftpUrl) || MyApp.ftpUrl == null) {
            if ("101".equals(MyApp.id)) {
                MyApp.ftpUrl = "172.16.6.22";
                ftp = FtpManager.getFtpManager("NEW_DYJ", "GY8Fy2Gx", MyApp.ftpUrl, 21);
            }
        } else {
            ftp = FtpManager.getFtpManager("dyjftp", "dyjftp", MyApp.ftpUrl, 21);
        }
        connFTP(handler, FTP_ERROR);
        mGvAdapter = new UploadPicAdapter(ObtainPicFromPhone.this, uploadPicInfos, new UploadPicAdapter.OnItemBtnClickListener() {
            @Override
            public void onClick(View v, int position) {
                final UploadPicInfo uploadPicInfo = uploadPicInfos.get(position);
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(ObtainPicFromPhone.this, pid))
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
                    MyToast.showToast(ObtainPicFromPhone.this, "当前图片已经上传完成");
                }
            }
        });
        gv.setAdapter(mGvAdapter);
    }


    private void connFTP(final Handler handler, final int target) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    ftp.connectAndLogin();
                } catch (IOException e) {
                    handler.sendEmptyMessage(target);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public String setInsertPicInfo(String checkWord, int cid, int did, int uid, String pid, String fileName, String filePath, String stypeID) throws IOException, XmlPullParserException {
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
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        str = response.toString();
        Log.e("zjy", "ObtainPicFromPhone.java->setInsertPicInfo(): insertRes==" + str);
        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_commit:
                pid = edPid.getText().toString().trim();
                if (TakePicActivity.checkPid(ObtainPicFromPhone.this, pid))
                    return;
                showProgressDialog();
                new Thread() {
                    @Override
                    public void run() {
                        if (commitImages(uploadPicInfos)) {
                            handler.sendEmptyMessage(LISTPICUPLOAD_SUCCESS);
                        }
                    }
                }.start();
                break;
            case R.id.review_getFromPhone:
                //最初的方法
                //                Intent intent = new Intent(Intent.ACTION_PICK);
                //                intent.setType("image/*");//相片类型
                //                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //                startActivityForResult(intent, 1000);
                Intent intent = new Intent(ObtainPicFromPhone.this, PickPicActivity.class);
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
                        return false;
                    }
                } catch (OutOfMemoryError e) {
                    handler.sendEmptyMessage(PIC_OOM);
                    e.printStackTrace();
                } catch (IOException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    return false;
                } catch (XmlPullParserException e) {
                    handler.sendEmptyMessage(PICUPLOAD_ERROR);
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean commitImage(UploadPicInfo uploadPicInfo, int cid, int did, String pid) throws IOException, XmlPullParserException {
        InputStream inputStream = new FileInputStream(uploadPicInfo.getPath());
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Bitmap waterBitmap = null;
        if (bitmap != null) {
            if (bitmap.getWidth() > 1080 && bitmap.getHeight() > 1080) {
                waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
            } else {
                waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_small);
            }
            Log.e("zjy", "ObtainPicFromPhone.java->commitImages():image size ==" + bitmap.getWidth() + "\t" + bitmap.getHeight());
            Bitmap textBitmap = ImageWaterUtils.drawTextToRightTop(ObtainPicFromPhone.this, bitmap, pid, (int) (bitmap.getWidth() * 0.015), Color.RED, 20, 20);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            compressImage = ImageWaterUtils.createWaterMaskRightBottom(ObtainPicFromPhone.this, textBitmap, waterBitmap, 0, 0);
            if (!waterBitmap.isRecycled()) {
                waterBitmap.recycle();
            }
            if (!textBitmap.isRecycled()) {
                textBitmap.recycle();
            }
            ByteArrayInputStream bai = new ByteArrayInputStream(MyImageUtls.compressBitmapAtsize(compressImage, 0.4f));
            String remark = edName.getText().toString().trim();
            //不带后缀名的文件名称
            String fileName = UploadUtils.getRomoteName(pid);
            //从手机取的图片，文件后缀加"_o"
            String suffix = "_o";
            if (!TextUtils.isEmpty(remark)) {
                fileName = fileName + "_" + remark + suffix;
            } else {
                fileName = fileName + suffix;
            }
            //上传路径
            String filePath = UploadUtils.getFilePath(MyApp.ftpUrl, UploadUtils.getRemoteDir(), fileName, "jpg");
            //文件名或者目录中有中文需要转码 new String(fileName.getBytes("UTF-8"), "iso-8859-1")
            boolean isSuccess;
            if ("101".equals(MyApp.id)) {
                isSuccess = ftp.upload(bai, "/ZJy", new String(fileName.getBytes("UTF-8"), "iso-8859-1") + ".jpg");
                filePath = "ftp://" + MyApp.ftpUrl + "/ZJy/" + fileName + ".jpg";
            } else {
                isSuccess = ftp.upload(bai, "/" + UploadUtils.getRemoteDir(), new String(fileName.getBytes("UTF-8"), "iso-8859-1") + ".jpg");
            }
            Log.e("zjy", "ObtainPicFromPhone.java->commitImages(): schemePath==" + filePath);
            if (isSuccess) {
                setInsertPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, fileName + ".jpg", filePath, "CKTZ");
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
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
        Log.e("zjy", "ObtainPicFromPhone.java->onDestroy(): clear uploadpicinfos");
        MyAdapter.mSelectedImage.clear();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    ftp.exit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}