package org.foree.zetianjia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity{
    private String Host = "http://www.biquge.com";
    private static final String TAG = ArticleActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        final TextView tv = (TextView)findViewById(R.id.tv_content);
        final WebView wb = (WebView)findViewById(R.id.wb_content);
        String url = getIntent().getStringExtra("href");
        String title = getIntent().getStringExtra("title");

        getSupportActionBar().setTitle(title);

        String target_url = Host + url;

        NetRequest.getHtml(target_url, new NetCallback() {
            @Override
            public void onSuccess(String data) {
                Log.i(TAG, data);
                wb.loadData(data,"text/html","UTF-8");
            }

            @Override
            public void onFail(String msg) {

            }
        });

    }
}
