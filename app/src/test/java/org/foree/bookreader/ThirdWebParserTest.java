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

                } catch (NoSuchElementException e){
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
    public void testBookInfo(){
        String[] rules = new String[]{
                "ruleBookAuthor",
                "ruleBookName",
                "ruleIntroduce",
                "ruleCoverUrl",
                "ruleChapterUrl"
        };

        try {
            Document doc = Jsoup.connect(TEST_BOOK_URL).ignoreContentType(true).get();
            if(doc != null){
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


    private Elements getElements(String ruleString, Element target){
        Elements result = null;
        String[] rulesAll = ruleString.split("!");

        // 使用忽略规则
        String[] ignoreRule = rulesAll[1].split(":");

        // 匹配规则
        String[] rules = rulesAll[0].split("@");

        for (int i = 0; i < rules.length; i++) {
            String[] subRules = rules[i].split("\\.");
            String type = subRules[0];
            String element_name = subRules[1];

            if(i != rules.length - 1){
                int location = subRules.length == 2 ? 0 : Integer.valueOf(subRules[2]); // 如果之前的元素没有指定位置，默认取0
                switch (type){
                    case "class":
                        target = target.getElementsByClass(element_name).get(location);
                        break;
                    case "tag":
                        target = target.getElementsByTag(element_name).get(location);
                        break;
                }

            }else{
                // last one, get content
                switch (type){
                    case "class":
                        result = target.getElementsByClass(element_name);
                        break;
                    case "tag":
                        result = target.getElementsByTag(element_name);
                        break;

                }

                if(result != null) {
                    Elements temp = result.clone();
                    result.clear();
                    for (int j = 0; j < temp.size(); j++) {

                        try {
                            for (int k = 0; k < ignoreRule.length; k++) {
                                if (ignoreRule[k].equals("%")) {
                                    ignoreRule[k] = (temp.size() - 1) + "";
                                }
                                if (j == Integer.valueOf(ignoreRule[k])) {
                                    throw new NoSuchElementException();
                                }
                            }
                        } catch (NoSuchElementException e){
                            continue;
                        }

                        result.add(temp.get(j));
                    }
                }



//                Log.d(TAG, "result = " + (result != null ? result.toString() : "null"));

            }
        }

        return result;
    }

    private String getElementString(String ruleString, Element target){
        String result = "";
        // regex string
        String[] regexRules = ruleString.split("#");

        String[] rules = regexRules[0].split("@");

        for (int i = 0; i < rules.length; i++) {
            String[] subRules = rules[i].split("\\.");

            if(i != rules.length - 1){
                switch (subRules[0]){
                    case "class":
                        Elements classes = target.getElementsByClass(subRules[1]);
                        if (classes.isEmpty()) {
                            throw new NoSuchElementException();
                        }
                        target = classes.get(Integer.valueOf(subRules[2]));
                        break;
                    case "id":
                        target = target.getElementById(subRules[1]);
                        break;
                    case "tag":
                        Elements tags = target.getElementsByTag(subRules[1]);
                        if(tags.isEmpty()) {
                            throw new NoSuchElementException();
                        }
                        target = tags.get(Integer.valueOf(subRules[2]));
                        break;
                }

            }else{
                // last one, get content
                switch (subRules[0]){
                    case "text":
                        result = target.text();
                        break;
                    case "href":
                        result = target.attr("href");
                        break;
                    case "src":
                        result = target.attr("src");

                }

//                Log.d(TAG, "result = " + result);

                // remove all space
                result = result.replaceAll(" ", "");

                if (regexRules.length > 1) {
                    // use regex replace
                    result = result.replaceAll(regexRules[1], "");
//                  Log.d(TAG, "after regex: result = " + result);
                }

            }
        }

        return result;
    }
}
