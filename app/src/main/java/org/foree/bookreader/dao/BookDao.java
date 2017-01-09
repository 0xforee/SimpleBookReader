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

    public BookDao(Context context){
        bookSQLiteOpenHelper = new BookSQLiteOpenHelper(context);
    }

    public List<Book> findAllBookList(){
        Cursor cursor;
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null,
                null, null, null, null, null);
        while(cursor.moveToNext()){
            String bookName = cursor.getString(cursor.getColumnIndex("book_name"));
            String bookUrl = cursor.getString(cursor.getColumnIndex("book_url"));
            String updateTime = cursor.getString(cursor.getColumnIndex("update_time"));
            String category = cursor.getString(cursor.getColumnIndex("category"));
            String author = cursor.getString(cursor.getColumnIndex("author"));
            Book book = new Book(bookName, bookUrl, updateTime, category, author);
            bookList.add(book);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return bookList;
    }

    public void addBookInfo(Book book){
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put("book_url", book.getUrl());
        contentValues.put("book_name", book.getBookName());
        contentValues.put("update_time", book.getUpdateTime());
        contentValues.put("category", book.getCategory());
        contentValues.put("author", book.getAuthor());
        if (db.insertWithOnConflict(BookSQLiteOpenHelper.DB_TABLE_BOOK_LIST, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            Log.e(TAG, "Database insert id: " + book.getUrl() + " error");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void insertChapterList(List<Chapter> chapterList){
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert chapterList.size= " + chapterList.size() + " to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while(chapterList.size()>(1000*tmp)){
                insertInternal(chapterList.subList(1000*(tmp-1),1000*tmp));
                tmp++;
            }
            insertInternal(chapterList.subList(1000*(tmp-1), chapterList.size()));
        }
    }

    private void insertInternal(List<Chapter> subItemList){
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (Chapter chapter : subItemList) {
            // 内容不重复
            Cursor cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null,
                    "url=?", new String[]{chapter.getUrl()}, null, null, null);
            if (cursor.getCount() == 0) {
                contentValues.put("title", chapter.getTitle());
                contentValues.put("host_url", chapter.getHostUrl());
                contentValues.put("url", chapter.getUrl());
                if (db.insert(BookSQLiteOpenHelper.DB_TABLE_CHAPTERS, null, contentValues) == -1) {
                    Log.e(TAG, "Database insert url: " + chapter.getUrl() + " error");
                }
            }
            cursor.close();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public List<Chapter> findChapterByHostUrl(String hostUrl){
        Log.d(TAG, "get chapterList from db, hostUrl = " + hostUrl);
        Cursor cursor;
        List<org.foree.bookreader.book.Chapter> chapterList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_WEBSITES, null,
                "host_url=?", new String[]{hostUrl}, null, null, null);
        while(cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String host_url = cursor.getString(cursor.getColumnIndex("host_url"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            Chapter chapter = new Chapter(title, url, host_url);
            chapterList.add(chapter);

        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return chapterList;
    }
}
