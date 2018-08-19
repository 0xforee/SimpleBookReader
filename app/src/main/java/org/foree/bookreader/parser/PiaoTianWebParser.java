package org.foree.bookreader.parser;

import android.text.Html;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.utils.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/3/15.
 */

class PiaoTianWebParser extends AbstractWebParser {
    private static final String TAG = PiaoTianWebParser.class.getSimpleName();

    @Override
    AbstractWebInfo getWebInfo() {
        return new AbstractWebInfo() {
            @Override
            public String getHostName() {
                return "飘天文学";
            }

            @Override
            public String getWebChar() {
                return "gb2312";
            }

            @Override
            public String getHostUrl() {
                return "http://m.piaotian.com";
            }

            @Override
            public String getSearchApi(String keyword) {
                String api = "http://m.piaotian.com/s.php";
                Log.d(TAG, "getSearchApi: api = " + api);
                return api;
            }
        };
    }

    @Override
    public List<Book> searchBook(String keyword, Map<String, String> params) {
        Document doc;
        Map<String, String> data = new HashMap<>();
        data.put("type", "articlename");
        data.put("s", keyword);
        data.put("submit", "");
        try {
            doc = Jsoup.connect(getWebInfo().getSearchApi("")).headers(getHeader()).data(data).
                    postDataCharset(getWebInfo().getWebChar()).post();
            if (doc != null) {
                List<Book> books = new ArrayList<>();
                //if (DEBUG) Log.d(TAG, "searchBook: doc = " + doc.toString());

                // parse book list
                Elements list = doc.getElementsByClass("line");
                for (Element resultBook : list) {
                    Book book = new Book();
                    for (Element info : resultBook.children()) {
                        if (info.hasAttr("href")) {
                            String href = info.attr("href");
                            if (href.contains("sort")) {
                                //System.out.println("category = " + info.text());
                            }
                            if (href.contains("book")) {
                                Log.d(TAG, "bookName = " + info.text() + ", book_url = " + getWebInfo().getHostUrl() + href);
                                book.setBookName(info.text());
                                book.setBookUrl(getWebInfo().getHostUrl() + href);

                                Element bookDoc = Jsoup.connect(getWebInfo().getHostUrl() + href).headers(getHeader()).get();
                                //Log.d(TAG,bookDoc.toString());
                                Element img = bookDoc.getElementsByTag("img").get(0);
                                Log.d(TAG, "img link = " + img.attr("src"));

                                book.setBookCoverUrl(img.attr("src"));

                                Element bookInfo = bookDoc.getElementsByClass("block_txt2").get(0);
                                //Log.d(TAG, bookInfo.toString());

                                Element description = bookDoc.getElementsByClass("intro_info").get(0);
                                Log.d(TAG, "description = " + description.text());

                                Element content = bookDoc.getElementsByClass("ablum_read").get(0).child(0);
                                Log.d(TAG, "content link = " + content.child(0).attr("href"));

                            }
                            if (href.contains("author")) {
                                //System.out.println("author = " + info.text());
                            }
                        }

                    }
                    books.add(book);
                }
                return books;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public Book getBookInfo(String bookUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(bookUrl).headers(getHeader()).get();
            if (doc != null) {
                Book book = new Book();
                Chapter newestChapter = new Chapter();

                //Log.d(TAG,bookDoc.toString());
                Element img = doc.getElementsByTag("img").get(0);
                Log.d(TAG, "img link = " + img.attr("src"));

                book.setBookCoverUrl(img.attr("src"));

                Element bookInfo = doc.getElementsByClass("block_txt2").get(0);
                //Log.d(TAG, bookInfo.toString());
                for (Element p : bookInfo.children()) {
                    if (!p.text().isEmpty()) {
                        String tmp = p.text();
                        if (tmp.contains("：")) {
                            if (tmp.contains("作者")) {
                                Log.d(TAG, "author = " + tmp.split("：")[1]);
                                book.setAuthor(tmp.split("：")[1]);
                            } else if (tmp.contains("分类")) {
                                Log.d(TAG, "category = " + tmp.split("：")[1]);
                                book.setCategory(tmp.split("：")[1]);
                            } else if (tmp.contains("更新")) {
                                Log.d(TAG, "update time = " + tmp.split("：")[1]);
                                book.setUpdateTime(DateUtils.parseNormal(tmp.split("：")[1]));
                            } else if (tmp.contains("最新")) {
                                Log.d(TAG, "newest chapter link = " + getWebInfo().getHostUrl() + p.child(0).attr("href")
                                        + ", name = " + tmp.split("：")[1]);

                                newestChapter.setChapterTitle(tmp.split("：")[1]);
                                newestChapter.setChapterUrl(getWebInfo().getHostUrl() + p.child(0).attr("href"));
                            }
                        } else {
                            book.setBookName(tmp);
                        }
                    }
                    // Log.d(TAG, p.text());
                }
                book.setNewestChapter(newestChapter);

                Element description = doc.getElementsByClass("intro_info").get(0);
                Log.d(TAG, "description = " + description.text());
                book.setDescription(description.text());

                Element content = doc.getElementsByClass("ablum_read").get(0).child(1);
                Log.d(TAG, "content link = " + content.child(0).attr("href"));
                book.setContentUrl(getWebInfo().getHostUrl() + content.child(0).attr("href"));
                book.setBookUrl(bookUrl);
                return book;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Book();
    }

    @Override
    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        Document doc;
        List<Chapter> chapters = new ArrayList<>();
        try {
            contentsUrl = contentsUrl.replace("http://m", "http://www");
            Log.d(TAG, "getContents: url = " + contentsUrl);
            doc = Jsoup.connect(contentsUrl).headers(getHeader()).get();
            if (doc != null) {
                if (DEBUG) Log.d(TAG, "getContents: doc = " + doc.toString());
                for (Element element : doc.getElementsByTag("li")) {
                    if (element.children().size() > 0) {
                        Chapter chapter = new Chapter();

                        String chapterUrl = contentsUrl.replace("index.html", "").replace("http://www", "http://m");
                        chapterUrl = chapterUrl + element.child(0).attr("href");
                        Log.d(TAG, "chapter url = " + chapterUrl + ", chapter name = " + element.text());

                        chapter.setChapterTitle(element.text());
                        chapter.setChapterUrl(chapterUrl);
                        chapter.setBookUrl(bookUrl);
                        chapter.setChapterIndex(getChapterId(chapterUrl));

                        chapters.add(chapter);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chapters;
    }

    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        Document doc;
        Chapter chapter = new Chapter();
        chapter.setChapterUrl(chapterUrl);
        try {
            doc = Jsoup.connect(chapterUrl).headers(getHeader()).get();
            // get chapter title
            Element title = doc.getElementById("nr_title");
            if (title != null && !title.text().isEmpty()) {
                Log.d(TAG, "Title : " + title.text());
                chapter.setChapterTitle(title.text());
            }

            // get contents
            Element content = doc.getElementById("nr1");
            if (content != null) {
                if (DEBUG) Log.d(TAG, content.toString());
                chapter.setContents(Html.fromHtml(content.toString()).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chapter;
    }

    @Override
    public List<Rank> getHomePageInfo() {
        return null;
    }
}
