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
import android.widget.ListView;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.dao.BookDao;
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
    }

    private void setupLayout() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookDao = new BookDao(this);
        tvNovelName = (TextView) findViewById(R.id.tv_novel_name);
        tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        tvNovelDescription = (TextView) findViewById(R.id.tv_description);
        bt = (Button) findViewById(R.id.bt_add);
        lv = (ListView) findViewById(R.id.lv_chapter_list);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookDao.addBookInfo(book);
            }
        });
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
                        bt.setClickable(true);
                    }
                }, 0);


            }

            @Override
            public void onFail(String msg) {

            }
        });
    }


}
