package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.data.book.Article;
import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 17-1-7.
 */

public class BiQuGeWebParser extends AbsWebParser {
    private static final String TAG = BiQuGeWebParser.class.getSimpleName();

    @Override
    String getHostName() {
        return "笔趣阁";
    }

    @Override
    String getWebChar() {
        return "utf-8";
    }

    @Override
    String getHostUrl() {
        return "http://www.biquge.com";
    }

    @Override
    String getSearchApi() {
        return "http://zhannei.baidu.com/cse/search?s=287293036948159515&q=";
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
    Book parseBookInfo(String bookUrl, Document doc) {
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
        book.setBookUrl(bookUrl);
        // ChapterList
        List<Chapter> chapters = new ArrayList<>();
        Elements elements_contents = doc.select("dd");
        Document contents = Jsoup.parse(elements_contents.toString());
        Elements elements_a = contents.getElementsByTag("a");
        for (Element link : elements_a) {
            Chapter chapter = new Chapter();

            chapter.setChapterTitle(link.text());
            chapter.setChapterUrl(getHostUrl() + link.attr("href"));
            // set bookUrl
            chapter.setBookUrl(bookUrl);
            // set chapterId for sort
            chapter.setChapterId(getChapterId(link.attr("href")));

            //Log.i("HH", link.text());
            //Log.i("HH", link.attr("href"));
            chapters.add(chapter);
        }
        book.setChapterList(chapters);

        return book;
    }

    @Override
    List<Chapter> parseChapterList(String bookUrl, Document doc) {
        return null;
    }

    @Override
    Article parseArticle(String chapterUrl, Document doc) {
        Article article = new Article();

        // get article title
        Elements titles = doc.getElementsByTag("h1");
        if (titles != null && titles.size() != 0) {
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
