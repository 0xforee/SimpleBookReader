package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;

import java.util.List;
import java.util.Map;

/**
 * Created by foree on 17-1-19.
 * 空对象模式
 */

class NullWebParser extends AbstractWebParser {

    @Override
    AbstractWebInfo getWebInfo() {
        return null;
    }

    @Override
    public List<Book> searchBook(String keyword, Map<String, String> params) {
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
    public List<Rank> getHomePageInfo() {
        return null;
    }
}
