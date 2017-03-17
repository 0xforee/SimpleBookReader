package org.foree.bookreader.bean.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 * TODO:性能优化，批量操作不使用循环
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
            Book book = new Book(bookName, bookUrl, updateTime, modifiedTime, category, author, description, pageIndex, recentChapterId, bookCoverUrl, contentUrl);
            bookList.add(book);
        }

        if (cursor != null) cursor.close();
        return bookList;
    }

    public void insertChapters(List<Chapter> chapters) {
        ContentValues[] contentValues = new ContentValues[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE, chapters.get(i).getChapterTitle());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL, chapters.get(i).getChapterUrl());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID, chapters.get(i).getChapterId());
            contentValue.put(BReaderContract.Chapters.COLUMN_NAME_BOOK_URL, chapters.get(i).getBookUrl());

            contentValues[i] = contentValue;
        }

        mResolver.bulkInsert(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                contentValues
        );
    }

    private List<Chapter> getChapters(String bookUrl) {
        Log.d(TAG, "get chapterList from db, bookUrl = " + bookUrl);
        List<Chapter> chapterList = new ArrayList<>();
        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        // chapter_id sort by desc or asc
        Cursor cursor = mResolver.query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                null,
                selection,
                new String[]{bookUrl},
                orderBy
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE));
                String url = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
                int chapter_id = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
                Chapter chapter = new Chapter(title, url, bookUrl, chapter_id, offline);
                chapterList.add(chapter);
            }
            cursor.close();
        }

        return chapterList;
    }

    public void updateModifiedTime(String bookUrl, String modifiedTime){
        Log.d(TAG, "update book " + bookUrl + " Time " + modifiedTime);

        ContentValues contentValues = new ContentValues();
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME, modifiedTime);

        mResolver.update(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues,
                selection,
                new String[]{bookUrl}
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

    public Book getBook(String bookUrl) {
        Log.d(TAG, "get book info from db, bookUrl = " + bookUrl);

        Book book = new Book();
        Cursor cursor;
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";
        cursor = mResolver.query(
                BReaderProvider.CONTENT_URI_BOOKS,
                null,
                selection,
                new String[]{bookUrl},
                null
        );
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            book.setBookName(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_NAME)));
            book.setUpdateTime(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME)));
            book.setModifiedTime(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME)));
            book.setBookUrl(bookUrl);
            book.setCategory(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CATEGORY)));
            book.setAuthor(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_AUTHOR)));
            book.setDescription(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_DESCRIPTION)));
            book.setRecentChapterUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL)));
            book.setPageIndex(cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX)));
            book.setBookCoverUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_COVER_URL)));
            cursor.close();
        }

        book.setChapters(getChapters(bookUrl));

        return book;
    }

    public void addBook(Book book) {
        ContentValues contentValues = new ContentValues();

        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_URL, book.getBookUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_BOOK_NAME, book.getBookName());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME, book.getUpdateTime());
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
        insertChapters(book.getChapters());
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

    public void updateBookState(String bookUrl, String chapterUrl, int pageIndex) {
        ContentValues contentValues = new ContentValues();

        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";

        contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL, chapterUrl);
        contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, pageIndex);

        mResolver.update(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues,
                selection,
                new String[]{bookUrl}
        );
    }

    /**
     * 根据chapterUrl获取chapterId
     *
     * @param chapterUrl 章节地址
     * @return chapterId
     */
    public int getChapterId(String chapterUrl) {
        int chapterId = 0;

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

        Cursor cursor = mResolver.query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID},
                selection,
                new String[]{chapterUrl},
                null
                );
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterId = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
            cursor.close();
        }

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

        String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + "=?";

        cursor = mResolver.query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL},
                selection,
                new String[]{chapterId + ""},
                null
        );
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            chapterUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
            cursor.close();

        }
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

            String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

            // 限定条件加入bookUrl限定
            cursor = mResolver.query(
                    BReaderProvider.CONTENT_URI_CHAPTERS,
                    new String[]{BReaderContract.Chapters.COLUMN_NAME_BOOK_URL},
                    selection,
                    new String[]{url},
                    null
            );
            if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
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

                cursor = mResolver.query(
                        BReaderProvider.CONTENT_URI_CHAPTERS,
                        new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL},
                        selection,
                        new String[]{bookUrl, chapterId + ""},
                        orderBy
                );

                if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                    chapterUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                } else {
                    // 没有上一章或者没有下一章
                    chapterUrl = null;
                }
            }

            if (cursor != null) cursor.close();
        }

        return chapterUrl;
    }

    /**
     * TODO: 根据指定url和偏移量获取目标url
     *
     * @param flag 偏移量，-1=上一章，1=后一章
     * @param url  指定url
     * @return 有则返回目标url，没有返回null
     */
    public Map<String, Boolean> getChapterUrlLimit(int flag, String url, int limit) {

        Cursor cursor;
        String chapterUrl = null;
        String bookUrl = null;
        String orderBy = null;
        Map<String, Boolean> mCachedMap = new HashMap<>();

        if (url != null) {

            int chapterId = getChapterId(url);

            String selection = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + "=?";

            // 限定条件加入bookUrl限定
            cursor = mResolver.query(
                    BReaderProvider.CONTENT_URI_CHAPTERS,
                    new String[]{BReaderContract.Chapters.COLUMN_NAME_BOOK_URL},
                    selection,
                    new String[]{url},
                    null
            );
            if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
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

                if(limit>0)
                    orderBy = orderBy + " limit " + limit;

                cursor = mResolver.query(
                        BReaderProvider.CONTENT_URI_CHAPTERS,
                        new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL, BReaderContract.Chapters.COLUMN_NAME_CACHED},
                        selection,
                        new String[]{bookUrl, chapterId + ""},
                        orderBy
                );

                if( cursor != null && cursor.getCount()!=0) {
                    while(cursor.moveToNext()){
                        chapterUrl = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                        boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
                        mCachedMap.put(chapterUrl,offline);
                    }
                    cursor.close();
                }
            }

        }

        return mCachedMap;
    }
}
