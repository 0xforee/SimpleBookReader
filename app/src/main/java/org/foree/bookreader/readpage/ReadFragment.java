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

    private TextView tvContents, tvTitle, tvTime, tvIndex, tvPageNum, tvSeparator;
    private View rootView;
    private boolean mNightMode;

    private SharedPreferences mNightPreference;

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
        tvSeparator.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_read, null);
        tvContents = (TextView) rootView.findViewById(R.id.book_content);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvTime = (TextView) rootView.findViewById(R.id.tv_time);
        tvIndex = (TextView) rootView.findViewById(R.id.tv_index);
        tvPageNum = (TextView) rootView.findViewById(R.id.tv_page_num);
        tvSeparator = (TextView) rootView.findViewById(R.id.tv_separator);
        if (getArguments() != null) {
            setData((ReadPageDataSet) getArguments().getSerializable(ARG_TITLE));
        }

        changeTheme(mNightMode);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNightPreference = getContext().getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
        mNightMode = mNightPreference.getBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNightPreference.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNightPreference.unregisterOnSharedPreferenceChangeListener(this);
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
        if(key.equals(SettingsActivity.KEY_PREF_NIGHT_MODE)){
            Boolean nightMode = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, false);
            changeTheme(nightMode);
        }
    }

    private void changeTheme(Boolean nightMode) {
        int backgroundColor = getResources().getColor(R.color.classical_page_background);
        int textColor = getResources().getColor(R.color.md_black_1000);
//        if(nightMode){
//            // set mNightMode
//            backgroundColor = getResources().getColor(R.color.nightBackground);
//            textColor = getResources().getColor(R.color.nightTextColor);
//            rootView.setBackgroundColor(backgroundColor);
//            tvTitle.setTextColor(textColor);
//            tvContents.setTextColor(textColor);
//            tvTime.setTextColor(textColor);
//            tvPageNum.setTextColor(textColor);
//            tvIndex.setTextColor(textColor);
//        }else{
//            rootView.setBackgroundColor(backgroundColor);
//            tvTitle.setTextColor(textColor);
//            tvContents.setTextColor(textColor);
//            tvIndex.setTextColor(textColor);
//            tvPageNum.setTextColor(textColor);
//            tvTime.setTextColor(textColor);
//        }
    }


}
