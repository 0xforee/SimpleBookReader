package org.foree.bookreader;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Rank;
import org.foree.bookreader.bean.book.Review;
import org.foree.bookreader.utils.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2018/7/9.
 * test for zhuishu webparser api
 */

public class ZhuishuWebParserUnitTest {
    private static final String TAG = ZhuishuWebParserUnitTest.class.getSimpleName();
    Map<String, String> headers = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");

    }

    @Test
    public void testZhuiShuSearchBook() {
        String TAG = "testZhuiShuSearchBook";
        String api = "http://api.zhuishushenqi.com/book/fuzzy-search";
        String imageApi = "http://statics.zhuishushenqi.com";
        String keyword = "五行天";
        String encodeKeyword = URLEncoder.encode(keyword);

        Log.d(TAG, "encodeKeyword = " + encodeKeyword);


        Map<String, String> data = new HashMap<>();
        data.put("query", encodeKeyword);
        data.put("start", "0");
        data.put("limit", "20");

        Document document;
        try {
            document = Jsoup.connect(api).headers(headers).data(data).ignoreContentType(true).get();

            if (document != null) {
                String json = document.body().text().trim();
                Log.d(TAG, json);

                JSONObject topObject = new JSONObject(json);

                JSONArray booksArray = topObject.getJSONArray("books");
                for (int i = 0; i < booksArray.length(); i++) {
                    Book book = new Book();
                    JSONObject bookObject = (JSONObject) booksArray.get(i);
                    String bookName = bookObject.getString("title");
                    String bookUrl = bookObject.getString("_id");
                    String bookCoverUrl = imageApi + bookObject.getString("cover");
                    String author = bookObject.getString("author");
                    String category = bookObject.getString("cat");
                    Log.d(TAG, bookName + ", " + bookUrl + ", " + bookCoverUrl);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getBookSourceId(String bookId) {
        //根据book_id获取书源，后续章节列表需要从某个书源获取

        String bookSourceApi = "http://api.zhuishushenqi.com/toc";

        Map<String, String> data = new HashMap<>();
        data.put("view", "summary");
        data.put("book", bookId);

        int my716_index = 1;
        try {
            Document document = Jsoup.connect(bookSourceApi).data(data).ignoreContentType(true).get();
            if (document != null) {
                JSONArray jsonArray = new JSONArray(document.body().text());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject sourceObject = (JSONObject) jsonArray.get(i);
                    if (sourceObject.getString("host").contains("my716")) {
                        my716_index = i;
                    }
                }
                JSONObject sourceObject = (JSONObject) jsonArray.get(my716_index);
                String sourceId = sourceObject.getString("_id");
                Log.d(TAG, sourceId);
                return sourceId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Test
    public void testZhuiShuBookInfo() {
        //api.zhuishushenqi.com/book/书籍id(_id)
        // 测试五行天，_id = 563552f7688af08743c2ce91
        String bookId = "563552f7688af08743c2ce91";
        String bookInfoApi = "http://api.zhuishushenqi.com/book/";
        String imageApi = "http://statics.zhuishushenqi.com";

        try {
            Document document = Jsoup.connect(bookInfoApi + bookId).ignoreContentType(true).headers(headers).get();
            if (document != null) {

                JSONObject bookInfoObject = new JSONObject(document.body().text());

                String bookUrl = bookInfoObject.getString("_id");
                String bookName = bookInfoObject.getString("title");
                String author = bookInfoObject.getString("author");
                String coverUrl = imageApi + bookInfoObject.getString("cover");
                String cate = bookInfoObject.getString("majorCate");
                String description = bookInfoObject.getString("longIntro");
                String updateTime = bookInfoObject.getString("updated");
                String lastChapter = bookInfoObject.getString("lastChapter");
                String contentsUrl = getBookSourceId(bookId);

                Log.d(TAG, description + ", " + updateTime + ", " + contentsUrl + ", " + lastChapter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getChapterId(String url) {// 提取最长的数字
        // convert http://www.biquge.la22600582.html ==> 22600582

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

    @Test
    public void testZhuiShuContents() {
        // 测试五行天，_id = 563552f7688af08743c2ce91
        // http://api.zhuishushenqi.com/atoc/sourceId?view=chapters
        String contentsApi = "http://api.zhuishushenqi.com/atoc/";
        String sourceId = "57076a32326011945ee8616b";
        String bookId = "563552f7688af08743c2ce91";

        Map<String, String> data = new HashMap<>();
        data.put("view", "chapters");

        try {
            Document document = Jsoup.connect(contentsApi + getBookSourceId(bookId)).data(data).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());
                String bookUrl = jsonObject.getString("book");

                JSONArray contentsArray = jsonObject.getJSONArray("chapters");
                for (int i = 0; i < contentsArray.length(); i++) {
                    //可能需要根据link来过滤重复章节
                    JSONObject chapterObject = (JSONObject) contentsArray.get(i);
                    String chatperUrl = chapterObject.getString("link");
                    String chapterTitle = chapterObject.getString("title");
                    int chapterId = i;

                    Log.d(TAG, chapterTitle + ", " + chatperUrl + ", " + chapterId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testZhuShuChapterContent() {
        String contentApi = "http://chapter2.zhuishushenqi.com/chapter/";
        String link = "http://book.my716.com/getBooks.aspx?method=content&bookId=857612&chapterFile=U_857612_201708021011392106_6506_1.txt";

        try {
            String encodeLink = URLEncoder.encode(link);
            Log.d(TAG, "get link = " + contentApi + encodeLink);

            Document document = Jsoup.connect(contentApi + encodeLink).ignoreContentType(true).get();
            if (document != null) {
                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONObject chapterObject = jsonObject.getJSONObject("chapter");
                String content = chapterObject.getString("body");

                Log.d(TAG, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTimeStyle() {
        String time = "2018-07-13T01:23:15.261Z";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date = simpleDateFormat.parse(time);
            Log.d(TAG, date.toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReview() {
        // 测试五行天，_id = 563552f7688af08743c2ce91
        String short_review_api = "http://api.zhuishushenqi.com/post/short-review";
        String testBookId = "563552f7688af08743c2ce91";
        String imageApi = "http://statics.zhuishushenqi.com";

        Map<String, String> data = new HashMap<>();
        data.put("book", testBookId);
        data.put("sortType", "newest");
        data.put("start", "0");
        data.put("limit", "10");

        try {
            Document document = Jsoup.connect(short_review_api).data(data).ignoreContentType(true).get();
            if (document != null) {
                Log.d(TAG, "[foree] testReview: " + document.body().text());

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

                    Log.d(TAG, review.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLongReviews() {
        // 测试五行天，_id = 563552f7688af08743c2ce91
        String short_review_api = "http://api.zhuishushenqi.com/post/review/by-book";
        String testBookId = "563552f7688af08743c2ce91";
        String imageApi = "http://statics.zhuishushenqi.com";

        Map<String, String> data = new HashMap<>();
        data.put("book", testBookId);
        data.put("sort", "updated");
        data.put("start", "0");
        data.put("limit", "10");

        try {
            Document document = Jsoup.connect(short_review_api).data(data).ignoreContentType(true).get();
            if (document != null) {
                Log.d(TAG, "[foree] testLongReview: " + document.body().text());

                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONArray docs = jsonObject.getJSONArray("reviews");
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

                    Log.d(TAG, review.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHomePage() {
        List<Rank> books = new ArrayList<>();
        String bookRankListApi = "http://api.zhuishushenqi.com/ranking/gender";
        String imageApi = "http://statics.zhuishushenqi.com";
        String[] groups = new String[]{"male", "female", "picture", "epub"};

        try {
            Document document = Jsoup.connect(bookRankListApi).ignoreContentType(true).get();
            if (document != null) {
                Log.d(TAG, "homePage = " + document.body().text());
                JSONObject object = new JSONObject(document.body().text());

                for (String cateName : groups) {
                    JSONArray cate = object.getJSONArray(cateName);
                    for (int i = 0; i < cate.length(); i++) {
                        JSONObject content = cate.getJSONObject(i);
                        Rank rank = new Rank.Builder()
                                .id(content.getString("_id"))
                                .title(content.getString("title"))
                                .cover(imageApi + content.getString("cover"))
                                .collapse(content.getBoolean("collapse"))
                                .monthRank(content.has("monthRank") ? content.getString("monthRank"): "")
                                .totalRank(content.has("totalRank") ? content.getString("totalRank") : "")
                                .shortTitle(content.getString("shortTitle"))
                                .group(cateName)
                                .build();

                        books.add(rank);
                    }
                }

                for (Rank rank :
                        books) {
                    Log.d(TAG, rank.toString());

                }

                generateCategoryList(books);


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<List<Rank>> generateCategoryList(List<Rank> ranks){
        Map<String, List<Rank>> rankMap = new HashMap<>();
        List<List<Rank>> rankList = new ArrayList<>();

        for (int i = 0; i < ranks.size(); i++) {
            Rank rank = ranks.get(i);
            if(rankMap.containsKey(rank.getGroup())){
                rankMap.get(rank.getGroup()).add(rank);
            }else{
                List<Rank> innerRanks = new ArrayList<>();
                innerRanks.add(rank);
                rankMap.put(rank.getGroup(), innerRanks);
            }
        }

        for (String key :
                rankMap.keySet()) {
            Log.d(TAG, key);
            rankList.add(rankMap.get(key));
        }

        return rankList;
    }

}
