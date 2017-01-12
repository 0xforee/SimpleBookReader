package org.foree.bookreader.ui.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.igexin.sdk.PushManager;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.service.RefreshService;
import org.foree.bookreader.ui.fragment.BookListAdapter;

import java.util.ArrayList;
import java.util.List;

public class BookShelfActivity extends AppCompatActivity implements RefreshService.StreamCallBack, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = BookShelfActivity.class.getSimpleName();
    private RefreshService.MyBinder mBinder;
    private RefreshService mRefreshService;
    private ActionMode mActionMode;
    private BookDao bookDao;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private static final int MSG_UPDATE_NOVEL = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

            }
        }
    };
    Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private BookListAdapter mAdapter;
    private List<Book> bookList = new ArrayList<>();

    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView tvNovelAuthor, tvNovelName, tvNovelCategory, tvNovelStatus, tvNovelUpdateTime, tvNovelUpdateChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        setUpLayoutViews();

        PushManager.getInstance().initialize(this.getApplicationContext());

        bookDao = new BookDao(this);

        // start refresh service
        Intent intent = new Intent(this, RefreshService.class);
        startService(intent);
        bindService(intent, mServiceConnect, BIND_AUTO_CREATE);

        // start refresh
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //syncNovelInfo();
            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        mRefreshService.unregisterCallBack();
        unbindService(mServiceConnect);
        super.onDestroy();
    }

    // SwipeRefreshLayout onRefresh
    @Override
    public void onRefresh() {
        syncNovelInfo();
    }

    private void syncNovelInfo() {
        if (mRefreshService != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            //mRefreshService.updateNovelInfo(2);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mSwipeRefreshLayout, "出了什么问题，请稍后再试", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notifyUpdateCallBack(final Book book) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);

                // refresh success, update text
                if (book != null) {
                    refreshNovelViews(book);
                }
            }
        }, 15);
    }

    private void setUpLayoutViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_book_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        // tvNovelCategory = (TextView) findViewById(R.id.tv_novel_category);
        // tvNovelName = (TextView)findViewById(R.id.tv_novel_name);
        // tvNovelStatus = (TextView)findViewById(R.id.tv_novel_status);
        tvNovelUpdateTime = (TextView) findViewById(R.id.tv_novel_update_time);
        tvNovelUpdateChapter = (TextView) findViewById(R.id.tv_novel_update_chapter);


        setUpRecyclerViewAdapter();
    }

    private void setUpRecyclerViewAdapter() {
        BookDao bookDao = new BookDao(this);
        bookList = bookDao.findAllBookList();
        mAdapter = new BookListAdapter(this, bookList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BookListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mActionMode != null) {
                    onListItemSelect(position);
                } else {
                    Intent intent = new Intent(BookShelfActivity.this, ArticleActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("book_url", bookList.get(position).getBookUrl());
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                onListItemSelect(position);
            }
        });
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);

        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            mActionMode = this.startSupportActionMode(new ToolbarActionModeCallback());
        } else if (!hasCheckedItems) {
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount() + "selected"));
        }
    }

    private void deleteBooks() {
        SparseBooleanArray selected = mAdapter.getSelectedItemsIds();
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                bookDao.removeBookInfo(bookList.get(i).getBookUrl());
                bookList.remove(i);
                mAdapter.notifyDataSetChanged();
            }
        }

        mActionMode.finish();
    }

    private void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    private void refreshNovelViews(Book book) {
//        tvNovelUpdateChapter.setText(getString(R.string.update_chapter_string) + book.getNewestChapter().getChapterTitle());
        //       tvNovelUpdateTime.setText(getString(R.string.update_time_string) + book.getUpdateTime());
    }

    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (RefreshService.MyBinder) iBinder;
            mRefreshService = mBinder.getService();
            mRefreshService.registerCallBack(BookShelfActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with searchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    class ToolbarActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteBooks();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.removeSelection();
            setNullToActionMode();
        }
    }
}
