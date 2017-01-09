package org.foree.bookreader.website;

import android.util.Log;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        List<Book> bookList = new ArrayList<>();
        Elements resultList = doc.getElementsByClass("result-game-item");
        for(Element result: resultList){
            Book book = new Book();
            //Log.d(TAG, result.toString());
            Elements titles = result.getElementsByClass("result-game-item-title-link");
            //Log.d(TAG, titles.toString());
            Element title = titles.get(0);

            Log.d(TAG, title.attr("href"));
            Log.d(TAG, title.attr("title"));

            book.setBook_name(title.attr("title"));
            book.setUrl(title.attr("href"));
            bookList.add(book);

        }
        return bookList;
    }

    @Override
    Book parseBookInfo(Document doc) {
        Book book = new Book();
        Chapter newestChapter = new Chapter();

        Elements updates = doc.select("[property~=og:book*]");
        for(Element update: updates){
            Log.i(TAG, update.toString());
            switch (update.attr("property")){
                case "og:book:category":
                    book.setCategory(update.attr("content"));
                    break;
                case "og:book:author":
                    book.setAuthor(update.attr("content"));
                    break;
                case "og:book:book_name":
                    book.setBook_name(update.attr("content"));
                    break;
                case "og:book:update_time":
                    book.setUpdate_time(update.attr("content"));
                    break;
                case "og:book:latest_chapter_name":
                    newestChapter.setTitle(update.attr("content"));
                    break;
                case "og:book:latest_chapter_url":
                    newestChapter.setUrl(update.attr("content"));
                    break;
            }
        }
        book.setNewest_chapter(newestChapter);

        // ChapterList
        List<Chapter> chapters = new ArrayList<>();
        Elements elements_contents = doc.select("dd");
        Document contents = Jsoup.parse(elements_contents.toString());
        Elements elements_a = contents.getElementsByTag("a");
        for(Element link: elements_a){
            Chapter chapter = new Chapter();
            chapter.setTitle(link.text());
            chapter.setUrl(url + link.attr("href"));
            //Log.i("HH", link.text());
            //Log.i("HH", link.attr("href"));
            chapters.add(chapter);
        }
        Collections.reverse(chapters);
        book.setChapter_list(chapters);

        return book;
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
