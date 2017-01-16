package org.foree.bookreader.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.pagination.PaginationStrategy;
import org.foree.bookreader.ui.adapter.ArticlePagerAdapter;
import org.foree.bookreader.ui.fragment.ArticleFragment;
import org.foree.bookreader.ui.adapter.ItemListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity implements ArticlePagerAdapter.UnlimitedPager {
    private static final String TAG = ArticleActivity.class.getSimpleName();
    FloatingActionButton turnNightMode;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    String chapterUrl, bookUrl;
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private List<Chapter> chapterList = new ArrayList<>();
    boolean turnFlag = true;
    private PopupWindow popupWindow;
    private View rootView;
    private BookDao bookDao;

    private int recentChapterId = -1;

    private ViewPager mViewPager;
    private ArticlePagerAdapter articlePagerAdapter;
    private TextView mTextView;

    private PaginationStrategy mPaginationStrategy;
    private ArticlePagerAdapter.UnlimitedPager unlimitedPager;

    private boolean initFinished = false;

    private int mOffset = -1;
    private String mInitString = "正在加载...";

    private String mPreviousContents = mInitString;
    private String mCurrentContents = mInitString;
    private String mNextContents = mInitString;

    private final ArticleFragment[] sFragments = new ArticleFragment[] {
            ArticleFragment.newInstance(mInitString),
            ArticleFragment.newInstance(mInitString),
            ArticleFragment.newInstance(mInitString)
    };
    private int mPageIndex = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_layout);

        bookUrl = getIntent().getExtras().getString("book_url");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setUpLayoutViews();

        bookDao = new BookDao(this);
        openBook(bookUrl);

        initTextView();

        /*turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(turnFlag) {
                    // night Mode
                    tvContent.setTextColor(getResources().getColor(R.color.nightTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.nightTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.nightBackground));
                    turnFlag = false;
                }else{
                    // day Mode
                    tvContent.setTextColor(getResources().getColor(R.color.dayTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.dayTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.dayBackground));
                    turnFlag = true;
                }
            }
        });
*/

    }

    private void initTextView() {
        unlimitedPager = this;

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

                if( mPaginationStrategy == null) {
                    mPaginationStrategy = new PaginationStrategy(
                            getApplicationContext(),
                            mTextView.getWidth(),
                            mTextView.getHeight(),
                            mTextView.getPaint(),
                            mTextView.getLineSpacingMultiplier(),
                            mTextView.getLineSpacingExtra(),
                            mTextView.getIncludeFontPadding());
                }
                if( !initFinished ) {

                    mViewPager = (ViewPager) findViewById(R.id.book_pager);
                    articlePagerAdapter = new ArticlePagerAdapter(mViewPager, getSupportFragmentManager());
                    articlePagerAdapter.setPage(unlimitedPager);
                    mViewPager.setAdapter(articlePagerAdapter);
                    initFinished = true;
                }

            }
        });
    }

    @Override
    public void onRefreshPage() {
        Log.d(TAG, "onRefreshPage");
        if (mPaginationStrategy != null) {
            // 准备好数据
            if (mOffset < 0) {
                Log.d(TAG, "向左");

            } else {
                Log.d(TAG, "向右");
            }

            mPreviousContents = mPaginationStrategy.getContents(chapterUrl, mOffset, mPageIndex);
            mCurrentContents = mPaginationStrategy.getContents(chapterUrl, mOffset, mPageIndex + 1);
            mNextContents = mPaginationStrategy.getContents(chapterUrl, mOffset, mPageIndex + 2);

            Log.d(TAG, "pageIndex = " + mPageIndex);
            Log.d(TAG, "mPreviousContent = " + mPreviousContents);
            Log.d(TAG, "mCurrentContents = " + mCurrentContents);
            Log.d(TAG, "mNextContents = " + mNextContents);

            resetPage();

        }
    }

    private void resetPage(){
        if( mPreviousContents != null)
            sFragments[0].setText(mPreviousContents);
        if( mCurrentContents != null)
            sFragments[1].setText(mCurrentContents);
        if( mNextContents != null)
            sFragments[2].setText(mNextContents);
    }

    @Override
    public void onDataChanged(int offset) {
        Log.d(TAG,"onDataChanged");
        mPageIndex += offset;
        mOffset = offset;

    }

    @Override
    public Fragment getItem(int position) {
        return sFragments[position];
    }

    @Override
    protected void onDestroy() {
        closeBook();
        super.onDestroy();
    }

    private void setUpLayoutViews() {
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_article, null);


        // get FloatActionButton
        turnNightMode = (FloatingActionButton) findViewById(R.id.fab);

        turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow == null)
                    showPopup();
                else
                    popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
        });
    }

    private void showPopup() {
        View view = LayoutInflater.from(this).inflate(R.layout.popupwindow_layout, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(dp.heightPixels / 4 * 3);

        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        setUpRecyclerViewAdapter();
    }

    private void setUpRecyclerViewAdapter() {
        mAdapter = new ItemListAdapter(this, chapterList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                chapterUrl = chapterList.get(position).getChapterUrl();
                recentChapterId = chapterList.get(position).getChapterId();
                popupWindow.dismiss();

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void openBook(String bookUrl) {
        //get all chapter
        Book book = bookDao.findBookInfoByUrl(bookUrl);
        // get chapterList
        chapterList = book.getChapterList();
        // check init
        if (book.getRecentChapterId() == -1) {
            // get first chapter id
            setChapterId(bookUrl, chapterList.get(0).getChapterId());
            // update book object recentChapterId
            book.setRecentChapterId(chapterList.get(0).getChapterId());
        }

        // open by chapter id
        chapterUrl = bookDao.findChapterUrlById(book.getRecentChapterId());
    }

    private void setChapterId(String bookUrl, int newId) {
        bookDao.updateRecentChapterId(bookUrl, newId);
    }

    private void closeBook() {
        // set ChapterId
        if (recentChapterId != -1)
            bookDao.updateRecentChapterId(bookUrl, recentChapterId);
    }


}
