package org.foree.bookreader.bookinfopage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.commit451.nativestackblur.NativeStackBlur;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.WebParser;
import org.foree.bookreader.readpage.ReadActivity;

import java.util.List;

/**
 * Created by foree on 17-1-10.
 */

public class BookInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = BookInfoActivity.class.getSimpleName();
    private TextView mTvNovelAuthor, mTvNovelDescription, mTvBookLatestChapter;
    private Button mBtAdd, mBtRead;
    private CommentListView mCommentList;
    private String bookUrl;
    private BookDao bookDao;
    private Toolbar toolbar;
    private Book book;
    private ImageView imageView, mFrameBack;
    private RelativeLayout relativeLayout;
    private ScrollView mScrollView;
    private FrameLayout mContent;

    private List<Review> mReviews;

    private int mDisplayWidth;

    private static final int STATE_FAILED = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_SUCCESS = 1;

    /**
     * 背景灰色蒙版
     */
    private final int MASK_HINT_COLOR = 0x39000000;

    private ProgressBar mProgressBar;
    private Handler mBgHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        // create bg handler
        HandlerThread mBgThread = new HandlerThread("bg", Process.THREAD_PRIORITY_BACKGROUND);
        mBgThread.start();
        mBgHandler = new Handler(mBgThread.getLooper());

        Bundle bundle = getIntent().getExtras();
        bookUrl = bundle.getString("book_url");
        bookDao = new BookDao(this);

        // set status bar transparent
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // get device width
        WindowManager windowManager = getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;

        initLayout();

        initViews();

        notifyUpdate(STATE_LOADING);
    }

    private void initLayout() {
        mContent = (FrameLayout) findViewById(R.id.activity_search_results);
        mScrollView = (ScrollView) findViewById(R.id.content_scroll);
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = mScrollView.getScrollY();
                int alpha = getAlpha(scrollY);
                Log.d(TAG, "[foree] onScrollChange: scrollY = " + scrollY + ", alpha = " + alpha);

                toolbar.getBackground().setAlpha(alpha);
                int color = getResources().getColor(R.color.primary);
                color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                getWindow().setStatusBarColor(color);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("");
        toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getBackground().setAlpha(0);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvNovelAuthor = (TextView) findViewById(R.id.tv_novel_author);
        mTvNovelDescription = (TextView) findViewById(R.id.tv_description);
        mTvBookLatestChapter = (TextView) findViewById(R.id.tv_book_info_latest_chapter);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_progress);
        relativeLayout = (RelativeLayout) findViewById(R.id.ll_book_info);
        mBtAdd = (Button) findViewById(R.id.bt_add);
        mBtRead = (Button) findViewById(R.id.bt_read);
        mCommentList = (CommentListView) findViewById(R.id.lv_comment_list);
        imageView = (ImageView) findViewById(R.id.iv_novel_image);
        mFrameBack = (ImageView) findViewById(R.id.frame_back);
        mFrameBack.setBackgroundColor(getResources().getColor(R.color.primary));

        mBtAdd.setOnClickListener(this);
        mBtRead.setOnClickListener(this);

        // update toolbar top margin
        int statbarHeight = -1;
        statbarHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statbarHeight > 0) {
            statbarHeight = getResources().getDimensionPixelSize(statbarHeight);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
            Log.d(TAG, "[foree] initLayout: statusbarHeight = " + statbarHeight);
            layoutParams.topMargin = statbarHeight;
            mContent.updateViewLayout(toolbar, layoutParams);
        }

    }

    private int getAlpha(int scrollY) {
        int alphaFadeLength = 160;
        if (scrollY <= 0) {
            return 0;
        } else if (scrollY < alphaFadeLength) {
            double sec = 255.0 / alphaFadeLength;
            return (int) (sec * scrollY);
        } else {
            return 255;
        }
    }

    private void notifyUpdate(int state) {
        switch (state) {
            case STATE_FAILED:
                break;
            case STATE_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.INVISIBLE);
                break;
            case STATE_SUCCESS:
                mProgressBar.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);
                break;
            default:
        }
    }

    private void initViews() {
        // get bookInfo
        mBgHandler.post(new Runnable() {
            @Override
            public void run() {
                // get bookinfo first
                final Book book = WebParser.getInstance().getBookInfo(bookUrl);
                if (book != null) {
                    // get chapters
                    book.setChapters(WebParser.getInstance().getContents(bookUrl, book.getContentUrl()));

                    // get comments
                    final List<Review> reviews = WebParser.getInstance().getLongReviews(bookUrl);

                    // update UI
                    mContent.post(new Runnable() {
                        @Override
                        public void run() {
                            updateBookInfo(book, reviews);
                        }
                    });

                }
            }
        });

    }

    /**
     * 更新UI，要在主线程执行
     * @param book 书籍信息
     * @param reviews 评论信息
     */
    private void updateBookInfo(Book book, List<Review> reviews) {
        mTvNovelAuthor.setText(book.getAuthor());
        mTvBookLatestChapter.setText(book.getRectentChapterTitle());
        toolbar.setTitle(book.getBookName());

        // update description
        if (book.getDescription() != null) {
            String text = Html.fromHtml(book.getDescription()).toString();
            mTvNovelDescription.setText(text);
        }
        mCommentList.setFocusable(false);
        // update comment
        CommentAdapter commentAdapter = new CommentAdapter(this, reviews);
        mCommentList.setAdapter(commentAdapter);

        if (book.getBookCoverUrl() != null) {
            Glide.with(BookInfoActivity.this).load(book.getBookCoverUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    imageView.setImageBitmap(resource);

                    mFrameBack.setColorFilter(MASK_HINT_COLOR, PorterDuff.Mode.DARKEN);
                    mFrameBack.setImageBitmap(createFrameBlurBackground(resource));

                    notifyUpdate(STATE_SUCCESS);
                }
            });
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 重置toolbar的透明度，不然返回书架页toolbar会状态异常
        toolbar.getBackground().setAlpha(255);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_read:
                Intent intent = new Intent(BookInfoActivity.this, ReadActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("book_url", bookUrl);
                bundle.putBoolean("online", true);
                intent.putExtras(bundle);

                startActivity(intent);
                break;
            case R.id.bt_add:
                bookDao.addBook(book);

                // update bt_add
                mBtAdd.setText(getResources().getText(R.string.bt_added));
                mBtAdd.setEnabled(false);
                break;
            default:
        }
    }

    private Bitmap createFrameBlurBackground(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Log.d(TAG, "[foree] createFrameBlurBackground: width = " + width + ", height = " + height);
        Matrix matrix = new Matrix();

        // 裁剪中间1/3，然后放大模糊
        int frameImageHeight = getResources().getDimensionPixelSize(R.dimen.activity_book_info_frame_back_height);
        Bitmap ddd = Bitmap.createBitmap(source, 0, height / 3,
                width, height / 3, null, true);

        // 放大
        int oldWidth = ddd.getWidth();
        int oldHeight = ddd.getHeight();
        float scale = ((float) mDisplayWidth) / oldWidth;
        float scaleY = ((float) frameImageHeight) / oldHeight;
        matrix.postScale(scale, scaleY);
        Bitmap dest = Bitmap.createBitmap(ddd, 0, 0,
                ddd.getWidth(), ddd.getHeight(), matrix, true);

        return NativeStackBlur.process(dest, 200);
    }
}
