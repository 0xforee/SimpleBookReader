package org.foree.bookreader.bean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 17-3-25.
 * 用于存放书籍打开之后的状态，将对书籍的所有处理集中到这个类中
 * 在打开书籍时，restoreBookRecord
 * 在关闭书籍时，saveBookRecord
 */

public class BookRecord {
    private final static String TAG = BookRecord.class.getSimpleName();

    private Book mBook;
    private List<Chapter> mChapters;

    private Context mContext;

    // 存储mChapters的url对应的index，加快索引速度
    private Map<String, Integer> mIndexMap;

    // 初始化是否完成，指第一次页码切换是否完成
    private boolean completed = false;

    public BookRecord(Context context) {
        mBook = new Book();
        mChapters = new ArrayList<>();
        mIndexMap = new HashMap<>();
        mContext = context;
    }

    /**
     * 根据bookUrl打开一本书状态
     *
     * @param bookUrl book的唯一标示
     */
    public void restoreBookRecord(String bookUrl) {

        mBook = bookInit(bookUrl);
        mChapters = chapterInit(bookUrl);

        // 打开书的时候就更新书籍的修改时间
        mBook.setModifiedTime(DateUtils.getCurrentTime());

    }

    /**
     * 保存当前打开书本的状态
     * 1. 当前阅读章节（切换章节的时候就设置好）
     * 2. 当前章节页码
     * 3. 当前修改时间（打开时就设置好）
     */
    public void saveBookRecord(int pageIndex) {
        mBook.setPageIndex(pageIndex);
        completed = false;

        saveToDatabase(mBook);
    }

    public int getPageIndex() {
        return mBook.getPageIndex();
    }

    public String getCurrentUrl() {
        return mBook.getRecentChapterUrl();
    }

    public void switchChapter(String newUrl) {
        mBook.setRecentChapterUrl(newUrl);
    }

    public int getCurPosition() {
        return getIndexFromUrl(getCurrentUrl());
    }

    public boolean isInitCompleted() {
        if (completed) {
            return true;
        } else {
            completed = true;
            return false;
        }
    }

    public String getUrl(int position) {
        return mChapters.get(position).getChapterUrl();
    }

    public void setCached(String url) {
        mChapters.get(mIndexMap.get(url)).setOffline(true);
    }

    public boolean isChapterCached(int index) {
        return mChapters.get(index).isOffline();
    }

    public int getChaptersSize() {
        return mChapters.size();
    }

    public int getIndexFromUrl(String url) {
        if (mIndexMap.containsKey(url))
            return mIndexMap.get(url);
        else
            return -1;
    }

    /**
     * 获取指定url对应偏移量的章节url
     *
     * @param flag 偏移量，-1表示前一章，1表示后一章
     * @param url  指定url
     * @return 有则返回目标url，没有返回null
     */
    private String getUrlFromFlag(int flag, String url) {
        int index = getIndexFromUrl(url);
        if (index != -1) {
            int targetIndex = index + flag;
            if (targetIndex > 0 && targetIndex < mChapters.size()) {
                return mChapters.get(targetIndex).getChapterUrl();
            }
        }
        return null;
    }

    public String getUrlFromFlag(int flag) {
        return getUrlFromFlag(flag, getCurrentUrl());
    }

    private List<Chapter> chapterInit(String bookUrl) {
        Log.d(TAG, "get chapterList from db, bookUrl = " + bookUrl);

        List<Chapter> chapterList = new ArrayList<>();
        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        // chapter_id sort by desc or asc
        Cursor cursor = mContext.getContentResolver().query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                null,
                selection,
                new String[]{bookUrl},
                orderBy
        );

        if (cursor != null) {
            int i = 0;
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE));
                String url = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
                int chapter_id = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
                Chapter chapter = new Chapter(title, url, bookUrl, chapter_id, offline);
                chapterList.add(chapter);

                // 使用hashMap加快索引位置
                mIndexMap.put(url, i++);
            }
            cursor.close();
        }

        return chapterList;

    }

    private Book bookInit(String bookUrl) {
        Log.d(TAG, "get book info from db, bookUrl = " + bookUrl);

        Book book = new Book();
        Cursor cursor;
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";
        cursor = mContext.getContentResolver().query(
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
        }
        if (cursor != null) cursor.close();

        return book;


    }

    private void saveToDatabase(Book book) {
        ContentValues contentValues = new ContentValues();
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";
        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME, book.getModifiedTime());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL, book.getRecentChapterUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, book.getPageIndex());

        mContext.getContentResolver().update(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues,
                selection,
                new String[]{book.getBookUrl()}

        );
    }
}
