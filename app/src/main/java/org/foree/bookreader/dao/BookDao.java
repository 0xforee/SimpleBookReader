package org.foree.bookreader.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;

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

    public List<Book> findAllBookList() {
        Cursor cursor;
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null,
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            String bookName = cursor.getString(cursor.getColumnIndex("book_name"));
            String bookUrl = cursor.getString(cursor.getColumnIndex("book_url"));
            String updateTime = cursor.getString(cursor.getColumnIndex("update_time"));
            String category = cursor.getString(cursor.getColumnIndex("category"));
            String author = cursor.getString(cursor.getColumnIndex("author"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            int recentChapterId = cursor.getInt(cursor.getColumnIndex("recent_chapter_id"));
            Book book = new Book(bookName, bookUrl, updateTime, category, author, description, recentChapterId);
            bookList.add(book);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return bookList;
    }

    public void addBookInfo(Book book) {
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
        if (db.insertWithOnConflict(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            Log.e(TAG, "Database insert id: " + book.getBookUrl() + " error");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        // insert chapter list
        insertChapterList(book.getChapterList());
    }

    public void removeBookInfo(String book_url) {
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null,
                "book_url=?", new String[]{book_url}, null, null, null);
        if (cursor.getCount() != 0) {
            if (db.delete(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, "book_url=?", new String[]{book_url}) == -1) {
                Log.e(TAG, "delete book_url:" + book_url + " error");
            }
        }

        cursor.close();
        db.close();
    }

    public void insertChapterList(List<Chapter> chapterList) {
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert chapterList.size= " + chapterList.size() + " to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while (chapterList.size() > (1000 * tmp)) {
                insertInternal(chapterList.subList(1000 * (tmp - 1), 1000 * tmp));
                tmp++;
            }
            insertInternal(chapterList.subList(1000 * (tmp - 1), chapterList.size()));
        }
    }

    private void insertInternal(List<Chapter> subItemList) {
        synchronized (this) {
            SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            for (Chapter chapter : subItemList) {
                contentValues.put("chapter_title", chapter.getChapterTitle());
                contentValues.put("chapter_url", chapter.getChapterUrl());
                contentValues.put("chapter_id", chapter.getChapterId());
                contentValues.put("book_url", chapter.getBookUrl());
                if (db.insertWithOnConflict(BookSQLiteOpenHelper.DB_TABLE_CHAPTER_LIST, null,
                        contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                    Log.e(TAG, "Database insert chapter_url: " + chapter.getChapterUrl() + " error");
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public List<Chapter> findChapterListByBookUrl(String bookUrl) {
        Log.d(TAG, "get chapterList from db, bookUrl = " + bookUrl);
        Cursor cursor;
        List<Chapter> chapterList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        // chapter_id sort by desc or asc
        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTER_LIST, null,
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

    public Book findBookInfoByUrl(String bookUrl) {
        Log.d(TAG, "get book info from db, bookUrl = " + bookUrl);
        Book book = new Book();
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null,
                "book_url=?", new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            book.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            book.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
            book.setBookUrl(bookUrl);
            book.setCategory(cursor.getString(cursor.getColumnIndex("category")));
            book.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            book.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            book.setRecentChapterId(cursor.getInt(cursor.getColumnIndex("recent_chapter_id")));
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        book.setChapterList(findChapterListByBookUrl(bookUrl));

        return book;
    }

    public void updateRecentChapterId(String bookUrl, int recentChapterId) {
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null,
                "book_url=?", new String[]{bookUrl}, null, null, null);
        if (cursor.getCount() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("recent_chapter_id", recentChapterId);
            if (db.update(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, contentValues,
                    "book_url=?", new String[]{bookUrl}) == -1) {
                Log.e(TAG, "Database insert book_url: " + bookUrl + " error");
            }
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

    }

    public String findChapterUrlById(int chapterId) {
        Cursor cursor;
        String chapterUrl = null;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTER_LIST, null,
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

    private int findIdByChapterUrl(String chapterUrl) {
        Cursor cursor;
        int ChapterId = 0;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTER_LIST, null,
                "chapter_url=?", new String[]{chapterUrl}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            ChapterId = cursor.getInt(cursor.getColumnIndex("chapter_id"));
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return ChapterId;
    }

    /**
     * TODO: 根据指定url和偏移量获取目标url
     * @param flag 偏移量
     * @param url 指定url
     * @return 目标url
     */
    public String getNextChapterUrlByUrl(int flag, String url) {

        int chapterId = findIdByChapterUrl(url);

        Cursor cursor;
        String chapterUrl;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        String selection = null;
        String orderBy = null;
        if (flag > 0) {
            // 获取下一章url
            selection = "chapter_id > ?";
            orderBy = "chapter_id asc";
        } else {
            // 获取上一章url
            selection = "chapter_id < ?";
            orderBy = "chapter_id desc";
        }

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTER_LIST, new String[]{"chapter_url"},
                selection, new String[]{chapterId + ""}, null, null, orderBy);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterUrl = cursor.getString(cursor.getColumnIndex("chapter_url"));
        } else {
            // 没有上一章或者没有下一章
            chapterUrl = "";
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return chapterUrl;
    }
}
