package org.foree.bookreader.pagination;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Article;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.ui.adapter.ArticlePagerAdapter;
import org.foree.bookreader.ui.fragment.ArticleFragment;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;

/**
 * Created by foree on 17-1-16.
 * 根据viewPager滑动的方向(左，右）
 * 和偏移量获取Pagination数据
 */

public class PaginationStrategy implements ArticlePagerAdapter.UnlimitedPager {
    private final static String TAG = PaginationStrategy.class.getSimpleName();

    private int mWidth;
    private int mHeight;
    private float mSpacingMult;
    private float mSpacingAdd;
    private TextPaint mPaint;
    private boolean mIncludePad;
    private Context mContext;
    private BookDao bookDao;
    private AbsWebParser absWebParser;
    private String initString;

    private ArticleFragment[] sFragments;

    private String mPreChapterUrl, mChapterUrl, mNextChapterUrl;

    private Pagination mPagination, mNextPagination, mPrePagination;

    private int mOffset = -1;
    private String mPreviousContents, mCurrentContents, mNextContents;

    private int mPageIndex = 0;

    private boolean mFullRefresh = true;

    public PaginationStrategy(Context context, int mWidth, int mHeight, TextPaint mPaint, float mSpacingMult, float mSpacingAdd, boolean mIncludePad) {
        this.mContext = context;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mSpacingMult = mSpacingMult;
        this.mSpacingAdd = mSpacingAdd;
        this.mPaint = mPaint;
        this.mIncludePad = mIncludePad;

        bookDao = new BookDao(mContext);

        initString = mContext.getString(R.string.loading);

        sFragments = new ArticleFragment[]{
                ArticleFragment.newInstance(initString),
                ArticleFragment.newInstance(initString),
                ArticleFragment.newInstance(initString)
        };

        absWebParser = WebParserManager.getAbsWebParser();

    }

    public void setChapterUrl(String chapterUrl) {
        this.mChapterUrl = chapterUrl;
    }

    @Override
    public void onRefreshPage() {
        Log.d(TAG, "onRefreshPage");
        // 准备好数据
        if (mOffset < 0) {
            Log.d(TAG, "向左");

        } else {
            Log.d(TAG, "向右");
        }

        // 首次进入或者切换章节的时候需要init，切换章节请调用reset
        if (mFullRefresh) {
            initPagination();
        }

        mPreviousContents = getContents(mOffset, mPageIndex);
        mCurrentContents = getContents(mOffset, mPageIndex + 1);
        mNextContents = getContents(mOffset, mPageIndex + 2);

        Log.d(TAG, "mChapterUrl = " + mChapterUrl);
        Log.d(TAG, "pageIndex = " + mPageIndex);
        Log.d(TAG, "mPreviousContent = " + mPreviousContents.substring(0, 4));
        Log.d(TAG, "mCurrentContents = " + mCurrentContents.substring(0, 4));
        Log.d(TAG, "mNextContents = " + mNextContents.substring(0, 4));

        resetPage();

    }


    private void resetPage() {
        sFragments[0].setText(mPreviousContents);
        sFragments[1].setText(mCurrentContents);
        sFragments[2].setText(mNextContents);
    }

    @Override
    public void onDataChanged(int offset) {
        Log.d(TAG, "onDataChanged");
        mPageIndex += offset;
        mOffset = offset;

    }

    @Override
    public Fragment getItem(int position) {
        return sFragments[position];
    }

