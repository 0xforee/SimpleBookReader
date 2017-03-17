package org.foree.bookreader.pagination;

import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.bean.cache.ChapterCache;
import org.foree.bookreader.bean.cache.DoubleCache;
import org.foree.bookreader.bean.cache.PaginationCache;
import org.foree.bookreader.bean.dao.BookDao;

import java.util.Iterator;
import java.util.Map;

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

        new Thread() {
            @Override
            public void run() {
                super.run();
                // start cached policy
                Map<String, Boolean> mCached = new BookDao(BaseApplication.getInstance()).getChapterUrlLimit(-1, url, 5);
                Iterator iterator = mCached.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    if (!(boolean) entry.getValue())
                        mRequestQueue.add(new ChapterRequest((String) entry.getKey(), paginationArgs, false));
                }

                mCached = new BookDao(BaseApplication.getInstance()).getChapterUrlLimit(1, url, 5);
                iterator = mCached.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    if (!(boolean) entry.getValue())
                        mRequestQueue.add(new ChapterRequest((String) entry.getKey(), paginationArgs, false));
                }
            }
        }.start();

    }

    public ChapterCache getChapterCache() {
        return chapterCache;
    }
}
