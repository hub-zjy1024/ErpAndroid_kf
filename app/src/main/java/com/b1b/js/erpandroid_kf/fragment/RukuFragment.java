package com.b1b.js.erpandroid_kf.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.b1b.js.erpandroid_kf.R;
import com.b1b.js.erpandroid_kf.activity.base.BaseMActivity;
import com.b1b.js.erpandroid_kf.config.ExtraParams;
import com.b1b.js.erpandroid_kf.entity.RukuInfo;

import java.util.ArrayList;
import java.util.List;

import utils.MyDecoration;
import utils.adapter.CommonAdapter;
import utils.adapter.ViewHolder;
import utils.adapter.recyclerview.BaseRvAdapter;
import utils.adapter.recyclerview.BaseRvViewholder;


public class RukuFragment extends Fragment implements RukuViewModel.IViewViewModel{

    private RukuViewModel mViewModel;

    protected BaseMActivity mParent;
    public static final int REQ_SEARCH = 0;
    private String loginid;
    private String storageid = "";
     List<RukuInfo> value = new ArrayList<>();
    private String lastPartNo = "";
    public static RukuFragment newInstance() {
        return new RukuFragment();
    }

    private ChaidanAdapter mAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.ruku_fragment, container, false);
        RecyclerView mRv = view.findViewById(R.id.frag_ruku_rv);

//        mViewModel.getData().getValue();
        mAdapter = new ChaidanAdapter(value, R.layout.item_rv_chaidan, getActivity(), new ChaidanAdapter.MClickListener() {
            @Override
            public void onClick(View v, RukuInfo item) {
                ShowEditDialog(item);
            }
        });
        loginid = getArguments().getString(ExtraParams.NM_LOGIN_ID);
        Activity activity = getActivity();
        if (activity != null) {
            Drawable mDivider =
                    activity.getResources().getDrawable(R.drawable.recyclerview_divider);
            mRv.addItemDecoration(new MyDecoration(mDivider, MyDecoration.VERTICAL));
            mRv.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            Log.e("zjy", "RukuFragment->onCreateView(): initMrv==");
        }
        mRv.setAdapter(mAdapter);
        return view;
    }

    public void ShowEditDialog(final RukuInfo minfo) {
        lastPartNo = minfo.getPartno();
        FragmentActivity activity = getActivity();
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View itemview = LayoutInflater.from(activity).inflate(R.layout.dialog_rk_chaidan, null, false);
        mBuilder.setView(itemview);
        mBuilder.setTitle("拆单");
        final List<RukuInfo> mdatas = new ArrayList<>();
        mdatas.add(minfo);
        final ChaiDanEdit mAdapter = new ChaiDanEdit(activity, mdatas, R.layout.item_rk_chaidan_edit);
//        final RecyclerView.Adapter mAdapter2 = new ChaidaiEditRvAdapter( mdatas, R.layout.item_rk_chaidan_edit,activity);
        Button btnAdd = itemview.findViewById(R.id.dialog_rk_chaidan_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RukuInfo tempInfo = new RukuInfo(minfo);
                tempInfo.setCount(0);
                mdatas.add(tempInfo);
                mAdapter.notifyDataSetChanged();
//                mAdapter2.notifyDataSetChanged();
            }
        });
        ListView lv = itemview.findViewById(R.id.dialog_rk_chaidan_lv);
