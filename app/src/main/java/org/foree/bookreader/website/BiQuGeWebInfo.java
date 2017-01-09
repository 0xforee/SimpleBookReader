package org.foree.bookreader.website;

import android.util.Log;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.utils.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by foree on 17-1-7.
 */

public class BiQuGeWebInfo extends WebInfo {
    private static final String TAG = BiQuGeWebInfo.class.getSimpleName();

    public BiQuGeWebInfo(){
        name = "笔趣阁";
        web_char = "utf-8";
        url = "http://www.biquge.com";
        searchApi = "http://zhannei.baidu.com/cse/search?s=287293036948159515&q=";
    }

    @Override
    List<Book> parseBookList(Document doc) {
        Elements resultList = doc.getElementsByClass("result-game-item");
        for(Element result: resultList){
            //Log.d(TAG, result.toString());
            Elements titles = result.getElementsByClass("result-game-item-title-link");
            //Log.d(TAG, titles.toString());
            Element title = titles.get(0);

            Log.d(TAG, title.attr("href"));
            Log.d(TAG, title.attr("title"));

        }
        return null;
    }

    @Override
    Book parseBookInfo(Document doc) {
        return null;
    }

    @Override
    List<Chapter> parseChapterList(Document doc) {
        return null;
    }

    @Override
    Article parseArticle(Document doc) {
        return null;
    }
}
