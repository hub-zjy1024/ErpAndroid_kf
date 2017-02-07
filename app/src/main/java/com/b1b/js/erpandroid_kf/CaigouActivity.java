package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.entity.Caigoudan;
import com.b1b.js.erpandroid_kf.utils.MyToast;
import com.b1b.js.erpandroid_kf.utils.WebserviceUtils;

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
    interface OnItemClickListener {
        public void onClick(int position);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caigou);
        mRview = (RecyclerView) findViewById(R.id.caigou_rview);
        //创建LayoutManager，并设置方向，水平或竖直
        LinearLayoutManager lManager = new LinearLayoutManager(CaigouActivity.this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRview.setLayoutManager(lManager);
        mRview.addItemDecoration(new DividerItemDecoration(CaigouActivity.this, DividerItemDecoration.VERTICAL));
        data = new ArrayList<>();
        adapter = new MyRviewAdapter(data, CaigouActivity.this, new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                MyToast.showToast(CaigouActivity.this, "点击了" + position);
            }
        });
        mRview.setAdapter(adapter);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String res = getCaigoudan("", 101, "");
                    JSONObject object = new JSONObject(res);
                    JSONArray array = object.getJSONArray("RES");
                    for (int i = 0; i < array.length(); i++) {
                        Caigoudan caigoudan = new Caigoudan();
                        JSONObject obj = array.getJSONObject(i);
//                        "单据编号": "755940",
//                                "制单日期": "2015/11/24 15:03:12",
//                                "单据状态": "等待采购",
//                                "业务员": "管理员",
//                                "采购员": "管理员",
//                                "型号": "TEST【10】TEST|【11】"
                        String pid = obj.getString("单据编号");
                        String createdDate = obj.getString("制单日期");
                        String state = obj.getString("单据状态");
                        String ywName = obj.getString("业务员");
                        String caigouName = obj.getString("采购员");
                        String partNo = obj.getString("型号");
                        caigoudan = new Caigoudan(state, pid, createdDate, ywName, caigouName, partNo);
                        caigouList.add(caigoudan);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.start();

        for (int i = 0; i < 30; i++) {
            data.add("何曾薨" + i);
        }

        adapter.notifyDataSetChanged();
    }

    class MyRviewAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<String> data;
        private Context mContext;
        private OnItemClickListener listener;

        public MyRviewAdapter(List<String> data, Context mContext, OnItemClickListener listener) {
            this.data = data;
            this.mContext = mContext;
            this.listener = listener;
        }

        public MyRviewAdapter(List<String> data, Context mContext) {
            this.data = data;
            this.mContext = mContext;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_caigou_simpleitem, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            String s = data.get(position);
            holder.tv1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(position);
                }
            });
            if (s != null) {
                holder.tv1.setText(s);
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
        Log.e("zjy", "CaigouActivity.java->getCaigoudan(): response==" + response.toString());
        return response.toString();
    }
}
