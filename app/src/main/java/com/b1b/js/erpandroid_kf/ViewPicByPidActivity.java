package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.adapter.ViewPicAdapter;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ViewPicByPidActivity extends AppCompatActivity {

    private EditText edPid;
    private GridView gv;
    private Button btnSearch;
    private List<FTPImgInfo> imgsData;
    private ViewPicAdapter adapter;
    private ProgressDialog pd;
    private boolean deleteOk = true;
    boolean isConn = false;
    int downCounts;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<FTPImgInfo> list = (List<FTPImgInfo>) msg.obj;
                    if (list != null) {
                        imgsData.addAll(list);
                    }
                    adapter.notifyDataSetChanged();
                    dismissDialog();
                    MyToast.showToast(ViewPicByPidActivity.this, "下载成功");
                    break;
                case 1:
                    dismissDialog();
                    MyToast.showToast(ViewPicByPidActivity.this, "当前单据没有对应的图片");
                    break;
                case 2:
                    dismissDialog();
                    MyToast.showToast(ViewPicByPidActivity.this, "当前网络质量较差，请重试");
                    break;
                case 3:
                    dismissDialog();
                    MyToast.showToast(ViewPicByPidActivity.this, "图片上传地址不在本地服务器，无法访问");
                    break;
                case 4:

                    break;
            }
        }
    };

    private void dismissDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pic_by_pid);
        edPid = (EditText) findViewById(R.id.view_pic_edpid);
        gv = (GridView) findViewById(R.id.view_pic_gv);
        btnSearch = (Button) findViewById(R.id.view_pic_btn_search);
        imgsData = new ArrayList<>();
        adapter = new ViewPicAdapter(imgsData, ViewPicByPidActivity.this);
        gv.setAdapter(adapter);
        pd = new ProgressDialog(ViewPicByPidActivity.this);
        pd.setCancelable(false);
        btnSearch.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             final String pid = edPid.getText().toString().trim();
                                             imgsData.clear();
                                             adapter.notifyDataSetChanged();
                                             String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                             final File file = new File(path + "/dyj_img/");
                                             if (!file.exists()) {
                                                 file.mkdirs();
                                                 startSearch(pid);
                                             } else {
                                                 String[] files = file.list();
                                                 if (files.length > 100) {
                                                     AlertDialog.Builder builder = new AlertDialog.Builder(ViewPicByPidActivity.this);
                                                     builder.setTitle("提示");
                                                     builder.setMessage("缓存图片超过100张，是否清理一下");
                                                     builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialog, int which) {
                                                             btnSearch.setEnabled(false);
                                                             MyToast.showToast(ViewPicByPidActivity.this, "清理完成可继续搜索");
                                                             final File[] files = file.listFiles();
                                                             for (File f : files) {
                                                                 f.delete();
                                                             }
                                                             btnSearch.setEnabled(true);
                                                         }
                                                     });
                                                     builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialog, int which) {
                                                             startSearch(pid);
                                                         }
                                                     });
                                                     builder.show();
                                                 } else {
                                                     startSearch(pid);
                                                 }
                                             }
                                         }


                                     }
        );
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FTPImgInfo item = (FTPImgInfo) parent.getItemAtPosition(position);
                if (item != null) {
                    Intent mIntent = new Intent(ViewPicByPidActivity.this, PicDetailActivity.class);
                    mIntent.putExtra("path", item.getImgPath());
                    ArrayList<String> paths = new ArrayList<>();
                    for (int i = 0; i < imgsData.size(); i++) {
                        paths.add(imgsData.get(i).getImgPath());
                    }
                    mIntent.putStringArrayListExtra("paths", paths);
                    mIntent.putExtra("pos", position);
                    startActivity(mIntent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String pid = getIntent().getStringExtra("pid");
        if (pid != null) {
            edPid.setText(pid);
            startSearch(pid);
        }

    }

    private void startSearch(final String pid) {
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                super.run();
                String result = "";
                try {
                    result = getRelativePicInfoByPid("", pid);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                    return;
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject root = new JSONObject(result);
                    JSONArray array = root.getJSONArray("表");
                    Log.e("zjy", "ViewPicByPidActivity.java->run():search pic count=" + array.length());
                    FTPClient client = new FTPClient();
                    List<FTPImgInfo> list = new ArrayList<>();
                     downCounts = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject tObj = array.getJSONObject(i);
                        String imgName = tObj.getString("pictureName");
                        String imgUrl = tObj.getString("pictureURL");
                        String urlNoShema = imgUrl.substring(6);
                        String remoteAbsolutePath = urlNoShema.substring(urlNoShema.indexOf("/"));
                        try {
                            remoteAbsolutePath = new String(remoteAbsolutePath.getBytes("utf-8"), "iso-8859-1");
                            String imgFtp = urlNoShema.substring(0, urlNoShema.indexOf("/"));
                            FTPImgInfo fti = new FTPImgInfo();
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                            File file = new File(path + "/dyj_img/" + imgName);
                            //图片未下载的需要下载
                            if (!file.exists()) {
                                File parent = file.getParentFile();
                                if (parent != null) {
                                    if (!parent.exists()) {
                                        parent.mkdirs();
                                    }
                                }
                                downLoadPic(client, remoteAbsolutePath, imgFtp, fti, file, list);
                            } else {
                                fti.setImgPath(file.getAbsolutePath());
                                list.add(fti);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mHandler.sendEmptyMessage(3);
                            return;
                        }
                    }
                    Log.e("zjy", "ViewPicByPidActivity.java->run(): downCouts==" + downCounts);
                    Message msg = mHandler.obtainMessage(0, list);
                    mHandler.sendMessage(msg);
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void downLoadPic(FTPClient client, String remoteAbsolutePath, String imgFtp, FTPImgInfo fii, File file, List<FTPImgInfo> list) throws IOException {
        if (!client.isConnected()) {
            client.connect(imgFtp, 21);
            if (imgFtp.equals("172.16.6.22")) {
                client.login("NEW_DYJ", "GY8Fy2Gx");
            } else {
                client.login("dyjftp", "dyjftp");
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            client.enterLocalPassiveMode();
        }
        //retrieveFile可以不用changeWorkDirectory，但是remoteName为文件的完整路径，例如："/dir/name.txt"
        InputStream inputStream = client.retrieveFileStream(remoteAbsolutePath);
        if (inputStream != null) {
            FileOutputStream fio = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                fio.write(buf, 0, len);
            }
            inputStream.close();
            fio.flush();
            fio.close();
            //retrieveFileStream之后需要调用才能进行下一次下载
            client.completePendingCommand();
            fii.setImgPath(file.getAbsolutePath());
            list.add(fii);
            downCounts++;
        }
    }

    public void showProgressDialog() {
        pd.setMessage("正在查询中");
        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
    }

    //    GetBILL_PictureRelatenfoByID
    //    name="checkWord" type="xs:string"
    //   name="ID" type="xs:string"
    public String getRelativePicInfoByPid(String checkWord, String pid) throws IOException, XmlPullParserException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("ID", pid);
        SoapObject request = WebserviceUtils.getRequest(map, "GetBILL_PictureRelatenfoByID");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.ChuKuServer);
        return response.toString();
    }

}
