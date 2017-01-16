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
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

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
    private String initString;

    private ArticleFragment[] sFragments;

    private String chapterUrl;

    private Pagination mPagination, mNextPagination, mPrePagination;

    private int mOffset = -1;
    private String mPreviousContents, mCurrentContents, mNextContents;

    private int mPageIndex = 0;

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

        sFragments = new ArticleFragment[] {
                ArticleFragment.newInstance(initString),
                ArticleFragment.newInstance(initString),
                ArticleFragment.newInstance(initString)
        };

    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
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

            mPreviousContents = getContents(mOffset, mPageIndex);
            mCurrentContents = getContents(mOffset, mPageIndex + 1);
            mNextContents = getContents(mOffset, mPageIndex + 2);

            Log.d(TAG, "chapterUrl = " + chapterUrl);
            Log.d(TAG, "pageIndex = " + mPageIndex);
            Log.d(TAG, "mPreviousContent = " + mPreviousContents);
            Log.d(TAG, "mCurrentContents = " + mCurrentContents);
            Log.d(TAG, "mNextContents = " + mNextContents);

            resetPage();

    }


    private void resetPage(){
        sFragments[0].setText(mPreviousContents);
        sFragments[1].setText(mCurrentContents);
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

    private String getContents(int flag, int pageIndex) {
        String results;

        if (mPagination == null) {
            mPagination = new Pagination(
                    mWidth,
                    mHeight,
                    mPaint,
                    mSpacingMult,
                    mSpacingAdd,
                    mIncludePad);

            WebInfo webInfo = new BiQuGeWebInfo();
            webInfo.getArticle(chapterUrl, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    mPagination.clear();
                    mPagination.splitPage(data.getContents());
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }


            // 右滑分章
        if (flag > 0 && (pageIndex + 1) > mPagination.size()) {
            // 分章
            getNextPagination(flag);
            pageIndex = 0;

            results = mNextPagination.get(pageIndex);

            // 左滑分章
        } else if (flag < 0 && pageIndex < 0) {
            getPrePagination(flag);
            pageIndex = mPrePagination.size() - 1;

            results = mPrePagination.get(pageIndex);

        } else {
            // 2. 分页情况
            results = mPagination.get(pageIndex);
        }

        return results != null ? results : initString;
    }

    private void getPrePagination(int flag) {
        // 当前章节 chapterUrl
        String preChapterUrl = bookDao.getNextChapterUrlByUrl(flag, chapterUrl);
        mPrePagination = new Pagination(
                mWidth,
                mHeight,
                mPaint,
                mSpacingMult,
                mSpacingAdd,
                mIncludePad);

        WebInfo webInfo = new BiQuGeWebInfo();
        if (preChapterUrl != null && !preChapterUrl.isEmpty()) {
            webInfo.getArticle(preChapterUrl, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    mPrePagination.splitPage(data.getContents());
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }


    }

    private void getNextPagination(int flag) {
        // 当前章节 chapterUrl
        String nextChapterUrl = bookDao.getNextChapterUrlByUrl(flag, chapterUrl);
        mNextPagination = new Pagination(
                mWidth,
                mHeight,
                mPaint,
                mSpacingMult,
                mSpacingAdd,
                mIncludePad);

        WebInfo webInfo = new BiQuGeWebInfo();
        if (nextChapterUrl != null && !nextChapterUrl.isEmpty()) {
            webInfo.getArticle(nextChapterUrl, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    mNextPagination.splitPage(data.getContents());
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }


    }
}
