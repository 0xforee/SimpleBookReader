package org.foree.bookreader.pagination;

import org.foree.bookreader.bean.BookRecord;
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

    public void init(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue();
            mRequestQueue.start();
        }

        PaginationCache.getInstance().init(paginationArgs);
    }

    public void setChapterCache(ChapterCache chapterCache) {
        this.chapterCache = chapterCache;
    }

    public void loadPagination(final String url) {
        mRequestQueue.add(new ChapterRequest(url, paginationArgs, true));

        startPaginationCache(url, 5);

    }

    public ChapterCache getChapterCache() {
        return chapterCache;
    }

    /**
     * 获取指定章节的前后偏移章节
     *
     * @param offset 当前章节的偏移量
     * @param url    当前章节的url
     */
    private void startPaginationCache(final String url, final int offset) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                int tmp = 1;
                final int index = BookRecord.getInstance().getIndexFromUrl(url);
                if (index != -1) {

                    // 前几章
                    int newIndex = index - tmp;
                    while (newIndex > 0 && tmp < offset) {
                        if (!BookRecord.getInstance().isChapterCached(newIndex)) {
                            // add request
                            mRequestQueue.add(new ChapterRequest(BookRecord.getInstance().getUrl(newIndex), paginationArgs, false));
                        }
                        tmp++;
                        newIndex = index - tmp;
                    }

                    // 后几章
                    tmp = 1;
                    newIndex = index + tmp;

                    while (newIndex < BookRecord.getInstance().getChaptersSize() && tmp < offset) {
                        if (!BookRecord.getInstance().isChapterCached(newIndex)) {
                            // add request
                            mRequestQueue.add(new ChapterRequest(BookRecord.getInstance().getUrl(newIndex), paginationArgs, false));

                        }
                        tmp++;
                        newIndex = index + tmp;
                    }

                }
            }
        }.start();

    }
}
