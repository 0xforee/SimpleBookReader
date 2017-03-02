package org.foree.bookreader.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 * TODO:性能优化，批量操作不使用循环
 */
public class BookDao {
    private static final String TAG = BookDao.class.getSimpleName();
    private BookSQLiteOpenHelper bookSQLiteOpenHelper;

    public BookDao(Context context) {
        bookSQLiteOpenHelper = new BookSQLiteOpenHelper(context);
    }

    /**
     * 获取本地所有书
     *
     * @return bookList
     */
    public List<Book> getAllBooks() {
        synchronized (this) {
            Cursor cursor;
            List<Book> bookList = new ArrayList<>();
            SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
            db.beginTransaction();

            cursor = db.query(BReaderContract.Books.TABLE_NAME, null,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                String bookName = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_NAME));
                String bookUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_URL));
                String updateTime = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME));
                String category = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CATEGORY));
                String author = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_AUTHOR));
                String description = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_DESCRIPTION));
                int recentChapterId = cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_RECENT_ID));
                int pageIndex = cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX));
                String bookCoverUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_COVER_URL));
                String contentUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CONTENT_URL));
                Book book = new Book(bookName, bookUrl, updateTime, category, author, description, pageIndex, recentChapterId, bookCoverUrl, contentUrl);
                bookList.add(book);
            }

            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            return bookList;
        }
    }

    // TODO:需要处理章节更新时会覆盖掉原有的缓存内容的问题
    public void insertChapters(List<Chapter> chapterList) {
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert chapterList.size= " + chapterList.size() + " to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while (chapterList.size() > (1000 * tmp)) {
                insertChaptersInternal(chapterList.subList(1000 * (tmp - 1), 1000 * tmp));
                tmp++;
            }
            insertChaptersInternal(chapterList.subList(1000 * (tmp - 1), chapterList.size()));
        }
    }

    private void insertChaptersInternal(List<Chapter> subList) {
        synchronized (this) {
            SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
            try {
                db.beginTransaction();
                ContentValues contentValues = new ContentValues();
                for (Chapter chapter : subList) {
                    contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE, chapter.getChapterTitle());
                    contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL, chapter.getChapterUrl());
                    contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID, chapter.getChapterId());
                    contentValues.put(BReaderContract.Chapters.COLUMN_NAME_BOOK_URL, chapter.getBookUrl());
                    db.insertWithOnConflict(BReaderContract.Chapters.TABLE_NAME, null,
                            contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                }

                db.setTransactionSuccessful();

            } finally {
                db.endTransaction();
                db.close();
            }
        }

    }

    private List<Chapter> getChapters(String bookUrl) {
        Log.d(TAG, "get chapterList from db, bookUrl = " + bookUrl);
        Cursor cursor;
        List<Chapter> chapterList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();
        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        // chapter_id sort by desc or asc
        cursor = db.query(BReaderContract.Chapters.TABLE_NAME, null,
                selection, new String[]{bookUrl}, null, null, orderBy);
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE));
            String url = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
            boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
            int chapter_id = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
            Chapter chapter = new Chapter(title, url, bookUrl, chapter_id, offline);
            chapterList.add(chapter);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapterList;
    }

    public void updateBookTime(String bookUrl, String updateTime) {
        synchronized (this) {
            Log.d(TAG, "update book " + bookUrl + " Time " + updateTime);
            SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

            // 内容不重复
            contentValues.put(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME, updateTime);

            db.beginTransaction();
            try {

                db.update(BReaderContract.Books.TABLE_NAME,
                        contentValues,
                        selection,
                        new String[]{bookUrl});

                db.setTransactionSuccessful();

            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public Book getBook(String bookUrl) {
        Log.d(TAG, "get book info from db, bookUrl = " + bookUrl);
        Book book = new Book();
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        cursor = db.query(BReaderContract.Books.TABLE_NAME, null,
                selection, new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            book.setBookName(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_NAME)));
            book.setUpdateTime(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME)));
            book.setBookUrl(bookUrl);
            book.setCategory(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CATEGORY)));
            book.setAuthor(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_AUTHOR)));
            book.setDescription(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_DESCRIPTION)));
            book.setRecentChapterId(cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_RECENT_ID)));
            book.setPageIndex(cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX)));
            book.setBookCoverUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_COVER_URL)));
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        book.setChapters(getChapters(bookUrl));

        return book;
    }

    public void addBook(Book book) {
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_URL, book.getBookUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_NAME, book.getBookName());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME, book.getUpdateTime());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_CATEGORY, book.getCategory());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_AUTHOR, book.getAuthor());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_DESCRIPTION, book.getDescription());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_ID, -1);
        contentValues.put(BReaderContract.Books.COLUMN_NAME_COVER_URL, book.getBookCoverUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_CONTENT_URL, book.getContentUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, 0);

        db.beginTransaction();

        try {
            db.insertWithOnConflict(BReaderContract.Books.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            db.close();
        }

        // insert chapters
        insertChapters(book.getChapters());
    }

    public void removeBook(String book_url) {
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();

        Cursor cursor = db.query(BReaderContract.Books.TABLE_NAME, null,
                selection, new String[]{book_url}, null, null, null);

        // remove bookInfo
        if (cursor.getCount() != 0) {
            try {
                db.delete(BReaderContract.Books.TABLE_NAME, selection, new String[]{book_url});

                // remove chapters
                cursor = db.query(BReaderContract.Chapters.TABLE_NAME, null,
                        selection, new String[]{book_url}, null, null, null);

                if (cursor.getCount() != 0) {
                    db.delete(BReaderContract.Chapters.TABLE_NAME, selection, new String[]{book_url});
                }
            } finally {
                cursor.close();
                db.close();
            }
        }

    }

    public void updateRecentChapterId(String bookUrl, int recentChapterId) {
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        cursor = db.query(BReaderContract.Books.TABLE_NAME, null,
                selection, new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_ID, recentChapterId);

            try {
                db.update(BReaderContract.Books.TABLE_NAME, contentValues,
                        selection, new String[]{bookUrl});
                db.setTransactionSuccessful();

            } finally {
                cursor.close();
                db.endTransaction();
                db.close();
            }
        }


    }

    public void updatePageIndex(String bookUrl, int pageIndex) {
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        cursor = db.query(BReaderContract.Books.TABLE_NAME, null,
                selection, new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, pageIndex);
            try {
                db.update(BReaderContract.Books.TABLE_NAME, contentValues,
                        selection, new String[]{bookUrl});
                db.setTransactionSuccessful();
            } finally {
                cursor.close();

                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 根据chapterUrl获取chapterId
     *
     * @param chapterUrl 章节地址
     * @return chapterId
     */
    public int getChapterId(String chapterUrl) {
        Cursor cursor;
        int chapterId = 0;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        cursor = db.query(BReaderContract.Chapters.TABLE_NAME, null,
                selection, new String[]{chapterUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterId = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapterId;
    }

    /**
     * 根据chapterId获取chapterUrl
     *
     * @param chapterId 根据chapterUrl解析出来的chapterId
     * @return chapterUrl
     */
    public String getChapterUrl(int chapterId) {
        Cursor cursor;
        String chapterUrl = null;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + "=?";

        cursor = db.query(BReaderContract.Chapters.TABLE_NAME, null,
                selection, new String[]{chapterId + ""}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapterUrl;
    }

    /**
     * TODO: 根据指定url和偏移量获取目标url
     *
     * @param flag 偏移量，-1=上一章，1=后一章
     * @param url  指定url
     * @return 有则返回目标url，没有返回null
     */
    public String getChapterUrl(int flag, String url) {

        Cursor cursor;
        String chapterUrl = null;
        String bookUrl = null;
        String orderBy = null;

        if (url != null) {

            int chapterId = getChapterId(url);

            SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
            db.beginTransaction();

            String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

            // 限定条件加入bookUrl限定
            cursor = db.query(BReaderContract.Chapters.TABLE_NAME, new String[]{BReaderContract.Chapters.COLUMN_NAME_BOOK_URL},
                    selection, new String[]{url}, null, null, null);
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                bookUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_BOOK_URL));
            }

            if (bookUrl != null) {
                if (flag > 0) {
                    // 获取下一章url
                    selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + " = ? and " +
                            BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " > ?";
                    orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";
                } else {
                    // 获取上一章url
                    selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + " = ? and " +
                            BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " < ?";
                    orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " desc";
                }

                cursor = db.query(BReaderContract.Chapters.TABLE_NAME, new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL},
                        selection, new String[]{bookUrl, chapterId + ""}, null, null, orderBy);
                if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                    chapterUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                } else {
                    // 没有上一章或者没有下一章
                    chapterUrl = null;
                }
            }

            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

        return chapterUrl;
    }

    /**
     * 从数据库中获取章节内容
     *
     * @param chapterUrl 根据chapterUrl提取
     * @return 章节对象
     */
    public Chapter getChapter(String chapterUrl) {
        Log.d(TAG, "getChapter " + chapterUrl + " from db");
        Cursor cursor;
        Chapter chapter = new Chapter();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        cursor = db.query(BReaderContract.Chapters.TABLE_NAME, null,
                selection, new String[]{chapterUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            String contents = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT));
            if (contents == null || contents.isEmpty()) {
                chapter = null;
            } else {
                chapter.setContents(contents);
                chapter.setChapterUrl(chapterUrl);
                chapter.setChapterTitle(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));
            }
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapter;
    }

    public void saveChapterContent(Chapter chapter) {
        Log.d(TAG, "save chapter " + chapter.getChapterUrl());
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        // 内容不重复
        contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT, chapter.getContents());
        contentValues.put(BReaderContract.Chapters.COLUMN_NAME_CACHED, true);

        try {
            db.update(BReaderContract.Chapters.TABLE_NAME,
                    contentValues,
                    selection,
                    new String[]{chapter.getChapterUrl()});
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            db.close();
        }

    }
}
