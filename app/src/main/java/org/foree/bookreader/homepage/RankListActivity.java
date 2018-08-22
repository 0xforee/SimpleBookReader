package org.foree.bookreader.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import org.foree.bookreader.searchpage.SearchListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foree
 * @date 2018/8/18
 * @description
 */
public class RankListActivity extends BaseActivity {
    private static final String TAG = "RankListActivity";
    private RecyclerView mRecyclerView;
    private SearchListAdapter mAdapter;
    private List<Book> bookList = new ArrayList<>();
    Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private String mRankId;
    private String mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranklist);

        mRankId = getIntent().getStringExtra("rankId");
        mTitle = getIntent().getStringExtra("title");

        Log.d(TAG, "[foree] onCreate: rankid = " + mRankId + ", title = " + mTitle);
        initViews();
        initData();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setTitle(mTitle);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mSwipeRefreshLayout.setEnabled(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_book_shelf);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new SearchListAdapter(this, bookList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new SearchListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(RankListActivity.this, BookInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("book_url", bookList.get(position).getBookUrl());
                bundle.putString("source_key", bookList.get(position).getSourceKey());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void initData() {
        WebParser.getInstance().getRankListAsync(mRankId, new NetCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> data) {
                bookList.clear();
                bookList.addAll(data);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();

                    }
                });
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // 重置背景颜色，不然从书籍详情页返回，会导致toolbar颜色异常
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }
}
