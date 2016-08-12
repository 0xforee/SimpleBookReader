package org.foree.zetianji.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by foree on 2016/8/6.
 * 数据库创建升级的帮助类
 */
public class NovelSQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String TAG = NovelSQLiteOpenHelper.class.getSimpleName();
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "zetianji.db";
    public static final String DB_TABLE_WEBSITES = "websites";
    public static final String DB_TABLE_CHAPTERS = "chapters";

    public NovelSQLiteOpenHelper(Context context) {
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
                "title varchar(255), " +
                "url varchar(255), " +
                ")"
        );
    }

}
