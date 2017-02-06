package org.foree.bookreader.data.cache;

import android.util.LruCache;

import org.foree.bookreader.book.Article;

/**
 * Created by foree on 17-2-6.
 */

public class MemoryCache extends ArticleCache {
    private static MemoryCache mInstance;

    private LruCache<String, Article> mMemoryCache;

    private MemoryCache() {
        int memorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = memorySize / 8;

        mMemoryCache = new LruCache<String, Article>(cacheSize) {
            @Override
            protected int sizeOf(String key, Article value) {
                return value.getContents().length();
            }
        };
    }

    public static MemoryCache getInstance() {
        if (mInstance == null) {
            synchronized (MemoryCache.class) {
                if (mInstance == null)
                    mInstance = new MemoryCache();
            }
        }
        return mInstance;
    }


    @Override
    public Article get(String chapterUrl) {
        return mMemoryCache.get(chapterUrl);
    }

    @Override
    public void put(String chapterUrl, Article article) {
        if (get(chapterUrl) == null)
            mMemoryCache.put(chapterUrl, article);
    }
}
