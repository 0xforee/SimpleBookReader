package org.foree.bookreader.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.helper.WebSiteInfo;

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

    public void insertWebSite(WebSiteInfo webSiteInfo){
        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        Cursor cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_WEBSITES, new String[]{"host_url"},
                "host_url=?", new String[]{webSiteInfo.getHost_url()}, null, null, null);
        if (cursor.getCount() == 0) {
            // 内容不重复
            contentValues.put("name", webSiteInfo.getName());
            contentValues.put("index_page", webSiteInfo.getIndex_page());
            contentValues.put("host_url", webSiteInfo.getHost_url());
            contentValues.put("web_char", webSiteInfo.getWeb_char());
            if (db.insert(BookSQLiteOpenHelper.DB_TABLE_WEBSITES, null, contentValues) == -1) {
                Log.e(TAG, "Database insert id: " + webSiteInfo.getHost_url() + " error");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public List<WebSiteInfo> findAllWebSites(){
        Log.d(TAG, "get website from db");
        Cursor cursor;
        List<WebSiteInfo> webSiteInfoList = new ArrayList<>();
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_WEBSITES, null,
                    null, null, null, null, null);
        while(cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String host_url = cursor.getString(cursor.getColumnIndex("host_url"));
            String index_page = cursor.getString(cursor.getColumnIndex("index_page"));
            String webSiteCharSet = cursor.getString(cursor.getColumnIndex("web_char"));
            WebSiteInfo webSiteInfo = new WebSiteInfo(id, name, host_url, index_page, webSiteCharSet);
            webSiteInfoList.add(webSiteInfo);

        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return webSiteInfoList;
    }

    public WebSiteInfo findWebSiteById(long id){
        Log.d(TAG, "get website from db, id = " + id);
        Cursor cursor;
        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        cursor = db.query(BookSQLiteOpenHelper.DB_TABLE_WEBSITES, null,
                "id=?", new String[]{id +""}, null, null, null);
        cursor.moveToNext();
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String host_url = cursor.getString(cursor.getColumnIndex("host_url"));
        String index_page = cursor.getString(cursor.getColumnIndex("index_page"));
        String webSiteCharSet = cursor.getString(cursor.getColumnIndex("web_char"));
        WebSiteInfo webSiteInfo = new WebSiteInfo(name, host_url, index_page, webSiteCharSet);

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return webSiteInfo;
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
//
//    /**
//     * 清空表
//     * @param table 表名称
//     */
//    public void cleanTable(String table){
//        SQLiteDatabase db = bookSQLiteOpenHelper.getWritableDatabase();
//        db.delete(table, null, null);
//        db.close();
//    }
//
//    /**
//     * 更新rssItem的unread字段
//     */
//    public int update(String id, boolean newValue){
//
//        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("unread", newValue);
//
//        int result = db.update(BookSQLiteOpenHelper.DB_TABLE_ENTRIES, contentValues, "id=?", new String[]{id});
//
//        db.close();
//
//        return result;
//    }
//
//    /**
//     * 删除某一项
//     * @param id rssItem的标示
//     */
//    public int delete(String id){
//
//        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
//        int result = db.delete(BookSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{id});
//        db.close();
//        return result;
//    }
//
//    /**
//     * 批量删除
//     * @param itemList item列表
//     */
//    public int deleteSome(List<RssItem> itemList){
//        int result = 0;
//        SQLiteDatabase db = bookSQLiteOpenHelper.getReadableDatabase();
//        for(RssItem item: itemList) {
//            result = db.delete(BookSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{item.getEntryId()});
//        }
//        db.close();
//        return result;
//
//    }
}
