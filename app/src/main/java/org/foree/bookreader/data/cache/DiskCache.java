package org.foree.bookreader.data.cache;

import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.data.dao.BookDao;

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
        BookDao bookDao = new BookDao(BaseApplication.getInstance());
        return bookDao.getChapter(chapterUrl);
    }

    @Override
    public void put(String chapterUrl, Chapter chapter) {
        BookDao bookDao = new BookDao(BaseApplication.getInstance());
        bookDao.saveChapterContent(chapter);
    }
}
