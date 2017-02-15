package org.foree.bookreader.data.cache;

import android.util.LruCache;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-6.
 */

public class MemoryCache extends ChapterCache {
    private static MemoryCache mInstance;

    private LruCache<String, Chapter> mMemoryCache;

    private MemoryCache() {
        int memorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = memorySize / 8;

        mMemoryCache = new LruCache<>(cacheSize);
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
    public Chapter get(String chapterUrl) {
        return mMemoryCache.get(chapterUrl);
    }

    @Override
    public void put(String chapterUrl, Chapter chapter) {
        if (get(chapterUrl) == null)
            mMemoryCache.put(chapterUrl, chapter);
    }
}
