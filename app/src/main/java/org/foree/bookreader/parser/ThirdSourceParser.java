package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;

import java.util.List;
import java.util.Map;

/**
 * @author foree
 * @date 2018/8/19
 * @description
 */
public class ThirdSourceParser extends AbstractWebParser {
    private AbstractWebInfo mWebInfo;

    public ThirdSourceParser(AbstractWebInfo webInfo) {
        mWebInfo = webInfo;
    }

    @Override
    AbstractWebInfo getWebInfo() {
        return mWebInfo;
    }

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
    @Override
    public List<Book> searchBook(String keyword, Map<String, String> params) {
        return null;
    }

    /**
     * get book detail
     *
     * @param bookUrl book id or url
     * @return book object
     */
    @Override
    public Book getBookInfo(String bookUrl) {
        return null;
    }

    /**
     * get contents of a book
     *
     * @param bookUrl     book id or url
     * @param contentsUrl contents id (same as sourceId)
     * @return chapter list
     */
    @Override
    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        return null;
    }

    /**
     * get chapter detail
     *
     * @param bookUrl    book id or url
     * @param chapterUrl chapter id or url
     * @return chapter object
     */
    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        return null;
    }

    /**
     * get all rank info
     *
     * @return rank list
     */
    @Override
    public List<Rank> getHomePageInfo() {
        return null;
    }
}
