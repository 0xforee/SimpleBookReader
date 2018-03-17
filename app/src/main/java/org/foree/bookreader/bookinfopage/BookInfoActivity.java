package org.foree.bookreader.bookinfopage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.WebParserProxy;

import java.util.List;

/**
 * Created by foree on 17-1-10.
 */

public class BookInfoActivity extends BaseActivity {
    private TextView tvNovelName, tvNovelAuthor, tvNovelDescription;
    private Button bt;
    private ListView lv;
    private String bookUrl;
    private BookDao bookDao;
    private Toolbar toolbar;
    private Book book;
    private ImageView imageView;
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

        initLayout();

        Bundle bundle = getIntent().getExtras();

        bookUrl = bundle.getString("book_url");

        initViews();

        notifyUpdate(STATE_LOADING);
    }

    private void initLayout() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.book_info);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bookDao = new BookDao(this);
        tvNovelName = (TextView) findViewById(R.id.tv_novel_name);
        tvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        tvNovelDescription = (TextView) findViewById(R.id.tv_description);
        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        linearLayout = (LinearLayout) findViewById(R.id.ll_book_info);
        bt = (Button) findViewById(R.id.bt_add);
        lv = (ListView) findViewById(R.id.lv_chapter_list);
        imageView = (ImageView) findViewById(R.id.iv_novel_image);

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

    private void notifyUpdate(int state) {
        switch (state) {
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

    private void initViews() {
        WebParserProxy.getInstance().getBookInfoAsync(bookUrl, new NetCallback<Book>() {
            @Override
            public void onSuccess(final Book data1) {
                WebParserProxy.getInstance().getContentsAsync(bookUrl, data1.getContentUrl(), new NetCallback<List<Chapter>>() {
                    @Override
                    public void onSuccess(final List<Chapter> data) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                book = data1;
                                book.setChapters(data);
                                tvNovelName.setText(data1.getBookName());
                                tvNovelAuthor.setText(data1.getAuthor());
                                if (data1.getDescription() != null)
                                    tvNovelDescription.setText(Html.fromHtml(data1.getDescription()));

                                if (book.getBookCoverUrl() != null) {
                                    Glide.with(BookInfoActivity.this).load(book.getBookCoverUrl()).crossFade().into(imageView);
                                }
                                notifyUpdate(STATE_SUCCESS);
                            }
                        }, 0);

                    }

                    @Override
                    public void onFail(String msg) {

                    }
                });


            }

            @Override
            public void onFail(String msg) {

            }
        });
    }


}
