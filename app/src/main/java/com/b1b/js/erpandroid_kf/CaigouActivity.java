package com.b1b.js.erpandroid_kf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.entity.CaigouGoodType;
import com.b1b.js.erpandroid_kf.entity.Caigoudan;
import utils.MyDecoration;
import utils.MyToast;
import utils.WebserviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CaigouActivity extends AppCompatActivity {

    private RecyclerView mRview;

    private List<String> data;
    private MyRviewAdapter adapter;
    private List<Caigoudan> caigouList;
    private EditText editPartNo;
    private Button btnSearch;
    private ArrayList<CaigouGoodType> typeLists;
    private ArrayList<CaigouGoodType> cTypeLists;
    private RadioGroup radioGroup;
    private View dialogView;
    private int clickPos = 0;
    private String pid;
    private Caigoudan currentCaigoudan;
    private String strValue;
    int times = 0;
    AlertDialog typeSelectDialog;

    interface OnItemClickListener {
        void onClick(int position);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    MyToast.showToast(CaigouActivity.this, "当前条件有误，或网络状态不佳");
                    break;
                case 2:
                    if (times > 1) {
                        radioGroup.removeAllViews();
                        for (int i = 0; i < typeLists.size(); i++) {
                            RadioButton rd = new RadioButton(CaigouActivity.this);
                            CaigouGoodType cgtype = typeLists.get(i);
                            rd.setText(cgtype.getStrText());
                            radioGroup.addView(rd);
                        }
                    }
                    break;
                case 3:
                    MyToast.showToast(CaigouActivity.this, "上传类别失败，或网络状态不佳");
                    break;
                case 4:
                    MyToast.showToast(CaigouActivity.this, "上传类别成功");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigou);
        mRview = (RecyclerView) findViewById(R.id.caigou_rview);
        editPartNo = (EditText) findViewById(R.id.caigou_ed_partNo);
        btnSearch = (Button) findViewById(R.id.caigou_serach);

        dialogView = LayoutInflater.from(CaigouActivity.this).inflate(R.layout.dialog_view_typeinfo, null);
        radioGroup = (RadioGroup) dialogView.findViewById(R.id.dialog_caigou_rg);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String partNo = editPartNo.getText().toString().trim();
                caigouList.clear();
                adapter.notifyDataSetChanged();
                if (TextUtils.isEmpty(MyApp.id)) {
                    MyToast.showToast(CaigouActivity.this, "登录id为空，请重新登录");
                    return;
                }
                getData("", Integer.parseInt(MyApp.id), partNo);
            }
        });
        //创建LayoutManager，并设置方向，水平或竖直
        LinearLayoutManager lManager = new LinearLayoutManager(CaigouActivity.this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRview.setLayoutManager(lManager);
        mRview.addItemDecoration(new MyDecoration(CaigouActivity.this, MyDecoration.VERTICAL));
        typeLists = new ArrayList<>();
        caigouList = new ArrayList<>();
        adapter = new MyRviewAdapter(caigouList, CaigouActivity.this, new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(CaigouActivity.this, CaigoudanEditActivity.class);
                currentCaigoudan = caigouList.get(position);
                if (currentCaigoudan != null) {
                    intent.putExtra("pid", currentCaigoudan.getPid());
                    intent.putExtra("partNo", currentCaigoudan.getPartNo());
                    startActivity(intent);
                }
                //                if ((currentCaigoudan != null)) {
                //                    AlertDialog.Builder builder = new AlertDialog.Builder(CaigouActivity.this);
                //                    typeSelectDialog = builder.create();
                //                    typeSelectDialog.setTitle("请选择类别");
                //                    dialogView = LayoutInflater.from(CaigouActivity.this).inflate(R.layout.dialog_view_typeinfo, null);
                //                    radioGroup = (RadioGroup) dialogView.findViewById(R.id.dialog_caigou_rg);
                //                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                //                        @Override
                //                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                //                            RadioButton tempButton = (RadioButton) group.findViewById(checkedId);
                //                            for (int i = 0; i < typeLists.size(); i++) {
                //                                if (typeLists.get(i).getStrText().equals(tempButton.getText().toString().trim())) {
                //                                    clickPos = i;
                //                                }
                //                            }
                //                        }
                //                    });
                //                    final EditText word = (EditText) dialogView.findViewById(R.id.dialog_caigou_serachEt);
                //                    Button searchBtn = (Button) dialogView.findViewById(R.id.dialog_caigou_serachBtn);
                //                    Button ok = (Button) dialogView.findViewById(R.id.dialog_caigou_finish);
                //                    ok.setOnClickListener(new View.OnClickListener() {
                //                                              @Override
                //                                              public void onClick(View v) {
                //                                                  CaigouGoodType caigouGoodType = typeLists.get(clickPos);
                //                                                  String strValue = caigouGoodType.getStrValue();
                //                                                  String strText = caigouGoodType.getStrText();
                //                                                  String title = currentCaigoudan.getPartNo();
                //                                                  final String uid = MyApp.id;
                //                                                  final String pid = currentCaigoudan.getPid();
                //                                                  final String string = strValue + "|" + strText + "|" + title;
                //                                                  new Thread() {
                //                                                      @Override
                //                                                      public void run() {
                //                                                          super.run();
                //                                                          try {
                //                                                              setPartTypeInfoInfo(pid, uid, string);
                //                                                              mHandler.sendEmptyMessage(4);
                //                                                          } catch (IOException e) {
                //                                                              mHandler.sendEmptyMessage(3);
                //                                                              e.printStackTrace();
                //                                                          } catch (XmlPullParserException e) {
                //                                                              mHandler.sendEmptyMessage(3);
                //                                                              e.printStackTrace();
                //                                                          }
                //                                                      }
                //                                                  }.start();
                //                                              }
                //                                          }
                //                    );
                //                    searchBtn.setOnClickListener(new View.OnClickListener()
                //
                //                                                 {
                //                                                     @Override
                //                                                     public void onClick(View v) {
                //                                                         getTypeInfo(word.getText().toString());
                //                                                     }
                //                                                 }
                //
                //                    );
                //                    word.requestFocus();
                //                    typeSelectDialog.setView(dialogView);
                //                    if (!typeSelectDialog.isShowing())
                //
                //                    {
                //                        typeSelectDialog.show();
                //                        if (typeLists.size() != 0) {
                //                            for (int i = 0; i < typeLists.size(); i++) {
                //                                RadioButton rd = new RadioButton(CaigouActivity.this);
                //                                CaigouGoodType cgtype = typeLists.get(i);
                //                                rd.setText(cgtype.getStrText());
                //                                radioGroup.addView(rd);
                //                            }
                //                        }
                //                    }
                //                }
            }
        }
        );
        mRview.setAdapter(adapter);
