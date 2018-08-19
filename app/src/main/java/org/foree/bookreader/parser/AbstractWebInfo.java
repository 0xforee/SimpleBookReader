package org.foree.bookreader.parser;

/**
 * @author foree
 * @date 2018/8/19
 * @description
 */
public abstract class AbstractWebInfo implements IWebInfo {
    /**
     * 获取目标网站名称
     *
     * @return 网站名称
     */
    @Override
    public String getHostName() {
        return null;
    }

    /**
     * 获取解析网站的网页编码
     *
     * @return 网页编码
     */
    @Override
    public String getWebChar() {
        return null;
    }

    /**
     * 获取目标网站地址
     *
     * @return 网页主机host地址
     */
    @Override
    public String getHostUrl() {
        return null;
    }

    /**
     * 获取搜索api用于传入搜索关键字
     *
     * @param keyword
     * @return 搜索api
     */
    @Override
    public String getSearchApi(String keyword) {
        return null;
    }
}