    private String getContents(int flag, int pageIndex) {
        String results;

        // 右滑
        if (flag > 0) {
            // 1. 分章
            if (pageIndex >= mPagination.size()) {
                // 基点没有变化，只获取下一章内容
                if (pageIndex == mPagination.size()) {
                    getNextPagination(flag);

                    results = mNextPagination.get(mPagination.size() - pageIndex);
                } else if (pageIndex == (mPagination.size() + 1)) {
                    // 基点变化，切换基点到下一章
                    mPrePagination.switchTo(mPagination);
                    mPagination.switchTo(mNextPagination);
                    mChapterUrl = mNextChapterUrl;

                    getNextPagination(flag);

                    // 确定此时索引
                    mPageIndex = mPrePagination.size() - pageIndex;

                    results = mPagination.get(mPageIndex + 2);
                } else {
                    // 不应该有这种情况
                    Log.e(TAG, "error");
                    results = "右滑error";
                }
            } else {
                // 2. 分页情况
                results = mPagination.get(pageIndex);
            }

            // 左滑分章
        } else if (flag < 0) {
            if (pageIndex < 0) {
                // 初始化，index == -1
                // 基点没有变化，只是获取上一章
                if (pageIndex == -1) {
                    getPrePagination(flag);
                    pageIndex = mPrePagination.size() - 1;

                    results = mPrePagination.get(pageIndex);
                } else if (pageIndex == -2) {
                    // 基点变化，切换到上一章
                    mNextPagination.switchTo(mPagination);
                    mPagination.switchTo(mPrePagination);
                    // 获取更前一章的内容
                    mChapterUrl = mPreChapterUrl;
                    getPrePagination(flag);

                    // 确定此时此刻的索引
                    mPageIndex = mPagination.size() - 2;

                    results = mPagination.get(mPageIndex);
                } else if (pageIndex == mPagination.size()) {
                    // 基点没有变化，只获取下一章内容
                    getNextPagination(flag);

                    results = mNextPagination.get(mPagination.size() - pageIndex);

                } else {
                    // 不应该有其他情况
                    Log.e(TAG, "error");
                    results = "左滑error";
                }
            } else {
                // 初始化 pageindex = 0 and 1 的情况
                // 2. 分页情况
                results = mPagination.get(pageIndex);
            }
        } else {
            // 不可能情况，flag != 0，< 0表示左滑，>0表示右滑
            results = "翻页error";
        }

        return results != null ? results : initString;
    }

    private void getPrePagination(int flag) {
        // 当前章节 mChapterUrl
        mPreChapterUrl = bookDao.getNextChapterUrlByUrl(flag, mChapterUrl);

        if (mPreChapterUrl != null && !mPreChapterUrl.isEmpty()) {
            absWebParser.getArticle(mPreChapterUrl, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    mPrePagination.clear();
                    mPrePagination.splitPage(data.getContents());
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }

    }

    private void getNextPagination(int flag) {
        // 当前章节 mChapterUrl
        mNextChapterUrl = bookDao.getNextChapterUrlByUrl(flag, mChapterUrl);

        if (mNextChapterUrl != null && !mNextChapterUrl.isEmpty()) {
            absWebParser.getArticle(mNextChapterUrl, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    mNextPagination.clear();
                    mNextPagination.splitPage(data.getContents());
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }

    }

    public void reset(String chapterUrl) {

        mOffset = -1;
        mPageIndex = -1;
        mChapterUrl = chapterUrl;
        mFullRefresh = true;

        onRefreshPage();
    }

    private void initPagination() {
        if (mPagination == null) {
            mPagination = new Pagination(
                    mWidth,
                    mHeight,
                    mPaint,
                    mSpacingMult,
                    mSpacingAdd,
                    mIncludePad);
        }

        if (mPrePagination == null) {
            mPrePagination = new Pagination(
                    mWidth,
                    mHeight,
                    mPaint,
                    mSpacingMult,
                    mSpacingAdd,
                    mIncludePad);
        }

        if (mNextPagination == null) {
            mNextPagination = new Pagination(
                    mWidth,
                    mHeight,
                    mPaint,
                    mSpacingMult,
                    mSpacingAdd,
                    mIncludePad);
        }

        mPagination.clear();
        mPrePagination.clear();
        mNextPagination.clear();

        // 获取当前章节，只在reset的时候进行
        absWebParser.getArticle(mChapterUrl, new NetCallback<Article>() {
            @Override
            public void onSuccess(Article data) {
                mPagination.splitPage(data.getContents());
            }

            @Override
            public void onFail(String msg) {

            }
        });

        getPrePagination(-1);
        getNextPagination(1);
        mFullRefresh = false;

    }
}
