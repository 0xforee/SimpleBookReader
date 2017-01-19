package org.foree.bookreader.parser;

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

    }

    public static AbsWebParser getAbsWebParser() {
        if (absWebParser == null) {
            absWebParser = new BiQuGeWebParser();
        }

        return absWebParser;
    }

    public AbsWebParser getWebParser(String url) {
        return mParserMap.get(url);
    }

    public void registerParser(String url, AbsWebParser parser) {
        mParserMap.put(url, parser);
    }

    public void unRegisterParser(String url) {
        mParserMap.remove(url);
    }


}
