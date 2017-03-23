package org.foree.bookreader.pagination;

import android.database.Cursor;

import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.bean.cache.ChapterCache;
import org.foree.bookreader.bean.cache.DoubleCache;
import org.foree.bookreader.bean.cache.PaginationCache;
import org.foree.bookreader.bean.dao.BReaderContract;
import org.foree.bookreader.bean.dao.BReaderProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 17-2-6.
 */

public class PaginationLoader {
    private PaginationArgs paginationArgs;
    private RequestQueue mRequestQueue;

    private static PaginationLoader mInstance;

    private ChapterCache chapterCache = new DoubleCache();

    public static PaginationLoader getInstance() {
        if (mInstance == null) {
            synchronized (PaginationLoader.class) {
                if (mInstance == null) {
                    mInstance = new PaginationLoader();
                }
            }
        }

        return mInstance;
    }

    private PaginationLoader() {
    }

    public void init(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;
        if (mRequestQueue == null) {
            mRequestQueue = new RequestQueue();
            mRequestQueue.start();
        }

        PaginationCache.getInstance().init(paginationArgs);
    }

    public void setChapterCache(ChapterCache chapterCache) {
        this.chapterCache = chapterCache;
    }

    public void loadPagination(final String url) {
        mRequestQueue.add(new ChapterRequest(url, paginationArgs, true));

        startPaginationCache(url, 5);

    }

    public ChapterCache getChapterCache() {
        return chapterCache;
    }


    private Map<String, Integer> mIndexMap = new HashMap<>();
    private ArrayList<PaginationCacheFlag> mCachedList = new ArrayList<>();

    private class PaginationCacheFlag {
        String url;
        boolean cached;

        PaginationCacheFlag(String url, boolean cached) {
            this.url = url;
            this.cached = cached;
        }

        String getUrl() {
            return url;
        }

        boolean isCached() {
            return cached;
        }

        void setCached() {
            this.cached = true;
        }
    }

    public void initPaginationCache(String bookUrl) {
        mIndexMap.clear();
        mCachedList.clear();

        String selection = BReaderContract.Chapters.COLUMN_NAME_BOOK_URL + "=?";
        String orderBy = BReaderContract.Chapters.COLUMN_NAME_CHAPTER_ID + " asc";

        // chapter_id sort by desc or asc
        Cursor cursor = BaseApplication.getInstance().getContentResolver().query(
                BReaderProvider.CONTENT_URI_CHAPTERS,
                new String[]{BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL, BReaderContract.Chapters.COLUMN_NAME_CACHED},
                selection,
                new String[]{bookUrl},
                orderBy
        );

        if (cursor != null) {
            int i = 0;
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_URL));
                boolean offline = cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1;
                mIndexMap.put(url, i++);
                mCachedList.add(new PaginationCacheFlag(url, offline));
            }
            cursor.close();
        }
    }

    public void setCached(String url) {
        mCachedList.get(mIndexMap.get(url)).setCached();
    }

    /**
     * 获取指定章节的前后偏移章节
     *
     * @param offset 当前章节的偏移量
     * @param url    当前章节的url
     */
    private void startPaginationCache(String url, final int offset) {
        int tmp = 1;
        if (mIndexMap.containsKey(url)) {
            final int index = mIndexMap.get(url);

            // 前几章
            int newIndex = index - tmp;
            while (newIndex > 0 && tmp < offset) {
                if (!mCachedList.get(newIndex).isCached()) {
                    // add request
                    mRequestQueue.add(new ChapterRequest(mCachedList.get(newIndex).getUrl(), paginationArgs, false));
                }
                tmp++;
                newIndex = index - tmp;
            }

            // 后几章
            tmp = 1;
            newIndex = index + tmp;

            while (newIndex < mCachedList.size() && tmp < offset) {
                if (!mCachedList.get(newIndex).isCached()) {
                    // add request
                    mRequestQueue.add(new ChapterRequest(mCachedList.get(newIndex).getUrl(), paginationArgs, false));

                }
                tmp++;
                newIndex = index + tmp;
            }

        }
    }
}
