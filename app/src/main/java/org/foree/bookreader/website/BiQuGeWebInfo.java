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

    public BiQuGeWebInfo() {
        name = "笔趣阁";
        web_char = "utf-8";
        url = "http://www.biquge.com";
        searchApi = "http://zhannei.baidu.com/cse/search?s=287293036948159515&q=";
    }

    @Override
    List<Book> parseBookList(Document doc) {
        List<Book> bookList = new ArrayList<>();
        Elements resultList = doc.getElementsByClass("result-game-item");
        for (Element result : resultList) {
            Book book = new Book();
            //Log.d(TAG, result.toString());
            Elements titles = result.getElementsByClass("result-game-item-title-link");
            //Log.d(TAG, titles.toString());
            Element title = titles.get(0);

            Log.d(TAG, title.attr("href"));
            Log.d(TAG, title.attr("title"));

            book.setBookName(title.attr("title"));
            book.setBookUrl(title.attr("href"));
            bookList.add(book);

        }
        return bookList;
    }

    @Override
    Book parseBookInfo(Document doc) {
        Book book = new Book();
        Chapter newestChapter = new Chapter();

        Elements updates = doc.select("[property~=og:*]");
        for (Element update : updates) {
            Log.i(TAG, update.toString());
            switch (update.attr("property")) {
                case "og:novel:category":
                    book.setCategory(update.attr("content"));
                    break;
                case "og:novel:author":
                    book.setAuthor(update.attr("content"));
                    break;
                case "og:novel:book_name":
                    book.setBookName(update.attr("content"));
                    break;
                case "og:novel:read_url":
                    book.setBookUrl(update.attr("content"));
                    break;
                case "og:description":
                    String description = update.attr("content");
                    if (!description.isEmpty()) {
                        book.setDescription(description.split("各位书友")[0]);
                    }
                    break;
                case "og:image":
                    // book cover
                    break;
                case "og:novel:update_time":
                    book.setUpdateTime(update.attr("content"));
                    break;
                case "og:novel:latest_chapter_name":
                    newestChapter.setChapterTitle(update.attr("content"));
                    break;
                case "og:novel:latest_chapter_url":
                    newestChapter.setChapterUrl(update.attr("content"));
                    break;

            }
        }
        book.setNewestChapter(newestChapter);

        // ChapterList
        List<Chapter> chapters = new ArrayList<>();
        Elements elements_contents = doc.select("dd");
        Document contents = Jsoup.parse(elements_contents.toString());
        Elements elements_a = contents.getElementsByTag("a");
        for (Element link : elements_a) {
            Chapter chapter = new Chapter();

            chapter.setChapterTitle(link.text());
            chapter.setChapterUrl(url + link.attr("href"));
            // set bookUrl
            chapter.setBookUrl(book.getBookUrl());
            // set chapterId for sort
            chapter.setChapterId(getChapterId(link.attr("href")));

            //Log.i("HH", link.text());
            //Log.i("HH", link.attr("href"));
            chapters.add(chapter);
        }
        book.setChapterList(chapters);

        return book;
    }

    private int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

    @Override
    List<Chapter> parseChapterList(Document doc) {
        return null;
    }

    @Override
    Article parseArticle(Document doc) {
        Article article = new Article();

        // get article title
        Elements titles = doc.getElementsByTag("h1");
        if( titles != null ){
            Log.d(TAG, "Title" + titles.get(0).toString());
            article.setTitle(titles.get(0).toString());
        }


        // get contents
        Element content = doc.getElementById("content");
        if (content != null) {
            content.select("script").remove();
            Log.d(TAG, content.toString());
            article.setContents(content.toString());
        }

        return article;
    }
}
