package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 解析器的公共api接口，用于构建标准的接口
 */

abstract class AbsWebParser implements IWebParser {
    private static final String TAG = AbsWebParser.class.getSimpleName();

    public interface WebInfo {
        /**
         * 获取目标网站名称
         *
         * @return 网站名称
         */
        String getHostName();

        /**
         * 获取解析网站的网页编码
         *
         * @return 网页编码
         */
        String getWebChar();

        /**
         * 获取目标网站地址
         *
         * @return 网页主机host地址
         */
        String getHostUrl();

        /**
         * 获取搜索api用于传入搜索关键字
         *
         * @return 搜索api
         */
        String getSearchApi(String keyword);
    }

    abstract WebInfo getWebInfo();

    List<Book> searchBook(final String keywords) {
        Document doc;
        try {
            doc = Jsoup.connect(getWebInfo().getSearchApi(keywords)).get();
            Log.d(TAG, "run: " + getWebInfo().getHostName());
            if (doc != null) {
                return parseBookList(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    Book getBookInfo(final String bookUrl) {
        Document doc;
        Book book = null;
        try {
            doc = Jsoup.connect(bookUrl).get();
            if (doc != null) {
                book = parseBookInfo(bookUrl, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    List<Chapter> getChapterList(final String bookUrl, final String contentUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(contentUrl).get();
            if (doc != null) {
                return parseChapterList(bookUrl, contentUrl, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    Chapter getChapterContents(final String chapterUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(chapterUrl).get();
            if (doc != null) {
                return parseChapterContents(chapterUrl, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Chapter();
    }

    int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

    List<Book> getHomePageInfo() {
        Document doc;
        try {
            doc = Jsoup.connect(getWebInfo().getHostUrl()).get();
            if (doc != null) {
                return parseHostUrl(getWebInfo().getHostUrl(), doc);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return new ArrayList<>();
    }

}
