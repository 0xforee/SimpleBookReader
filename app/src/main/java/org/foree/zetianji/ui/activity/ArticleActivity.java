package org.foree.zetianji.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    SwipeRefreshLayout mSwipeRefreshLayout;
    String webChar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        tv = (TextView)findViewById(R.id.tv_content);
        Bundle bundle = getIntent().getExtras();
        chapter = (Chapter)bundle.getSerializable("chapter");
        webChar = bundle.getString("web_char");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(chapter.getTitle());
        }

        syncArticleContent();

    }
    private void syncArticleContent(){
        mSwipeRefreshLayout.setRefreshing(true);
        apiHelper = new BQGWebSiteHelper();
        apiHelper.getChapterContent(chapter.getUrl(), webChar, new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                if(data != null) {
                    Log.i(TAG, data);
                    updateUI(data);
                }else{
                    updateUI("获取数据失败，请下拉刷新重新获取");
                }
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(ArticleActivity.this, "getArticleError: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void updateUI(String data){
        // use textView format
        tv.setText(Html.fromHtml(data));
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        syncArticleContent();
    }
}
