package org.foree.zetianji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.zetianji.helper.AbsWebSiteHelper;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity{
    BQGWebSiteHelper apiHelper;
    private static final String TAG = ArticleActivity.class.getSimpleName();
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

        apiHelper = new BQGWebSiteHelper();
        apiHelper.getChapterContent(url, new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                Log.i(TAG, data);
                updateUI(data);
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(ArticleActivity.this, "getArticleError: " + msg, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateUI(String data){
        if (wb != null) {
            wb.getSettings().setDefaultTextEncodingName(apiHelper.getWebsiteCharSet());
            wb.loadDataWithBaseURL(null, data,"text/html",apiHelper.getWebsiteCharSet(),null);
        }
    }
}
