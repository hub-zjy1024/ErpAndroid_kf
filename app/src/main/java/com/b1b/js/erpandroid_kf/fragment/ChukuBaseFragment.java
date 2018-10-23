package com.b1b.js.erpandroid_kf.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.b1b.js.erpandroid_kf.R;

public class ChukuBaseFragment extends Fragment implements View.OnClickListener {


    public ChukuBaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChukuBaseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChukuBaseFragment newInstance(String param1, String param2) {
        ChukuBaseFragment fragment = new ChukuBaseFragment();
        return fragment;
    }

    public Button getBtnSearch() {
        return null;
    }

    public EditText getEdPid() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chuku_base, container, false);
    }

    @Override
    public void onClick(View v) {

    }
}
