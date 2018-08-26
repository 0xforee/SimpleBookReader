package org.foree.bookreader.readpage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookLoadCompleteEvent;
import org.foree.bookreader.parser.WebParser;
import org.foree.bookreader.utils.DateUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 17-3-25.
 * 用于存放书籍打开之后的状态，区分从书架还是详情界面打开
 * 异步，在未加载好之前，使用loading状态
 * <p>
 * 在打开书籍时，restoreBookRecord
 * restore状态包括：book基本信息，章节列表，上次阅读章节和分页
 * <p>
 * 在切换章节，分页时，保存这两个状态
 * <p>
 * 在关闭书籍时，saveBookRecord
 * 保存上次阅读章节和分页
 */

public class BookRecord {
    private final static String TAG = BookRecord.class.getSimpleName();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private Book mBook;
    private List<Chapter> mChapters;

    private Context mContext;
    private Handler mHandler;

    /**
     * 存储mChapters的url对应的index，加快索引速度
     */
    private Map<String, Integer> mIndexMap = new HashMap<>();

    /**
     * 初始化是否完成，指第一次页码切换是否完成
     */
    private boolean completed = false;

    /**
     * 书籍源
     */
    private List<Source> mSources = new ArrayList<>();

    /**
     * magic split key
     */
    private final String SPLIT_KEY = GlobalConfig.MAGIC_SPLIT_KEY;

    /**
     * 是否从书籍详情页（在线）打开
     * 用户获取书籍状态只有两个入口：
     * 1.书架
     * 2.详情页
     */
    private boolean mOnline = false;
    private String mBookUrl;
    private BookDao mBookDao;
    private String mOldContentUrl;

    public BookRecord(Context context) {
        mContext = context.getApplicationContext();
        mBookDao = new BookDao(context);

        HandlerThread workThread = new HandlerThread("workThread", Process.THREAD_PRIORITY_BACKGROUND);
        workThread.start();
        mHandler = new Handler(workThread.getLooper());
    }

    /**
     * 根据bookUrl打开一本书状态，区分在线还是本地
     *
     * @param bookUrl book的唯一标示
     */
    public void restoreBookRecord(final String bookUrl, final boolean onLine) {
        mOnline = onLine;
        mBookUrl = bookUrl;

        mHandler.post(mRestoreBookRunnable);
    }

    /**
     * 恢复书籍状态
     */
    private Runnable mRestoreBookRunnable = new Runnable() {
        @Override
        public void run() {
            initBookInfo();
            initChapters(false);
            if (mOnline) {
                mBook.setRecentChapterUrl(mChapters.get(0).getChapterUrl());
            }

            // 打开书的时候就更新书籍的修改时间
            mBook.setModifiedTime(new Date());

            mOldContentUrl = mBook.getContentUrl();

            sendCompleteMessage();

        }
    };

    private void sendCompleteMessage() {
        // send complete message
        EventBus.getDefault().post(new BookLoadCompleteEvent(mBook != null && mChapters != null));
    }

    /**
     * 保存当前打开书本的状态
     * 1. 当前阅读章节（切换章节的时候就设置好）
     * 2. 当前章节页码
     * 3. 当前修改时间（打开时就设置好）
     * TODO:网络模式进入不做处理，后续根据是否加入书架这一行为来处理
     */
    public void saveBookRecord() {
        completed = false;

        if (mOnline) {
            return;
        }

        saveToDatabase();
    }

    public boolean isOnline() {
        return mOnline;
    }

    /**
     * 上次用户阅读的章节分页
     *
     * @return 书籍详情页总是返回0，书架页返回记录的数据
     */
    public int getPageIndex() {
        return mBook.getPageIndex();
    }

    public void switchPageIndex(int pageIndex) {
        if (mBook != null) {
            mBook.setPageIndex(pageIndex);
        }
    }

    public String getCurrentUrl() {
        return mBook.getRecentChapterUrl();
    }

    public void switchChapter(String newUrl) {
        mBook.setRecentChapterUrl(newUrl);
    }

    /**
     * 用于标定章节在list中的位置
     *
     * @return 获取当前章节所处的位置
     */
    public int getCurrentChapterPos() {
        return getChapterIndex(getCurrentUrl());
    }

