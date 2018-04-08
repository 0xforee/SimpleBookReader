package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;

import java.util.List;

/**
 * Created by foree on 2018/4/8.
 */

public class ZhuishuWebParser extends AbsWebParser {
    @Override
    public List<Book> searchBook(String keyword) {
        return null;
    }

    @Override
    public Book getBookInfo(String bookUrl) {
        // must set params

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

    @Override
    WebInfo getWebInfo() {
        return new WebInfo() {
            @Override
            public String getHostName() {
                return "追书";
            }

            @Override
            public String getWebChar() {
                return null;
            }

            @Override
            public String getHostUrl() {
                return "api.zhuishu.com";
            }

            @Override
            public String getSearchApi(String keyword) {
                return null;
            }
        };
    }
}
