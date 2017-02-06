package org.foree.bookreader.pagination;

import org.foree.bookreader.data.cache.ArticleCache;
import org.foree.bookreader.data.cache.DoubleCache;

/**
 * Created by foree on 17-2-6.
 */

public class PaginationLoader {

    private static PaginationLoader mInstance;
    private ArticleCache articleCache = new DoubleCache();

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

    public void init() {

    }

    public void setArticleCache(ArticleCache articleCache) {
        this.articleCache = articleCache;
    }
}
