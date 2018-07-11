package org.foree.bookreader;

import org.foree.bookreader.bean.book.Book;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
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
    public void testZhuiShuSearchBook(){
        String TAG = "testZhuiShuSearchBook";
        String api = "http://api.zhuishushenqi.com/book/fuzzy-search";
        String imageApi = "http://statics.zhuishushenqi.com";
        String keyword = "五行天";
        String encodeKeyword = URLEncoder.encode(keyword);

        Log.d(TAG,"encodeKeyword = " + encodeKeyword);


        Map<String, String> data = new HashMap<>();
        data.put("query", encodeKeyword);
        data.put("start", "0");
        data.put("limit", "20");

        Document document;
        try {
            document = Jsoup.connect(api).headers(headers).data(data).ignoreContentType(true).get();

            if( document != null){
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

    private String getBookSourceId(String bookId){
        //根据book_id获取书源，后续章节列表需要从某个书源获取

        String bookSourceApi = "http://api.zhuishushenqi.com/toc";

        Map<String, String> data = new HashMap<>();
        data.put("view", "summary");
        data.put("book", bookId);

        try {
            Document document = Jsoup.connect(bookSourceApi).data(data).ignoreContentType(true).get();
            if (document != null){
                JSONArray jsonArray = new JSONArray(document.body().text());
                JSONObject sourceObject = (JSONObject) jsonArray.get(2);
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
    public void testZhuiShuBookInfo(){
        //api.zhuishushenqi.com/book/书籍id(_id)
        // 测试五行天，_id = 563552f7688af08743c2ce91
        String bookId = "563552f7688af08743c2ce91";
        String bookInfoApi = "http://api.zhuishushenqi.com/book/";
        String imageApi = "http://statics.zhuishushenqi.com";

        try {
            Document document = Jsoup.connect(bookInfoApi + bookId).ignoreContentType(true).headers(headers).get();
            if(document != null){

                JSONObject bookInfoObject = new JSONObject(document.body().text());

                String bookUrl = bookInfoObject.getString("_id");
                String bookName = bookInfoObject.getString("title");
                String author = bookInfoObject.getString("author");
                String coverUrl = imageApi + bookInfoObject.getString("cover");
                String cate = bookInfoObject.getString("majorCate");
                String description = bookInfoObject.getString("longIntro");
                String updateTime = bookInfoObject.getString("updated");
                String contentsUrl = getBookSourceId(bookId);

                Log.d(TAG, description + ", " + updateTime + ", " + contentsUrl);
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
    public void testZhuiShuContents(){
        // http://api.zhuishushenqi.com/atoc/sourceId?view=chapters
        String contentsApi = "http://api.zhuishushenqi.com/atoc/";
        String sourceId = "57076a32326011945ee8616b";

        Map<String, String> data = new HashMap<>();
        data.put("view", "chapters");

        try {
            Document document = Jsoup.connect(contentsApi + sourceId).data(data).ignoreContentType(true).get();
            if (document != null){
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
    public void testZhuShuChapterContent(){
        String contentApi = "http://chapter2.zhuishushenqi.com/chapter/";
        String link = "http://book.my716.com/getBooks.aspx?method=content&bookId=857612&chapterFile=U_857612_201708021011392106_6506_1.txt";

        try {
            String encodeLink = URLEncoder.encode(link);
            Log.d(TAG, "get link = " + contentApi + encodeLink);

            Document document = Jsoup.connect(contentApi + encodeLink).ignoreContentType(true).get();
            if (document != null){
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
}
