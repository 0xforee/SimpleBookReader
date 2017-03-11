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

    @Override
    List<List<Book>> parseHostUrl(String hostUrl, Document doc) {
        List<List<Book>> bookStoreList = new ArrayList<>();
        List<Book> childList = new ArrayList<>();

        // hot content
        Element hotContentElement = doc.getElementById("hotcontent");
        String hotBookCategory = hotContentElement.getElementsByTag("h2") != null ?
                hotContentElement.getElementsByTag("h2").text()
                : "强烈推荐";
        if (DEBUG) Log.d(TAG, "hot book_category = " + hotBookCategory);
        Elements categories = hotContentElement.getElementsByClass("item");
        for (Element item : categories) {

            String hotBookName = item.getElementsByTag("a").text();
            if (DEBUG) Log.d(TAG, "hot book_name = " + hotBookName);

            String hotBookUrl = getHostUrl() + item.getElementsByTag("a").attr("href");
            if (DEBUG) Log.d(TAG, "hot book_url = " + hotBookUrl);

            String hotBookCoverUrl = item.getElementsByTag("img").attr("src");
            if (DEBUG) Log.d(TAG, "hot book_cover url = " + hotBookCoverUrl);

            String description = item.getElementsByTag("dd").text();
            if (DEBUG) Log.d(TAG, "hot book description = " + description);

            Book book = new Book(hotBookName, hotBookUrl, hotBookCoverUrl, hotBookCategory, description);
            childList.add(book);

        }

        bookStoreList.add(childList);
        childList = new ArrayList<>();

        // not hot
        Elements otherElements = doc.select("[class~=content*]");
        for (Element other : otherElements) {
            String otherBookCategory = other.getElementsByTag("h2").text();
            if (DEBUG) Log.d(TAG, "other book_category = " + otherBookCategory);

            // get top
            Element top = other.getElementsByClass("top").get(0);

            String otherTopBookCoverUrl = top.getElementsByTag("img").attr("src");
            if (DEBUG) Log.d(TAG, "top book_cover url = " + otherTopBookCoverUrl);

            String otherTopBookName = top.getElementsByTag("a").get(1).text();
            if (DEBUG) Log.d(TAG, "top book_name = " + otherTopBookName);

            String otherTopBookUrl = getHostUrl() + top.getElementsByTag("a").attr("href");
            if (DEBUG) Log.d(TAG, "top book_url = " + otherTopBookUrl);

            String otherTopDescription = top.getElementsByTag("dd").text();
            if (DEBUG) Log.d(TAG, "top book description = " + otherTopDescription);

            Book otherTopBook = new Book(otherTopBookName, otherTopBookUrl, otherTopBookCoverUrl, otherBookCategory, otherTopDescription);
            childList.add(otherTopBook);

            // get no image book
            if (false) {
                Elements no_image_books = other.getElementsByTag("li");
                for (Element no_image_book : no_image_books) {
                    String noImageBookName = no_image_book.getElementsByTag("a").text();
                    if (DEBUG) Log.d(TAG, "noimage book_name = " + noImageBookName);

                    String noImageBookUrl = getHostUrl() + no_image_book.getElementsByTag("a").attr("href");
                    if (DEBUG) Log.d(TAG, "noimage book_url = " + noImageBookUrl);

                    Book noImageBook = new Book(noImageBookName, noImageBookUrl, "", otherBookCategory, "");
                    childList.add(noImageBook);
                }
            }
            if (DEBUG) Log.d(TAG, "==============group separator================");
            bookStoreList.add(childList);
            childList = new ArrayList<>();

        }
        return bookStoreList;
    }
}
