package org.foree.bookreader.data.cache;

import org.foree.bookreader.book.Article;

/**
 * Created by foree on 17-2-6.
 */

public class DiskCache extends ArticleCache {
    private static DiskCache mInstance;

    public static DiskCache getInstance() {
        if (mInstance == null) {
            synchronized (DiskCache.class) {
                if (mInstance == null)
                    mInstance = new DiskCache();
            }
        }
        return mInstance;
    }

    @Override
    public Article get(String chapterUrl) {
        return null;
    }

    @Override
    public void put(String chapterUrl, Article article) {

    }
}