//        RecyclerView recyclerView = itemview.findViewById(R.id.dialog_rk_chaidan_rv);
//        LinearLayoutManager lm=new LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false);
//        recyclerView.setLayoutManager(lm);
//        recyclerView.setAdapter(mAdapter2);
        final TextView tvMsg = itemview.findViewById(R.id.dialog_rk_chaidan_tv_msg);
        final Button btnCommit = itemview.findViewById(R.id.dialog_rk_chaidan_btn_commit);

        lv.setAdapter(mAdapter);

        final int[] count = {0};

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count[0] == 0) {
                    StringBuilder sb = new StringBuilder();
                    for (RukuInfo mData : mdatas) {
                        //                    JSONObject mobj = new JSONObject();
                        //                    mobj.put("BatchNo", mData.getPihao());
                        //                    mobj.put("Number", mData.getCount() + "");
                        //                    marr.add(mobj);
                        sb.append(String.format("批号：%s,数量:%d\r\n", mData.getPihao(), mData.getCount()));
                    }
                    String msg = sb.toString();
                    tvMsg.setText(msg);
                    mParent.showMsgToast("如果结果无误，请再次点击拆单");
                    count[0]++;
                    return;
                }
                mViewModel.commitSplit(mdatas, loginid);
            }
        });
        mBuilder.setNegativeButton("取消", null);
        mBuilder.show();
    }

    static class ChaidaiEditRvAdapter extends BaseRvAdapter<RukuInfo> {


        public ChaidaiEditRvAdapter(List<RukuInfo> mData, int layoutId, Context mContext) {
            super(mData, layoutId, mContext);
        }

        @Override
        protected void convert(final BaseRvViewholder helper, final RukuInfo item) {
            helper.setText(R.id.item_rk_chaidan_edit_ed_count, "" + item.getCount());
            helper.setText(R.id.item_rk_chaidan_edit_ed_pihao, "" + item.getPihao());
            EditText edCount = helper.getView(R.id.item_rk_chaidan_edit_ed_count);
            EditText edPihao = helper.getView(R.id.item_rk_chaidan_edit_ed_pihao);
            edCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        item.setCount(Integer.parseInt(s.toString()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        if (mContext instanceof BaseMActivity) {
                            BaseMActivity mActivity = (BaseMActivity) mContext;
                            mActivity.showMsgToast("数量为必须为整数");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            edPihao.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setPihao(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }
    static class ChaiDanEdit extends CommonAdapter<RukuInfo> {
        SparseArray<List<TextWatcher> > mWatcher = new SparseArray<>();

        public ChaiDanEdit(Context context, List<RukuInfo> mDatas, int itemLayoutId) {
            super(context, mDatas, itemLayoutId);
        }
        @Override
        public void convert(final ViewHolder helper, final RukuInfo item) {
           EditText edCount = helper.getView(R.id.item_rk_chaidan_edit_ed_count);
            EditText edPihao = helper.getView(R.id.item_rk_chaidan_edit_ed_pihao);
            int position = helper.getPosition();



            TextWatcher countWatcher = new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        int nowCount = Integer.parseInt(s.toString());
                        item.setCount(nowCount);
//                        int position = helper.getPosition();
//                        if (position == 0) {
//
//                        } else {
//                            RukuInfo item1 = getItem(position);
//                            item1.setCount(item1.getCount() - nowCount);
////                            notifyDataSetChanged();
//                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        if (mContext instanceof BaseMActivity) {
                            BaseMActivity mActivity = (BaseMActivity) mContext;
                            mActivity.showMsgToast("数量为必须为整数");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };
            Object tag = edPihao.getTag();
            List<TextWatcher> listWatcher;
            if (tag != null) {
                listWatcher = (List<TextWatcher>) tag;
                TextWatcher oldCountWatcher = listWatcher.get(0);
                TextWatcher oldPihaoWatcher = listWatcher.get(1);
                edCount.removeTextChangedListener(oldCountWatcher);
                edPihao.removeTextChangedListener(oldPihaoWatcher);
                helper.setText(R.id.item_rk_chaidan_edit_ed_count, "" + item.getCount());
                helper.setText(R.id.item_rk_chaidan_edit_ed_pihao, "" + item.getPihao());

                listWatcher = new ArrayList<>();
                edPihao.setTag(listWatcher);
            } else {
                helper.setText(R.id.item_rk_chaidan_edit_ed_count, "" + item.getCount());
                helper.setText(R.id.item_rk_chaidan_edit_ed_pihao, "" + item.getPihao());

                listWatcher=new ArrayList<>();
                edPihao.setTag(listWatcher);
            }
            edCount.addTextChangedListener(countWatcher);
            TextWatcher pihaoWatcher =
                    new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count,
                                                              int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    item.setPihao(s.toString());
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            };
            edPihao.addTextChangedListener(pihaoWatcher);
            listWatcher.add(countWatcher);
            listWatcher.add(pihaoWatcher);
            mWatcher.put(position, listWatcher);
        }

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof BaseMActivity) {
            mParent = (BaseMActivity) getActivity();
        } else {
            throw new IllegalStateException("此fragment父类必须继承" + BaseMActivity.class.getName());
        }
        mViewModel = ViewModelProviders.of(this).get(RukuViewModel.class);
        mViewModel.registerView(this);
        LifecycleOwner mOwner = this;
        //        final List<RukuInfo> value = new ArrayList<>();
        mViewModel.getData().observe(mOwner, new Observer<List<RukuInfo>>() {
            @Override
            public void onChanged(@Nullable List<RukuInfo> rukuInfo) {
                value.clear();
                value.addAll(rukuInfo);
                mAdapter.notifyDataSetChanged();
                mParent.showMsgToast("查询到" + rukuInfo.size() + "条待拆单数据");
            }
        });
        mViewModel.getError().observe(mOwner, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                showError(s);
            }
        });
        mViewModel.getChaidan().observe(mOwner, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                showAlertMsg("拆单结果：" + s);
                mViewModel.startSearch("0", lastPartNo, storageid);
            }
        });
    }

    @Override
    public void showError(String msg) {
        mParent.showMsgDialog(msg);
    }


    @Override
    public int preLoading(String msg) {
        return mParent.showProgressWithID(msg);
    }

    public void showAlertMsg(String msg) {
         mParent.showMsgDialog(msg);
    }

    @Override
    public void cancelLoading(int id) {
        mParent.cancelDialogById(id);
    }

    public void startSearch(String code, String partno, String storId) {
        mViewModel.startSearch(code, partno,storId  );
    }
    public void onCamScanBack(String code, int reqCode) {
        if (reqCode == REQ_SEARCH) {
            mViewModel.startSearch(code, "", storageid);
        }
    }

    static class ChaidanAdapter extends BaseRvAdapter<RukuInfo> {
        public ChaidanAdapter(List<RukuInfo> mData, int layoutId, Context mContext,
                              MClickListener mListener) {
            super(mData, layoutId, mContext);
            this.mListener = mListener;
        }

        MClickListener mListener;

        interface MClickListener{
             void onClick(View v, RukuInfo item);
        }
        public ChaidanAdapter(List<RukuInfo> mData, int layoutId, Context mContext) {
            super(mData, layoutId, mContext);
        }

        @Override
        protected void convert(BaseRvViewholder holder, final RukuInfo item) {
            holder.setText(R.id.item_chaidan_tv_pid, "单据号=" + item.getPid());
            holder.setText(R.id.item_chaidan_tv_info, item.toString());

            holder.setOnclick(R.id.item_chaidan_btn_chaidan, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(v, item);
                    }
                }
            });
        }
    }
}
