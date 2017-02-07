package org.foree.bookreader.pagination;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.ui.adapter.ArticlePagerAdapter;
import org.foree.bookreader.ui.fragment.ArticleFragment;

/**
 * Created by foree on 17-1-16.
 * 根据viewPager滑动的方向(左，右）
 * 和偏移量获取Pagination数据
 */

public class PaginationSwitch implements ArticlePagerAdapter.UnlimitedPager {
    private final static String TAG = PaginationSwitch.class.getSimpleName();

    private Context mContext;
    private BookDao bookDao;

    private ArticleFragment[] sFragments;

    private String mPreChapterUrl, mChapterUrl, mNextChapterUrl;

    private Pagination mPagination, mNextPagination, mPrePagination;

    private int mOffset = -1;
    private String mPreviousContents, mCurrentContents, mNextContents;

    private int mFirstPageIndex = 0;

    private boolean mFullRefresh = true;

    public PaginationSwitch(Context context) {

        mContext = context;

        bookDao = new BookDao(mContext);

        sFragments = new ArticleFragment[]{
                new ArticleFragment(),
                new ArticleFragment(),
                new ArticleFragment()
        };

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

        mPreviousContents = getContents(mOffset, mFirstPageIndex);
        mCurrentContents = getContents(mOffset, mFirstPageIndex + 1);
        mNextContents = getContents(mOffset, mFirstPageIndex + 2);

        Log.d(TAG, "mChapterUrl = " + mChapterUrl);
        Log.d(TAG, "pageIndex = " + mFirstPageIndex);

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
        mFirstPageIndex += offset;
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
                    mFirstPageIndex = mPrePagination.size() - pageIndex;

                    results = mPagination.get(mFirstPageIndex + 2);
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
                    mFirstPageIndex = mPagination.size() - 2;

                    results = mPagination.get(mFirstPageIndex);
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

        return results != null ? results : "";
    }

    private void getPrePagination(int flag) {
        // 当前章节 mChapterUrl
        mPreChapterUrl = bookDao.getNextChapterUrlByUrl(flag, mChapterUrl);

        mPrePagination = PaginationLoader.getInstance().getPagination(mPreChapterUrl);

    }

    private void getNextPagination(int flag) {
        // 当前章节 mChapterUrl
        mNextChapterUrl = bookDao.getNextChapterUrlByUrl(flag, mChapterUrl);

        mNextPagination = PaginationLoader.getInstance().getPagination(mNextChapterUrl);

    }

    // 章节列表切换章节，reset相关index
    public void reset(String chapterUrl) {

        mOffset = -1;
        mFirstPageIndex = -1;
        mChapterUrl = chapterUrl;
        mFullRefresh = true;

        onRefreshPage();
    }

    private void initPagination() {
        mPagination = PaginationLoader.getInstance().getPagination(mChapterUrl);

        getPrePagination(-1);
        getNextPagination(1);
        mFullRefresh = false;

    }
}
