package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by foree on 17-1-19.
 */

public class NullWebParser extends AbsWebParser{
    @Override
    String getHostName() {
        return null;
    }

    @Override
    String getWebChar() {
        return null;
    }

    @Override
    String getHostUrl() {
        return null;
    }

    @Override
    String getSearchApi() {
        return null;
    }

    @Override
    List<Book> parseBookList(Document doc) {
        return null;
    }

    @Override
    Book parseBookInfo(String bookUrl, Document doc) {
        return null;
    }

    @Override
    List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc) {
        return null;
    }

    @Override
    Chapter parseChapterContents(String chapterUrl, Document doc) {
        return null;
    }
}
