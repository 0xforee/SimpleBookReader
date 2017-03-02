package org.foree.bookreader.data.dao;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by foree on 17-3-2.
 */

public class BReaderProvider extends ContentProvider {

    private BookDataBaseHelper mOpenHelper;

    private SQLiteDatabase db;

    private static final String AUTHOR = "org.foree.bookreader.provider";

    // create a uriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHOR, BReaderContract.Books.TABLE_NAME, 1);
        sUriMatcher.addURI(AUTHOR, BReaderContract.Chapters.TABLE_NAME, 2);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BookDataBaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = mOpenHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {

        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }

    /**
     * Created by foree on 2016/8/6.
     * 数据库创建升级的帮助类
     */
    protected static final class BookDataBaseHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "bookReader.db";

        BookDataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onUpgrade(db, 0, DB_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            for (int version = oldVersion + 1; version <= newVersion; version++) {
                onUpgradeTo(db, version);
            }
        }

        private void onUpgradeTo(SQLiteDatabase db, int version) {
            switch (version) {
                case 1:
                    createEntriesTable(db);
                    break;
                default:
                    throw new IllegalStateException("Don't known to upgrade to " + version);
            }
        }

        private void createEntriesTable(SQLiteDatabase db) {
            db.execSQL("create table " + BReaderContract.Books.TABLE_NAME + "(" +
                    BReaderContract.Books._ID + " integer primary key," +
                    BReaderContract.Books.COLUMN_NAME_BOOK_URL + " varchar unique," +
                    BReaderContract.Books.COLUMN_NAME_CONTENT_URL + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_BOOK_NAME + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_COVER_URL + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_UPDATE_TIME + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_PAGE_INDEX + " integer," +
                    BReaderContract.Books.COLUMN_NAME_RECENT_ID + " integer," +
                    BReaderContract.Books.COLUMN_NAME_CATEGORY + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_DESCRIPTION + " varchar," +
                    BReaderContract.Books.COLUMN_NAME_AUTHOR + " varchar" +
                    ")"
            );

            //章节有对应的book_url, chapter_url的md5为章节内容的缓存文件名称
            // chapter_id 用于章节排序和获取前后章节
            db.execSQL("create table " + BReaderContract.Chapters.TABLE_NAME + "(" +
                    BReaderContract.Chapters._ID + " integer primary key," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + " varchar unique," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " integer unique," +
                    BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CACHED + " integer" +
                    ")"
            );
        }

    }

}