    public boolean isInitCompleted() {
        if (completed) {
            return true;
        } else {
            completed = true;
            return false;
        }
    }

    public void reInit() {
        completed = false;
    }

    public String getChapterUrl(int position) {
        return mChapters.get(position).getChapterUrl();
    }

    public void setChapterCached(String url) {
        if (mIndexMap.get(url) != null) {
            if(DEBUG) {
                Log.d(TAG, "[foree] setChapterCached: url = " + url);
            }
            mChapters.get(mIndexMap.get(url)).setOffline(true);
        }
    }

    public Chapter getChapter(String url) {
        if (mIndexMap.get(url) != null) {
            return mChapters.get(mIndexMap.get(url));
        }
        return new Chapter("", "");
    }

    public boolean isChapterCached(int index) {
        return mChapters.get(index).isOffline();
    }

    public int getChaptersSize() {
        return mChapters.size();
    }

    public int getChapterIndex(String url) {
        if(DEBUG) {
            Log.d(TAG, "getChapterIndex() called with: url = [" + url + "]");
        }
        if (mIndexMap.containsKey(url)) {
            return mIndexMap.get(url);
        } else {
            return 0;
        }
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }

    public List<Source> getSources() {
        return mSources;
    }

    public void updateSources(List<Source> newData){
        if(newData != null && !newData.isEmpty()) {
            mSources.clear();
            mSources.addAll(newData);
        }
    }

    public void updateChapters(List<Chapter> chapters){
        if(mChapters == null){
            mChapters = chapters;
        }else{
            if(chapters != null){
                for (int i = chapters.size(); i < mChapters.size() - 1 ; i++) {
                    mChapters.remove(i);
                }

                for (int i = 0; i < chapters.size(); i++) {
                    if(i >= mChapters.size()){
                        mChapters.add(i, chapters.get(i));
                    }else{
                        mChapters.set(i, updateChapterInfo(mChapters.get(i), chapters.get(i)));
                    }
                }
            }
            initChapterIndexMap(mChapters);
        }


    }

    private Chapter updateChapterInfo(Chapter oldData, Chapter newData){
        oldData.setChapterUrl(newData.getChapterUrl());
        oldData.setChapterTitle(newData.getChapterTitle());

        return oldData;
    }


    public int getCurrentSourcePos(){
        return getSourceIndex(getContentsUrl());
    }

    private int getSourceIndex(String contentsUrl){
        if(mSources != null){
            for (int i = 0; i < mSources.size(); i++) {
                if(mSources.get(i).getSourceId().equals(contentsUrl)){
                    return i;
                }
            }
        }

        return 0;
    }

    public String getBookKey(){
        return mBook.getBookName() + SPLIT_KEY + mBook.getAuthor();
    }

    public String getBookUrl() {
        return mBook.getBookUrl();
    }

    public String getContentsUrl() {
        return mBook.getContentUrl();
    }

    /**
     * 获取指定chapter url对应偏移量的章节url
     *
     * @param offset 偏移量，-1表示前一章，1表示后一章
     * @param url    指定url
     * @return 有则返回目标url，没有返回null
     */
    private String getOffsetChapter(int offset, String url) {
        int index = getChapterIndex(url);
        if (index != -1) {
            int targetIndex = index + offset;
            if (targetIndex > 0 && targetIndex < mChapters.size()) {
                return mChapters.get(targetIndex).getChapterUrl();
            }
        }
        return null;
    }

    public String getCurrentOffsetChapter(int offset) {
        return getOffsetChapter(offset, getCurrentUrl());
    }

    private void initChapters(boolean force) {
        mChapters = mOnline || force ? initChapterListOnline() : initChapterListLocal();

        if (mChapters == null) {
            mChapters = new ArrayList<>();
        }

        // update chapters index
        initChapterIndexMap(mChapters);
    }

