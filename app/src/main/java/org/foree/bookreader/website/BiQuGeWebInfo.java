package org.foree.bookreader.website;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 */

public class BiQuGeWebInfo extends WebInfo {

    public BiQuGeWebInfo(){

    }

    @Override
    public List<Book> searchBook(String keyword) {
        return null;
    }

    @Override
    public Book parseBookInfo(String bookUrl) {
        return null;
    }

    @Override
    public List<Chapter> parseChapterList(String bookUrl) {
        return null;
    }

    @Override
    public Article parseArticle(String chapterUrl) {
        return null;
    }
}
