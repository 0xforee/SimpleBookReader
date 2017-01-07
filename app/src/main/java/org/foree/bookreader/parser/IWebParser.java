package org.foree.bookreader.parser;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 每个web都要实现的
 */

public interface IWebParser {
    List<Book> searchBook(String keyword);
    Book parseBookInfo(String bookUrl);
    List<Chapter> parseChapterList(String bookUrl);
    Article parseArticle(String chapterUrl);
}
