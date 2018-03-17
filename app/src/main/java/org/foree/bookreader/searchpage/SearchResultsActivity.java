package org.foree.bookreader.searchpage;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bookinfopage.BookInfoActivity;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.WebParser;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends BaseActivity {
    private static final String TAG = SearchResultsActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private SearchListAdapter mAdapter;
    private List<Book> bookList = new ArrayList<>();
    Toolbar toolbar;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        handlerIntent(getIntent());

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_book_shelf);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new SearchListAdapter(this, bookList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new SearchListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SearchResultsActivity.this, BookInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("book_url", bookList.get(position).getBookUrl());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlerIntent(intent);
    }

    private void handlerIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "query keywords = " + query);

            WebParser.getInstance().searchBookAsync(query, new NetCallback<List<Book>>() {
                @Override
                public void onSuccess(final List<Book> data) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bookList.clear();
                            bookList.addAll(data);
                            mAdapter.notifyDataSetChanged();
                        }
                    }, 0);

                }

                @Override
                public void onFail(String msg) {

                }
            });
        }
    }
}
