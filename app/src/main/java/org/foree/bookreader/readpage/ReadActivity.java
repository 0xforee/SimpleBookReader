package org.foree.bookreader.readpage;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.bean.event.PaginationEvent;
import org.foree.bookreader.common.FontDialog;
import org.foree.bookreader.pagination.PaginationArgs;
import org.foree.bookreader.pagination.PaginationLoader;
import org.foree.bookreader.settings.SettingsActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by foree on 16-7-21.
 */
public class ReadActivity extends BaseActivity implements ReadViewPager.onPageAreaClickListener, LoaderManager.LoaderCallbacks {
    private static final String TAG = ReadActivity.class.getSimpleName();

    String bookUrl;
    private BookRecord mBookRecord;
    private boolean mSlipLeft = false;
    // view pager
    private ReadViewPager mViewPager;
    private PageAdapter pageAdapter;
    private TextView mTextView;
    private Button mBtnLoading;

    // popWindow
    private PopupWindow menuPop;
    private Dialog contentDialog;
    private FontDialog.Builder fontDialog;
    private View rootView;
    private ListView chapterTitleListView;
    private ContentAdapter contentAdapter;
    // menuPop
    private TextView tvContent, tvProgress, tvFont, tvBrightness;

    // loading state
    private static final int MSG_FAILED = -1;
    private static final int MSG_LOADING = 0;
    private static final int MSG_SUCCESS = 1;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOADING:
                    mBtnLoading.setVisibility(View.VISIBLE);
                    mBtnLoading.setClickable(false);
                    mBtnLoading.setText(getResources().getText(R.string.loading));
                    break;
                case MSG_FAILED:
                    mBtnLoading.setVisibility(View.VISIBLE);
                    mBtnLoading.setClickable(true);
                    mBtnLoading.setText(getResources().getText(R.string.load_fail_other));
                    break;
                case MSG_SUCCESS:
                    mBtnLoading.setVisibility(View.GONE);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vp_layout);

        // register EventBus
        EventBus.getDefault().register(this);
        mBookRecord = new BookRecord(this);

        bookUrl = getIntent().getExtras().getString("book_url");

        mBookRecord.restoreBookRecord(bookUrl);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        initTextView(savedInstanceState);
        initMenuPop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BaseActivity.KEY_RECREATE, true);
    }

    private void initViews() {
        //init textView
        mBtnLoading = (Button) findViewById(R.id.loading);

        mBtnLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchChapter(mBookRecord.getCurrentUrl(), false, true);
            }
        });

        mViewPager = (ReadViewPager) findViewById(R.id.book_pager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        rootView = LayoutInflater.from(this).inflate(R.layout.vp_layout, null);
        mViewPager.setAdapter(pageAdapter);

        mViewPager.setOnPageAreaClickListener(this);

    }

    private void initTextView(final Bundle savedInstanceState) {
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

                if (savedInstanceState != null && savedInstanceState.getBoolean(BaseActivity.KEY_RECREATE)) {
                    switchChapter(mBookRecord.getCurrentUrl(), false, false);
                    Log.d(TAG, "onCreate: recreate activity");
                } else {
                    // loading
                    switchChapter(mBookRecord.getCurrentUrl(), false, true);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // init smart load offset
        int offset = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_PREF_OFFLINE_OFFSET, "5"));
        PaginationLoader.getInstance().smartLoadInit(mBookRecord, offset);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PaginationEvent pageEvent) {
        Log.d("EventBus", "notifyState");

        if (mHandler.hasMessages(MSG_LOADING))
            mHandler.removeMessages(MSG_LOADING);

        Chapter chapter = pageEvent.getChapter();
        if (pageEvent.isCurrent()) {
            if (chapter != null) {

                mBookRecord.setCached(chapter.getChapterUrl());

                if (pageEvent.isCurrent()) {

                    mHandler.sendEmptyMessage(MSG_SUCCESS);

                    pageAdapter.setChapter(chapter);

                    // if open book ,load index page, otherwise load normal
                    if (mBookRecord.isInitCompleted()) {
                        mViewPager.setCurrentItem(isSlipLeft() ? chapter.numberOfPages() - 1 : 0, false);
                    } else {
                        mViewPager.setCurrentItem(mBookRecord.getPageIndex(), false);
                    }
                }
            } else {
                mHandler.sendEmptyMessage(MSG_FAILED);
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

        mBookRecord.saveBookRecord(mViewPager.getCurrentItem());
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
        menuPop.setBackgroundDrawable(new ColorDrawable(GlobalConfig.getInstance().getPageBackground()));

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
                chapterTitleListView.setSelection(mBookRecord.getCurPosition() - 2);
                contentAdapter.notifyDataSetChanged();

            }
        });

//        tvBrightness.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ReadActivity.this);
//                boolean nightMode = GlobalConfig.getInstance().isNightMode();
//                Log.d(TAG, "onClick: nightMode = " + nightMode);
//                preferences.edit().putBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, !nightMode).apply();
//                // change theme
//                GlobalConfig.getInstance().changeTheme();
//
//                recreate();
//
//                if (menuPop.isShowing()) {
//                    menuPop.dismiss();
//                }
//            }
//        });

        tvFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                if (fontDialog == null) {
                    showFontDialog();
                } else {
                    fontDialog.showDialog();
                }

            }
        });
    }

    @Override
    public void onMediumAreaClick() {
        menuPop.setBackgroundDrawable(new ColorDrawable(GlobalConfig.getInstance().getPageBackground()));
        menuPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onPreChapterClick() {
        switchChapter(mBookRecord.getUrlFromFlag(-1), true, false);
    }

    @Override
    public void onNextChapterClick() {
        switchChapter(mBookRecord.getUrlFromFlag(1), false, false);
    }

    private void showFontDialog() {
        fontDialog = new FontDialog.Builder(this);
        fontDialog.showDialog();
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
                switchChapter(mBookRecord.getUrl(position), false, true);
                contentAdapter.setSelectedPosition(position);
            }
        });
    }

    private void switchChapter(String newChapterUrl, boolean slipLeft, boolean skip) {
        if (newChapterUrl != null) {
            pageAdapter.setChapter(null);
            if (skip)
                mHandler.sendEmptyMessage(MSG_LOADING);
            PaginationLoader.getInstance().loadPagination(newChapterUrl);
            mBookRecord.switchChapter(newChapterUrl);
            mSlipLeft = slipLeft;
        }
    }

    private boolean isSlipLeft() {
        return mSlipLeft;
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

        chapterTitleListView.setSelection(mBookRecord.getCurPosition() - 2);
        contentAdapter.setSelectedPosition(mBookRecord.getCurPosition());
        contentAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLoaderReset(Loader loader) {
        contentAdapter.swapCursor(null);
    }


}
