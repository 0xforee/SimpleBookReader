package org.foree.bookreader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author foree
 * @date 2018/8/20
 * @description 阅读APP的源
 */
public class ThirdWebParserTest {
    private static final String TAG = "ThirdWebParserTest";
    private static String TEST_KEYWORD = "五行天";
    private static String TEST_BOOK_URL = "http://www.b5200.net/5_5864/";
    private static String TEST_CHAPTER_URL = "http://www.b5200.net/5_5864/155452620.html";
    private static final String TEST_SOURCE_PATH = "F:\\Android\\文档\\source_test.txt";
    private static final String TEST_SOME_SOURCE_PATH = "F:\\Android\\文档\\sources.txt";
    JSONObject mSourceObject;

    private List<String> mAbortUrl = new ArrayList<>();

    @Before
    public void setUp() {
        // load test source json
        StringBuffer sb = new StringBuffer();
        try {
            byte[] temp = new byte[1024];
            InputStream f = new FileInputStream(TEST_SOURCE_PATH);

            while (f.read(temp) != -1) {
                sb.append(new String(temp));
            }

            f.close();

            mSourceObject = new JSONArray(sb.toString()).getJSONObject(2);

            mAbortUrl.add("https://b.faloo.com");
            mAbortUrl.add("http://www.gxwztv.com");
            mAbortUrl.add("http://www.3dllc.cc");
            // 请求失败
            mAbortUrl.add("https://www.biquwu.cc");
            mAbortUrl.add("https://www.kuxiaoshuo.com");
            // 未加入searchPage
            mAbortUrl.add("https://www.biqubao.com");
            mAbortUrl.add("http://www.biqubu.com");
            mAbortUrl.add(" http://www.mibaoge.com");
            // ssl
            mAbortUrl.add("https://www.biqudu.com");


            // 书源网站
//            Log.d(TAG, "sourceUrl = " + mSourceObject.getString("bookSourceUrl"));
//            // 书源分组
//            Log.d(TAG, "sourceName = " + mSourceObject.getString("bookSourceName"));
//            // 书源名称
//            Log.d(TAG, "sourceGroup = " + mSourceObject.getString("bookSourceGroup"));
//
//            // 是否合法
//            Log.d(TAG, "enable = " + mSourceObject.getBoolean("enable"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSomeSource() {
        // load test source json
        StringBuffer sb = new StringBuffer();
        try {
            byte[] temp = new byte[1024];
            InputStream f = new FileInputStream(TEST_SOME_SOURCE_PATH);

            while (f.read(temp) != -1) {
                sb.append(new String(temp));
            }

            f.close();

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                mSourceObject = jsonArray.getJSONObject(i);

                // 书源名称
                String sourceName = mSourceObject.getString("bookSourceName");
                // 书源网站
                String sourceUrl = mSourceObject.getString("bookSourceUrl");
                Log.d(TAG, "sourceUrl = " + sourceUrl);

                // 是否合法
                boolean enable = mSourceObject.getBoolean("enable");
                if (!enable) {
                    Log.d(TAG, sourceName + " disabled, skip!!!");
                    continue;
                }

                // 屏蔽一些暂时不通过的源
                if (mAbortUrl.contains(sourceUrl)) {
                    continue;
                }

                Log.d(TAG, "sourceName = " + sourceName);

                // 书源分组
//                Log.d(TAG, "sourceGroup = " + mSourceObject.getString("bookSourceGroup"));

                try {
                    // customize keyword
                    String[] keywords = new String[]{"郡主", "五行天", "大道朝天", "大主宰"};
                    for (int j = 0; j < keywords.length; j++) {
                        TEST_KEYWORD = keywords[j];
                        Elements books = getSearchBook(TEST_KEYWORD);

                        // customize book url
                        int random = (int) (Math.random() * 1);
                        if (books.size() > random) {
                            String bookUrl = getElementString(mSourceObject.getString("ruleSearchNoteUrl"), books.get(random));

                            Elements chapters = getBookInfo(bookUrl);

                            // customize chapter url
                            int chapterRandom = (int) (Math.random() * 1);
                            if (chapters.size() > chapterRandom) {
                                String chapterUrl = getElementString(mSourceObject.getString("ruleContentUrl"), chapters.get(chapterRandom));
                                Log.d(TAG, getChapter(chapterUrl));
                            }
                        }

                        Log.d(TAG, "===========================================================");
                    }
                } catch (SearchUrlInvalidException e) {
                    Log.d(TAG, " search url invalid, skip!!!!");
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSearch() {
        try {
            getSearchBook(TEST_KEYWORD);
        } catch (SearchUrlInvalidException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBookInfo() {
        getBookInfo(TEST_BOOK_URL);
    }

    @Test
    public void testChapter() {
        String result = getChapter(TEST_CHAPTER_URL);
        Log.d(TAG, "content = " + result);
    }

    private Elements getSearchBook(String keyword) throws SearchUrlInvalidException {
        Elements books = null;
        Log.d(TAG, mSourceObject.toString());
        try {
            // search url
            String searchUrl = mSourceObject.getString("ruleSearchUrl");

            // invalid search url
            if (!searchUrl.startsWith("http")) {
                throw new SearchUrlInvalidException();
            }

            searchUrl = searchUrl.replace("searchKey", keyword);

            Document doc;

            if (searchUrl.contains("@") || searchUrl.contains("searchPage")) {
                throw new SearchUrlInvalidException();
                // post and get location
//                doc = Jsoup.connect(searchUrl.split("@")[0]).requestBody(searchUrl.split("@")[1]).followRedirects(true).post();
            } else {
                // load search result
                doc = Jsoup.connect(searchUrl).ignoreContentType(true).get();
            }

            Element body = doc.body();

            // search list
            books = getElements(mSourceObject.getString("ruleSearchList"), body);

            for (Element target : books) {
                try {
                    // book name
                    String bookName = getElementString(mSourceObject.getString("ruleSearchName"), target);
                    Log.d(TAG, "bookName = " + bookName);
                    if ("".equals(bookName)) {
                        continue;
                    }

                    // book author
                    String bookAuthor = getElementString(mSourceObject.getString("ruleSearchAuthor"), target);
                    Log.d(TAG, "bookAuthor = " + bookAuthor);

                    // last chapter
                    String lastChapter = getElementString(mSourceObject.getString("ruleSearchLastChapter"), target);
                    Log.d(TAG, "lastChapter = " + lastChapter);

                    // search kind
                    String searchKind = getElementString(mSourceObject.getString("ruleSearchKind"), target);
                    Log.d(TAG, "searchKind = " + searchKind);

                    // ruleSearchNoteUrl
                    String ruleSearchNoteUrl = getElementString(mSourceObject.getString("ruleSearchNoteUrl"), target);
                    Log.d(TAG, "ruleSearchNoteUrl = " + ruleSearchNoteUrl);

                    // ruleSearchCoverUrl
                    String ruleSearchCoverUrl = getElementString(mSourceObject.getString("ruleSearchCoverUrl"), target);
                    Log.d(TAG, "ruleSearchCoverUrl = " + ruleSearchCoverUrl);

                } catch (NoSuchElementException e) {
                    continue;
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return books;
    }

    private Elements getBookInfo(String bookUrl) {
        Elements chapters = null;
        String[] rules = new String[]{
                "ruleBookAuthor",
                "ruleBookName",
                "ruleIntroduce",
                "ruleCoverUrl",
                "ruleChapterUrl"
        };

        try {
            Document doc = Jsoup.connect(bookUrl).ignoreContentType(true).get();
            if (doc != null) {
                for (int i = 0; i < rules.length; i++) {
                    String rule = rules[i];
                    String result = getElementString(mSourceObject.getString(rule), doc.body());
                    Log.d(TAG, rule + " = " + result);
                }

                // test contents
                chapters = getElements(mSourceObject.getString("ruleChapterList"), doc.body());
                for (Element target : chapters) {
                    // chapter name
                    String chapterName = getElementString(mSourceObject.getString("ruleChapterName"), target);
                    // chapter url
                    String chapterUrl = getElementString(mSourceObject.getString("ruleContentUrl"), target);

                    Log.d(TAG, "chapterName = " + chapterName + ", chapterUrl = " + chapterUrl);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return chapters;
    }

    private String getChapter(String chapterUrl) {
        String content = null;
        try {
            Document doc = Jsoup.connect(chapterUrl).ignoreContentType(true).get();
            if (doc != null) {
                content = getElementString(mSourceObject.getString("ruleBookContent"), doc.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }

    private Elements getElements(String ruleString, Element target) {
        Elements result = new Elements();
        String[] rulesAll = ruleString.split("!");

        // 匹配规则
        String[] findRules = rulesAll[0].split("@");

        result = getRecursion(findRules, new Elements(target), 0);

        // 忽略规则
        if (rulesAll.length > 1) {
            String[] ignoreRule = rulesAll[1].split(":");

            for (int k = ignoreRule.length - 1; k >= 0; k--) {
                if (ignoreRule[k].equals("%")) {
                    ignoreRule[k] = (result.size() - 1) + "";
                }

                int index = Integer.valueOf(ignoreRule[k]);
                if (result.size() > index) {
                    result.remove(index);
                }
            }
        }

        return result;
    }

    private String getElementString(String ruleString, Element target) {
        Log.d(TAG, "getElementString() called with: ruleString = [" + ruleString + "]");
        StringBuilder result = new StringBuilder();
        // regex string
        String[] regexRules = ruleString.split("#");

        String[] rules = regexRules[0].split("@");

        // get all elements
        Elements all = getRecursion(rules, new Elements(target), 0);

        // get text from elements
        String[] subRules = rules[rules.length - 1].split("\\.");

        // last one, get content
        if (all != null && !all.isEmpty()) {
            for (Element el : all) {
                String temp = "";
                switch (subRules[0]) {
                    case "text":
                        temp = el.text();
                        break;
                    case "href":
                        temp = el.attr("abs:href");
                        break;
                    case "src":
                        temp = el.attr("abs:src");
                        break;
                    case "textNodes":
                        StringBuilder sb = new StringBuilder();
                        for (TextNode tn : el.textNodes()) {
                            sb.append(tn.text());
                        }
                        temp = sb.toString();
                        break;
                }

                // remove all space
                temp = temp.replaceAll(" ", "");

                if (regexRules.length > 1) {
                    // use regex replace
                    temp = temp.replaceAll(regexRules[1], "");
//                  Log.d(TAG, "after regex: result = " + result);
                }

                result.append(temp);
            }

        }

        return result.toString();
    }

    private Elements getRecursion(String[] ruleString, Elements target, int level) {
        if (level == ruleString.length) {
            // 如果规则解析到达末尾，结束递归
            return target;
        } else {
            // 计算值
            Elements result = new Elements();
            String[] subRules = ruleString[level].split("\\.");
//            Log.d(TAG, "subRules = " + Arrays.toString(subRules));
            for (Element el : target) {
                Elements classes = null;
                switch (subRules[0]) {
                    case "id":
                        result.add(el.getElementById(subRules[1]));
                        continue;
                    case "class":
                        classes = (el.getElementsByClass(subRules[1]));
                        break;
                    case "tag":
                        classes = el.getElementsByTag(subRules[1]);
                        break;
                    default:
                        // 如果最后一个字段为text或者href，textNode等，那么结束递归
                        return target;
                }

                if (classes == null || classes.isEmpty()) {
                    continue;
                }
                if (subRules.length > 2) {
                    result.add(classes.get(Integer.valueOf(subRules[2])));
                } else {
                    result.addAll(classes);
                }
            }

            return getRecursion(ruleString, result, level + 1);
        }
    }

    class SearchUrlInvalidException extends RuntimeException {

    }
}
