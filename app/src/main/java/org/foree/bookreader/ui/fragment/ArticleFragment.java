package org.foree.bookreader.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;

public class ArticleFragment extends Fragment {
    private TextView tvContents;

    public static ArticleFragment newInstance(String contents) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString("content", contents);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.module_text_view, null);
        tvContents = (TextView) view.findViewById(R.id.book_content);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        tvContents.setText(Html.fromHtml(getArguments().getString("content")));

    }

}
