package org.foree.bookreader.parser;

import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.SearchHotWord;
import org.foree.bookreader.bean.book.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 17-1-7.
 * 解析器的公共api接口，用于构建标准的接口
 */

abstract class AbstractWebParser implements IWebParser {
    private static final String TAG = AbstractWebParser.class.getSimpleName();
    static boolean DEBUG = false;

    public static final String SPLIT_KEY = GlobalConfig.MAGIC_SPLIT_KEY;
    public static final String DEFAULT_SOURCE_ID = "http://api.zhuishu.com";


    abstract AbstractWebInfo getWebInfo();

    int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

    Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
        return headers;
    }

    @Override
    public List<Source> getBookSource(String bookId) {
        return null;
    }

    /**
     * get book source info (sourceId == contentsId)
     *
     * @param bookId  book id or url
     * @param bookKey use for third parser recognize different book
     * @return source list
     */
    @Override
    public List<Source> getBookSource(String bookId, String bookKey) {
        return null;
    }

    @Override
    public List<Review> getShortReviews(String bookId, Map<String, String> params) {
        return null;
    }

    @Override
    public List<Review> getLongReviews(String bookId, Map<String, String> params) {
        return null;
    }

    @Override
    public List<Book> getRankList(String rankId) {
        return null;
    }

    /**
     * get search hot words
     *
     * @return hot word list
     */
    @Override
    public List<SearchHotWord> getSearchHotWords() {
        return null;
    }

    String wrapSplitKey(String url){
        return getWebInfo().getHostUrl() + SPLIT_KEY + url;
    }

    /**
     * 获取合法的sourceId
     * @param url 新版本包含sourceId，旧版本不包含sourceId，使用默认的Id
     * @return sourceId
     */
    public static String getValidSourceId(String url){
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
    public static String getValidRealId(String url){
        String[] values = url.split(SPLIT_KEY);
        if(values.length == 1){
            return values[0];
        }else{
            return values[1];
        }
    }
}
