package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 2018/3/15.
 */

class PiaoTianWebParser extends AbsWebParser {
    private static final String TAG = PiaoTianWebParser.class.getSimpleName();
    @Override
    public List<Book> parseBookList(Document doc) {
        List<Book> books = new ArrayList<>();
        Log.d(TAG, "parseBookList: " + doc.toString());
        Elements trs = doc.getElementsByTag("tr");
        for(Element tr: trs){
            // skip first table header
            if (tr.hasAttr("align")) {
                continue;
            }else{
                Book book = new Book();
                // parse search book info
                Log.d(TAG, "parseBookList: bookUrl = " + tr.child(0).child(0).attr("href"));
                Log.d(TAG, "parseBookList: bookName = " + tr.child(0).child(0).text());
                Log.d(TAG, "parseBookList: author = " + tr.child(2).text());

            }
        }
        return null;
    }

    @Override
    public Book parseBookInfo(String bookUrl, Document doc) {
        return null;
    }

    @Override
    public List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc) {
        return null;
    }

    @Override
    public Chapter parseChapterContents(String chapterUrl, Document doc) {
        return null;
    }

    @Override
    public List<Book> parseHostUrl(String hostUrl, Document doc) {
        return null;
    }

    @Override
    WebInfo getWebInfo() {
        return new WebInfo() {
            @Override
            public String getHostName() {
                return "飘天文学";
            }

            @Override
            public String getWebChar() {
                return "gbk";
            }

            @Override
            public String getHostUrl() {
                return "http://m.piaotian.com";
            }

            @Override
            public String getSearchApi(String keyword) {
                try {
                    keyword = java.net.URLEncoder.encode(keyword, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String api = "https://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=" + keyword + "&Submit=+%CB%D1+%CB%F7+&page=1";
                Log.d(TAG, "getSearchApi: api = " + api);
                return api;
            }
        };
    }
}
