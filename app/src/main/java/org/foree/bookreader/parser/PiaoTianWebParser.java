package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by foree on 2018/3/15.
 */

class PiaoTianWebParser extends AbsWebParser {
    private static final String TAG = PiaoTianWebParser.class.getSimpleName();

    @Override
    WebInfo getWebInfo() {
        return new WebInfo() {
            @Override
            public String getHostName() {
                return "飘天文学";
            }

            @Override
            public String getWebChar() {
                return "gbk";
            }

            @Override
            public String getHostUrl() {
                return "http://m.piaotian.com";
            }

            @Override
            public String getSearchApi(String keyword) {
                try {
                    keyword = java.net.URLEncoder.encode(keyword, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String api = "https://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=" + keyword + "&Submit=+%CB%D1+%CB%F7+&page=1";
                Log.d(TAG, "getSearchApi: api = " + api);
                return api;
            }
        };
    }

    @Override
    public List<Book> searchBook(String keyword) {
        return null;
    }

    @Override
    public Book getBookInfo(String bookUrl) {
        return null;
    }

    @Override
    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        return null;
    }

    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        return null;
    }

    @Override
    public List<Book> getHomePageInfo() {
        return null;
    }
}
