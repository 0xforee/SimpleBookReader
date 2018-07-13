package org.foree.bookreader.readpage;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.bean.event.BookLoadCompleteEvent;
import org.foree.bookreader.bean.event.PaginationEvent;
import org.foree.bookreader.common.FontDialog;
import org.foree.bookreader.pagination.PaginationArgs;
import org.foree.bookreader.pagination.PaginationLoader;
import org.foree.bookreader.settings.SettingsActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-21.
 */
public class ReadActivity extends BaseActivity implements ReadViewPager.onPageAreaClickListener, View.OnClickListener {
    private static final String TAG = ReadActivity.class.getSimpleName();

    String mBookUrl;
    boolean mOnline;
    private BookRecord mBookRecord;
    private boolean mSlipLeft = false;
    // view pager
    private ReadViewPager mViewPager;
    private PageAdapter pageAdapter;
    private TextView mTextView;
    private Button mBtnLoading;

    // popWindow
    private PopupWindow menuPop;
    private Dialog contentDialog, mSourceChangeDialog;
    private FontDialog.Builder fontDialog;
    private View rootView;
    private ListView chapterTitleListView, mSourceChangeListView;
    private CustomChapterListAdapter contentAdapter;
    private SimpleAdapter mSourceChangeAdapter;
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBookUrl = getIntent().getExtras().getString("book_url");
            mOnline = bundle.getBoolean("online", false);
        }

        mHandler.sendEmptyMessage(MSG_LOADING);

        mBookRecord.restoreBookRecord(mBookUrl, mOnline);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews(savedInstanceState);
        initMenuPop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BaseActivity.KEY_RECREATE, true);
    }

    private void initViews(final Bundle savedInstanceState) {
        //init textView
        mBtnLoading = (Button) findViewById(R.id.loading);
        mBtnLoading.setOnClickListener(this);

        mViewPager = (ReadViewPager) findViewById(R.id.book_pager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        rootView = LayoutInflater.from(this).inflate(R.layout.vp_layout, null);
        mViewPager.setAdapter(pageAdapter);

        mViewPager.setOnPageAreaClickListener(this);

        initTextView(savedInstanceState);

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
                    Log.d(TAG, "onCreate: recreate activity for theme apply");
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

        // set brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = GlobalConfig.getInstance().getAppBrightness();
        getWindow().setAttributes(lp);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PaginationEvent pageEvent) {
        Log.d("EventBus", "notifyState");

        if (mHandler.hasMessages(MSG_LOADING))
            mHandler.removeMessages(MSG_LOADING);

        Chapter chapter = pageEvent.getChapter();
        if (pageEvent.isCurrent()) {
            if (chapter != null) {

                mBookRecord.setChapterCached(chapter.getChapterUrl());

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BookLoadCompleteEvent event) {

        mHandler.sendEmptyMessage(event.getState() ? MSG_SUCCESS : MSG_FAILED);
        if (event.getState()) {
            switchChapter(mBookRecord.getCurrentUrl(), false, false);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

        mBookRecord.switchPageIndex(mViewPager.getCurrentItem());
        mBookRecord.saveBookRecord();
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


        tvContent.setOnClickListener(this);

        tvBrightness.setOnClickListener(this);

        tvFont.setOnClickListener(this);
    }

    @Override
    public void onMediumAreaClick() {
        menuPop.setBackgroundDrawable(new ColorDrawable(GlobalConfig.getInstance().getPageBackground()));
        menuPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onPreChapterClick() {
        switchChapter(mBookRecord.getOffsetChapter(-1), true, false);
    }

    @Override
    public void onNextChapterClick() {
        switchChapter(mBookRecord.getOffsetChapter(1), false, false);
    }

    private void showFontDialog() {
        fontDialog = new FontDialog.Builder(this);
        fontDialog.showDialog();
    }


    private void showContentDialog() {
        contentDialog = getDialog(getString(R.string.content));

        chapterTitleListView = (ListView) contentDialog.findViewById(R.id.rv_item_list);

        contentAdapter = new CustomChapterListAdapter(this);
        contentAdapter.updateData(mBookRecord.getChapters());
        chapterTitleListView.setAdapter(contentAdapter);

        contentDialog.show();

        chapterTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contentDialog.dismiss();
                switchChapter(mBookRecord.getChapterUrl(position), false, true);
                contentAdapter.setSelectedPosition(position);
            }
        });
    }

    private void showChangeDialog() {
        mSourceChangeDialog = getDialog(getString(R.string.source_change));

        mSourceChangeListView = (ListView) mSourceChangeDialog.findViewById(R.id.rv_item_list);
        List<Map<String, ?>> sourceList = new ArrayList<>();
        for (int i = 0; i < mBookRecord.getSourceList().size(); i++) {
            Source source = mBookRecord.getSourceList().get(i);
            Map<String, String> map = new HashMap<>();
            map.put("updated", source.getUpdated().toString());
            map.put("title", source.getLastChapter());
            map.put("host", source.getHost());

            sourceList.add(map);
        }

        mSourceChangeAdapter = new SimpleAdapter(this, sourceList, R.layout.lv_source_change_item_holder,
                new String[]{"updated", "title", "host"}, new int[]{R.id.tv_updated, R.id.tv_last_chapter, R.id.tv_source_host});


        mSourceChangeListView.setAdapter(mSourceChangeAdapter);

        mSourceChangeDialog.show();

        mSourceChangeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSourceChangeDialog.dismiss();
                //TODO:点击切换书源
            }
        });
    }

    private Dialog getDialog(String tile) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_content_layout, null);
        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        Dialog contentDialog = new Dialog(this, R.style.contentDialogStyle);
        contentDialog.setTitle(tile);
        contentDialog.setContentView(view);
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

        return contentDialog;
    }

    /**
     * 切换章节
     *
     * @param newChapterUrl 切换的目标章节url(id)
     * @param slipLeft      是否向左翻页
     * @param skip          非连续切换章节（从目录跳转）
     */
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.content:
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                if (contentDialog == null) {
                    showContentDialog();
                } else {
                    contentDialog.show();
                }
                chapterTitleListView.setSelection(mBookRecord.getCurrentChapterPos() - 2);
                contentAdapter.notifyDataSetChanged();
                break;
            case R.id.font:
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                if (fontDialog == null) {
                    showFontDialog();
                } else {
                    fontDialog.showDialog();
                }
                break;
            case R.id.loading:
                switchChapter(mBookRecord.getCurrentUrl(), false, true);
                break;
            case R.id.brightness:
                // 切换书源
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                if (mSourceChangeDialog == null) {
                    showChangeDialog();
                } else {
                    mSourceChangeDialog.show();
                }
                //TODO: 更新
                break;
            default:
        }
    }
}
