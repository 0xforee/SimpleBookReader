package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.net.NetCallback;

import java.util.List;

/**
 * Created by foree on 2018/3/17.
 */

interface IWebParser {
    // async function
    void searchBookAsync(String keyword, NetCallback<List<Book>> netCallback);

    void getBookInfoAsync(String bookUrl, NetCallback<Book> netCallback);

    void getContentsAsync(String bookUrl, String contentsUrl, NetCallback<List<Chapter>> netCallback);

    void getChapterAsync(String bookUrl, String chapterUrl, NetCallback<Chapter> netCallback);

    void getHomePageInfoAsync(NetCallback<List<Book>> netCallback);

    // sync function
    List<Book> searchBook(String keyword);

    Book getBookInfo(String bookUrl);

    List<Chapter> getContents(String bookUrl, String contentsUrl);

    Chapter getChapter(String bookUrl, String chapterUrl);

    List<Book> getHomePageInfo();

    List<Source> getBookSource(String bookId);

    List<Review> getShortReviews(String bookId);
}
