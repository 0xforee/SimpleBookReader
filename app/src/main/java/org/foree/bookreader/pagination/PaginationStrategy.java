package org.foree.bookreader.pagination;

import android.content.Context;
import android.text.TextPaint;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

/**
 * Created by foree on 17-1-16.
 * 根据viewPager滑动的方向(左，右）
 * 和偏移量获取Pagination数据
 */

public class PaginationStrategy {
    private Pagination mPagination, mNextPagination, mPrePagination;

    private int mWidth;
    private int mHeight;
    private float mSpacingMult;
    private float mSpacingAdd;
    private TextPaint mPaint;
    private boolean mIncludePad;
    private Context mContext;
    private BookDao bookDao;

    public PaginationStrategy(Context context, int mWidth, int mHeight, TextPaint mPaint, float mSpacingMult, float mSpacingAdd, boolean mIncludePad) {
        this.mContext = context;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mSpacingMult = mSpacingMult;
        this.mSpacingAdd = mSpacingAdd;
        this.mPaint = mPaint;
        this.mIncludePad = mIncludePad;

        bookDao = new BookDao(mContext);

    }

    public String getContents(String chapterUrl, int flag, int pageIndex) {

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


        // 1. 分章
        if (flag > 0) {
            // 右滑情况
            if ((pageIndex + 1) > mPagination.size()) {
                // 分章
                getNextPagination(chapterUrl, flag);
                pageIndex = 0;

                return mNextPagination.get(pageIndex);

            }

        } else if (flag < 0) {
            // 左滑情况
            if (pageIndex < 0) {
                // 分章
                getPrePagination(chapterUrl, flag);
                pageIndex = mPrePagination.size() - 1;

                return mPrePagination.get(pageIndex);

            }
        }

        // 2. 分页情况
        return mPagination.get(pageIndex);
    }

    private void getPrePagination(String chapterUrl, int flag) {
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

    private void getNextPagination(String chapterUrl, int flag) {
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
