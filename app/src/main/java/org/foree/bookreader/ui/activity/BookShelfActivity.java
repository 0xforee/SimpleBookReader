package org.foree.bookreader.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.igexin.sdk.PushManager;

import org.foree.bookreader.R;
import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.data.dao.BookDao;
import org.foree.bookreader.data.event.BookUpdateEvent;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;
import org.foree.bookreader.ui.adapter.BookShelfAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BookShelfActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = BookShelfActivity.class.getSimpleName();

    private ActionMode mActionMode;
    private BookDao bookDao;

    Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private BookShelfAdapter mAdapter;
    private List<Book> bookList = new ArrayList<>();

    private ThreadPoolExecutor executor;
    private int updatedBookNum;
    private int updatedNovelNum = 0;
    private long startTime;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        EventBus.getDefault().register(this);

        setUpLayoutViews();

        PushManager.getInstance().initialize(this.getApplicationContext());

        // init thread about
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");

        // refresh booklist
        bookList.clear();
        bookList.addAll(bookDao.getAllBooks());
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        executor.shutdown();
    }

    // SwipeRefreshLayout onRefresh
    @Override
    public void onRefresh() {
        syncNovelInfo();
    }

    // 判断小说是否有更新
    private void syncNovelInfo() {
        updatedBookNum = bookList.size();
        startTime = System.currentTimeMillis();
        // 获取更新
        for (final Book oldBook : bookList) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    AbsWebParser webParser = WebParserManager.getInstance().getWebParser(oldBook.getBookUrl());
                    Book newBook = webParser.getBookInfo(oldBook.getBookUrl());
                    if (isUpdated(oldBook.getUpdateTime(), newBook.getUpdateTime())) {
                        // update chapters
                        EventBus.getDefault().post(new BookUpdateEvent(newBook, true));
                    } else {
                        EventBus.getDefault().post(new BookUpdateEvent(newBook, false));
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final BookUpdateEvent bookUpdateEvent) {
        Log.d(TAG, "onEventMainThread " + bookUpdateEvent.getBook().getBookUrl());
        // 若有更新，更新章节列表
        if (bookUpdateEvent.isUpdateChapters()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Book book = bookUpdateEvent.getBook();
                    AbsWebParser webParser = WebParserManager.getInstance().getWebParser(book.getBookUrl());
                    List<Chapter> chapters = webParser.getChapterList(book.getBookUrl(), book.getContentUrl());
                    if (chapters != null) {
                        bookDao.insertChapters(chapters);
                        bookDao.updateBookTime(book.getBookUrl(), book.getUpdateTime());
                    }
                }
            });
            updatedNovelNum++;
        }
        updatedBookNum--;
        if (updatedBookNum == 0) {
            Log.d(TAG, "costs " + (System.currentTimeMillis() - startTime) + " ms to check update");

            // refresh false
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout.isRefreshing())
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 500);

            // update UI
            if (updatedNovelNum > 0) {
                String message = updatedNovelNum + "本小说更新啦";
                Snackbar.make(mSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
            } else {
                String message = "未更新";
                Snackbar.make(mSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();

            }
        }

    }

    private boolean isUpdated(String oldUpdateTime, String newUpdateTime) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        try {
            Date oldDate = simpleFormat.parse(oldUpdateTime);
            Date newDate = simpleFormat.parse(newUpdateTime);

            if (newDate.after(oldDate)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setUpLayoutViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_book_shelf);

        setUpRecyclerViewAdapter();
    }

    private void setUpRecyclerViewAdapter() {
        bookDao = new BookDao(this);

        bookList = bookDao.getAllBooks();
        mAdapter = new BookShelfAdapter(this, bookList);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mActionMode != null) {
                    onListItemSelect(position);
                } else {
                    Intent intent = new Intent(BookShelfActivity.this, ReadActivity.class);
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
                bookDao.removeBook(bookList.get(selected.keyAt(i)).getBookUrl());
                bookList.remove(selected.keyAt(i));
                mAdapter.notifyDataSetChanged();
            }
        }

        mActionMode.finish();
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
            if (mActionMode != null)
                mActionMode = null;
        }
    }
}
