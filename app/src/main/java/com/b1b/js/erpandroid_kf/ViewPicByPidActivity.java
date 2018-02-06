package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.adapter.ViewPicAdapter;
import com.b1b.js.erpandroid_kf.dtr.zxing.activity.BaseScanActivity;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;

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

import utils.CameraScanInterface;
import utils.DialogUtils;
import utils.FTPUtils;
import utils.FtpManager;
import utils.MyFileUtils;
import utils.MyToast;
import utils.WebserviceUtils;

public class ViewPicByPidActivity extends BaseScanActivity implements CameraScanInterface{

    private EditText edPid;
    private GridView gv;
    private Button btnSearch;
    private List<FTPImgInfo> imgsData;
    private ViewPicAdapter adapter;
    private ProgressDialog pd;
    private boolean deleteOk = true;
    boolean isConn = false;
    int downCounts = 0;
    private AlertDialog alertDialog;
    private String downloadResult = "";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    dismissDialog();
                    if (downloadResult.length() > 100) {
                        downloadResult = downloadResult.substring(0, 100);
                        downloadResult += "....";
                    }
                    if (!downloadResult.equals("")) {
                        downloadResult += "总共找到" + imgsData.size() + "张图片";
                        alertDialog.setMessage(downloadResult);
                    } else {
                        alertDialog.setMessage("没有数据");
                    }
                    alertDialog.show();
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
                    int totalSize = msg.arg1;
                    int current = msg.arg2 + 1;
                    pd.setMessage("正在下载图片" + current + "/" + totalSize);
                    break;
                case 5:
                    dismissDialog();
                    MyToast.showToast(ViewPicByPidActivity.this, "图片上传地址不在本地服务器，无法访问");
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
        Button btnScan = (Button) findViewById(R.id.view_pic_btn_scan);
        setcScanInterface(this);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        imgsData = new ArrayList<>();
        adapter = new ViewPicAdapter(imgsData, ViewPicByPidActivity.this, R.layout.item_viewpicbypid);
        gv.setAdapter(adapter);
        pd = new ProgressDialog(ViewPicByPidActivity.this);
        pd.setCancelable(false);
        btnSearch.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             final String pid = edPid.getText().toString().trim();
                                             imgsData.clear();
                                             if (pid.equals("")) {
                                                 MyToast.showToast(ViewPicByPidActivity.this, "请输入单据号");
                                                 return;
                                             }
                                             adapter.notifyDataSetChanged();
                                             File imgFile = MyFileUtils.getFileParent();
                                             if (imgFile == null) {
                                                 MyToast.showToast(ViewPicByPidActivity.this, "当前无可用的存储设备");
                                                 return;
                                             }
                                             final File file = new File(imgFile, "dyj_img/");
                                             MyFileUtils.checkImgFileSize(file, 100, ViewPicByPidActivity.this);
                                             startSearch(pid);
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
        alertDialog = (AlertDialog) DialogUtils.getSpAlert(this, "", "结果");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_view_pic_by_pid;
    }

    @Override
    public void resultBack(String result) {
        edPid.setText(result);
        imgsData.clear();
        adapter.notifyDataSetChanged();
        startSearch(result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imgsData.clear();
        adapter.notifyDataSetChanged();
        String pid = getIntent().getStringExtra("pid");
        if (pid != null) {
            edPid.setText(pid);
            File imgFile = MyFileUtils.getFileParent();
            if (imgFile == null) {
                MyToast.showToast(ViewPicByPidActivity.this, "当前无可用的存储设备");
                return;
            }
            if (pid.equals("")) {
                MyToast.showToast(ViewPicByPidActivity.this, "请输入单据号");
                return;
            }
            startSearch(pid);
        }

    }

    private void startSearch(final String pid) {
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                super.run();
                downCounts = 0;
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
                    int searchSize = array.length();
                    FTPUtils mFtpClient=null;
                    String tempUrl = "";
                    downloadResult = "总共查询到" + searchSize + "张图片\r\n";
                    for (int i = 0; i < searchSize; i++) {
                        JSONObject tObj = array.getJSONObject(i);
                        String imgName = tObj.getString("pictureName");
                        String imgUrl = tObj.getString("pictureURL");
                        String urlNoShema = imgUrl.substring(6);
                        String remoteAbsolutePath = urlNoShema.substring(urlNoShema.indexOf("/"));
                        try {
                            remoteAbsolutePath = new String(remoteAbsolutePath.getBytes("utf-8"), "iso-8859-1");
                            String imgFtp = urlNoShema.substring(0, urlNoShema.indexOf("/"));
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
                            FTPImgInfo fti = new FTPImgInfo();
                            File fileParent = MyFileUtils.getFileParent();
                            File file = new File(fileParent, "dyj_img/" + imgName);
                            //图片未下载的需要下载
                            if (!file.exists()) {
                                if (!tempUrl.equals(imgFtp)) {
                                    if (finalHost.equals(FtpManager.mainAddress)) {
                                        mFtpClient = new FTPUtils(finalHost, port, FtpManager.mainName, FtpManager
                                                .mainPwd);
                                    } else {
                                        mFtpClient = new FTPUtils(finalHost, port, FtpManager.ftpName, FtpManager
                                                .ftpPassword);
                                    }
                                }
                                Log.e("zjy", "ViewPicByPidActivity->run(): fileName==" + imgUrl);
                                if (!mFtpClient.serverIsOpen()) {
                                    mFtpClient.login();
                                }
                                boolean exitsFIle = mFtpClient.fileExists(remoteAbsolutePath);
                                Log.e("zjy", "ViewPicByPidActivity->run(): file exist==" + exitsFIle);
                                if (exitsFIle) {
                                    mFtpClient.download(file.getAbsolutePath(), remoteAbsolutePath);
                                    downCounts++;
                                    fti.setImgPath(file.getAbsolutePath());
                                    list.add(fti);
                                    downloadResult += "第" + (i + 1) + "张,下载成功\r\n";
                                    mHandler.obtainMessage(4, searchSize, i).sendToTarget();
                                } else {
                                    throw new IOException("ftp文件不存在");
                                }

                            } else {
                                downloadResult += "第" + (i + 1) + "张,已从手机找到\r\n";
                                fti.setImgPath(file.getAbsolutePath());
                                list.add(fti);
                            }
                            tempUrl = imgFtp;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            downloadResult += "第" + (i + 1) + "张,下载失败，原因：连接服务器失败\r\n";
                        }
                    }
                    imgsData.addAll(list);
                    Message msg = mHandler.obtainMessage(0, downloadResult);
                    mHandler.sendMessage(msg);
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void downLoadPic(FTPClient client, String remoteAbsolutePath, String imgFtp, FTPImgInfo fii, File file,
                             List<FTPImgInfo> list) throws IOException {
        if (!client.isConnected()) {
            client.connect(imgFtp, 21);
            if (imgFtp.equals(FtpManager.mainAddress)) {
                client.login(FtpManager.mainName, FtpManager.mainPwd);
            } else {
                client.login(FtpManager.ftpName, FtpManager.ftpPassword);
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            client.enterLocalPassiveMode();
        }
        //retrieveFile可以不用changeWorkDirectory，但是remoteName为文件的完整路径，例如："/dir/name.txt"
        InputStream inputStream = client.retrieveFileStream(remoteAbsolutePath);
        if (inputStream != null) {
            FileOutputStream fio = new FileOutputStream(file);
            byte[] buf = new byte[8*1024];
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
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils
                .ChuKuServer);
        return response.toString();
    }


    @Override
    public void getCameraScanResult(String result) {
        edPid.setText(result);
        startSearch(result);
    }
}
