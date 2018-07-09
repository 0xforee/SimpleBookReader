package org.foree.bookreader;

import org.foree.bookreader.bean.book.Book;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    @Test
    public void testZhuiShuSearchBook(){
        String TAG = "testZhuiShuSearchBook";
        String api = "http://api.zhuishushenqi.com/book/fuzzy-search";
        String imageApi = "http://statics.zhuishushenqi.com";
        String keyword = "五行天";
        String encodeKeyword = URLEncoder.encode(keyword);

        Log.d(TAG,"encodeKeyword = " + encodeKeyword);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");

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
                    Log.d(TAG, bookName + ", " + bookUrl + ", " + bookCoverUrl);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testZhuiShuBookInfo(){

    }
}
