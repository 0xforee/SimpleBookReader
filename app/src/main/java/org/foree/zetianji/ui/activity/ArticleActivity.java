package org.foree.zetianji.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.zetianji.net.NetCallback;
import org.foree.zetianji.R;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.helper.BQGWebSiteHelper;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity{
    BQGWebSiteHelper apiHelper;
    private static final String TAG = ArticleActivity.class.getSimpleName();
    WebView wb;
    String webChar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        TextView tv = (TextView)findViewById(R.id.tv_content);
        wb = (WebView)findViewById(R.id.wb_content);
        Bundle bundle = getIntent().getExtras();
        Chapter chapter = (Chapter)bundle.getSerializable("chapter");
        webChar = bundle.getString("web_char");

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(chapter.getTitle());
        }

        apiHelper = new BQGWebSiteHelper();
        apiHelper.getChapterContent(chapter.getUrl(), webChar, new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                if(data != null) {
                    Log.i(TAG, data);
                    updateUI(data);
                }else{
                    updateUI("获取数据失败，请重新获取");
                }
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(ArticleActivity.this, "getArticleError: " + msg, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateUI(String data){
        if (wb != null) {
            Log.d(TAG, "webChar: " + webChar);
            wb.getSettings().setDefaultTextEncodingName(webChar);
            wb.loadDataWithBaseURL(null, data,"text/html",webChar,null);
        }
    }
}
