package org.foree.zetianji.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.igexin.sdk.PushManager;

import org.foree.zetianji.R;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.dao.NovelDao;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.foree.zetianji.helper.WebSiteInfo;
import org.foree.zetianji.net.NetCallback;
import org.foree.zetianji.service.RefreshService;
import org.foree.zetianji.ui.fragment.ItemListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterListActivity extends AppCompatActivity implements RefreshService.StreamCallBack{
    Toolbar toolbar;
    NovelDao novelDao;
    FloatingActionButton testFloatingButton;
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private List<Chapter> chapterList = new ArrayList<>();
    BQGWebSiteHelper absWebSiteHelper;
    WebSiteInfo webSiteInfo;
    private RefreshService.MyBinder mBinder;
    private RefreshService mStreamService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        novelDao = new NovelDao(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        PushManager.getInstance().initialize(this.getApplicationContext());

        // bind service
        Intent intent = new Intent(this, RefreshService.class);
        bindService(intent, mServiceConnect, BIND_AUTO_CREATE);

        // get FloatActionButton
        testFloatingButton = (FloatingActionButton)findViewById(R.id.fab);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mStreamService != null){
                    mStreamService.getChapterList(1);
                }
            }
        });
        initAdapter();

        syncDate();
    }
    private void initAdapter() {
        mAdapter = new ItemListAdapter(this, chapterList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ChapterListActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("chapter", chapterList.get(position));
                bundle.putString("web_char",absWebSiteHelper.getWebsiteCharSet());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void syncDate(){
        // getChapterList
        webSiteInfo = novelDao.findWebSiteById(2);
        absWebSiteHelper  = new BQGWebSiteHelper(webSiteInfo);
        absWebSiteHelper.getNovel(new NetCallback<Novel>() {
            @Override
            public void onSuccess(Novel data) {
                chapterList.clear();
                chapterList.addAll(data.getChapter_list());
                novelDao.insertChapterList(chapterList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(ChapterListActivity.this, "getContentListError: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void notifyUpdate() {

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
