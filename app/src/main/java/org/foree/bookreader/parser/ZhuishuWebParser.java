package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.utils.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/4/8.
 */

public class ZhuishuWebParser extends AbsWebParser {
    private static final String TAG = ZhuishuWebParser.class.getSimpleName();
    private String mImageApi = "http://statics.zhuishushenqi.com";
    private static final boolean DEBUG = false;

    @Override
    public List<Book> searchBook(String keyword) {
        List<Book> bookList = new ArrayList<>();
        String encodeKeyword = URLEncoder.encode(keyword);

        if (DEBUG) {
            Log.d(TAG, "encodeKeyword = " + encodeKeyword);
        }

        Map<String, String> data = new HashMap<>();
        data.put("query", encodeKeyword);
        data.put("start", "0");
        data.put("limit", "20");

        Document document;
        try {
            document = Jsoup.connect(getWebInfo().getSearchApi("")).headers(getHeader()).data(data).ignoreContentType(true).get();

            if (document != null) {
                String json = document.body().text().trim();
                if (DEBUG) {
                    Log.d(TAG, json);
                }

                JSONObject topObject = new JSONObject(json);

                JSONArray booksArray = topObject.getJSONArray("books");
                for (int i = 0; i < booksArray.length(); i++) {
                    JSONObject bookObject = (JSONObject) booksArray.get(i);
                    Book book = new Book();
                    String bookName = bookObject.getString("title");
                    String bookUrl = bookObject.getString("_id");
                    String bookCoverUrl = mImageApi + bookObject.getString("cover");
                    String author = bookObject.getString("author");
                    String category = bookObject.getString("cat");
                    if (DEBUG) {
                        Log.d(TAG, bookName + ", " + bookUrl + ", " + bookCoverUrl);
                    }
                    book.setBookName(bookName);
                    book.setAuthor(author);
                    book.setCategory(category);
                    book.setBookUrl(bookUrl);
                    book.setBookCoverUrl(bookCoverUrl);

                    bookList.add(book);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    @Override
    public Book getBookInfo(String bookId) {
        // must set params
        Book book = new Book();
        String bookInfoApi = "http://api.zhuishushenqi.com/book/";

        try {
            Document document = Jsoup.connect(bookInfoApi + bookId).ignoreContentType(true).headers(getHeader()).get();
            if (document != null) {

                JSONObject bookInfoObject = new JSONObject(document.body().text());
                String bookUrl = bookInfoObject.getString("_id");
                String bookName = bookInfoObject.getString("title");
                String author = bookInfoObject.getString("author");
                String coverUrl = mImageApi + bookInfoObject.getString("cover");
                String cate = bookInfoObject.getString("majorCate");
                String description = bookInfoObject.getString("longIntro");
                String updateTime = bookInfoObject.getString("updated");
                String contentUrl = getDefaultSourceId(bookId);
                String lastChapter = bookInfoObject.getString("lastChapter");

                Log.d(TAG, bookUrl + ", " + bookName + ", " + author + ", " + coverUrl + ", " + cate + ", " +
                        description + ", " + updateTime + ", " + contentUrl + ", " + lastChapter);

                book.setDescription(description);
                book.setUpdateTime(updateTime);
                book.setContentUrl(contentUrl);
                book.setBookName(bookName);
                book.setBookUrl(bookUrl);
                book.setAuthor(author);
                book.setBookCoverUrl(coverUrl);
                book.setCategory(cate);
                book.setRectentChapterTitle(lastChapter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return book;
    }

    @Override
    public List<Chapter> getContents(String bookId, String contentsUrl) {
        // http://api.zhuishushenqi.com/atoc/sourceId?view=chapters
        List<Chapter> chapterList = new ArrayList<>();
        String contentsApi = "http://api.zhuishushenqi.com/atoc/";

        Map<String, String> data = new HashMap<>();
        data.put("view", "chapters");

        Log.d(TAG, "getContents() called with: bookId = [" + bookId + "], contentsUrl = [" + contentsUrl + "]");

        try {
            Document document = Jsoup.connect(contentsApi + contentsUrl).data(data).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());

                JSONArray contentsArray = jsonObject.getJSONArray("chapters");
                for (int i = 0; i < contentsArray.length(); i++) {
                    //可能需要根据link来过滤重复章节
                    Chapter chapter = new Chapter();
                    JSONObject chapterObject = (JSONObject) contentsArray.get(i);
                    String chatperUrl = chapterObject.getString("link");
                    String chapterTitle = chapterObject.getString("title");
                    int chapterIndex = i;

                    if (DEBUG) {
                        Log.d(TAG, chapterTitle + ", " + chatperUrl + ", " + chapterIndex);
                    }

                    chapter.setBookUrl(bookId);
                    chapter.setChapterIndex(chapterIndex);
                    chapter.setChapterTitle(chapterTitle);
                    chapter.setChapterUrl(chatperUrl);

                    chapterList.add(chapter);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "get url = " + contentsApi + contentsUrl + " error");
        }
        Log.d(TAG, "[foree] getContents finish!");
        return chapterList;
    }

    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        Chapter chapter = new Chapter();
        String contentApi = "http://chapter2.zhuishushenqi.com/chapter/";

        chapter.setChapterUrl(chapterUrl);
        try {
            String encodeLink = URLEncoder.encode(chapterUrl);
            Log.d(TAG, "get link = " + contentApi + encodeLink);

            Document document = Jsoup.connect(contentApi + encodeLink).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONObject chapterObject = jsonObject.getJSONObject("chapter");
                String content = "\n" + chapterObject.getString("body");
                content = content.replaceAll("\n", "\n        ");

                if (DEBUG) {
                    Log.d(TAG, content);
                }
                chapter.setContents(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chapter;
    }

    @Override
    public List<Book> getHomePageInfo() {
        return null;
    }

    @Override
    WebInfo getWebInfo() {
        return new WebInfo() {
            @Override
            public String getHostName() {
                return "追书";
            }

            @Override
            public String getWebChar() {
                return null;
            }

            @Override
            public String getHostUrl() {
                return "api.zhuishu.com";
            }

            @Override
            public String getSearchApi(String keyword) {
                String api = "http://api.zhuishushenqi.com/book/fuzzy-search";
                return api;
            }
        };
    }

    @Override
    public List<Source> getBookSource(String bookId) {
        //根据book_id获取书源信息，后续章节列表需要从某个书源获取
        List<Source> sourceList = new ArrayList<>();
        String bookSourceApi = "http://api.zhuishushenqi.com/toc";

        Map<String, String> data = new HashMap<>(2);
        data.put("view", "summary");
        data.put("book", bookId);

        try {
            Document document = Jsoup.connect(bookSourceApi).data(data).ignoreContentType(true).get();
            if (document != null) {
                JSONArray jsonArray = new JSONArray(document.body().text());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject sourceObject = (JSONObject) jsonArray.get(i);
                    Source source = new Source();
                    source.setChapterCount(sourceObject.getInt("chaptersCount"));
                    source.setCharge(sourceObject.getBoolean("isCharge"));
                    source.setHost(sourceObject.getString("host"));
                    source.setLastChapter(sourceObject.getString("lastChapter"));
                    source.setSourceId(sourceObject.getString("_id"));
                    source.setSourceLink(sourceObject.getString("link"));
                    source.setSourceName(sourceObject.getString("name"));
                    source.setStarting(sourceObject.getBoolean("starting"));
                    source.setUpdated(DateUtils.formatJSDate(sourceObject.getString("updated")));

                    sourceList.add(source);

                    if (DEBUG) {
                        Log.d(TAG, source.getHost() + ", " + source.getSourceName() + ", " + source.getLastChapter() + ", " + source.getUpdated());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //交换第一个和第二个书源（zhuishuvip为第一个，不可用）
        if(!sourceList.isEmpty() && sourceList.size() > 1){
            Source tmp = sourceList.get(0);
            sourceList.set(0, sourceList.get(1));
            sourceList.set(1, tmp);
        }

        return sourceList;
    }

    /**
     * source对应的就是章节列表，sourceId = contentUrl
     * @param bookId 书籍id
     * @return 默认的sourceId
     */
    private String getDefaultSourceId(String bookId) {
        //根据book_id获取书源，后续章节列表需要从某个书源获取

        List<Source> sourceList = getBookSource(bookId);
        String sourceId = sourceList.get(0).getSourceId();
        return sourceId;
    }

    @Override
    public List<Review> getShortReviews(String bookId) {
        List<Review> reviews = new ArrayList<>();
        String shortReviewApi = "http://api.zhuishushenqi.com/post/short-review";
        String imageApi = "http://statics.zhuishushenqi.com";

        Map<String, String> data = new HashMap<>(4);
        data.put("book", bookId);
        data.put("sortType", "newest");
        data.put("start", "0");
        data.put("limit", "20");

        try {
            Document document = Jsoup.connect(shortReviewApi).data(data).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONArray docs = jsonObject.getJSONArray("docs");
                // 默认获取10个
                for (int i = 0; i < docs.length(); i++) {
                    if(i > 9){
                        break;
                    }

                    JSONObject shortReview = docs.getJSONObject(i);
                    Review review = new Review();
                    review.setContent(shortReview.getString("content"));
                    review.setId(shortReview.getString("_id"));
                    review.setLikeCount(shortReview.getInt("likeCount"));
                    review.setUpdated(shortReview.getString("updated"));
                    review.setCreated(shortReview.getString("created"));

                    JSONObject authorObject = shortReview.getJSONObject("author");
                    Review.Author author = new Review.Author();
                    author.setAvatar(imageApi + authorObject.getString("avatar"));
                    author.setId(authorObject.getString("_id"));
                    author.setLv(authorObject.getInt("lv"));
                    author.setNickname(authorObject.getString("nickname"));

                    review.setAuthor(author);

                    reviews.add(review);
                    Log.d(TAG, review.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }
}
