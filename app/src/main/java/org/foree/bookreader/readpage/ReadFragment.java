package org.foree.bookreader.readpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;

import java.util.Calendar;

public class ReadFragment extends Fragment {
    private static final String ARG_TITLE = "title";

    private TextView tvContents, tvTitle, tvTime, tvIndex, tvPageNum, tvSeparator;
    private View rootView;

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

        return rootView;
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
}
