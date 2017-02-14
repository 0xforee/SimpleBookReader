package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Article;
import org.foree.bookreader.pagination.PaginationArgs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 17-1-20.
 * Pagination的缓存article，通过chapterUrl来取用
 */

public class PaginationCache {
    private PaginationArgs paginationArgs;
    private Map<String, Article> mArticleCache;
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
        if (mArticleCache == null)
            mArticleCache = new HashMap<>();
    }

    public void put(String key, Article article) {
        if (!mArticleCache.containsKey(key)) {
            mArticleCache.put(key, article);
        }
    }

    public Article get(String key) {
        return mArticleCache.get(key);

    }
}
