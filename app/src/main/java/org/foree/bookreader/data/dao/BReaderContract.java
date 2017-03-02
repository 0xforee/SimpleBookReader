package org.foree.bookreader.data.dao;

import android.provider.BaseColumns;

/**
 * Created by foree on 17-3-1.
 * 数据库相关常量
 */

public final class BReaderContract {

    private BReaderContract() {
    }

    /* books table */
    public static class Books implements BaseColumns {
        public static final String TABLE_NAME = "books";
        public static final String COLUMN_NAME_BOOK_NAME = "book_name";
        public static final String COLUMN_NAME_BOOK_URL = "book_url";
        public static final String COLUMN_NAME_CONTENT_URL = "content_url";
        public static final String COLUMN_NAME_COVER_URL = "cover_url";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
        public static final String COLUMN_NAME_PAGE_INDEX = "page_index";
        public static final String COLUMN_NAME_RECENT_ID = "recent_id";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_AUTHOR = "author";
    }

    public static class Chapters implements BaseColumns {
        public static final String TABLE_NAME = "chapters";
        public static final String COLUMN_NAME_CHAPTER_URL = "chapter_url";
        public static final String COLUMN_NAME_CHAPTER_ID = "chapter_id";
        public static final String COLUMN_NAME_BOOK_URL = "book_url";
        public static final String COLUMN_NAME_CHAPTER_TITLE = "chapter_title";
        public static final String COLUMN_NAME_CHAPTER_CONTENT = "chapter_content";
        public static final String COLUMN_NAME_CACHED = "cached";
    }
}
