package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 * webParser要实现的解析网站的方法
 */

interface IWebParser {
    // parse api
    List<Book> parseBookList(Document doc);

    Book parseBookInfo(String bookUrl, Document doc);

    List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc);

    Chapter parseChapterContents(String chapterUrl, Document doc);

    List<Book> parseHostUrl(String hostUrl, Document doc);
}
