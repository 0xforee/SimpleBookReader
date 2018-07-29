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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import org.foree.bookreader.utils.DateUtils;
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
public class ReadActivity extends BaseActivity implements ReadViewPager.onPageAreaClickListener, View.OnClickListener, ReadPageAdapter.Callback {
    private static final String TAG = ReadActivity.class.getSimpleName();

    String mBookUrl;
    boolean mOnline;
    private BookRecord mBookRecord;
    private boolean mSlipLeft = false;
    // view pager
    private ReadViewPager mViewPager;
    private ReadPageAdapter mReadPageAdapter;
    private TextView mTvContent;
    private Button mBtnLoading;

    // popWindow
    private PopupWindow menuPop;
    private Dialog contentDialog, mSourceChangeDialog;
    private FontDialog.Builder fontDialog;
    private View rootView;
    private ListView chapterTitleListView, mSourceChangeListView;
    private CustomChapterListAdapter contentAdapter;
    private CustomSourceListAdapter mSourceChangeAdapter;
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

                // second, init book info
                mBookRecord.restoreBookRecord(mBookUrl, mOnline);

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

        mBookRecord.switchPageIndex(mViewPager.getCurrentItem());
        mBookRecord.saveBookRecord();
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
                switchChapter(mBookRecord.getChapterUrl(position),true);
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
            Map<String, String> map = new HashMap<>(3);
            map.put("updated", getString(R.string.source_change_updated) + DateUtils.relativeDate(getApplicationContext(), source.getUpdated()));
            map.put("title", source.getLastChapter());
            map.put("host", getString(R.string.source_change_host) + source.getHost());

            sourceList.add(map);
        }

        mSourceChangeAdapter = new CustomSourceListAdapter(this, sourceList, R.layout.lv_source_change_item_holder,
                new String[]{"updated", "title", "host"}, new int[]{R.id.tv_updated, R.id.tv_last_chapter, R.id.tv_source_host});


        mSourceChangeListView.setAdapter(mSourceChangeAdapter);

        mSourceChangeDialog.show();

        mSourceChangeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSourceChangeDialog.dismiss();
                mSourceChangeAdapter.setSelectedPosition(position);
                mSourceChangeAdapter.notifyDataSetChanged();
                mHandler.sendEmptyMessage(MSG_LOADING);
                // change sourceId (contentUrl)
                mBookRecord.changeSourceId(mBookRecord.getSourceList().get(position).getSourceId());
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
     * @param loadCurrent   是否重新load当前章节，重新load当前章节会初始化head指针，会从0开始加载
     */
    private void switchChapter(String newChapterUrl, boolean loadCurrent) {
        mBookRecord.switchChapter(newChapterUrl);
        if(loadCurrent) {
            PaginationLoader.getInstance().loadPagination(newChapterUrl);
        }
        PaginationLoader.getInstance().smartLoad();
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
                contentAdapter.updateData(mBookRecord.getChapters());
                contentAdapter.setSelectedPosition(mBookRecord.getChapterIndex(mBookRecord.getCurrentUrl()));
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
                switchChapter(mBookRecord.getCurrentUrl(), true);
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
                mSourceChangeAdapter.setSelectedPosition(mBookRecord.getSourceIndex());
                mSourceChangeAdapter.notifyDataSetChanged();
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
}
