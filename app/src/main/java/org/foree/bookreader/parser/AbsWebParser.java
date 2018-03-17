package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.net.NetCallback;

import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 解析器的公共api接口，用于构建标准的接口
 */

abstract class AbsWebParser implements IWebParser {
    private static final String TAG = AbsWebParser.class.getSimpleName();

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

    abstract WebInfo getWebInfo();

    @Override
    public void searchBookAsync(String keyword, NetCallback<List<Book>> netCallback) {
        // default implement
    }

    @Override
    public void getBookInfoAsync(String bookUrl, NetCallback<Book> netCallback) {
        // default implement
    }

    @Override
    public void getContentsAsync(String bookUrl, String contentsUrl, NetCallback<List<Chapter>> netCallback) {
        // default implement
    }

    @Override
    public void getChapterAsync(String bookUrl, String chapterUrl, NetCallback<Chapter> netCallback) {
        // default implement
    }

    @Override
    public void getHomePageInfoAsync(NetCallback<List<Book>> netCallback) {
        // default implement
    }

    int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

}
