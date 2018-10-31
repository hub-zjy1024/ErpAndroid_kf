package com.b1b.js.erpandroid_kf;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChukuBaseFragment extends Fragment implements View.OnClickListener {

    public ChukuBaseFragment() {
        // Required empty public constructor
    }

    public void addArgLoginId(String id) {
        Bundle b = new Bundle();
        b.putString("loginID", id);
        setArguments(b);
    }
    public  String getFormatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public String getStringDateBefore(int day) {
        Calendar c = Calendar.getInstance(); // 当时的日期和时间
        int oldtime = c.get(Calendar.DAY_OF_MONTH) - day;
        c.set(Calendar.DAY_OF_MONTH, oldtime);
        return getFormatDate(c.getTime());
    }
    protected void setTvTime(final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                textView.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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
