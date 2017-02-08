package org.foree.bookreader.pagination;

import org.foree.bookreader.data.cache.ArticleCache;
import org.foree.bookreader.data.cache.DoubleCache;
import org.foree.bookreader.data.cache.PaginationCache;
import org.foree.bookreader.data.event.PaginationState;
import org.greenrobot.eventbus.EventBus;

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
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue();
            mRequestQueue.start();
        }

        PaginationCache.getInstance().init(paginationArgs);
    }

    public void setArticleCache(ArticleCache articleCache) {
        this.articleCache = articleCache;
    }

    public Pagination getPagination(String url) {

        Pagination pagination = PaginationCache.getInstance().get(url);

        if (pagination == null) {
            pagination = new Pagination(paginationArgs);
            ArticleRequest articleRequest = new ArticleRequest(pagination, url);

            mRequestQueue.add(articleRequest);
        }else {
            EventBus.getDefault().post(new PaginationState(PaginationState.STATE_SUCCESS, url));
        }

        return pagination;

    }
}
