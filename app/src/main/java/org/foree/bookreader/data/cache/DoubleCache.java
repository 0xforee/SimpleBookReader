package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Article;

/**
 * Created by foree on 17-2-6.
 */

public class DoubleCache extends ArticleCache {
    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public void DoubleCache() {
        memoryCache = MemoryCache.getInstance();
        diskCache = DiskCache.getInstance();
    }

    @Override
    public Article get(String chapterUrl) {
        Article article = memoryCache.get(chapterUrl);
        if (article == null) {
            article = diskCache.get(chapterUrl);
            if (article != null) {
                memoryCache.put(chapterUrl, article);
            }
        }
        return article;
    }

    @Override
    public void put(String chapterUrl, Article article) {
        memoryCache.put(chapterUrl, article);
        diskCache.put(chapterUrl, article);
    }
}
