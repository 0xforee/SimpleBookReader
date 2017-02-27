package org.foree.bookreader.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.ReadPageDataEvent;

import java.util.Calendar;

public class ReadFragment extends Fragment {
    private static final String ARG_TITLE = "title";

    private TextView tvContents, tvTitle, tvTime, tvIndex, tvPageNum;

    public static ReadFragment newInstance(ReadPageDataEvent readPageDataEvent) {
        ReadFragment fragment = new ReadFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TITLE, readPageDataEvent);
        fragment.setArguments(args);
        return fragment;
    }

    public ReadFragment() {

    }

    private void setData(ReadPageDataEvent readPageDataEvent) {
        if (tvContents != null) {
            tvContents.setText(readPageDataEvent.getContents());
        }

        if (tvTitle != null) {
            tvTitle.setText(readPageDataEvent.getTitle());
        }

        tvTime.setText(getCurrentTime());
        tvPageNum.setText(readPageDataEvent.getPageNum());
        tvIndex.setText(readPageDataEvent.getIndex());
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
            setData((ReadPageDataEvent) getArguments().getSerializable(ARG_TITLE));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //tvContents.setData(Html.fromHtml(getArguments().getString(ARG_CONTENT)));

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
