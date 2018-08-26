package org.foree.bookreader.readpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.BatteryManager;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.event.BookLoadCompleteEvent;
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
public class ReadActivity extends BaseActivity implements ReadViewPager.onPageAreaClickListener, View.OnClickListener,
        ReadPageAdapter.Callback, FontDialog.OnFontChangeListener, ItemListDialog.OnItemClickListener {
    private static final String TAG = ReadActivity.class.getSimpleName();

    String mBookUrl;
    boolean mOnline;
    private BookRecord mBookRecord;
    // view pager
    private ReadViewPager mViewPager;
    private ReadPageAdapter mReadPageAdapter;
    private TextView mTvContent;
    private Button mBtnLoading;

    // popWindow
    private PopupWindow menuPop;
    private ItemListDialog mContentsDialog;
    private FontDialog.Builder fontDialog;
    private View rootView;
    private Receiver mReceiver;
    /**
     * for menu pop image button
     */
    private ImageButton mIbContent, mIbProgress, mIbFont, mIbBrightness;

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
                default:

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


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        initMenuPop();

        mReceiver = new Receiver();
        mReceiver.init();

        // show touch mode init page
        boolean initialed = getPreferences(MODE_PRIVATE).getBoolean(SettingsActivity.KEY_READ_INITIAL, false);
        if (!initialed) {
            Intent intent = new Intent(ReadActivity.this, TouchModeSelectorActivity.class);
            startActivityForResult(intent, 0);
        }

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            getPreferences(MODE_PRIVATE).edit().putBoolean(SettingsActivity.KEY_READ_INITIAL, true).apply();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BaseActivity.KEY_RECREATE, true);
    }

    private void initViews() {
        //init textView
        mBtnLoading = (Button) findViewById(R.id.loading);
        mBtnLoading.setOnClickListener(this);

        mViewPager = (ReadViewPager) findViewById(R.id.book_pager);
        mReadPageAdapter = new ReadPageAdapter(getSupportFragmentManager(), mViewPager, mBookRecord);
        mReadPageAdapter.registerCallback(this);
        mViewPager.setOnPageAreaClickListener(this);

        rootView = LayoutInflater.from(this).inflate(R.layout.vp_layout, null);
        mViewPager.setAdapter(mReadPageAdapter);

        initTextView();

    }

    private void initTextView() {
        mTvContent = (TextView) findViewById(R.id.book_content_layout);
        mTvContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTvContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTvContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                // init pagination
                reInitPaginationArgs();

                //init book info
                mBookRecord.restoreBookRecord(mBookUrl, mOnline);

            }
        });
    }

    private void reInitPaginationArgs() {
        // get new textSize
        float textSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_SIZE,
                mTvContent.getTextSize() / mTvContent.getPaint().density);
        mTvContent.setTextSize(textSize);

        // load line spacing
        float lineSpacing = PreferenceManager.getDefaultSharedPreferences(this).getFloat(SettingsActivity.KEY_READ_PAGE_TEXT_LINE_SPACING,
                mTvContent.getLineSpacingExtra());
        mTvContent.setLineSpacing(lineSpacing, 1);

        //first, reset init var
        mBookRecord.reInit();

        // second, init pageArgs
        int top = mTvContent.getPaddingTop();
        int bottom = mTvContent.getPaddingBottom();
        int left = mTvContent.getPaddingLeft();
        int right = mTvContent.getPaddingRight();
        // first, init PaginationLoader
        PaginationLoader.getInstance().init(new PaginationArgs(
                mTvContent.getWidth() - left - right,
                mTvContent.getHeight() - top - bottom,
                mTvContent.getLineSpacingMultiplier(),
                mTvContent.getLineSpacingExtra(),
                mTvContent.getPaint(),
                mTvContent.getIncludeFontPadding()));

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

        if (mHandler.hasMessages(MSG_LOADING)) {
            mHandler.removeMessages(MSG_LOADING);
        }

        Chapter chapter = pageEvent.getChapter();

        if (chapter != null) {
            // add chapterTile
            String chapterTile = mBookRecord.getChapter(chapter.getChapterUrl()).getChapterTitle();
            chapter.setChapterTitle(chapterTile);

            mBookRecord.setChapterCached(chapter.getChapterUrl());

            mReadPageAdapter.addChapter(chapter);

            if (pageEvent.isCurrent()) {

                mHandler.sendEmptyMessage(MSG_SUCCESS);

                // if open book ,load index page, otherwise load normal
                if (mBookRecord.isInitCompleted()) {
                    mReadPageAdapter.initChapter(chapter.getChapterUrl());
                } else {
                    mReadPageAdapter.initChapter(chapter.getChapterUrl(), mBookRecord.getPageIndex());
                }
            }
        } else {
            mHandler.sendEmptyMessage(MSG_FAILED);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BookLoadCompleteEvent event) {

        mHandler.sendEmptyMessage(event.getState() ? MSG_SUCCESS : MSG_FAILED);
        if (event.getState()) {
            switchChapter(mBookRecord.getCurrentUrl(), true);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

        mBookRecord.saveBookRecord();

        mReceiver.unregister();
    }

    private void initMenuPop() {
        // 弹出一个popupMenu
        View view = LayoutInflater.from(this).inflate(R.layout.popupmenu_read_menu, null, false);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        menuPop = new PopupWindow(this);
        menuPop.setContentView(view);
        menuPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        menuPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menuPop.setFocusable(true);
        menuPop.setBackgroundDrawable(new ColorDrawable(GlobalConfig.getInstance().getPageBackground()));
        menuPop.setElevation(100);

        menuPop.setOutsideTouchable(true);

        mIbContent = (ImageButton) view.findViewById(R.id.content);
        mIbProgress = (ImageButton) view.findViewById(R.id.progress);
        mIbFont = (ImageButton) view.findViewById(R.id.font);
        mIbBrightness = (ImageButton) view.findViewById(R.id.brightness);

        mIbContent.setOnClickListener(this);
        mIbProgress.setOnClickListener(this);
        mIbBrightness.setOnClickListener(this);

        mIbFont.setOnClickListener(this);
    }

    @Override
    public void onMediumAreaClick() {
        menuPop.setBackgroundDrawable(new ColorDrawable(GlobalConfig.getInstance().getPageBackground()));
        menuPop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    private void showFontDialog() {
        if (fontDialog == null) {
            fontDialog = new FontDialog.Builder(this);
        }
        fontDialog.setOnFontChangeListener(this);
        fontDialog.showDialog();
    }


    private void showContentDialog(int id) {
        ItemListDialog.Builder builder = new ItemListDialog.Builder(this);

        if (mContentsDialog == null || mContentsDialog.getId() != id) {

            switch (id) {
                case R.id.content:
                    builder = builder.withTitle(getString(R.string.content))
                            .withAdapter(new CusChapterListAdapter(this));
                    break;
                case R.id.brightness:
                    builder.withTitle(getString(R.string.source_change))
                            .withAdapter(new CusSourceListAdapter(this));
                    break;
                default:
            }

            mContentsDialog = builder.withBookRecord(mBookRecord)
                    .withClickListener(this)
                    .withId(id)
                    .build();
        }

        mContentsDialog.show();
    }

    /**
     * 切换章节
     *
     * @param newChapterUrl 切换的目标章节url(id)
     * @param loadCurrent   是否重新load当前章节，重新load当前章节会初始化head指针，会从0开始加载
     */
    private void switchChapter(String newChapterUrl, boolean loadCurrent) {
//        Log.d(TAG, "switchChapter() called with: newChapterUrl = [" + newChapterUrl + "], loadCurrent = [" + loadCurrent + "]");
        mBookRecord.switchChapter(newChapterUrl);
        if (loadCurrent) {
            mReadPageAdapter.reset();
            PaginationLoader.getInstance().loadPagination(newChapterUrl);
        }
        PaginationLoader.getInstance().smartLoad();
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
                showContentDialog(R.id.content);
                break;
            case R.id.font:
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                showFontDialog();
                break;
            case R.id.loading:
                switchChapter(mBookRecord.getCurrentUrl(), true);
                break;
            case R.id.brightness:
                // 切换书源
                if (menuPop.isShowing()) {
                    menuPop.dismiss();
                }
                showContentDialog(R.id.brightness);
                break;
            case R.id.progress:
                Toast.makeText(this, R.string.about_tips, Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    /**
     * 在章节切换的时候触发，方便监听者做出处理
     *
     * @param newChapterUrl
     */
    @Override
    public void onChapterSwitched(String newChapterUrl) {
        switchChapter(newChapterUrl, false);

    }

    /**
     * 通知字体大小变化
     *
     * @param flag FLAG_FONT_DECREASE or FLAG_FONT_INCREASE
     */
    @Override
    public void onFontChanged(int flag, float value) {
        float scaleSize = mTvContent.getTextSize() / mTvContent.getPaint().density;
        float lineSpacingExtra = mTvContent.getLineSpacingExtra();
        switch (flag) {
            case FLAG_FONT_DECREASE:
                scaleSize -= value;
                break;
            case FLAG_FONT_INCREASE:
                scaleSize += value;
                break;
            case FLAG_SPACING_DECREASE:
                lineSpacingExtra -= value;
                break;
            case FLAG_SPACING_INCREASE:
                lineSpacingExtra += value;
                break;
            default:
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().
                putFloat(SettingsActivity.KEY_READ_PAGE_TEXT_SIZE, scaleSize).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().
                putFloat(SettingsActivity.KEY_READ_PAGE_TEXT_LINE_SPACING, lineSpacingExtra).apply();
        reInitPaginationArgs();
        switchChapter(mBookRecord.getCurrentUrl(), true);

    }

    /**
     * 在列表中的子项被点击之后
     *
     * @param position location
     */
    @Override
    public void onItemClick(int position) {
        if (mContentsDialog.getId() == R.id.brightness) {
            mHandler.sendEmptyMessage(MSG_LOADING);
            // reload
            reInitPaginationArgs();
            // change sourceId (contentUrl)
            mBookRecord.changeSourceId(mBookRecord.getSources().get(position).getSourceId());
        } else if (mContentsDialog.getId() == R.id.content) {
            switchChapter(mBookRecord.getChapters().get(position).getChapterUrl(), true);
        }
    }

    private final class Receiver extends BroadcastReceiver {
        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(this, intentFilter);
        }

        public void unregister() {
            unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                Log.d(TAG, "[foree] onReceive: level = " + level);
                mReadPageAdapter.updateBatteryLevel(level);
            }
        }
    }
}
