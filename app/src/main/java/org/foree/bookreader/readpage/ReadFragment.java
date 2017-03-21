package org.foree.bookreader.readpage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.ReadPageDataSet;
import org.foree.bookreader.settings.SettingsActivity;

import java.util.Calendar;

public class ReadFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String ARG_TITLE = "title";

    private TextView tvContents, tvTitle, tvTime, tvIndex, tvPageNum;

    private SharedPreferences nightPreference;

    public static ReadFragment newInstance(ReadPageDataSet readPageDataSet) {
        ReadFragment fragment = new ReadFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TITLE, readPageDataSet);
        fragment.setArguments(args);
        return fragment;
    }

    public ReadFragment() {

    }

    private void setData(ReadPageDataSet readPageDataSet) {
        if (tvContents != null) {
            tvContents.setText(readPageDataSet.getContents());
        }

        if (tvTitle != null) {
            tvTitle.setText(readPageDataSet.getTitle());
        }

        tvTime.setText(getCurrentTime());
        tvPageNum.setText(readPageDataSet.getPageNum());
        tvIndex.setText(readPageDataSet.getIndex());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, null);
        tvContents = (TextView) view.findViewById(R.id.book_content);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTime = (TextView) view.findViewById(R.id.tv_time);
        tvIndex = (TextView) view.findViewById(R.id.tv_index);
        tvPageNum = (TextView) view.findViewById(R.id.tv_page_num);
        if (getArguments() != null) {
            setData((ReadPageDataSet) getArguments().getSerializable(ARG_TITLE));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //tvContents.setData(Html.fromHtml(getArguments().getString(ARG_CONTENT)));

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nightPreference = getContext().getSharedPreferences(SettingsActivity.KEY_PREF_NIGHT_MODE, Context.MODE_PRIVATE);
        nightPreference.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nightPreference.unregisterOnSharedPreferenceChangeListener(this);
    }

    private String getCurrentTime() {
        Calendar car = Calendar.getInstance();
        int hour = car.get(Calendar.HOUR_OF_DAY);
        int min = car.get(Calendar.MINUTE);

        if (min < 10)
            return hour + ":0" + min;
        else
            return hour + ":" + min;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
