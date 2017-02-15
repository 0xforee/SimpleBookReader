package org.foree.bookreader.pagination;

import org.foree.bookreader.data.cache.ChapterCache;
import org.foree.bookreader.data.cache.DoubleCache;
import org.foree.bookreader.data.cache.PaginationCache;

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

    public void loadPagination(String url) {
        mRequestQueue.add(new ChapterRequest(url, paginationArgs));

    }
}
