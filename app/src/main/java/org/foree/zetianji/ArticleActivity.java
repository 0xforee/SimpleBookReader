package org.foree.zetianji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity{
    private String Host = "http://www.biquge.com";
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private String articleData;
    WebView wb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        TextView tv = (TextView)findViewById(R.id.tv_content);
        wb = (WebView)findViewById(R.id.wb_content);
        String url = getIntent().getStringExtra("href");
        String title = getIntent().getStringExtra("title");

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(title);
        }

        String target_url = Host + url;

        NetRequest.getHtml(target_url, new NetCallback() {
            @Override
            public void onSuccess(String data) {
                Log.i(TAG, data);
                parseHtml(data);
                updateUI();
            }

            @Override
            public void onFail(String msg) {

            }
        });

    }

    private void updateUI(){
        if (wb != null) {
            wb.getSettings().setDefaultTextEncodingName("UTF-8");
            wb.loadDataWithBaseURL(null, articleData,"text/html","UTF-8",null);
        }
    }

    private void parseHtml(String data){
        Document doc = Jsoup.parse(data);
        Element content = doc.getElementById("content");
        articleData = content.toString();
        Log.i(TAG, content.toString());
    }
}
