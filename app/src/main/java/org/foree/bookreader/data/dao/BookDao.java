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
        Cursor cursor;
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null,
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            String bookName = cursor.getString(cursor.getColumnIndex("book_name"));
            String bookUrl = cursor.getString(cursor.getColumnIndex("book_url"));
            String updateTime = cursor.getString(cursor.getColumnIndex("update_time"));
            String category = cursor.getString(cursor.getColumnIndex("category"));
            String author = cursor.getString(cursor.getColumnIndex("author"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            int recentChapterId = cursor.getInt(cursor.getColumnIndex("recent_chapter_id"));
            int pageIndex = cursor.getInt(cursor.getColumnIndex("page_index"));
            String bookCoverUrl = cursor.getString(cursor.getColumnIndex("book_cover_url"));
            String contentUrl = cursor.getString(cursor.getColumnIndex("content_url"));
            Book book = new Book(bookName, bookUrl, updateTime, category, author, description, pageIndex, recentChapterId, bookCoverUrl, contentUrl);
            bookList.add(book);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return bookList;
    }

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
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            for (Chapter chapter : subList) {
                contentValues.put("chapter_title", chapter.getChapterTitle());
                contentValues.put("chapter_url", chapter.getChapterUrl());
                contentValues.put("chapter_id", chapter.getChapterId());
                contentValues.put("book_url", chapter.getBookUrl());
                if (db.insertWithOnConflict(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                        contentValues, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
                    Log.e(TAG, "Database insert chapter_url: " + chapter.getChapterUrl() + " error");
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    private List<Chapter> getChapters(String bookUrl) {
        Log.d(TAG, "get chapterList from db, bookUrl = " + bookUrl);
        Cursor cursor;
        List<Chapter> chapterList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        // chapter_id sort by desc or asc
        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                "book_url=?", new String[]{bookUrl}, null, null, "chapter_id asc");
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("chapter_title"));
            String url = cursor.getString(cursor.getColumnIndex("chapter_url"));
            int chapter_id = cursor.getInt(cursor.getColumnIndex("chapter_id"));
            Chapter chapter = new Chapter(title, url, bookUrl, chapter_id);
            chapterList.add(chapter);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapterList;
    }

    public void updateBookTime(String bookUrl, String updateTime) {
        Log.d(TAG, "update book " + bookUrl + " Time " + updateTime);
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put("update_time", updateTime);
        if (db.update(BookSQLiteOpenHelper.DB_TABLE_BOOKS,
                contentValues,
                "book_url=?",
                new String[]{bookUrl}) == -1) {
            Log.e(TAG, "Database insert id: " + bookUrl + " error");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public Book getBook(String bookUrl) {
        Log.d(TAG, "get book info from db, bookUrl = " + bookUrl);
        Book book = new Book();
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null,
                "book_url=?", new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            book.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            book.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
            book.setBookUrl(bookUrl);
            book.setCategory(cursor.getString(cursor.getColumnIndex("category")));
            book.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            book.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            book.setRecentChapterId(cursor.getInt(cursor.getColumnIndex("recent_chapter_id")));
            book.setPageIndex(cursor.getInt(cursor.getColumnIndex("page_index")));
            book.setBookCoverUrl(cursor.getString(cursor.getColumnIndex("book_cover_url")));
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
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put("book_url", book.getBookUrl());
        contentValues.put("book_name", book.getBookName());
        contentValues.put("update_time", book.getUpdateTime());
        contentValues.put("category", book.getCategory());
        contentValues.put("author", book.getAuthor());
        contentValues.put("description", book.getDescription());
        contentValues.put("recent_chapter_id", -1);
        contentValues.put("book_cover_url", book.getBookCoverUrl());
        contentValues.put("content_url", book.getContentUrl());
        contentValues.put("page_index", 0);
        if (db.insertWithOnConflict(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            Log.e(TAG, "Database insert id: " + book.getBookUrl() + " error");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        // insert chapters
        insertChapters(book.getChapters());
    }

    public void removeBook(String book_url) {
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null,
                "book_url=?", new String[]{book_url}, null, null, null);

        // remove bookInfo
        if (cursor.getCount() != 0) {
            if (db.delete(BookSQLiteOpenHelper.DB_TABLE_BOOKS, "book_url=?", new String[]{book_url}) == -1) {
                Log.e(TAG, "delete book_url:" + book_url + " error");
            } else {
                // remove chapters
                cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                        "book_url=?", new String[]{book_url}, null, null, null);
                if (cursor.getCount() != 0) {
                    if (db.delete(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, "book_url=?", new String[]{book_url}) == -1) {
                        Log.e(TAG, "delete book's " + book_url + "chapters error");
                    }
                }
            }
        }

        cursor.close();
        db.close();
    }

    public void updateRecentChapterId(String bookUrl, int recentChapterId) {
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null,
                "book_url=?", new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("recent_chapter_id", recentChapterId);
            if (db.update(BookSQLiteOpenHelper.DB_TABLE_BOOKS, contentValues,
                    "book_url=?", new String[]{bookUrl}) == -1) {
                Log.e(TAG, "Database insert book_url: " + bookUrl + " error");
            }
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

    }

    public void updatePageIndex(String bookUrl, int pageIndex) {
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOKS, null,
                "book_url=?", new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("page_index", pageIndex);
            if (db.update(BookSQLiteOpenHelper.DB_TABLE_BOOKS, contentValues,
                    "book_url=?", new String[]{bookUrl}) == -1) {
                Log.e(TAG, "Database insert book_url: " + bookUrl + " error");
            }
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
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

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                "chapter_url=?", new String[]{chapterUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterId = cursor.getInt(cursor.getColumnIndex("chapter_id"));
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

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                "chapter_id=?", new String[]{chapterId + ""}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterUrl = cursor.getString(cursor.getColumnIndex("chapter_url"));
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
        String selection = null;
        String orderBy = null;

        if (url != null) {

            int chapterId = getChapterId(url);

            SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
            db.beginTransaction();

            // 限定条件加入bookUrl限定
            cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, new String[]{"book_url"},
                    "chapter_url=?", new String[]{url}, null, null, null);
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                bookUrl = cursor.getString(cursor.getColumnIndex("book_url"));
            }

            if (bookUrl != null) {
                if (flag > 0) {
                    // 获取下一章url
                    selection = "book_url = ? and chapter_id > ?";
                    orderBy = "chapter_id asc";
                } else {
                    // 获取上一章url
                    selection = "book_url = ? and chapter_id < ?";
                    orderBy = "chapter_id desc";
                }

                cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, new String[]{"chapter_url"},
                        selection, new String[]{bookUrl, chapterId + ""}, null, null, orderBy);
                if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                    chapterUrl = cursor.getString(cursor.getColumnIndex("chapter_url"));
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

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                "chapter_url=?", new String[]{chapterUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            String contents = cursor.getString(cursor.getColumnIndex("chapter_content"));
            if (contents == null || contents.isEmpty()) {
                chapter = null;
            } else {
                chapter.setContents(contents);
                chapter.setChapterUrl(chapterUrl);
                chapter.setChapterTitle(cursor.getString(cursor.getColumnIndex("chapter_title")));
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

        // 内容不重复
        contentValues.put("chapter_content", chapter.getContents());
        if (db.update(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS,
                contentValues,
                "chapter_url=?",
                new String[]{chapter.getChapterUrl()}) == -1) {
            Log.e(TAG, "Database insert id: " + chapter.getChapterUrl() + " error");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
