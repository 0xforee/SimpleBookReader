package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.net.NetCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/3/17.
 * 用于对外的统一接口，所有的对书籍的资源请求都通过这个代理类处理
 */

public class WebParser implements IWebParser {
    private Map<String, AbsWebParser> mParserMap = new HashMap<>();
    private static WebParser mInstance;

    private WebParser() {

        //registerParser("http://www.biquge.cn", new BiQuGeWebParser());
        //registerParser("http://m.piaotian.com", new PiaoTianWebParser());
        //registerParser("http://www.piaotian.com", new PiaoTianWebParser());
        //registerParser("http://api.zhuishushenqi.com", new ZhuishuWebParser());
        registerParser("http://chapter2.zhuishushenqi.com", new ZhuishuWebParser());
    }


    public static WebParser getInstance() {
        // double check
        if (mInstance == null) {
            synchronized (WebParser.class) {
                if (mInstance == null) {
                    mInstance = new WebParser();
                }
            }
        }
        return mInstance;
    }


    public void registerParser(String url, AbsWebParser parser) {
        if (!url.startsWith("http://"))
            throw new RuntimeException("Url Must be start With \"http://\"");
        else
            mParserMap.put(url, parser);
    }

    public void unRegisterParser(String url) {
        mParserMap.remove(url);
    }

    private AbsWebParser getWebParser(String url) {
        // parse host url
//        if (url.contains("http://") && url.length() > 7) {
//            int index = url.indexOf("/", 7);
//            String keyUrl = url.substring(0, index == -1 ? url.length() : index);
//            return mParserMap.get(keyUrl);
//        } else {
        return new ZhuishuWebParser();
//        }
    }

    @Override
    public void searchBookAsync(final String keyword, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Book> books = new ArrayList<>();
                for (AbsWebParser parser : mParserMap.values()) {
                    List<Book> results = parser.searchBook(keyword);
                    if (results != null) {
                        books.addAll(results);
                    }
                    netCallback.onSuccess(books);
                }
            }
        }.start();
    }

    @Override
    public void getBookInfoAsync(final String bookUrl, final NetCallback<Book> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(bookUrl).getBookInfo(bookUrl));
            }
        }.start();
    }

    @Override
    public void getContentsAsync(final String bookUrl, final String contentsUrl, final NetCallback<List<Chapter>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(contentsUrl).getContents(bookUrl, contentsUrl));
            }
        }.start();
    }

    @Override
    public void getChapterAsync(final String bookUrl, final String chapterUrl, final NetCallback<Chapter> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(chapterUrl).getChapter(bookUrl, chapterUrl));

            }
        }.start();
    }

    @Override
    public void getHomePageInfoAsync(final NetCallback<List<Rank>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Rank> list = new ArrayList<>();
                for (AbsWebParser parser : mParserMap.values()) {
                    List<Rank> result = (List<Rank>) parser.getHomePageInfo();
                    if (result != null) {
                        list.addAll(result);
                    }
                    netCallback.onSuccess(list);
                }
            }
        }.start();
    }

    @Override
    public List<Book> searchBook(String keyword) {
        return null;
    }

    @Override
    public Book getBookInfo(String bookUrl) {
        return getWebParser(bookUrl).getBookInfo(bookUrl);
    }

    @Override
    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        return getWebParser(bookUrl).getContents(bookUrl, contentsUrl);
    }

    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        return getWebParser(chapterUrl).getChapter(bookUrl, chapterUrl);
    }

    @Override
    public List<Rank> getHomePageInfo() {
        List<Rank> list = new ArrayList<>();
        for (AbsWebParser parser : mParserMap.values()) {
            List<Rank> result = (List<Rank>) parser.getHomePageInfo();
            if (result != null) {
                list.addAll(result);
            }
        }
        return list;
    }

    @Override
    public List<Source> getBookSource(String bookId) {
        return getWebParser(bookId).getBookSource(bookId);
    }

    @Override
    public List<Review> getShortReviews(String bookId) {
        return getWebParser(bookId).getShortReviews(bookId);
    }

    @Override
    public List<Review> getLongReviews(String bookId){
        return getWebParser(bookId).getLongReviews(bookId);
    }

}
