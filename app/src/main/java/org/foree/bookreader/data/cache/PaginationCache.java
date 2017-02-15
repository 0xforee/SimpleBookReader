package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.pagination.PaginationArgs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 17-1-20.
 * Pagination的缓存chapter，通过chapterUrl来取用
 */

public class PaginationCache {
    private PaginationArgs paginationArgs;
    private Map<String, Chapter> mChapterCache;
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
        if (mChapterCache == null)
            mChapterCache = new HashMap<>();
    }

    public void put(String key, Chapter chapter) {
        if (!mChapterCache.containsKey(key)) {
            mChapterCache.put(key, chapter);
        }
    }

    public Chapter get(String key) {
        return mChapterCache.get(key);

    }
}
