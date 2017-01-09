package org.foree.bookreader.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.igexin.sdk.PushManager;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.service.RefreshService;
import org.foree.bookreader.ui.fragment.ItemListAdapter;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

import java.util.ArrayList;
import java.util.List;

public class ChapterListActivity extends AppCompatActivity implements RefreshService.StreamCallBack{
    Toolbar toolbar;
    BookDao bookDao;
    FloatingActionButton testFloatingButton;
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private List<Chapter> chapterList = new ArrayList<>();
    private RefreshService.MyBinder mBinder;
    private String bookUrl;
    private RefreshService mStreamService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapterlist);

        bookDao = new BookDao(this);

        Bundle bundle = getIntent().getExtras();
        bookUrl = bundle.getString("book_url");

        setUpLayoutViews();

        PushManager.getInstance().initialize(this.getApplicationContext());

        // bind service
        Intent refreshIntent = new Intent(this, RefreshService.class);
        bindService(refreshIntent, mServiceConnect, BIND_AUTO_CREATE);

        syncChapterList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnect);
    }

    private void setUpLayoutViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        // get FloatActionButton
        testFloatingButton = (FloatingActionButton)findViewById(R.id.fab);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mStreamService != null){
                    mStreamService.downloadNovel(chapterList);
                }
            }
        });

        setUpRecyclerViewAdapter();
    }

    private void setUpRecyclerViewAdapter() {
        mAdapter = new ItemListAdapter(this, chapterList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ChapterListActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putSerializable("chapter", chapterList.get(position));
                bundle.putString("url",chapterList.get(position).getUrl());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void syncChapterList(){
        // downloadNovel
//        webSiteInfo = bookDao.findWebSiteById(2);
//        absWebSiteHelper  = new BQGWebSiteHelper(webSiteInfo);
//        absWebSiteHelper.getNovel(new NetCallback<Book>() {
//            @Override
//            public void onSuccess(final Book data) {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        chapterList.clear();
//                        chapterList.addAll(data.getChapter_list());
//                        bookDao.insertChapterList(chapterList);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                },0);
//
//            }
//
//            @Override
//            public void onFail(String msg) {
//                Toast.makeText(ChapterListActivity.this, "getContentListError: " + msg, Toast.LENGTH_LONG).show();
//            }
//        });

        WebInfo webInfo = new BiQuGeWebInfo();
        webInfo.getBookInfo(bookUrl, new NetCallback<Book>() {
            @Override
            public void onSuccess(final Book data) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chapterList.clear();
                        chapterList.addAll(data.getChapter_list());
                        bookDao.insertChapterList(chapterList);
                        mAdapter.notifyDataSetChanged();
                    }
                },0);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    @Override
    public void notifyUpdateCallBack(Book book) {

    }

    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (RefreshService.MyBinder) iBinder;
            mStreamService = mBinder.getService();
            mStreamService.registerCallBack(ChapterListActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }
}
