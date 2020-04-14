package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.b1b.js.erpandroid_kf.activity.base.ToolbarHasSunmiActivity;
import com.b1b.js.erpandroid_kf.adapter.ViewPicAdapter;
import com.b1b.js.erpandroid_kf.entity.FTPImgInfo;
import com.b1b.js.erpandroid_kf.entity.IntentKeys;
import com.b1b.js.erpandroid_kf.picupload.FtpUploader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import utils.common.MyFileUtils;
import utils.framwork.DialogUtils;
import utils.framwork.SoftKeyboardUtils;
import utils.handler.NoLeakHandler;
import utils.net.wsdelegate.ChuKuServer;

public class ViewPicByPidActivity extends ToolbarHasSunmiActivity {

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
    protected String dyjFTP = MyApp.ftpUrl;

    public static final int MSG_Error = 8;
    public static final int MSG_OK = 0;
    public static final int MSG_LIMIT = 300;

    private Handler mHandler = new NoLeakHandler(this);
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_Error:
                String errmsg = msg.obj.toString();
                showMsgToast(errmsg);
                dismissDialog();
                break;
            case MSG_OK:
                List<FTPImgInfo> list = (List<FTPImgInfo>) msg.obj;
                Log.e("zjy", getClass() + "->handleMessage(): listSize==" + list.size());
                imgsData.clear();
                imgsData.addAll(list);
                adapter.notifyDataSetChanged();
                dismissDialog();
                if (downloadResult.length() > MSG_LIMIT) {
                    downloadResult = downloadResult.substring(0, MSG_LIMIT);
                    downloadResult += "....";
                }
                alertDialog.setMessage(downloadResult);
                alertDialog.show();
                break;
        }
    }

    private void dismissDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    @Override
    public String setTitle() {
        return "图片查看";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.relaseCache();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pic_by_pid);
        edPid = (EditText) findViewById(R.id.view_pic_edpid);
        gv = (GridView) findViewById(R.id.view_pic_gv);
        btnSearch = (Button) findViewById(R.id.view_pic_btn_search);
        Button btnScan = (Button) findViewById(R.id.view_pic_btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });
        imgsData = new ArrayList<>();
        adapter = new ViewPicAdapter(imgsData, mContext, R.layout.item_viewpicbypid);
        gv.setAdapter(adapter);
        pd = new ProgressDialog(mContext);
        pd.setCancelable(false);
        btnSearch.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             final String pid = edPid.getText().toString().trim();

                                             if (pid.equals("")) {
                                                showMsgToast( "请输入单据号");
                                                 return;
                                             }

                                             File imgFile = Environment.getExternalStorageDirectory();
                                             if (imgFile == null) {
                                                showMsgToast( "当前无可用的存储设备");
                                                 return;
                                             }
                                             final File file = new File(imgFile, "dyj_img/");
                                             MyFileUtils.checkImgFileSize(file, 300, mContext);
                                             imgsData.clear();
                                             adapter.notifyDataSetChanged();
                                             startSearch(pid);
                                         }
                                     }
        );
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FTPImgInfo item = (FTPImgInfo) parent.getItemAtPosition(position);
                if (item != null) {
                    Intent mIntent = new Intent(mContext, PicDetailActivity.class);
                    mIntent.putExtra(PicDetailActivity.ex_Path, item.getImgPath());
                    ArrayList<String> paths = new ArrayList<>();
                    for (int i = 0; i < imgsData.size(); i++) {
                        paths.add(imgsData.get(i).getImgPath());
                    }
                    mIntent.putStringArrayListExtra(PicDetailActivity.ex_Paths, paths);
                    mIntent.putExtra("pos", position);
                    startActivity(mIntent);
                }
            }
        });
        alertDialog = (AlertDialog) DialogUtils.getSpAlert(this, "", "结果");

        String pid = getIntent().getStringExtra(IntentKeys.key_pid);
        if (pid != null) {
            edPid.setText(pid);
            File imgFile = Environment.getExternalStorageDirectory();
            if (imgFile == null) {
               showMsgToast( "当前无可用的存储设备");
                return;
            }
            if (pid.equals("")) {
               showMsgToast( "请输入单据号");
                return;
            }
            startSearch(pid);
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void setListeners() {

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

    }

    public void startDownLoad(String url, String local) throws IOException {
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

    public List<FTPImgInfo> getPicList(String pid) throws IOException, JSONException {
        String errMsg = "";
        String result = "";
        try {
             result = getRelativePicInfoByPid("", pid);
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

    private void startSearch(final String pid) {
        showProgressDialog();
        SoftKeyboardUtils.closeInputMethod(edPid, mContext);
        new Thread() {
            @Override
            public void run() {
                super.run();
                downCounts = 0;
                List<FTPImgInfo> list = new ArrayList<>();

                String errmsg = "";
                try {
                    list = getPicList(pid);
                    final List<FTPImgInfo> finalList = list;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pd.setMessage("查找到" + finalList.size() + "张图片，开始下载");
                        }
                    });
                    downloadResult = "总共查询到" + list.size() + "张图片\r\n";
                } catch (IOException e) {
                    e.printStackTrace();
                    errmsg = e.getMessage();

                } catch (Exception e) {
                    errmsg = "其他," + e.getMessage();
                    e.printStackTrace();
                }
                List<FTPImgInfo> foundPics = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    try {
                        FTPImgInfo fti = list.get(i);
                        String localPath = fti.getImgPath();
                        String imgUrl = fti.getFtp();
                        File file = new File(localPath);
                        Log.e("zjy",
                                "ViewPicByPidActivity->run(): mfile.Len==" + file.getName() + "," + file.length());
                        if (!file.exists() || file.length() == 0) {
                            startDownLoad(imgUrl, localPath);
                        } else {
                            downloadResult += "第" + (i + 1) + "张,已从手机找到\r\n";
                        }
                        foundPics.add(fti);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        downloadResult += "第" + (i + 1) + "张,下载失败，" + e.getMessage() + "\r\n";
                    }
                }
                if (!"".equals(errmsg)) {
                    mHandler.obtainMessage(MSG_Error, errmsg).sendToTarget();
                    return;
                }
                Message msg = mHandler.obtainMessage(MSG_OK, foundPics);
                mHandler.sendMessage(msg);
            }
        }.start();
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
        return ChuKuServer.GetBILL_PictureRelatenfoByID(checkWord, pid);
    }


    @Override
    public void getCameraScanResult(String result) {
        edPid.setText(result);
        startSearch(result);
    }
}
