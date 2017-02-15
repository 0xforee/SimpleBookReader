package org.foree.bookreader.parser;

import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.net.NetCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by foree on 17-1-7.
 */

public abstract class AbsWebParser implements IWebParser {
    private static final String TAG = AbsWebParser.class.getSimpleName();

    // webParser base info
    /**
     * 获取目标网站名称
     *
     * @return 网站名称
     */
    abstract String getHostName();

    /**
     * 获取解析网站的网页编码
     *
     * @return 网页编码
     */
    abstract String getWebChar();

    /**
     * 获取目标网站地址
     *
     * @return 网页主机host地址
     */
    abstract String getHostUrl();

    /**
     * 获取搜索api用于传入搜索关键字
     *
     * @return 搜索api
     */
    abstract String getSearchApi();


    // parse api
    abstract List<Book> parseBookList(Document doc);

    abstract Book parseBookInfo(String bookUrl, Document doc);

    abstract List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc);

    abstract Chapter parseChapterContents(String chapterUrl, Document doc);

    @Override
    public void searchBook(final String keywords, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(getSearchApi() + keywords).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseBookList(doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    @Override
    public void getBookInfo(final String bookUrl, final NetCallback<Book> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(bookUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseBookInfo(bookUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();

    }

    @Override
    public void getChapterList(final String bookUrl, final String contentUrl, final NetCallback<List<Chapter>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(contentUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseChapterList(bookUrl, contentUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    @Override
    public void getChapterContents(final String chapterUrl, final NetCallback<Chapter> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(chapterUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseChapterContents(chapterUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }
}
