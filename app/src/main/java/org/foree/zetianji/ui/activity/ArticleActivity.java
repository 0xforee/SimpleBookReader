package org.foree.zetianji.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.foree.zetianji.R;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.foree.zetianji.net.NetCallback;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    BQGWebSiteHelper apiHelper;
    private static final String TAG = ArticleActivity.class.getSimpleName();
    TextView tv;
    Chapter chapter;
    FloatingActionButton turnNightMode;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    String webChar;
    Toolbar toolbar;
    boolean turnFlag = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv = (TextView)findViewById(R.id.tv_content);

        Bundle bundle = getIntent().getExtras();
        chapter = (Chapter)bundle.getSerializable("chapter");
        webChar = bundle.getString("web_char");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ActionBar ab = getSupportActionBar();
        if( ab != null)
            ab.setTitle(chapter.getTitle());
        // toolbar设置标题不管用....
        //toolbar.setTitle(chapter.getTitle());

        // get FloatActionButton
        turnNightMode = (FloatingActionButton)findViewById(R.id.fab);
        turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(turnFlag) {
                    // night Mode
                    tv.setTextColor(getResources().getColor(R.color.nightTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.nightBackground));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.nightBackground));
                    turnFlag = false;
                }else{
                    // day Mode
                    tv.setTextColor(getResources().getColor(R.color.dayTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.dayBackground));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.primary));
                    turnFlag = true;
                }
            }
        });


        syncArticleContent();

    }
    private void syncArticleContent(){
        mSwipeRefreshLayout.setRefreshing(true);
        apiHelper = new BQGWebSiteHelper();
        apiHelper.getChapterContent(chapter.getUrl(), webChar, new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                    updateUI(data);
            }

            @Override
            public void onFail(String msg) {
                updateUI(null);
            }
        });
    }

    private void updateUI(final String data){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                if( data != null){
                    // use textView format
                    tv.setText(Html.fromHtml(data));
                    Snackbar.make(mSwipeRefreshLayout, "加载成功", Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(mSwipeRefreshLayout, "获取数据失败，请下拉刷新重新获取", Snackbar.LENGTH_LONG).show();
                }
            }
        }, 0);


    }

    @Override
    public void onRefresh() {
        syncArticleContent();
    }
}
