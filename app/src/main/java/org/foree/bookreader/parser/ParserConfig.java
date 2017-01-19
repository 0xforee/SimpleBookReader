package org.foree.bookreader.parser;

/**
 * Created by foree on 17-1-19.
 * 接收每个Parser的配置
 */

public class ParserConfig {

    /**
     * 解析网站的名称
     */
    public String name;

    /**
     * 网页编码
     */
    public String webChar;

    /**
     * 搜索API
     */
    public String searchApi;

    public ParserConfig(String name, String webChar, String searchApi) {
        this.name = name;
        this.webChar = webChar;
        this.searchApi = searchApi;
    }

    public ParserConfig() {
    }
}
