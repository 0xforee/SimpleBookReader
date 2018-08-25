package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;

import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/3/17.
 * sync function
 */

interface IWebParser {
    /**
     * search book
     *
     * @param keyword book name or author
     * @param params  http request params:
     *                query: keyword
     *                start: start offset
     *                limit: results limit
     * @return book list
     */
    List<Book> searchBook(String keyword, Map<String, String> params);

    /**
     * get book detail
     *
     * @param bookUrl book id or url
     * @return book object
     */
    Book getBookInfo(String bookUrl);

    /**
     * get contents of a book
     *
     * @param bookUrl     book id or url
     * @param contentsUrl contents id (same as sourceId)
     * @return chapter list
     */
    List<Chapter> getContents(String bookUrl, String contentsUrl);

    /**
     * get chapter detail
     *
     * @param bookUrl    book id or url
     * @param chapterUrl chapter id or url
     * @return chapter object
     */
    Chapter getChapter(String bookUrl, String chapterUrl);

    /**
     * get all rank info
     *
     * @return rank list
     */
    List<Rank> getHomePageInfo();

    /**
     * get book source info (sourceId == contentsId)
     *
     * @param bookId book id or url
     * @return source list
     */
    List<Source> getBookSource(String bookId);

    /**
     * get book source info (sourceId == contentsId)
     *
     * @param bookId book id or url
     * @param bookKey use for third parser recognize different book
     * @return source list
     */
    List<Source> getBookSource(String bookId, String bookKey);

    /**
     * get short review info
     *
     * @param bookId book id or url
     * @param params http request params:
     *               book: book id
     *               sortType: (lastUpdated|newest|mostlike)
     *               start: start index
     *               limit: results limit
     * @return review list
     */
    List<Review> getShortReviews(String bookId, Map<String, String> params);

    /**
     * get long review info
     *
     * @param bookId book id or url
     * @param params http request params:
     *               book: book id
     *               sort: (updated|created|comment-count)
     *               start: start index
     *               limit: results limit
     * @return review list
     */
    List<Review> getLongReviews(String bookId, Map<String, String> params);

    /**
     * get rank detail
     *
     * @param rankId rank id
     * @return book list of rankId
     */
    List<Book> getRankList(String rankId);
}
