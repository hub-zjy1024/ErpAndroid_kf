package com.b1b.js.erpandroid_kf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link SFFragment.OnFragmentInteractionListener} interface
 to handle interaction events. */
public class SFFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Spinner spiType;

    public SFFragment() {
        // Required empty public constructor
    }

    private Context mContext ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sf, null, false);
        spiType = (Spinner) v.findViewById(R.id.sf_frag_spi_type);
        String[] serverTypes = new String[]{"2-顺丰隔日(陆)", "1-顺丰次日(空)", "5-顺丰次晨",
                "6-顺丰即日", "7-物流普运", "18-重货快运"};
        spiType.setAdapter(new ArrayAdapter<String>(mContext, R.layout.item_province,
                R.id.item_province_tv, serverTypes));
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     This interface must be implemented by activities that contain this
     fragment to allow an interaction in this fragment to be communicated
     to the activity and potentially other fragments contained in that
     activity.
     See the Android Training lesson <a href=
     "http://developer.android.com/training/basics/fragments/communicating.html"
     >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
