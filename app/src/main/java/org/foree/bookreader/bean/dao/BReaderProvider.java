package org.foree.bookreader.bean.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.foree.bookreader.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by foree on 17-3-2.
 */

public class BReaderProvider extends ContentProvider {
    private static final String TAG = BReaderProvider.class.getSimpleName();

    private BookDataBaseHelper mOpenHelper;

    private SQLiteDatabase db;

    public static final String AUTHORITY = "org.foree.bookreader.provider";

    public static final Uri CONTENT_URI_BOOKS = Uri.parse("content://" + AUTHORITY + "/" + BReaderContract.Books.TABLE_NAME);
    public static final Uri CONTENT_URI_CHAPTERS = Uri.parse("content://" + AUTHORITY + "/" + BReaderContract.Chapters.TABLE_NAME);

    // create a uriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // uriMatcher stuff
    private static final int CODE_BOOKS = 1;
    private static final int CODE_CHAPTERS = 2;

    static {
        sUriMatcher.addURI(AUTHORITY, BReaderContract.Books.TABLE_NAME, CODE_BOOKS);
        sUriMatcher.addURI(AUTHORITY, BReaderContract.Chapters.TABLE_NAME, CODE_CHAPTERS);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BookDataBaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = mOpenHelper.getReadableDatabase();
        return db.query(matchTable(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        db = mOpenHelper.getWritableDatabase();
        long rowId = db.insertWithOnConflict(matchTable(uri), null, values, SQLiteDatabase.CONFLICT_IGNORE);
        notifyChange(uri);
        return ContentUris.withAppendedId(uri, rowId);
    }

    private void notifyChange(@NonNull Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
        int rowId = db.delete(matchTable(uri), selection, selectionArgs);
        //notifyChange(uri);
        return rowId;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
        int rowId = db.update(matchTable(uri), values, selection, selectionArgs);
        notifyChange(uri);
        return rowId;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int result = bulkInsertInternal(uri, values);
        notifyChange(uri);
        return result;
    }

    private String matchTable(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_BOOKS:
                return BReaderContract.Books.TABLE_NAME;
            case CODE_CHAPTERS:
                return BReaderContract.Chapters.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    // 使用事务加快大量数据插入速度
    private int bulkInsertInternal(Uri uri, ContentValues[] contentValues) {
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert uri = " + uri.toString() + " " + contentValues.length + " items to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while (contentValues.length > (1000 * tmp)) {
                insertWithTransaction(uri, Arrays.copyOfRange(contentValues, 1000 * (tmp - 1), 1000 * tmp));
                tmp++;
            }
            insertWithTransaction(uri, Arrays.copyOfRange(contentValues, 1000 * (tmp - 1), contentValues.length));
        }
        return contentValues.length;
    }

    private void insertWithTransaction(Uri uri, ContentValues[] contentValues) {
        db = mOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (ContentValues value : contentValues) {
                // 内容不重复
                insert(uri, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }

    }

    /**
     * Created by foree on 2016/8/6.
     * 数据库创建升级的帮助类
     */
    protected static final class BookDataBaseHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 3;
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
                case 2:
                    addModifiedTimeColumn(db);
                    break;
                case 3:
                    modifyChapterIdType(db);
                    break;
                default:
                    throw new IllegalStateException("Don't known to upgrade to " + version);
            }
        }

        /**
         * 去掉chapter表中chapter_id的unique属性
         * @param db
         */
        private void modifyChapterIdType(SQLiteDatabase db){
            db.execSQL("alter table " + BReaderContract.Chapters.TABLE_NAME +
                    " rename to temp_table_name");

            db.execSQL("create table " + BReaderContract.Chapters.TABLE_NAME + "(" +
                    BReaderContract.Chapters._ID + " integer primary key," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL + " varchar unique," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " integer," +
                    BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_CONTENT + " varchar," +
                    BReaderContract.Chapters.COLUMN_NAME_CACHED + " integer" +
                    ")"
            );

            db.execSQL("insert into " + BReaderContract.Chapters.TABLE_NAME + " select * from temp_table_name");

            db.execSQL("drop table temp_table_name");

        }

        private void addModifiedTimeColumn(SQLiteDatabase db) {
            db.execSQL("alter table " + BReaderContract.Books.TABLE_NAME + " add column " +
                    BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME + " varchar");

            // 设置点击时间为当前时间
            System.out.println();// new Date()为获取当前系统时间
            ContentValues contentValue = new ContentValues();
            contentValue.put(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME, DateUtils.getCurrentTime());
            db.update(BReaderContract.Books.TABLE_NAME, contentValue, null, null);
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
                    BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL + " varchar," +
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
