package org.foree.bookreader.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.data.dao.BookDao;
import org.foree.bookreader.data.event.PaginationEvent;
import org.foree.bookreader.pagination.PaginationArgs;
import org.foree.bookreader.pagination.PaginationLoader;
import org.foree.bookreader.ui.adapter.PageAdapter;
import org.foree.bookreader.ui.view.ReadViewPager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity implements ReadViewPager.onPageAreaClickListener {
    private static final String TAG = ArticleActivity.class.getSimpleName();

    String chapterUrl, bookUrl, newChapterUrl;

    private List<Chapter> chapterList = new ArrayList<>();

    private BookDao bookDao;

    private int recentChapterId = -1;

    private boolean slipLeft = false;

    // view pager
    private ReadViewPager mViewPager;
    private PageAdapter pageAdapter;
    private TextView mTextView, mTvError, mTvLoading;

    // popWindow
    private PopupWindow chapterListPop, menuPop;
    private View rootView;
    private ListView chapterTitleListView;

    // menuPop
    private TextView tvContent, tvProgress, tvFont, tvBrightness;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_layout);

        // register EventBus
        EventBus.getDefault().register(this);

        // get chapterUrl and recentId
        bookDao = new BookDao(this);
        bookUrl = getIntent().getExtras().getString("book_url");
        openBook(bookUrl);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setUpLayoutViews();
        initTextView();
        setUpMenuPop();

        notifyState(PaginationEvent.STATE_LOADING);

    }

    private void setUpLayoutViews() {
        //init textView
        mTvError = (TextView) findViewById(R.id.load_fail);
        mTvLoading = (TextView) findViewById(R.id.loading);

        mViewPager = (ReadViewPager) findViewById(R.id.book_pager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        rootView = LayoutInflater.from(this).inflate(R.layout.view_pager_layout, null);
        mViewPager.setAdapter(pageAdapter);

        mViewPager.setOnPageAreaClickListener(this);

    }

    private void initTextView() {
        mTextView = (TextView) findViewById(R.id.book_content);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int top = mTextView.getPaddingTop();
                int bottom = mTextView.getPaddingBottom();
                int left = mTextView.getPaddingLeft();
                int right = mTextView.getPaddingRight();
                // init PaginationLoader
                PaginationLoader.getInstance().init(new PaginationArgs(
                        mTextView.getWidth() - left - right,
                        mTextView.getHeight() - top - bottom,
                        mTextView.getLineSpacingMultiplier(),
                        mTextView.getLineSpacingExtra(),
                        mTextView.getPaint(),
                        mTextView.getIncludeFontPadding()));

                PaginationLoader.getInstance().loadPagination(chapterUrl);

            }
        });
    }

    private void notifyState(int state) {
        switch (state) {
            case PaginationEvent.STATE_FAILED:
                mTvLoading.setVisibility(View.GONE);
                mTvError.setVisibility(View.VISIBLE);
                break;
            case PaginationEvent.STATE_LOADING:
                mTvLoading.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                break;
            case PaginationEvent.STATE_SUCCESS:
                mTvLoading.setVisibility(View.GONE);
                mTvError.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PaginationEvent pageEvent) {
        notifyState(pageEvent.getState());
        Log.d("EventBus", "notifyState");
        if (pageEvent.getUrl().equals(chapterUrl))
            if (pageEvent.getPagination() != null) {
                pageAdapter.setTitle(bookDao.getChapterName(pageEvent.getUrl()));
                pageAdapter.setPages(pageEvent.getPagination().getPages());
                if (slipLeft)
                    mViewPager.setCurrentItem(pageEvent.getPagination().getPages().size() - 1, false);
                else
                    mViewPager.setCurrentItem(0, false);
            }
    }

    @Override
    protected void onDestroy() {
        closeBook();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setUpMenuPop() {
        // 弹出一个popupMenu
        View view = LayoutInflater.from(this).inflate(R.layout.popupmenu_read_menu, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        menuPop = new PopupWindow(this);
        menuPop.setContentView(view);
        menuPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        menuPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menuPop.setFocusable(true);
        menuPop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        menuPop.setOutsideTouchable(true);

        tvContent = (TextView) view.findViewById(R.id.content);
        tvProgress = (TextView) view.findViewById(R.id.progress);
        tvFont = (TextView) view.findViewById(R.id.font);
        tvBrightness = (TextView) view.findViewById(R.id.brightness);


        tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                if (chapterListPop == null)
                    showPopup();
                else
                    chapterListPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
        });

    }

    @Override
    public void onMediumAreaClick() {
        menuPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onPreChapterClick() {
        newChapterUrl = bookDao.getChapterUrl(-1, chapterUrl);
        if (newChapterUrl != null) {
            switchChapter(newChapterUrl);
            slipLeft = true;
        }
    }

    @Override
    public void onNextChapterClick() {
        newChapterUrl = bookDao.getChapterUrl(1, chapterUrl);
        if (newChapterUrl != null) {
            switchChapter(newChapterUrl);
            slipLeft = false;
        }
    }

    private void showPopup() {
        View view = LayoutInflater.from(this).inflate(R.layout.popupwindow_layout, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        chapterListPop = new PopupWindow(this);
        chapterListPop.setContentView(view);
        chapterListPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        chapterListPop.setHeight(dp.heightPixels / 4 * 3);
        chapterListPop.setFocusable(true);
        chapterListPop.setOutsideTouchable(true);
        chapterListPop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        chapterListPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);


        getChapterTitle();
        chapterTitleListView = (ListView) view.findViewById(R.id.rv_item_list);

        chapterTitleListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_list_holder, getChapterTitle()));

        chapterTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chapterListPop.dismiss();
                switchChapter(chapterList.get(position).getChapterUrl());
            }
        });
    }

    private List<String> getChapterTitle() {
        List<String> chapterTitle = new ArrayList<>();

        for(Chapter chapter: chapterList){
            chapterTitle.add(chapter.getChapterTitle());
        }

        return chapterTitle;
    }

    private void switchChapter(String newChapterUrl) {
        updateChapterUrl(newChapterUrl);
        notifyState(PaginationEvent.STATE_LOADING);
        PaginationLoader.getInstance().loadPagination(chapterUrl);
    }

    private void openBook(String bookUrl) {
        //get all chapter
        Book book = bookDao.getBook(bookUrl);
        // get chapterList
        chapterList = book.getChapters();
        // check init
        if (book.getRecentChapterId() == -1) {
            // get first chapter id
            if (chapterList != null && !chapterList.isEmpty()) {
                setChapterId(bookUrl, chapterList.get(0).getChapterId());
                // update book object recentChapterId
                book.setRecentChapterId(chapterList.get(0).getChapterId());
            }
        }

        // open by chapter id
        updateChapterUrl(bookDao.getChapterUrl(book.getRecentChapterId()));
    }

    private void setChapterId(String bookUrl, int newId) {
        bookDao.updateRecentChapterId(bookUrl, newId);
    }

    private void closeBook() {
        // set ChapterId
        if (recentChapterId != -1)
            bookDao.updateRecentChapterId(bookUrl, recentChapterId);
    }

    private void updateChapterUrl(String newUrl){
        recentChapterId = bookDao.getChapterId(newUrl);
        chapterUrl = newUrl;
    }
}
