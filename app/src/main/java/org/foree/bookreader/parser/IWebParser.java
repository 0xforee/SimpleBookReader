package org.foree.bookreader.parser;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.net.NetCallback;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 每个web都要实现的
 */

public interface IWebParser {
    void searchBook(String keywords, NetCallback<List<Book>> netCallback);
    void getBookInfo(String bookUrl, NetCallback<Book> netCallback);
    void getChapterList(String bookUrl, NetCallback<List<Chapter>> netCallback);
    void getArticle(String chapterUrl, NetCallback<Article> netCallback);
}
