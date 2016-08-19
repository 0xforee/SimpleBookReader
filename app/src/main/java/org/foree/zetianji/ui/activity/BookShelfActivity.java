package org.foree.zetianji.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.foree.zetianji.R;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.service.RefreshService;

public class BookShelfActivity extends AppCompatActivity implements CardView.OnClickListener, RefreshService.StreamCallBack, SwipeRefreshLayout.OnRefreshListener{

    private RefreshService.MyBinder mBinder;
    private RefreshService mRefreshService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private static final int MSG_UPDATE_NOVEL = 0;

    Handler mHandler;
    CardView cardView;
    Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView tvNovelAuthor, tvNovelName, tvNovelCategory, tvNovelStatus, tvNovelUpdateTime, tvNovelUpdateChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        cardView = (CardView) findViewById(R.id.novel_card);
        cardView.setOnClickListener(this);

        initNovelTextView();

        // start refresh service
        Intent intent = new Intent(this, RefreshService.class);
        startService(intent);
        bindService(intent, mServiceConnect, BIND_AUTO_CREATE);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_UPDATE_NOVEL:
                        updateNovelInfo((Novel)msg.obj);

                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(BookShelfActivity.this, ChapterListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnect);
        super.onDestroy();
    }

    @Override
    public void notifyUpdate(final Novel novel) {
        mSwipeRefreshLayout.setRefreshing(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateNovelInfo(novel);
            }
        },15);
    }
    private void updateNovelInfo(Novel novel){
        tvNovelUpdateChapter.setText(getString(R.string.update_chapter_string) + novel.getNewest_chapter().getTitle());
        tvNovelUpdateTime.setText(getString(R.string.update_time_string) + novel.getUpdate_time());
    }

    private void initNovelTextView() {
        //tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        //tvNovelCategory = (TextView) findViewById(R.id.tv_novel_category);
       // tvNovelName = (TextView)findViewById(R.id.tv_novel_name);
      //  tvNovelStatus = (TextView)findViewById(R.id.tv_novel_status);
        tvNovelUpdateTime = (TextView)findViewById(R.id.tv_novel_update_time);
        tvNovelUpdateChapter = (TextView)findViewById(R.id.tv_novel_update_chapter);
    }

    @Override
    public void onRefresh() {
        if( mRefreshService != null){
            mSwipeRefreshLayout.setRefreshing(true);
            mRefreshService.updateNovelInfo(2);
        }
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
