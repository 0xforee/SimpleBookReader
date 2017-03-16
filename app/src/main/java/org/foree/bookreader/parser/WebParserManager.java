package org.foree.bookreader.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 17-1-18.
 * 管理WebParser（小说源解析器）的切换逻辑
 */
public class WebParserManager {
    private static AbsWebParser absWebParser;
    private static WebParserManager mInstance;

    private Map<String, AbsWebParser> mParserMap;

    public static WebParserManager getInstance() {
        // double check
        if (mInstance == null) {
            synchronized (WebParserManager.class) {
                if (mInstance == null) {
                    mInstance = new WebParserManager();
                }
            }
        }
        return mInstance;
    }

    private WebParserManager() {
        mParserMap = new HashMap<>();
        mParserMap.put("http://www.biquge.cn", new BiQuGeWebParser());

    }

    public AbsWebParser getWebParser(String url) {
        // parse host url
        if (url.contains("http://") && url.length() > 7) {
            String keyUrl = url.substring(0, url.indexOf("/", 7));
            return mParserMap.get(keyUrl);
        } else {
            return new NullWebParser();
        }
    }

    public Collection<AbsWebParser> getParsers(){
        return mParserMap.values();
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


}
