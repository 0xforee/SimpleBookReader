package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
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

public class ZhuishuWebParser extends AbstractWebParser {
    private static final String TAG = ZhuishuWebParser.class.getSimpleName();
    private String mImageApi = "http://statics.zhuishushenqi.com";
    private static final boolean DEBUG = false;

    @Override
    public List<Book> searchBook(String keyword, Map<String, String> params) {
        List<Book> bookList = new ArrayList<>();

        Document document;
        try {
            document = Jsoup.connect(getWebInfo().getSearchApi("")).headers(getHeader()).data(params).ignoreContentType(true).get();

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
                    book.setSourceKey(getWebInfo().getHostUrl());

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
                boolean isSerial = bookInfoObject.getBoolean("isSerial");
                int wordCount = bookInfoObject.getInt("wordCount");

                if (DEBUG) {
                    Log.d(TAG, bookUrl + ", " + bookName + ", " + author + ", " + coverUrl + ", " + cate + ", " +
                            description + ", " + updateTime + ", " + contentUrl + ", " + lastChapter + ", " + isSerial + ", " + wordCount);
                }

                book.setDescription(description);
                book.setUpdateTime(DateUtils.formatJSDate(updateTime));
                book.setContentUrl(contentUrl);
                book.setBookName(bookName);
                book.setBookUrl(bookUrl);
                book.setAuthor(author);
                book.setBookCoverUrl(coverUrl);
                book.setCategory(cate);
                book.setRectentChapterTitle(lastChapter);
                book.setSerial(isSerial);
                book.setWordCount(wordCount);
                book.setSourceKey(getWebInfo().getHostUrl());
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

        if (DEBUG) {
            Log.d(TAG, "getContents() called with: bookId = [" + bookId + "], contentsUrl = [" + contentsUrl + "]");
        }

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
            if (DEBUG) Log.d(TAG, "get link = " + contentApi + encodeLink);

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
    public List<Rank> getHomePageInfo() {
        List<Rank> books = new ArrayList<>();
        String bookRankListApi = "http://api.zhuishushenqi.com/ranking/gender";
        String[] groups = new String[]{"male", "female", "picture", "epub"};
        String[] groupsShowName = new String[]{"男生", "女生", "漫画", "出版物"};

        try {
            Document document = Jsoup.connect(bookRankListApi).ignoreContentType(true).get();
            if (document != null) {
                Log.d(TAG, "homePage = " + document.body().text());
                JSONObject object = new JSONObject(document.body().text());

                for (int j = 0; j < groups.length; j++) {
                    JSONArray cate = object.getJSONArray(groups[j]);
                    for (int i = 0; i < cate.length(); i++) {
                        JSONObject content = cate.getJSONObject(i);
                        Rank rank = new Rank.Builder()
                                .id(content.getString("_id"))
                                .title(content.getString("title"))
                                .cover(mImageApi + content.getString("cover"))
                                .collapse(content.getBoolean("collapse"))
                                .monthRank(content.has("monthRank") ? content.getString("monthRank") : "")
                                .totalRank(content.has("totalRank") ? content.getString("totalRank") : "")
                                .shortTitle(content.getString("shortTitle"))
                                .group(groupsShowName[j])
                                .build();

                        books.add(rank);
                    }
                }

                for (Rank rank :
                        books) {
                    Log.d(TAG, rank.toString());

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    AbstractWebInfo getWebInfo() {
        return new AbstractWebInfo() {
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
                return "http://api.zhuishu.com";
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

        int my716SourceIndex = 1;
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

                    if (source.getHost().contains("my716")) {
                        my716SourceIndex = i;
                    }

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
        if (!sourceList.isEmpty() && sourceList.size() > my716SourceIndex) {
            Source tmp = sourceList.get(0);
            sourceList.set(0, sourceList.get(my716SourceIndex));
            sourceList.set(my716SourceIndex, tmp);
        }

        return sourceList;
    }

    /**
     * source对应的就是章节列表，sourceId = contentUrl
     *
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
    public List<Review> getShortReviews(String bookId, Map<String, String> params) {
        Log.d(TAG, "getShortReviews() called with: bookId = [" + bookId + "]");
        List<Review> reviews = new ArrayList<>();
        String shortReviewApi = "http://api.zhuishushenqi.com/post/short-review";
        String imageApi = "http://statics.zhuishushenqi.com";


        try {
            Document document = Jsoup.connect(shortReviewApi).data(params).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONArray docs = jsonObject.getJSONArray("docs");
                // 默认获取10个
                for (int i = 0; i < docs.length(); i++) {
                    if (i > 9) {
                        break;
                    }

                    JSONObject shortReview = docs.getJSONObject(i);
                    Review review = new Review();
                    review.setContent(shortReview.getString("content"));
                    review.setId(shortReview.getString("_id"));
                    review.setLikeCount(shortReview.getInt("likeCount"));
                    review.setUpdated(DateUtils.formatJSDate(shortReview.getString("updated")));
                    review.setCreated(DateUtils.formatJSDate(shortReview.getString("created")));

                    JSONObject authorObject = shortReview.getJSONObject("author");
                    Review.Author author = new Review.Author();
                    author.setAvatar(imageApi + authorObject.getString("avatar"));
                    author.setId(authorObject.getString("_id"));
                    author.setLv(authorObject.getInt("lv"));
                    author.setNickname(authorObject.getString("nickname"));

                    review.setAuthor(author);

                    reviews.add(review);
                    if (DEBUG) Log.d(TAG, review.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    @Override
    public List<Review> getLongReviews(String bookId, Map<String, String> params) {
        List<Review> reviews = new ArrayList<>();
        // 测试五行天，_id = 563552f7688af08743c2ce91
        String short_review_api = "http://api.zhuishushenqi.com/post/review/by-book";
        String testBookId = "563552f7688af08743c2ce91";
        String imageApi = "http://statics.zhuishushenqi.com";


        try {
            Document document = Jsoup.connect(short_review_api).data(params).ignoreContentType(true).get();
            if (document != null) {
                if (DEBUG) Log.d(TAG, "[foree] testLongReview: " + document.body().text());

                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONArray docs = jsonObject.getJSONArray("reviews");
                // 默认获取4个
                for (int i = 0; i < docs.length(); i++) {
                    if (i > 3) {
                        break;
                    }

                    JSONObject shortReview = docs.getJSONObject(i);
                    Review review = new Review();
                    review.setContent(shortReview.getString("content"));
                    review.setId(shortReview.getString("_id"));
                    review.setLikeCount(shortReview.getInt("likeCount"));
                    review.setUpdated(DateUtils.formatJSDate(shortReview.getString("updated")));
                    review.setCreated(DateUtils.formatJSDate(shortReview.getString("created")));

                    JSONObject authorObject = shortReview.getJSONObject("author");
                    Review.Author author = new Review.Author();
                    author.setAvatar(imageApi + authorObject.getString("avatar"));
                    author.setId(authorObject.getString("_id"));
                    author.setLv(authorObject.getInt("lv"));
                    author.setNickname(authorObject.getString("nickname"));

                    review.setAuthor(author);

                    reviews.add(review);

                    if (DEBUG) Log.d(TAG, review.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    public List<Book> getRankList(String rankId) {
        List<Book> rankList = new ArrayList<>();
        String rankApi = "http://api.zhuishushenqi.com/ranking/";
        try {
            Document document = Jsoup.connect(rankApi + rankId).ignoreContentType(true).get();
            if (document != null) {
                JSONArray booksArray = new JSONObject(document.body().text()).getJSONObject("ranking").getJSONArray("books");
                for (int i = 0; i < booksArray.length(); i++) {
                    JSONObject bookObj = booksArray.getJSONObject(i);
                    Book book = new Book();
                    book.setBookUrl(bookObj.getString("_id"));
                    book.setBookName(bookObj.getString("title"));
                    book.setAuthor(bookObj.getString("author"));
                    book.setBookCoverUrl(mImageApi + bookObj.getString("cover"));

                    rankList.add(book);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rankList;
    }
}
