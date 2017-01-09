package org.foree.bookreader.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Article;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = ArticleActivity.class.getSimpleName();
    TextView tvContent,tvTitle;
    FloatingActionButton turnNightMode;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    String chapterUrl;
    boolean turnFlag = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvContent = (TextView)findViewById(R.id.tv_content);
        tvTitle = (TextView)findViewById(R.id.tv_title);
        Bundle bundle = getIntent().getExtras();
        chapterUrl = bundle.getString("url");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // get FloatActionButton
        turnNightMode = (FloatingActionButton)findViewById(R.id.fab);
        turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(turnFlag) {
                    // night Mode
                    tvContent.setTextColor(getResources().getColor(R.color.nightTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.nightTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.nightBackground));
                    turnFlag = false;
                }else{
                    // day Mode
                    tvContent.setTextColor(getResources().getColor(R.color.dayTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.dayTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.dayBackground));
                    turnFlag = true;
                }
            }
        });


        syncArticleContent();

    }
    private void syncArticleContent(){
        mSwipeRefreshLayout.setRefreshing(true);
        WebInfo webInfo = new BiQuGeWebInfo();
        webInfo.getArticle(chapterUrl, new NetCallback<Article>() {
            @Override
            public void onSuccess(Article data) {
                updateUI(data);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    private void updateUI(final Article article){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                if( article != null){
                    // use textView format
                    tvContent.setText(Html.fromHtml(article.getContents()));
                    tvTitle.setText(Html.fromHtml(article.getTitle()));
                    Snackbar.make(mSwipeRefreshLayout, R.string.load_success, Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(mSwipeRefreshLayout, R.string.load_fail , Snackbar.LENGTH_LONG).show();
                }
            }
        }, 0);


    }

    @Override
    public void onRefresh() {
        syncArticleContent();
    }
}
