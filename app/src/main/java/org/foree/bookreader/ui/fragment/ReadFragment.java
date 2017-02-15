package org.foree.bookreader.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;

public class ReadFragment extends Fragment {
    private static final String ARG_CONTENT = "contents";
    private static final String ARG_TITLE = "title";

    private TextView tvContents, tvTitle;

    public static ReadFragment newInstance(String title, String contents) {
        ReadFragment fragment = new ReadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT, contents);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public ReadFragment() {

    }

    public void setText(String title, String contents) {
        if (getArguments() != null) {
            getArguments().putString(ARG_CONTENT, contents);
        }
        if (tvContents != null) {
            tvContents.setText(contents);
        }

        if(tvTitle != null){
            tvTitle.setText(title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, null);
        tvContents = (TextView) view.findViewById(R.id.book_content);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        if (getArguments() != null) {
            setText(getArguments().getString(ARG_TITLE),
                    getArguments().getString(ARG_CONTENT));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //tvContents.setText(Html.fromHtml(getArguments().getString(ARG_CONTENT)));

    }

}
