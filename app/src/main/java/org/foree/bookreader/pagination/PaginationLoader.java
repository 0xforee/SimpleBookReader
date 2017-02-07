package org.foree.bookreader.pagination;

import org.foree.bookreader.data.cache.ArticleCache;
import org.foree.bookreader.data.cache.DoubleCache;

/**
 * Created by foree on 17-2-6.
 */

public class PaginationLoader {
    private PaginationArgs paginationArgs;
    private RequestQueue mRequestQueue;

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

    public void init(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;

        mRequestQueue = new RequestQueue();
        mRequestQueue.start();
    }

    public void setArticleCache(ArticleCache articleCache) {
        this.articleCache = articleCache;
    }

    public Pagination getPagnation(String url) {
        Pagination pagination = new Pagination(paginationArgs);

        ArticleRequest articleRequest = new ArticleRequest(pagination, url);

        mRequestQueue.add(articleRequest);

        return pagination;

    }
}
