package org.foree.bookreader.parser;

import android.text.Html;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
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
    private static boolean DEBUG = false;

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
        return "http://www.biquge.cn";
    }

    @Override
    String getSearchApi() {
        return "http://zhannei.baidu.com/cse/search?s=11869390265411396408&ie=utf-8&q=";
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
            if (DEBUG) Log.i(TAG, update.toString());
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
                    String bookCoverUrl = update.attr("content");
                    book.setBookCoverUrl(bookCoverUrl);
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
        book.setContentUrl(bookUrl);

        return book;
    }

    @Override
    List<Chapter> parseChapterList(String bookUrl, String contentUrl, Document doc) {
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

            if (DEBUG) Log.d("HH", link.text());
            if (DEBUG) Log.d("HH", link.attr("href"));
            chapters.add(chapter);
        }
        return chapters;
    }

    @Override
    Chapter parseChapterContents(String chapterUrl, Document doc) {
        Chapter chapter = new Chapter();

        chapter.setChapterUrl(chapterUrl);

        // get chapter title
        Elements titles = doc.getElementsByTag("h1");
        if (titles != null && titles.size() != 0) {
            Log.d(TAG, "Title" + titles.get(0).text());
            chapter.setChapterTitle(titles.get(0).text());
        }


        // get contents
        Element content = doc.getElementById("content");
        if (content != null) {
            content.select("script").remove();
            if (DEBUG) Log.d(TAG, content.toString());
            chapter.setContents(Html.fromHtml(content.toString()).toString());
        }

        return chapter;
    }
}
