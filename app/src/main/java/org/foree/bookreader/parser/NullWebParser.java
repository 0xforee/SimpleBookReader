package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by foree on 17-1-19.
 * 空对象模式
 */

class NullWebParser extends AbsWebParser{

    @Override
    WebInfo getWebInfo() {
        return new WebInfo() {
            @Override
            public String getHostName() {
                return null;
            }

            @Override
            public String getWebChar() {
                return null;
            }

            @Override
            public String getHostUrl() {
                return null;
            }

            @Override
            public String getSearchApi(String keyword) {
                return null;
            }
        };
    }

    @Override
    public List<Book> parseBookList(Document doc) {
        return null;
    }

    @Override
    public Book parseBookInfo(String bookUrl, Document doc) {
        return null;
    }

    @Override
    public List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc) {
        return null;
    }

    @Override
    public Chapter parseChapterContents(String chapterUrl, Document doc) {
        return null;
    }

    @Override
    public List<Book> parseHostUrl(String hostUrl, Document doc) {
        return null;
    }
}
