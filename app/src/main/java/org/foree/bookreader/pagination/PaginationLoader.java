package org.foree.bookreader.pagination;

import org.foree.bookreader.readpage.BookRecord;
import org.foree.bookreader.bean.cache.ChapterCache;
import org.foree.bookreader.bean.cache.DoubleCache;
import org.foree.bookreader.bean.cache.PaginationCache;

/**
 * Created by foree on 17-2-6.
 */

public class PaginationLoader {
    private PaginationArgs paginationArgs;
    private RequestQueue mRequestQueue;

    private static PaginationLoader mInstance;

    private ChapterCache chapterCache = new DoubleCache();

    public static PaginationLoader getInstance() {
        if (mInstance == null) {
            synchronized (PaginationLoader.class) {
                if (mInstance == null) {
                    mInstance = new PaginationLoader();
                }
            }
        }

        return mInstance;
    }

    private PaginationLoader() {
    }

    public PaginationLoader init(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue();
            mRequestQueue.start();
        }

        PaginationCache.getInstance().init(paginationArgs);

        return this;
    }

    private BookRecord mBookRecord;
    private int mOffset;

    public PaginationLoader smartLoadInit(BookRecord bookRecord, int offset) {
        mBookRecord = bookRecord;
        mOffset = offset;

        return this;
    }

    public void loadPagination(final String url) {
        mRequestQueue.add(new ChapterRequest(url, paginationArgs, true));

        smartLoad();
    }

    public ChapterCache getChapterCache() {
        return chapterCache;
    }

    /**
     * 获取指定章节的前后偏移章节
     */
    private void smartLoad() {
        if (mBookRecord != null && mOffset != 0) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    int tmp = 1;
                    final int index = mBookRecord.getIndexFromUrl(mBookRecord.getCurrentUrl());
                    if (index != -1) {

                        // 前几章
                        int newIndex = index - tmp;
                        while (newIndex > 0 && tmp <= mOffset) {
                            if (!mBookRecord.isChapterCached(newIndex)) {
                                // add request
                                mRequestQueue.add(new ChapterRequest(mBookRecord.getUrl(newIndex), paginationArgs, false));
                            }
                            tmp++;
                            newIndex = index - tmp;
                        }

                        // 后几章
                        tmp = 1;
                        newIndex = index + tmp;

                        while (newIndex < mBookRecord.getChaptersSize() && tmp <= mOffset) {
                            if (!mBookRecord.isChapterCached(newIndex)) {
                                // add request
                                mRequestQueue.add(new ChapterRequest(mBookRecord.getUrl(newIndex), paginationArgs, false));

                            }
                            tmp++;
                            newIndex = index + tmp;
                        }

                    }
                }
            }.start();
        }

    }
}
