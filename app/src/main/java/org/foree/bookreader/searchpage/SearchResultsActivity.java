package org.foree.bookreader.searchpage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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
    EditText mEtSearchText;
    private String mSearchString;
    SwipeRefreshLayout mSwipeRefreshLayout;
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

        // start from bookshelf activity
        if (savedInstanceState == null) {
            // hide first
            hideToolBarContent();

            ViewTreeObserver viewTreeObserver = toolbar.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    Transition transition = new AutoTransition();
                    transition.setDuration(200);
                    //toolbar fade in
                    TransitionManager.beginDelayedTransition(toolbar, transition);

                    showToolBarContent();

                }
            });
        }
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mEtSearchText = (EditText) toolbar.findViewById(R.id.dt_search_text);
        mEtSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchString = textView.getText().toString();
                    hideKeyBoard();
                    mSwipeRefreshLayout.setRefreshing(true);
                    handlerSearch(mSearchString);

                }
                return false;
            }
        });

    }

    private void handlerSearch(String query) {
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
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 0);

            }

            @Override
            public void onFail(String msg) {
                if (mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void hideKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            mEtSearchText.setText(null);
            mEtSearchText.requestFocus();
            //showKeyboard();
            bookList.clear();
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToMain() {

    }


    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        // do transition animation
        Transition transition = new AutoTransition();
        transition.setDuration(250);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                SearchResultsActivity.super.finish();
                // not do window animation
                overridePendingTransition(0, 0);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(toolbar, transition);
        hideToolBarContent();

    }

    private void hideToolBarContent() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            toolbar.getChildAt(i).setVisibility(View.GONE);
        }
    }

    private void showToolBarContent() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            toolbar.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }
}
