package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.net.NetCallback;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 每个web都要实现的
 */

public interface IWebParser {
    void searchBook(String keywords, NetCallback<List<Book>> netCallback);

    void getBookInfo(String bookUrl, NetCallback<Book> netCallback);

    void getChapterList(String bookUrl, String contentUrl, NetCallback<List<Chapter>> netCallback);

    void getChapterContents(String chapterUrl, NetCallback<Chapter> netCallback);
}
