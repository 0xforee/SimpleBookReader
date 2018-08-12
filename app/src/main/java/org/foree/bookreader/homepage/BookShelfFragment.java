package org.foree.bookreader.homepage;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.readpage.ReadActivity;
import org.foree.bookreader.thread.SyncBooksThread;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class BookShelfFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = BookShelfFragment.class.getSimpleName();

    private ActionMode mActionMode;
    private BookDao bookDao;
    private BookShelfActivity bookShelfActivity;

    private RecyclerView mRecyclerView;
    private BookShelfAdapter mAdapter;
    private List<Book> bookList = new ArrayList<>();

    private Thread syncThread;

    SwipeRefreshLayout mSwipeRefreshLayout;

    private BookContentObserver bookObserver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                Log.d(TAG, "book list updated");
                mAdapter.notifyDataSetChanged();
            }

        }
    };

    public BookShelfFragment() {
        // Required empty public constructor
    }

    public static BookShelfFragment newInstance() {
        BookShelfFragment fragment = new BookShelfFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        bookDao = new BookDao(getContext());

        bookObserver = new BookContentObserver(null);

        registerContentObserver();
    }

    private void registerContentObserver() {
        getContext().getContentResolver().registerContentObserver(
                BReaderProvider.CONTENT_URI_BOOKS,
                false,
                bookObserver
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BaseActivity.KEY_RECREATE, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_shelf, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_book_shelf);

        initRecyclerAdapter();

        if (savedInstanceState != null && savedInstanceState.getBoolean(BaseActivity.KEY_RECREATE)) {
            Log.d(TAG, "onCreate: recreate activity");
        } else {
            // loading
            syncNovelInfo();
        }

        return view;
    }

    private void initRecyclerAdapter() {

        bookList = bookDao.getAllBooks();
        mAdapter = new BookShelfAdapter(getContext(), bookList);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mActionMode != null) {
                    onListItemSelect(position);
                } else {
                    Intent intent = new Intent(getActivity(), ReadActivity.class);
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


    // SwipeRefreshLayout onRefresh
    @Override
    public void onRefresh() {
        syncNovelInfo();
    }

    // 判断小说是否有更新
    private void syncNovelInfo() {
        if (!bookList.isEmpty()) {
            // on activity create , set refresh true manually
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);

                    }
                }, 100);
            }

            // if refresh start, get newer book info
            syncThread = new SyncBooksThread(bookDao);
            syncThread.start();

        } else {
            // bookList is empty, set refresh false
            resetRefreshState();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        bookShelfActivity = (BookShelfActivity) getActivity();


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        if (syncThread != null && !syncThread.isInterrupted())
            syncThread.interrupt();

        getContext().getContentResolver().unregisterContentObserver(bookObserver);
    }


    private void resetRefreshState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final BookUpdateEvent bookUpdateEvent) {
        Log.d(TAG, "onEventMainThread " + bookUpdateEvent.getUpdatedNum());

        int updatedNovelNum = bookUpdateEvent.getUpdatedNum();

        resetRefreshState();

        // update UI
        if (updatedNovelNum > 0) {
            String message = updatedNovelNum + "本小说更新啦";
            Snackbar.make(mSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
        } else {
            String message = "未更新";
            Snackbar.make(mSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();

        }
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);

        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            mActionMode = bookShelfActivity.startSupportActionMode(new ToolbarActionModeCallback());
        } else if (!hasCheckedItems) {
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount() + "  " +getString(R.string.bookshelf_edit_tips)));
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

    class BookContentObserver extends ContentObserver {

        public BookContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "data changed");
            bookList.clear();
            bookList.addAll(bookDao.getAllBooks());
            mHandler.sendEmptyMessage(0);
        }
    }
}
