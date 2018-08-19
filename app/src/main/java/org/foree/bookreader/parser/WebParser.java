package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.net.NetCallback;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/3/17.
 * 用于对外的统一接口，所有的对书籍的资源请求都通过这个代理类处理
 */

public class WebParser {
    private Map<String, AbstractWebParser> mParserMap = new HashMap<>();
    private static WebParser mInstance;

    private WebParser() {
        // init zhuishu
        AbstractWebParser webParser = new ZhuishuWebParser();
        registerParser(webParser.getWebInfo().getHostUrl(), webParser);

//        // init third
//        webParser = new ThirdSourceParser()
//        registerParser("http://www.biquge.cn", new BiQuGeWebParser());
//        registerParser("http://m.piaotian.com", new PiaoTianWebParser());
//        registerParser("http://www.piaotian.com", new PiaoTianWebParser());
//        //registerParser("http://api.zhuishushenqi.com", new ZhuishuWebParser());
        loadIfNecessary();
    }

    private void loadIfNecessary(){
        // 动态加载第三方api
        AbstractWebInfo webInfo = new AbstractWebInfo() {
            /**
             * 获取目标网站地址
             *
             * @return 网页主机host地址
             */
            @Override
            public String getHostUrl() {
                return "http://www.piaotian.com";
            }
        };
        AbstractWebParser webParser = new ThirdSourceParser(webInfo);
        registerParser(webParser.getWebInfo().getHostUrl(), webParser);
    }

    private AbstractWebParser loadFromConfig(String sourceId){
        // generate webInfo from config

        // init source to cachedMap
        return null;
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


    /**
     * register source parser
     * @param sourceId
     * @param parser
     */
    public void registerParser(String sourceId, AbstractWebParser parser) {
        if(!mParserMap.containsKey(sourceId)) {
            mParserMap.put(sourceId, parser);
        }
    }

    public void unRegisterParser(String sourceId) {
        if(mParserMap.containsKey(sourceId)) {
            mParserMap.remove(sourceId);
        }
    }

    /**
     * get parser by sourceId
     * @param sourceId id or url use for recognize parser
     * @return parser
     */
    private AbstractWebParser getWebParser(String sourceId) {
        return new ZhuishuWebParser();
//        if(mParserMap.containsKey(sourceId)){
//            return mParserMap.get(sourceId);
//        }else{
//            AbstractWebParser parser = loadFromConfig(sourceId);
//            if(parser == null) {
//                return new NullWebParser();
//            }else{
//                return parser;
//            }
//        }
    }

    /**
     * 异步搜索书籍
     * @param sourceIds 需要发起搜索的sourceId列表，默认是全部
     * @param keyword 关键字
     * @param netCallback 异步回调
     */
    public void searchBookAsync(final List<String> sourceIds, final String keyword, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                String encodeKeyword = URLEncoder.encode(keyword);
                List<Book> books = new ArrayList<>();

                Map<String, String> data = new HashMap<>();
                data.put("query", encodeKeyword);
                data.put("start", "0");
//        data.put("limit", "20");

                if(sourceIds == null || sourceIds.isEmpty()){
                    for (AbstractWebParser parser : mParserMap.values()) {
                        List<Book> results = parser.searchBook(keyword, data);
                        if (results != null) {
                            books.addAll(results);
                        }
                        netCallback.onSuccess(books);
                    }
                }else{
                    for (String sourceId : sourceIds) {
                        List<Book> results = mParserMap.get(sourceId).searchBook(keyword, data);
                        if(results != null){
                            books.addAll(results);
                        }
                        netCallback.onSuccess(books);


                    }
                }

            }
        }.start();
    }

    public void getBookInfoAsync(final String sourceId, final String bookUrl, final NetCallback<Book> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(sourceId).getBookInfo(bookUrl));
            }
        }.start();
    }

    public void getContentsAsync(final String sourceId, final String bookUrl, final String contentsUrl, final NetCallback<List<Chapter>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(sourceId).getContents(bookUrl, contentsUrl));
            }
        }.start();
    }

    public void getChapterAsync(final String sourceId, final String bookUrl, final String chapterUrl, final NetCallback<Chapter> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(sourceId).getChapter(bookUrl, chapterUrl));

            }
        }.start();
    }

    public void getHomePageInfoAsync(List<String> sourceIds, final NetCallback<List<Rank>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Rank> list = new ArrayList<>();
                for (AbstractWebParser parser : mParserMap.values()) {
                    List<Rank> result = (List<Rank>) parser.getHomePageInfo();
                    if (result != null) {
                        list.addAll(result);
                    }
                    netCallback.onSuccess(list);
                }
            }
        }.start();
    }

    public List<Book> searchBook(String sourceId, String keyword) {
        return null;
    }

    public Book getBookInfo(String sourceId, String bookUrl) {
        return getWebParser(sourceId).getBookInfo(bookUrl);
    }

    public List<Chapter> getContents(String sourceId, String bookUrl, String contentsUrl) {
        return getWebParser(sourceId).getContents(bookUrl, contentsUrl);
    }

    public Chapter getChapter(String sourceId, String bookUrl, String chapterUrl) {
        return getWebParser(sourceId).getChapter(bookUrl, chapterUrl);
    }

    public List<Rank> getHomePageInfo() {
        List<Rank> list = new ArrayList<>();
        for (AbstractWebParser parser : mParserMap.values()) {
            List<Rank> result = (List<Rank>) parser.getHomePageInfo();
            if (result != null) {
                list.addAll(result);
            }
        }
        return list;
    }

    /**
     * 获取书源
     * @param bookId
     * @return
     */
    public List<Source> getBookSource(String bookId, String bookName) {
        if(!bookId.startsWith("http")){
            // zhuishu

        }

        // third
        return getWebParser(bookId).getBookSource(bookId);
    }

    public List<Review> getShortReviews(String sourceId, String bookId) {

        Map<String, String> params = new HashMap<>(4);
        params.put("book", bookId);
        params.put("sortType", "newest");
        params.put("start", "0");
        params.put("limit", "20");
        return getWebParser(sourceId).getShortReviews(bookId, params);
    }

    public List<Review> getLongReviews(String sourceId, String bookId) {
        Map<String, String> params = new HashMap<>(4);
        params.put("book", bookId);
        params.put("sort", "updated");
        params.put("start", "0");
        params.put("limit", "4");
        return getWebParser(sourceId).getLongReviews(bookId, params);
    }

    public List<Book> getRankList(List<String> sourceIds, String rankId) {
        if(sourceIds == null || sourceIds.isEmpty()) {
            for (AbstractWebParser parser : mParserMap.values()) {
                return parser.getRankList(rankId);

            }
            return null;
        }else {
            return getWebParser(sourceIds.get(0)).getRankList(rankId);
        }
    }

    public void getRankListAsync(final String rankId, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Book> books = getRankList(null, rankId);
                if (books != null) {
                    netCallback.onSuccess(books);
                }
            }
        }.start();
    }
}
