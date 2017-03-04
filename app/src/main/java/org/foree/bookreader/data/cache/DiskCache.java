package org.foree.bookreader.data.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.data.dao.BReaderContract;
import org.foree.bookreader.data.dao.BReaderProvider;
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
        Chapter chapter = new Chapter();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        Cursor cursor = BaseApplication.getInstance().getContentResolver().query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE, BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT},
                selection,
                new String[]{chapterUrl},
                null
        );

        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            String contents = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT));
            if (contents == null || contents.isEmpty()) {
                chapter = null;
            } else {
                chapter.setContents(contents);
                chapter.setChapterUrl(chapterUrl);
                chapter.setChapterTitle(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));
            }
            cursor.close();
        }

        return chapter;
    }

    @Override
    public void put(String chapterUrl, Chapter chapter) {
        ContentValues contentValues = new ContentValues();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        // 内容不重复
        contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT, chapter.getContents());
        contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CACHED, true);
        BaseApplication.getInstance().getContentResolver().update(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                contentValues,
                selection,
                new String[]{chapterUrl}
        );
    }
}
