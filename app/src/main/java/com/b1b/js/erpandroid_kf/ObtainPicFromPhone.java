package com.b1b.js.erpandroid_kf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.b1b.js.erpandroid_kf.utils.FtpUpFile;
import com.b1b.js.erpandroid_kf.utils.ImageWaterUtils;
import com.b1b.js.erpandroid_kf.utils.MyImageUtls;
import com.b1b.js.erpandroid_kf.utils.MyToast;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class ObtainPicFromPhone extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private Button btn_commit;
    private Button btn_commitOrigin;
    FtpUpFile ftp;
    private Bitmap compressImage;
    private ProgressDialog pd;
    private String remoteName;
    //更新progressDialog
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    pd.setMessage("上传成功");
                    handler.sendEmptyMessageDelayed(0, 1500);
                    break;
                case 0:
                    pd.dismiss();
                    break;
                case 3:
                    pd.dismiss();
                    MyToast.showToast(ObtainPicFromPhone.this, "上传失败");
                    break;
                case 5:
                    MyToast.showToast(ObtainPicFromPhone.this, "压缩完成");
                    imageView.setImageBitmap(compressImage);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                FileInputStream fis = openFileInput("get.jpg");
                                String edname = edName.getText().toString().trim();
                                String fileName = TakePicActivity.getRomoteName(MyApp.id);
                                if (!TextUtils.isEmpty(edname)) {
                                    fileName = fileName + "_" + edname;
                                }
                                //从手机取的图片，文件名加"_o"
                                //上传路径
                                String filePath = "ftp://" + MyApp.ftpUrl + "/" + TakePicActivity.getRemoteDir() + "/" + fileName + "_o.jpg";
                                ftp.upload(fis, "/" + TakePicActivity.getRemoteDir(), fileName + "_o.jpg");
                                SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                                final int cid = sp.getInt("cid", -1);
                                final int did = sp.getInt("did", -1);
                                Intent intent = getIntent();
                                String pid = getPid(intent);
                                setInsertPicInfo("", cid, did, Integer.parseInt(MyApp.id), pid, fileName + "_o.jpg", filePath, "CKTZ");
                                handler.sendEmptyMessage(1);
                            } catch (IOException e) {
                                handler.sendEmptyMessage(3);
                                e.printStackTrace();
                            } catch (FtpUpFile.RemoteDeleteException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
            }
        }
    };
    private EditText edName;

    private String getPid(Intent intent) {
        String pid = intent.getStringExtra("pid");
        if (pid == null) {
            pid = MyApp.id;
        }
        return pid;
    }

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_view);
        btn_commitOrigin = (Button) findViewById(R.id.review_getFromPhone);
        btn_commit = (Button) findViewById(R.id.review_commit);
        edName = (EditText) findViewById(R.id.review_name);
        btn_commit.setOnClickListener(this);
        btn_commitOrigin.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.review_iv);
        imageView.setImageResource(R.mipmap.imageerror);
        //        try {
        //            InputStream is = openFileInput("compress.jpg");
        //            compressImage = BitmapFactory.decodeStream(is);
        //            is.close();
        //
        //            if (compressImage == null) {
        //                MyToast.showToast(this, "加载出现问题，请返回重新拍摄");
        //                imageView.setImageResource(R.mipmap.imageerror);
        //            } else {
        //                imageView.setImageBitmap(compressImage);
        //            }
        //        } catch (FileNotFoundException e) {
        //            e.printStackTrace();
        //        } catch (OutOfMemoryError e) {
        //            e.printStackTrace();
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }

    public static String getRomoteName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        return sdf.format(new Date());
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
        Log.e("zjy", "TakePicActivity.java->setInsertPicInfo(): file==" + fileName);
        Log.e("zjy", "TakePicActivity.java->setInsertPicInfo(): filepath==" + filePath);
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
                if (MyApp.ftpUrl.equals("") || MyApp.ftpUrl == null) {
                    MyApp.ftpUrl = "172.16.6.22";
                    ftp = FtpUpFile.getFtpUpFile("NEW_DYJ", "GY8Fy2Gx", MyApp.ftpUrl, 21);
                } else {
                    ftp = FtpUpFile.getFtpUpFile("dyjftp", "dyjftp", MyApp.ftpUrl, 21);
                }
                Log.e("zjy", "ObtainPicFromPhone.java->onClick(): ftp==" + MyApp.ftpUrl);
                showDialog();
                final InputStream inputStream;
                try {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[1024]);
                    byteArrayInputStream.reset();
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Bitmap waterBitmap = null;
                    if (bitmap.getWidth() > 1080 && bitmap.getHeight() > 1080) {
                        waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waterpic);
                    } else {
                        waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_small);
                    }
                    Log.e("zjy", "ObtainPicFromPhone.java->onClick(): ==" + bitmap.getWidth() + "\t" + bitmap.getHeight());
                    compressImage = ImageWaterUtils.createWaterMaskRightBottom(ObtainPicFromPhone.this, bitmap, waterBitmap, 0, 0);
                    if (!waterBitmap.isRecycled()) {
                        waterBitmap.recycle();
                    }
                    MyImageUtls.compressBitmapAtsize(compressImage, openFileOutput("get.jpg", 0), 0.5f);
                    //                    Cursor cursor = getContentResolver().query(imageUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                    //                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    //                    if (index != -1) {
                    //                        cursor.moveToFirst();
                    //                        String path = cursor.getString(index);
                    //                    }
                    handler.sendEmptyMessage(5);
                } catch (OutOfMemoryError e) {
                    MyToast.showToast(ObtainPicFromPhone.this, "图片过大，超出可用内存");

                    //                    Bitmap bitmap=BitmapFactory.decodeStream()
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.review_getFromPhone:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");//相片类型
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1000);
                break;
        }
    }

    private void showDialog() {
        pd = new ProgressDialog(this);
        pd.setMessage("正在上传");
        pd.show();
        pd.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 & resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                imageView.setImageBitmap(bitmap);
                btn_commit.setEnabled(true);
            } catch (OutOfMemoryError e) {
                MyToast.showToast(ObtainPicFromPhone.this, "图片太大，超出可用内存");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
