package org.foree.bookreader.parser;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.net.NetCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.SerializablePermission;
import java.io.StringReader;
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
    private static final String TAG = "WebParser";
    private Map<String, AbstractWebParser> mParserMap = new HashMap<>();
    private static WebParser mInstance;
    private static final String SPLIT_KEY = GlobalConfig.MAGIC_SPLIT_KEY;
    private static final String DEFAULT_SOURCE_ID = "http://api.zhuishu.com";

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

        try {
            InputStream inputStream = BaseApplication.getInstance().getResources().openRawResource(R.raw.source_test);

            byte[] bytes = new byte[1024];

            StringBuilder sb = new StringBuilder();

            while (inputStream.read(bytes) != -1) {
                sb.append(new String(bytes));
            }

            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(sb.toString()));
            reader.setLenient(true);
            ThirdWebInfo[] webInfos = gson.fromJson(reader, ThirdWebInfo[].class);

            for (int i = 0; i < webInfos.length; i++) {
                // 动态加载第三方api
                AbstractWebParser webParser = new ThirdSourceParser(webInfos[i]);
                registerParser(webInfos[i].getBookSourceUrl(), webParser);
            }


        }catch (IOException e){
            Log.e(TAG, e.toString());
        }
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
        if(mParserMap.containsKey(sourceId)){
            return mParserMap.get(sourceId);
        }else{
            AbstractWebParser parser = loadFromConfig(sourceId);
            if(parser == null) {
                return new NullWebParser();
            }else{
                return parser;
            }
        }
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

    /**
     * 兼容旧格式
     * @param bookUrl 新版本包含sourceId；旧版本不含sourceId，只有bookId
     * @param netCallback 回调
     */
    public void getBookInfoAsync(String bookUrl, NetCallback<Book> netCallback){
        getBookInfoAsync(getValidSourceId(bookUrl), getValidRealId(bookUrl), netCallback);
    }

    private void getBookInfoAsync(final String sourceId, final String bookUrl, final NetCallback<Book> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(sourceId).getBookInfo(bookUrl));
            }
        }.start();
    }

    private void getContentsAsync(final String sourceId, final String bookUrl, final String contentsUrl, final NetCallback<List<Chapter>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netCallback.onSuccess(getWebParser(sourceId).getContents(bookUrl, contentsUrl));
            }
        }.start();
    }

    private void getChapterAsync(final String sourceId, final String bookUrl, final String chapterUrl, final NetCallback<Chapter> netCallback) {
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

    public List<Book> searchBook(String[] sourceIds, String keyword) {
        return null;
    }

    public Book getBookInfo(String bookUrl) {
        return getWebParser(getValidSourceId(bookUrl)).getBookInfo(getValidRealId(bookUrl));
    }

    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        return getWebParser(getValidSourceId(bookUrl)).getContents(getValidRealId(bookUrl), getValidRealId(contentsUrl));
    }

    public Chapter getChapter(String bookUrl, String chapterUrl) {
        return getWebParser(getValidSourceId(chapterUrl)).getChapter(bookUrl, getValidRealId(chapterUrl));
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

    public List<Review> getShortReviews(String bookId) {

        String realBookId = getValidRealId(bookId);
        Map<String, String> params = new HashMap<>(4);
        params.put("book", realBookId);
        params.put("sortType", "newest");
        params.put("start", "0");
        params.put("limit", "20");
        return getWebParser(getValidSourceId(bookId)).getShortReviews(realBookId, params);
    }

    public List<Review> getLongReviews(String bookId) {
        String realBookId = getValidRealId(bookId);
        Map<String, String> params = new HashMap<>(4);
        params.put("book", realBookId);
        params.put("sort", "updated");
        params.put("start", "0");
        params.put("limit", "4");
        return getWebParser(getValidSourceId(bookId)).getLongReviews(realBookId, params);
    }

    public List<Book> getRankList(String rankId) {
        return getWebParser(getValidSourceId(rankId)).getRankList(rankId);
    }

    public void getRankListAsync(final String rankId, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Book> books = getRankList(rankId);
                if (books != null) {
                    netCallback.onSuccess(books);
                }
            }
        }.start();
    }

    /**
     * 获取合法的sourceId
     * @param url 新版本包含sourceId，旧版本不包含sourceId，使用默认的Id
     * @return sourceId
     */
    private String getValidSourceId(String url){
        String[] values = url.split(SPLIT_KEY);
        if(values.length == 1){
            return DEFAULT_SOURCE_ID;
        }

        return values[0];
    }

    /**
     * 解析合法真实的Id（包括bookId, contentsId, chapterId)
     * @param url 可能为新、旧版本的id
     * @return 解析的真实Id
     */
    private String getValidRealId(String url){
        String[] values = url.split(SPLIT_KEY);
        if(values.length == 1){
            return values[0];
        }else{
            return values[1];
        }
    }
}
