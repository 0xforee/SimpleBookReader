package org.foree.bookreader.readpage;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.parser.WebParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foree
 * @date 2018/7/13
 * @description 章节列表的异步加载器
 * 在线，从网络直接加载
 * 非在线，从数据库加载
 */
public class CustomChapterListLoader extends AsyncTaskLoader<List<Chapter>> {
    private static final String TAG = CustomChapterListLoader.class.getSimpleName();
    private boolean mOnline;
    private String mContentsUrl;
    private String mBookUrl;
    private Context mContext;

    public CustomChapterListLoader(Context context, boolean online, String bookUrl, String contentsUrl) {
        super(context);
        this.mOnline = online;
        this.mBookUrl = bookUrl;
        this.mContentsUrl = contentsUrl;
        mContext = context;
    }

    @Override
    public List<Chapter> loadInBackground() {
        Log.d(TAG, "[foree] loadInBackground: mOnline = " + mOnline);

        if (mOnline) {
            return WebParser.getInstance().getContents(mBookUrl, mContentsUrl);
        } else {
            List<Chapter> chapterList = null;
            Uri baseUri = BReaderProvider.CONTENT_URI_CHAPTERS;
            String[] projection = new String[]{
                    BReaderContract.Chapters._ID,
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL,
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE,
                    BReaderContract.Chapters.COLUMN_NAME_CACHED,
                    BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID
            };
            String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
            String[] selectionArgs = new String[]{mBookUrl};
            String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

            Cursor cursor = getContext().getContentResolver().query(baseUri, projection, selection,
                    selectionArgs, orderBy);
            if (cursor != null) {
                try {
                    chapterList = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        Chapter chapter = new Chapter();
                        chapter.setBookUrl(mBookUrl);
                        chapter.setChapterUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL)));
                        chapter.setChapterTitle(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));
                        chapter.setOffline(cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) != 0);
                        chapterList.add(chapter);
                    }
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            return chapterList;
        }

    }
}
