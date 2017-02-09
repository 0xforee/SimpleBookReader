package org.foree.bookreader.data.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by foree on 2016/8/6.
 * 数据库创建升级的帮助类
 */
public class BookSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = BookSQLiteOpenHelper.class.getSimpleName();
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "bookreader.db";
    public static final String DB_TABLE_CHAPTER_LIST = "chapter_list";
    public static final String DB_TABLE_BOOK_LIST = "book_list";

    public BookSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
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
        //章节有对应的book_url, chapter_url的md5为章节内容的缓存文件名称
        db.execSQL("create table chapter_list(" +
                "id integer primary key autoincrement," +
                "chapter_url varchar(255) unique," +
                "chapter_id integer unique," +
                "book_url varchar(255)," +
                "chapter_title varchar(255)," +
                "offline integer," +
                "read integer" +
                ")"
        );

        db.execSQL("create table book_list(" +
                "id integer primary key autoincrement," +
                "book_url varchar(255) unique," +
                "book_name varchar(255)," +
                "update_time varchar(255)," +
                "recent_chapter_id integer(255)," +
                "category varchar(255)," +
                "description varchar(255)," +
                "author varchar(255)" +
                ")"
        );
    }

}
