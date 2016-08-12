package org.foree.zetianji.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.zetianji.helper.WebSiteInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 * TODO:性能优化，批量操作不使用循环
 */
public class NovelDao {
    private static final String TAG = NovelDao.class.getSimpleName();
    private NovelSQLiteOpenHelper novelSQLiteOpenHelper;

    public NovelDao(Context context){
        novelSQLiteOpenHelper = new NovelSQLiteOpenHelper(context);
    }


    public void insertWebSite(WebSiteInfo webSiteInfo){
        SQLiteDatabase db = novelSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        Cursor cursor = db.query(NovelSQLiteOpenHelper.DB_TABLE_WEBSITES, new String[]{"host_url"},
                "host_url=?", new String[]{webSiteInfo.getHost_url()}, null, null, null);
        if (cursor.getCount() == 0) {
            // 内容不重复
            contentValues.put("name", webSiteInfo.getName());
            contentValues.put("index_page", webSiteInfo.getIndex_page());
            contentValues.put("host_url", webSiteInfo.getHost_url());
            contentValues.put("web_char", webSiteInfo.getWeb_char());
            if (db.insert(NovelSQLiteOpenHelper.DB_TABLE_WEBSITES, null, contentValues) == -1) {
                Log.e(TAG, "Database insert id: " + webSiteInfo.getHost_url() + " error");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * 根据feedId, unread状态来查询文章
     * @return 符合要求的rssItemList
     */
    public List<WebSiteInfo> findAll(){
        Log.d(TAG, "get website from db");
        Cursor cursor;
        List<WebSiteInfo> webSiteInfoList = new ArrayList<>();
        SQLiteDatabase db = novelSQLiteOpenHelper.getReadableDatabase();

        cursor = db.query(NovelSQLiteOpenHelper.DB_TABLE_WEBSITES, null,
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
        db.close();
        return webSiteInfoList;
    }

    public WebSiteInfo find(long id){
        Log.d(TAG, "get website from db, id = " + id);
        Cursor cursor;
        SQLiteDatabase db = novelSQLiteOpenHelper.getReadableDatabase();

        cursor = db.query(NovelSQLiteOpenHelper.DB_TABLE_WEBSITES, null,
                "id=?", new String[]{id +""}, null, null, null);
        cursor.moveToNext();
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String host_url = cursor.getString(cursor.getColumnIndex("host_url"));
        String index_page = cursor.getString(cursor.getColumnIndex("index_page"));
        String webSiteCharSet = cursor.getString(cursor.getColumnIndex("web_char"));
        WebSiteInfo webSiteInfo = new WebSiteInfo(name, host_url, index_page, webSiteCharSet);

        cursor.close();
        db.close();
        return webSiteInfo;
    }
//
//    /**
//     * 清空表
//     * @param table 表名称
//     */
//    public void cleanTable(String table){
//        SQLiteDatabase db = novelSQLiteOpenHelper.getWritableDatabase();
//        db.delete(table, null, null);
//        db.close();
//    }
//
//    /**
//     * 更新rssItem的unread字段
//     */
//    public int update(String id, boolean newValue){
//
//        SQLiteDatabase db = novelSQLiteOpenHelper.getReadableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("unread", newValue);
//
//        int result = db.update(NovelSQLiteOpenHelper.DB_TABLE_ENTRIES, contentValues, "id=?", new String[]{id});
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
//        SQLiteDatabase db = novelSQLiteOpenHelper.getReadableDatabase();
//        int result = db.delete(NovelSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{id});
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
//        SQLiteDatabase db = novelSQLiteOpenHelper.getReadableDatabase();
//        for(RssItem item: itemList) {
//            result = db.delete(NovelSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{item.getEntryId()});
//        }
//        db.close();
//        return result;
//
//    }
}
