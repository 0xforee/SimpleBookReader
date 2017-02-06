package org.foree.bookreader.data.cache;

import org.foree.bookreader.pagination.Pagination;
import org.foree.bookreader.pagination.PaginationArgs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 17-1-20.
 * Pagination的缓存，通过chapterUrl来取用
 */

public class PaginationCache {
    private PaginationArgs paginationArgs;
    private Map<String, Pagination> mPaginationCache;
    private static PaginationCache mInstance;

    public static PaginationCache getInstance() {
        if (mInstance == null) {
            synchronized (PaginationCache.class) {
                if (mInstance == null)
                    mInstance = new PaginationCache();
            }
        }
        return mInstance;
    }

    public void init(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;

        mPaginationCache = new HashMap<>();
    }

    private void put(String key, Pagination pagination) {
        if (!mPaginationCache.containsKey(key)) {
            mPaginationCache.put(key, pagination);
        }
    }

    private Pagination get(String key) {
        Pagination pagination = mPaginationCache.get(key);

        if (pagination == null) {
            pagination = new Pagination(paginationArgs);
        }

        return pagination;
    }
}
