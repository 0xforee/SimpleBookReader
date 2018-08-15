package org.foree.bookreader.readpage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.settings.SettingsActivity;

import java.util.Calendar;

public class ReadFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String ARG_TITLE = "title";

    private TextView mTvContent, tvTitle, tvTime, tvIndex, tvBatteryLevel;
    private ImageView mIvBatteryIcon;
    private View rootView;
    private String mBatteryLevel;

    public static ReadFragment newInstance() {
        ReadFragment fragment = new ReadFragment();
        // Bundle args = new Bundle();
        // args.putSerializable(ARG_TITLE, readPageDataSet);
        // fragment.setArguments(args);
        return fragment;
    }

    public ReadFragment() {

    }

    public void setData(ReadPageDataSet readPageDataSet) {
        if (readPageDataSet != null) {
            if (mTvContent != null) {
                mTvContent.setText(readPageDataSet.getContents());
            }

            if (tvTitle != null) {
                tvTitle.setText(readPageDataSet.getTitle());
            }

            String index = (readPageDataSet.getIndex() + 1) + "/" + readPageDataSet.getPageNum();
            tvTime.setText(getCurrentTime());
            tvIndex.setText(index);
            tvBatteryLevel.setVisibility(View.VISIBLE);
            tvBatteryLevel.setText(mBatteryLevel);
            mIvBatteryIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_level));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_read, null);
        mTvContent = (TextView) rootView.findViewById(R.id.book_content_layout);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvTime = (TextView) rootView.findViewById(R.id.tv_time);
        tvIndex = (TextView) rootView.findViewById(R.id.tv_index);
        tvBatteryLevel = (TextView) rootView.findViewById(R.id.battery_level);
        mIvBatteryIcon = (ImageView) rootView.findViewById(R.id.battery_icon);

        // set default textSize
        float textSize = PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_SIZE,
                mTvContent.getTextSize() / mTvContent.getPaint().density);
        mTvContent.setTextSize(textSize);

        // load line spacing
        float lineSpacing = PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_LINE_SPACING,
                mTvContent.getLineSpacingExtra());
        mTvContent.setLineSpacing(lineSpacing, 1);

        rootView.setBackgroundColor(GlobalConfig.getInstance().getPageBackground());
        return rootView;
    }

    public void updateBatteryLevel(int level) {
        mBatteryLevel = level + "";
        if (tvBatteryLevel != null) {
            tvBatteryLevel.setText(mBatteryLevel);
        }
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
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.KEY_PREF_PAGE_BACKGROUND)) {
            rootView.setBackgroundColor(GlobalConfig.getInstance().getPageBackground());
        } else if (key.equals(SettingsActivity.KEY_READ_PAGE_TEXT_SIZE)) {
            mTvContent.setTextSize(sharedPreferences.getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_SIZE,
                    mTvContent.getTextSize() / mTvContent.getPaint().density));
        } else if (key.equals(SettingsActivity.KEY_READ_PAGE_TEXT_LINE_SPACING)) {
            mTvContent.setLineSpacing(sharedPreferences.getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_LINE_SPACING,
                    mTvContent.getLineSpacingExtra()), 1);
        }
    }
}