    private List<Chapter> initChapterListLocal() {
        Log.d(TAG, "get chapterList from db, mBookUrl = " + mBookUrl);

        List<Chapter> chapterList = new ArrayList<>();
        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        // chapter_id sort by desc or asc
        Cursor cursor = mContext.getContentResolver().query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                null,
                selection,
                new String[]{mBookUrl},
                orderBy
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE));
                String url = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
                int chapter_id = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID));
                Chapter chapter = new Chapter(title, url, mBookUrl, chapter_id, offline);
                chapterList.add(chapter);

            }
            cursor.close();
        }

        return chapterList;

    }

    /**
     * 从网络获取章节信息
     *
     * @return 章节列表
     */
    private List<Chapter> initChapterListOnline() {
        return WebParser.getInstance().getContents(mBookUrl, mBook.getContentUrl());
    }

    private void initChapterIndexMap(List<Chapter> chapters) {
        mIndexMap.clear();
        for (int i = 0; i < chapters.size(); i++) {
            // 使用hashMap加快索引位置
            mIndexMap.put(chapters.get(i).getChapterUrl(), i);
        }
    }

    private void initBookInfo() {
        mBook = mOnline ? initBookInfoOnline() : initBookInfoLocal();

        if (mBook == null) {
            mBook = new Book();
        }
    }

    private Book initBookInfoLocal() {
        Log.d(TAG, "get book info from db, mBookUrl = " + mBookUrl);

        Book book = new Book();
        Cursor cursor;
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";
        cursor = mContext.getContentResolver().query(
                BReaderProvider.CONTENT_URI_BOOKS,
                null,
                selection,
                new String[]{mBookUrl},
                null
        );
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            book.setBookName(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_BOOK_NAME)));
            book.setUpdateTime(DateUtils.parseNormal(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_UPDATE_TIME))));
            book.setModifiedTime(DateUtils.parseNormal(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME))));
            book.setBookUrl(mBookUrl);
            book.setCategory(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CATEGORY)));
            book.setAuthor(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_AUTHOR)));
            book.setDescription(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_DESCRIPTION)));
            book.setRecentChapterUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL)));
            book.setPageIndex(cursor.getInt(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX)));
            book.setBookCoverUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_COVER_URL)));
            book.setContentUrl(cursor.getString(cursor.getColumnIndex(BReaderContract.Books.COLUMN_NAME_CONTENT_URL)));
        }
        if (cursor != null) {
            cursor.close();
        }

        return book;


    }

    /**
     * 通过网络初始化书籍信息
     */
    private Book initBookInfoOnline() {
        Book book = WebParser.getInstance().getBookInfo(mBookUrl);
        book.setPageIndex(0);
        return book;
    }

    private void saveToDatabase() {
        // if content url changed
        boolean change = true || (!mOnline && !mOldContentUrl.equals(mBook.getContentUrl()));

        // update bookInfo
        ContentValues contentValues = new ContentValues();
        String selection = BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?";
        // 内容不重复
        contentValues.put(BReaderContract.Books.COLUMN_NAME_MODIFIED_TIME, DateUtils.formatDateToString(mBook.getModifiedTime()));
        contentValues.put(BReaderContract.Books.COLUMN_NAME_RECENT_CHAPTER_URL, mBook.getRecentChapterUrl());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_PAGE_INDEX, mBook.getPageIndex());
        contentValues.put(BReaderContract.Books.COLUMN_NAME_CONTENT_URL, mBook.getContentUrl());

        mContext.getContentResolver().update(
                BReaderProvider.CONTENT_URI_BOOKS,
                contentValues,
                selection,
                new String[]{mBook.getBookUrl()}

        );

        if(DEBUG) {
            Log.d(TAG, "[foree] saveToDatabase: change = " + change + ", bookUrl = " + mBookUrl);
        }
        if (change) {
            // clean old chapters, and update new
            mContext.getContentResolver().delete(
                    BReaderProvider.CONTENT_URI_CHAPTERS,
                    BReaderContract.Books.COLUMN_NAME_BOOK_URL + "=?",
                    new String[]{mBookUrl}
            );
            //thirdly, reload from network and insert db
            mBookDao.insertChapters(mBookUrl, mChapters);
        }

    }

    public void changeSourceId(final String sourceId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBook.setContentUrl(sourceId);
                // get old index
                int oldChapterIndex = getChapterIndex(getCurrentUrl());
                initChapters(true);
                // set new chapter use old index
                switchChapter(getChapterUrl(oldChapterIndex));
                sendCompleteMessage();
            }
        });
    }

}
