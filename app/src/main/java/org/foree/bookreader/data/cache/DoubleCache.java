package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-6.
 */

public class DoubleCache extends ChapterCache {
    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public void DoubleCache() {
        memoryCache = MemoryCache.getInstance();
        diskCache = DiskCache.getInstance();
    }

    @Override
    public Chapter get(String chapterUrl) {
        Chapter chapter = memoryCache.get(chapterUrl);
        if (chapter == null) {
            chapter = diskCache.get(chapterUrl);
            if (chapter != null) {
                memoryCache.put(chapterUrl, chapter);
            }
        }
        return chapter;
    }

    @Override
    public void put(String chapterUrl, Chapter chapter) {
        memoryCache.put(chapterUrl, chapter);
        diskCache.put(chapterUrl, chapter);
    }
}
