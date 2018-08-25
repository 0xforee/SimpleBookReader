package org.foree.bookreader.bean.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 */
public class BookDao {
    private static final String TAG = BookDao.class.getSimpleName();
    private ContentResolver mResolver;

    public BookDao(Context context) {
        mResolver = context.getContentResolver();
    }

    /**
     * 获取本地所有书
     *
     * @return bookList
     */
    public List<Book> getAllBooks() {
        Cursor cursor;
        List<Book> bookList = new ArrayList<>();
        cursor = mResolver.query(BReaderProvider.CONTENT_URI_BOOKS, null, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String bookName = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_NAME));
            String bookUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_URL));
            String updateTime = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME));
            String modifiedTime = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME));
            String category = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CATEGORY));
            String author = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_AUTHOR));
            String description = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_DESCRIPTION));
            String recentChapterId = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL));
            int pageIndex = cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX));
            String bookCoverUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_COVER_URL));
            String contentUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CONTENT_URL));
            Book book = new Book(bookName, bookUrl, DateUtils.parseNormal(updateTime),
                    DateUtils.parseNormal(modifiedTime), category, author, description, pageIndex, recentChapterId, bookCoverUrl, contentUrl);
            bookList.add(book);
        }

        if (cursor != null) cursor.close();
        return bookList;
    }

    public void insertChapters(String bookUrl, List<Chapter> chapters) {
        Log.d(TAG, "[foree] insertChapters: bookUrl = " + bookUrl);

        ContentValues[] contentValues = new ContentValues[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE, chapters.get(i).getChapterTitle());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL, chapters.get(i).getChapterUrl());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID, chapters.get(i).getChapterIndex());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_BOOK_URL, bookUrl);

            contentValues[i] = contentValue;
        }

        mResolver.bulkInsert(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                contentValues
        );
    }

    public void updateBookTime(String bookUrl, String updateTime) {
        Log.d(TAG, "update book " + bookUrl + " Time " + updateTime);

        ContentValues contentValues = new ContentValues();
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME, updateTime);

        mResolver.update(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues,
                selection,
                new String[]{bookUrl}
        );
    }

    public void addBook(Book book) {
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_URL, book.getBookUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_NAME, book.getBookName());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME, DateUtils.formatDateToString(book.getUpdateTime()));
        contentValues.put(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME, DateUtils.getCurrentTime());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_CATEGORY, book.getCategory());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_AUTHOR, book.getAuthor());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_DESCRIPTION, book.getDescription());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL, book.getChapters().get(0).getChapterUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_COVER_URL, book.getBookCoverUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_CONTENT_URL, book.getContentUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, 0);

        mResolver.insert(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues
        );

        // insert chapters
        insertChapters(book.getBookUrl(), book.getChapters());
    }

    public void removeBook(String book_url) {
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        // delete book info
        mResolver.delete(
                BReaderProvider.CONTENT_URI_BOOKS,
                selection,
                new String[]{book_url}
        );

        // delete chapters
        mResolver.delete(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                selection,
                new String[]{book_url}
        );

    }

}
