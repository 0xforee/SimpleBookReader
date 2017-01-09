package org.foree.bookreader.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by foree on 2016/8/6.
 * 数据库创建升级的帮助类
 */
public class BookSQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String TAG = BookSQLiteOpenHelper.class.getSimpleName();
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "bookreader.db";
    public static final String DB_TABLE_WEBSITES = "websites";
    public static final String DB_TABLE_CHAPTERS = "chapters";
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

        for ( int version = oldVersion +1; version <= newVersion; version++){
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
        db.execSQL("create table websites(" +
                "id integer primary key autoincrement," +
                "name varchar(255), " +
                "host_url varchar(255), " +
                "index_page varchar(255), " +
                "web_char varchar(255)" +
                ")"
        );

        db.execSQL("create table chapters(" +
                "url varchar(255) primary key," +
                "host_url varchar(255)," +
                "title varchar(255)," +
                "content varchar," +
                "offline integer," +
                "read integer" +
                ")"
        );

        db.execSQL("create table book_list(" +
                "book_url varchar(255) primary key," +
                "book_name varchar(255)" +
                ")"
        );
    }

}
