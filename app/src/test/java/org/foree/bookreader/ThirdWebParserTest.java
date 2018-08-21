package org.foree.bookreader;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * @author foree
 * @date 2018/8/20
 * @description 阅读APP的源
 */
public class ThirdWebParserTest {
    private static final String TAG = "ThirdWebParserTest";
    private static final String TEST_KEYWORD = "五行天";
    private static final String TEST_BOOK_URL = "http://www.b5200.net/5_5864/";
    private static final String TEST_CHAPTER_URL = "http://www.b5200.net/5_5864/155452620.html";
    private static final String TEST_SOURCE_PATH = "F:\\Android\\文档\\source_test.txt";
    JSONObject mSourceObject;

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

            mSourceObject = new JSONObject(sb.toString());

            // 书源网站
            Log.d(TAG, "sourceUrl = " + mSourceObject.getString("bookSourceUrl"));
            // 书源分组
            Log.d(TAG, "sourceName = " + mSourceObject.getString("bookSourceName"));
            // 书源名称
            Log.d(TAG, "sourceGroup = " + mSourceObject.getString("bookSourceGroup"));

            // 是否合法
            Log.d(TAG, "enable = " + mSourceObject.getBoolean("enable"));

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
        Log.d(TAG, mSourceObject.toString());
        try {
            // search url
            String searchUrl = mSourceObject.getString("ruleSearchUrl");
            searchUrl = searchUrl.replace("searchKey", TEST_KEYWORD);

            // load search result
            Document doc = Jsoup.connect(searchUrl).ignoreContentType(true).get();

            Element body = doc.body();

            // search list
            Elements elements = getElements(mSourceObject.getString("ruleSearchList"), body);

            for (Element target : elements) {
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
    }

    @Test
    public void testBookInfo() {
        String[] rules = new String[]{
                "ruleBookAuthor",
                "ruleBookName",
                "ruleIntroduce",
                "ruleCoverUrl",
                "ruleChapterUrl"
        };

        try {
            Document doc = Jsoup.connect(TEST_BOOK_URL).ignoreContentType(true).get();
            if (doc != null) {
                for (int i = 0; i < rules.length; i++) {
                    String rule = rules[i];
                    String result = getElementString(mSourceObject.getString(rule), doc.body());
                    Log.d(TAG, rule + " = " + result);
                }

                // test contents
                Elements elements = getElements(mSourceObject.getString("ruleChapterList"), doc.body());
                for (Element target : elements) {
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

    }

    @Test
    public void testChapter() {
        try {
            Document doc = Jsoup.connect(TEST_CHAPTER_URL).ignoreContentType(true).get();
            if (doc != null) {
                String content = getElementString(mSourceObject.getString("ruleBookContent"), doc.body());
                Log.d(TAG, "content = " + content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                        temp = el.attr("href");
                        break;
                    case "src":
                        temp = el.attr("src");
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
}
