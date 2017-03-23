package org.foree.bookreader.readpage;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.PaginationEvent;
import org.foree.bookreader.homepage.BookShelfActivity;
import org.foree.bookreader.pagination.PaginationArgs;
import org.foree.bookreader.pagination.PaginationLoader;
import org.foree.bookreader.settings.SettingsActivity;
import org.foree.bookreader.utils.DateUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 16-7-21.
 */
public class ReadActivity extends AppCompatActivity implements ReadViewPager.onPageAreaClickListener, LoaderManager.LoaderCallbacks {
    private static final String TAG = ReadActivity.class.getSimpleName();
    // loading state
    private static final int STATE_FAILED = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_SUCCESS = 1;
    String chapterUrl, bookUrl;
    private List<Chapter> chapterList = new ArrayList<>();
    private BookDao bookDao;
    private int pageIndex;
    private boolean mSlipLeft = false;
    // view pager
    private ReadViewPager mViewPager;
    private PageAdapter pageAdapter;
    private TextView mTextView, mTvLoading;
    // popWindow
    private PopupWindow menuPop;
    private Dialog contentDialog;
    private View rootView;
    private ListView chapterTitleListView;
    private ContentAdapter contentAdapter;
    // menuPop
    private TextView tvContent, tvProgress, tvFont, tvBrightness;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vp_layout);

        // register EventBus
        EventBus.getDefault().register(this);

        bookDao = new BookDao(this);
        bookUrl = getIntent().getExtras().getString("book_url");
        openBook(bookUrl);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        initTextView();
        initMenuPop();

        notifyState(STATE_LOADING);

    }

    private void initViews() {
        //init textView
        mTvLoading = (TextView) findViewById(R.id.loading);

        mViewPager = (ReadViewPager) findViewById(R.id.book_pager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        rootView = LayoutInflater.from(this).inflate(R.layout.vp_layout, null);
        mViewPager.setAdapter(pageAdapter);

        mViewPager.setOnPageAreaClickListener(this);

    }

    private void initTextView() {
        mTextView = (TextView) findViewById(R.id.book_content);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
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
            case STATE_FAILED:
                mTvLoading.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);
                Chapter chapter = new Chapter();
                chapter.addPage(getResources().getText(R.string.load_fail).toString());
                pageAdapter.setChapter(chapter);
                mViewPager.setCurrentItem(0, false);
                break;
            case STATE_LOADING:
                mTvLoading.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                break;
            case STATE_SUCCESS:
                mTvLoading.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PaginationEvent pageEvent) {
        Log.d("EventBus", "notifyState");
        Chapter chapter = pageEvent.getChapter();
        if (chapter != null) {
            notifyState(STATE_SUCCESS);

            pageAdapter.setChapter(chapter);

            // if open book ,load index page
            if (chapter.getChapterUrl().equals(chapterUrl)) {
                //Log.d(TAG, "slip to page " + pageIndex);
                mViewPager.setCurrentItem(pageIndex, false);
            } else {
                updateChapterUrl(chapter.getChapterUrl());
                if (isSlipLeft())
                    mViewPager.setCurrentItem(chapter.numberOfPages() - 1, false);
                else
                    mViewPager.setCurrentItem(0, false);
            }

        } else {
            notifyState(STATE_FAILED);
        }
    }

    @Override
    protected void onDestroy() {
        closeBook();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initMenuPop() {
        // 弹出一个popupMenu
        View view = LayoutInflater.from(this).inflate(R.layout.popupmenu_read_menu, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        menuPop = new PopupWindow(this);
        menuPop.setContentView(view);
        menuPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        menuPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menuPop.setFocusable(true);
        menuPop.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.windowBackgroundColor)));

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
                if (contentDialog == null) {
                    showContentDialog();
                } else {
                    contentDialog.show();
                    getLoaderManager().restartLoader(0, null, ReadActivity.this);
                }
                chapterTitleListView.setSelection(getChapterPosition() - 2);
                contentAdapter.notifyDataSetChanged();

            }
        });

        tvBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
                boolean nightMode = GlobalConfig.getInstance().isNightMode();
                Log.d(TAG, "onClick: nightMode = " + nightMode);
                preferences.edit().putBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, !nightMode).apply();
                // change theme
                GlobalConfig.getInstance().changeTheme();

                recreate();

                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ReadActivity.this, BookShelfActivity.class));
        finish();
        overridePendingTransition(0, android.R.anim.slide_out_right);
    }

    @Override
    public void onMediumAreaClick() {
        menuPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onPreChapterClick() {
        switchChapter(bookDao.getChapterUrl(-1, chapterUrl), true);
    }

    @Override
    public void onNextChapterClick() {
        switchChapter(bookDao.getChapterUrl(1, chapterUrl), false);
    }

    private void showContentDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_content_layout, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        contentDialog = new Dialog(this, R.style.contentDialogStyle);
        contentDialog.setContentView(view);
        contentDialog.setTitle(R.string.content);
        Window dialogWindow = contentDialog.getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.BOTTOM);

            lp.x = 0;
            lp.y = 0;
            lp.width = dp.widthPixels;
            lp.height = dp.heightPixels / 5 * 4;

            dialogWindow.setAttributes(lp);
        }
        contentDialog.setCanceledOnTouchOutside(true);

        chapterTitleListView = (ListView) view.findViewById(R.id.rv_item_list);

        contentAdapter = new ContentAdapter(this, null, 0);
        chapterTitleListView.setAdapter(contentAdapter);

        contentDialog.show();

        getLoaderManager().initLoader(0, null, this);

        chapterTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contentDialog.dismiss();
                switchChapter(chapterList.get(position).getChapterUrl(), false);
                contentAdapter.setSelectedPosition(position);
            }
        });
    }

    private void switchChapter(String newChapterUrl, boolean slipLeft) {
        if (newChapterUrl != null) {
            notifyState(STATE_LOADING);
            PaginationLoader.getInstance().loadPagination(newChapterUrl);
            mSlipLeft = slipLeft;
        }
    }

    private boolean isSlipLeft() {
        return mSlipLeft;
    }

    private void openBook(String bookUrl) {
        //get all chapter
        Book book = bookDao.getBook(bookUrl);
        // get chapterList
        chapterList = book.getChapters();

        // get chapterUrl
        updateChapterUrl(book.getRecentChapterUrl());

        pageIndex = book.getPageIndex();
        //Log.d(TAG, "open Book at position " + pageIndex);

        bookDao.updateModifiedTime(bookUrl, DateUtils.getCurrentTime());

    }

    private void closeBook() {
        bookDao.updateBookState(bookUrl, chapterUrl, mViewPager.getCurrentItem());
    }

    private void updateChapterUrl(String newUrl) {
        chapterUrl = newUrl;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri baseUri = BReaderProvider.CONTENT_URI_CHAPTERS;
        String[] projection = new String[]{
                BReaderContract.Chapters._ID,
                BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL,
                BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE,
                BReaderContract.Chapters.COLUMN_NAME_CACHED,
                BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID
        };
        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String[] selectionArgs = new String[]{bookUrl};
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        return new CursorLoader(this, baseUri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(TAG, "onLoadFinished");
        contentAdapter.changeCursor((Cursor) data);

        chapterTitleListView.setSelection(getChapterPosition() - 2);
        contentAdapter.setSelectedPosition(getChapterPosition());
        contentAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLoaderReset(Loader loader) {
        contentAdapter.swapCursor(null);
    }

    private int getChapterPosition() {
        for (int i = 0; i < chapterList.size(); i++) {
            if (chapterList.get(i).getChapterUrl().equals(chapterUrl)) {
                return i;
            }
        }
        return 0;
    }
}
