package org.foree.bookreader.ui.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    private static final String TAG = SearchResultsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        handlerIntent(getIntent());

        Log.d(TAG, "onCreate");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlerIntent(intent);
    }

    private void handlerIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "query keywords = " + query);

            WebInfo webinfo = new BiQuGeWebInfo();
            webinfo.searchBook(query, new NetCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> data) {

                }

                @Override
                public void onFail(String msg) {

                }
            });
        }
    }
}
