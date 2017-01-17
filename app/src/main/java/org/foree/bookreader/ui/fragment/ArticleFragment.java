package org.foree.bookreader.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;

import java.util.Locale;

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

    public void setText(String contents) {
        getArguments().putString(ARG_CONTENT, contents);
        if (tvContents != null) {
            tvContents.setText(Html.fromHtml(contents));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.module_text_view, null);
        tvContents = (TextView) view.findViewById(R.id.book_content);
        setText(getArguments().getString(ARG_CONTENT));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //tvContents.setText(Html.fromHtml(getArguments().getString(ARG_CONTENT)));

    }

}
