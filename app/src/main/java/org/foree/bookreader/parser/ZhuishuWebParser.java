package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
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

    @Override
    public List<Book> searchBook(String keyword) {
        List<Book> bookList = new ArrayList<>();
        String encodeKeyword = URLEncoder.encode(keyword);

        Log.d(TAG,"encodeKeyword = " + encodeKeyword);

        Map<String, String> data = new HashMap<>();
        data.put("query", encodeKeyword);
        data.put("start", "0");
        data.put("limit", "20");

        Document document;
        try {
            document = Jsoup.connect(getWebInfo().getSearchApi("")).headers(getHeader()).data(data).ignoreContentType(true).get();

            if( document != null){
                String json = document.body().text().trim();
                Log.d(TAG, json);

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
                    Log.d(TAG, bookName + ", " + bookUrl + ", " + bookCoverUrl);
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
            if(document != null){

                JSONObject bookInfoObject = new JSONObject(document.body().text());
                String bookUrl = bookInfoObject.getString("_id");
                String bookName = bookInfoObject.getString("title");
                String author = bookInfoObject.getString("author");
                String coverUrl = mImageApi + bookInfoObject.getString("cover");
                String cate = bookInfoObject.getString("majorCate");
                String description = bookInfoObject.getString("longIntro");
                String updateTime = bookInfoObject.getString("updated");
                String contentsUrl = getBookSourceId(bookId);

                Log.d(TAG, bookUrl + ", " + bookName + ", " + author + ", " + coverUrl + ", " + cate + ", " +
                        description + ", " + updateTime + ", " + contentsUrl);

                book.setDescription(description);
                book.setUpdateTime(updateTime);
                book.setContentUrl(contentsUrl);
                book.setBookName(bookName);
                book.setBookUrl(bookUrl);
                book.setAuthor(author);
                book.setBookCoverUrl(coverUrl);
                book.setCategory(cate);
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

        try {
            Document document = Jsoup.connect(contentsApi + getBookSourceId(bookId)).data(data).ignoreContentType(true).get();
            if (document != null){
                JSONObject jsonObject = new JSONObject(document.body().text());

                JSONArray contentsArray = jsonObject.getJSONArray("chapters");
                for (int i = 0; i < contentsArray.length(); i++) {
                    //可能需要根据link来过滤重复章节
                    Chapter chapter = new Chapter();
                    JSONObject chapterObject = (JSONObject) contentsArray.get(i);
                    String chatperUrl = chapterObject.getString("link");
                    String chapterTitle = chapterObject.getString("title");
                    int chapterIndex = i;

//                    Log.d(TAG, chapterTitle + ", " + chatperUrl + ", " + chapterIndex);

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
            if (document != null){
                JSONObject jsonObject = new JSONObject(document.body().text());
                JSONObject chapterObject = jsonObject.getJSONObject("chapter");
                String content = chapterObject.getString("body");

                Log.d(TAG, content);
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
                JSONObject sourceObject = (JSONObject) jsonArray.get(jsonArray.length() > 1 ? 1 : 0);
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
}
