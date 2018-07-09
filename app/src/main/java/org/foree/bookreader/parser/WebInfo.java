package org.foree.bookreader.parser;

/**
 * Created by foree on 2018/6/19.
 */

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
