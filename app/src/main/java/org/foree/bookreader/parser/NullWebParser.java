package org.foree.bookreader.parser;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
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
    List<Chapter> parseChapterList(String bookUrl, Document doc) {
        return null;
    }

    @Override
    Article parseArticle(String chapterUrl, Document doc) {
        return null;
    }
}
