package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-6.
 */

public abstract class ChapterCache {
    public abstract Chapter get(String chapterUrl);

    public abstract void put(String chapterUrl, Chapter chapter);
}