//        if (!TextUtils.isEmpty(MyApp.id)) {
//            getData("", Integer.parseInt(MyApp.id), "");
//        } else {
//            MyToast.showToast(CaigouActivity.this, "用户id不存在，请重新登录");
//        }
        //        getTypeInfo("10");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(MyApp.id)) {
            caigouList.clear();
            adapter.notifyDataSetChanged();
            getData("", Integer.parseInt(MyApp.id), "");
        } else {
            MyToast.showToast(CaigouActivity.this, "用户id不存在，请重新登录");
        }
    }

    private void getData(final String checkWord, final int buyerId, final String partNo) {

        new Thread() {
            @Override
            public void run() {
                try {
                    String res = getCaigoudan(checkWord, buyerId, partNo);
                    String json = "";
                    if (res.length() > 7) {
                        json = res.substring(7);
                    }
                    JSONObject object = new JSONObject(json);
                    JSONArray array = object.getJSONArray("RES");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        //   单据编号": "755940",
                        //          "制单日期": "2015/11/24 15:03:12",
                        //          "单据状态": "等待采购",
                        //          "业务员": "管理员",
                        //          "采购员": "管理员",
                        //          "型号": "TEST【10】|TEST【11】"
                        String pid = obj.getString("单据编号");
                        String createdDate = obj.getString("制单日期");
                        String state = obj.getString("单据状态");
                        String ywName = obj.getString("业务员");
                        String caigouName = obj.getString("采购员");
                        String partNo1 = obj.getString("型号");
//                        partNo1 = partNo1.substring(0, partNo1.indexOf("【"));
                        Caigoudan caigoudan = new Caigoudan(state, pid, createdDate, ywName, caigouName, partNo1);
                        caigouList.add(caigoudan);
                    }
                    mHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class MyRviewAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<Caigoudan> data;
        private Context mContext;
        private MyViewHolder mHolder;
        private OnItemClickListener itemClickListener;

        public MyRviewAdapter(List<Caigoudan> data, Context mContext, OnItemClickListener itemClickListener) {
            this.data = data;
            this.mContext = mContext;
            this.itemClickListener = itemClickListener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.item_caigou_simpleitem, parent, false);
            //            view.setBackgroundColor(Color.RED);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onClick(((RecyclerView) parent).getChildAdapterPosition(view));
                }
            });
            mHolder = new MyViewHolder(view);
            return mHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Caigoudan s = data.get(position);
            if (s != null) {
                holder.tv1.setText(s.toString());
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv1;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.item_caigou_tv);
        }
    }

    public String getCaigoudan(String checkWord, int buyerId, String partNo) throws IOException, XmlPullParserException {
        //        GetBillByPartNo
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("checkWord", checkWord);
        map.put("buyerID", buyerId);
        map.put("partNo", partNo);
        SoapObject request = WebserviceUtils.getRequest(map, "GetBillByPartNo");
        SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.MartService);
        Log.e("zjy", "CaigouActivity.java->getCaigoudanByPidAndPartNo(): response==" + response.toString());
        return response.toString();
    }

    public String getTypeInfo(final String keyword) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                typeLists.clear();
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("selValue", keyword);
                SoapObject request = WebserviceUtils.getRequest(map, "GetXinHaoManageInfo");
                try {
                    SoapPrimitive response = WebserviceUtils.getSoapPrimitiveResponse(request, SoapEnvelope.VER11, WebserviceUtils.MartService);
                    Log.e("zjy", "CaigouActivity.java->run(): response==" + response.toString());
                    JSONObject obj = new JSONObject(response.toString());
                    JSONArray jsonArray = obj.getJSONArray("表");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject tObj = jsonArray.getJSONObject(i);
                        String strValue = tObj.getString("objid");
                        String StrText = tObj.getString("objvalue");
                        //                        CaigouGoodType caigouGoodType = new CaigouGoodType(strValue, "", StrText);
                        CaigouGoodType caigouGoodType = new CaigouGoodType(i, strValue, "", StrText);
                        typeLists.add(caigouGoodType);
                    }
                    times++;
                    mHandler.sendEmptyMessage(2);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }.start();
        return null;
    }


}
