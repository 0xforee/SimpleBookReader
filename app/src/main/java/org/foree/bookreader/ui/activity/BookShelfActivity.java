package org.foree.bookreader.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.helper.BQGWebSiteHelper;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.service.RefreshService;
import org.foree.bookreader.ui.fragment.BookListAdapter;
import org.foree.bookreader.ui.fragment.ItemListAdapter;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

import java.util.ArrayList;
import java.util.List;

public class BookShelfActivity extends AppCompatActivity implements RefreshService.StreamCallBack, SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = BookShelfActivity.class.getSimpleName();
    private RefreshService.MyBinder mBinder;
    private RefreshService mRefreshService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private static final int MSG_UPDATE_NOVEL = 0;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

            }
        }
    };
    CardView cardView;
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

        // start refresh service
        Intent intent = new Intent(this, RefreshService.class);
        startService(intent);
        bindService(intent, mServiceConnect, BIND_AUTO_CREATE);

        // start refresh
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncNovelInfo();
            }
        }, 300);
    }

//    @Override
//    public void onClick(View view) {
//        Intent intent = new Intent(BookShelfActivity.this, ChapterListActivity.class);
//        startActivity(intent);
//    }

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
        if( mRefreshService != null){
            mSwipeRefreshLayout.setRefreshing(true);
            mRefreshService.updateNovelInfo(2);
        }else {
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
                if( book != null) {
                    refreshNovelViews(book);
                }
            }
        },15);
    }

    private void setUpLayoutViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_book_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        //cardView = (CardView) findViewById(R.id.novel_card);
        //cardView.setOnClickListener(this);

        WebInfo webInfo = new BiQuGeWebInfo();
        webInfo.getArticle("http://www.biquge.com/11_11298/7058348.html", new NetCallback<Article>() {
            @Override
            public void onSuccess(Article data) {

            }

            @Override
            public void onFail(String msg) {

            }
        });

        //tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
       // tvNovelCategory = (TextView) findViewById(R.id.tv_novel_category);
       // tvNovelName = (TextView)findViewById(R.id.tv_novel_name);
       // tvNovelStatus = (TextView)findViewById(R.id.tv_novel_status);
        tvNovelUpdateTime = (TextView)findViewById(R.id.tv_novel_update_time);
        tvNovelUpdateChapter = (TextView)findViewById(R.id.tv_novel_update_chapter);


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
                Intent intent = new Intent(BookShelfActivity.this, ChapterListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("book_url",bookList.get(position).getUrl());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void refreshNovelViews(Book book){
//        tvNovelUpdateChapter.setText(getString(R.string.update_chapter_string) + book.getNewest_chapter().getTitle());
 //       tvNovelUpdateTime.setText(getString(R.string.update_time_string) + book.getUpdate_time());
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
}
