package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-6.
 */

public class DiskCache extends ChapterCache {
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
    public Chapter get(String chapterUrl) {
        return null;
    }

    @Override
    public void put(String chapterUrl, Chapter chapter) {

    }
}
