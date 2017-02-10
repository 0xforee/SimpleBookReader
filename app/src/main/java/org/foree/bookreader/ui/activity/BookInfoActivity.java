package org.foree.bookreader.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;

/**
 * Created by foree on 17-1-10.
 */

public class BookInfoActivity extends AppCompatActivity {
    private TextView tvNovelName, tvNovelAuthor, tvNovelDescription;
    private Button bt;
    private ListView lv;
    private String bookUrl;
    private BookDao bookDao;
    private Toolbar toolbar;
    private Book book;
    private LinearLayout linearLayout;

    private static final int STATE_FAILED = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_SUCCESS = 1;

    private ProgressBar progressBar;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        setupLayout();

        Bundle bundle = getIntent().getExtras();

        bookUrl = bundle.getString("book_url");

        setupView();

        notifyUpdate(STATE_LOADING);
    }

    private void setupLayout() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookDao = new BookDao(this);
        tvNovelName = (TextView) findViewById(R.id.tv_novel_name);
        tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        tvNovelDescription = (TextView) findViewById(R.id.tv_description);
        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        linearLayout = (LinearLayout)findViewById(R.id.ll_book_info);
        bt = (Button) findViewById(R.id.bt_add);
        lv = (ListView) findViewById(R.id.lv_chapter_list);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookDao.addBook(book);

                // update bt_add
                bt.setBackgroundColor(getResources().getColor(R.color.material_drawer_dark_hint_text));
                bt.setText(getResources().getText(R.string.bt_added));
                bt.setClickable(false);
            }
        });
    }

    private void notifyUpdate(int state){
        switch (state){
            case STATE_FAILED:
                break;
            case STATE_LOADING:
                progressBar.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                tvNovelDescription.setVisibility(View.INVISIBLE);
                break;
            case STATE_SUCCESS:
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                tvNovelDescription.setVisibility(View.VISIBLE);

                break;

        }
    }
    private void setupView() {
        AbsWebParser webinfo = WebParserManager.getInstance().getWebParser(bookUrl);
        webinfo.getBookInfo(bookUrl, new NetCallback<Book>() {
            @Override
            public void onSuccess(final Book data) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        book = data;
                        tvNovelName.setText(data.getBookName());
                        tvNovelAuthor.setText(data.getAuthor());
                        tvNovelDescription.setText(Html.fromHtml(data.getDescription()));
                        notifyUpdate(STATE_SUCCESS);
                    }
                }, 0);


            }

            @Override
            public void onFail(String msg) {

            }
        });
    }


}
