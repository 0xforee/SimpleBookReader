package org.foree.bookreader.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;

public class ArticleFragment extends Fragment {
    private static final String ARG_CONTENT = "contents";

    private TextView tvContents;

    public static ArticleFragment newInstance(String contents) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT, contents);
        fragment.setArguments(args);
        return fragment;
    }

    public ArticleFragment() {

    }

    public void setText(String contents) {
        if (getArguments() != null)
            getArguments().putString(ARG_CONTENT, contents);
        if (tvContents != null) {
            tvContents.setText(contents);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, null);
        tvContents = (TextView) view.findViewById(R.id.book_content);
        if (getArguments() != null) {
            setText(getArguments().getString(ARG_CONTENT));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //tvContents.setText(Html.fromHtml(getArguments().getString(ARG_CONTENT)));

    }

}
