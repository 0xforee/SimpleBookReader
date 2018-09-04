package org.foree.bookreader;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.utils.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSimilarity(){
        String test = "大主宰";
        String test1 = "五行天";
        String test2 = "五行天师";
        String test3 = "五行天域";
        String test4 = "我是一个帅哥";
        String test5 = "帅哥是我";

        Map<Character, int[]> vectorMap = new HashMap<>();
        // 分析第一个字符串
        for(Character character: test2.toCharArray()){
            if(vectorMap.containsKey(character)){
                vectorMap.get(character)[0]++;
            }else{
                int[] tmp = new int[]{1, 0};
                vectorMap.put(character, tmp);
            }
        }

        // 分析第二个字符串
        for(Character character: test3.toCharArray()){
            if(vectorMap.containsKey(character)){
                vectorMap.get(character)[1]++;
            }else{
                int[] tmp = new int[]{0, 1};
                vectorMap.put(character, tmp);
            }
        }

        // 余弦分母
        double str1 = 0;
        double str2 = 0;
        for(Character character: vectorMap.keySet()){
            int[] tmp = vectorMap.get(character);
            str1 += tmp[0] * tmp[0];
            str2 += tmp[1] * tmp[1];
        }
        double denominator = Math.sqrt(str1 * str2);

        // 余弦分子
        double numerator = 0;
        for(Character character: vectorMap.keySet()){
            int[] tmp = vectorMap.get(character);
            numerator += tmp[0] * tmp[1];
        }

        // 计算余弦值
        double similarity = numerator / denominator;

        Log.d("TAG","similarity = " + similarity);

    }

    @Test
    public void convertUrl2Id() {
        Map<String, String> urls = new HashMap<>();
        urls.put("/0_168/2512063.html", "2512063");
        urls.put( "http://m.bxwx9.org/b/98/98289//0_168/2512063.html","2512063");
        urls.put("http://m.piaotian.com/html/7/7564/6285377.html", "6285377");

        for(Map.Entry<String, String> url: urls.entrySet()) {
            String[] subString = url.getKey().split("/|\\.");
            String id = subString[subString.length - 2];

            assertEquals(id, url.getValue());
        }
    }

    @Test
    public void testUrlSplit(){
        String url1 = "http://m.bxwx9.org/b/98/98289//0_168/2512063.html";
        String url2 = "http://m.piaotian.com/html/7/7564/6285377.html";
        String magicKey = "~_~";

        String sourceUrl = url1 + magicKey;

        String[] values = sourceUrl.split(magicKey);
        if(values.length == 1){
            values[1] = "http";
        }
    }

    @Test
    public void testParseBookInfoByBQGM() {
        String url = "http://m.biquge.com";

        final String TAG = "BOOKINFO";

        Document doc = null;
        try {
            doc = Jsoup.connect("http://m.biquge.com/0_168/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Book book = new Book();
        Chapter newestChapter = new Chapter();

        System.out.println("hhhahaha");
        Elements updates = doc.getElementsByClass("block_txt2");
        if (updates != null && !updates.isEmpty()) {
            Element bookInfos = updates.get(0);
            Elements bookNames = bookInfos.getElementsByTag("h2");
            if (bookNames != null && !bookNames.isEmpty()) {
                book.setBookName(bookNames.get(0).toString());
            }

            Elements bookOthers = bookInfos.getElementsByTag("p");
            if (bookOthers != null && !bookOthers.isEmpty()) {
                for (Element otherInfo : bookOthers) {
                    String otherInfoString = otherInfo.text();
                    //System.out.println(otherInfo.toString());
                    //Log.d(TAG, otherInfo.toString());
                    if (otherInfoString.contains("：")) {
                        //System.out.println(otherInfo.text().split("：")[1]);
                        switch (otherInfoString.split("：")[0]) {
                            case "作者":
                                book.setAuthor(otherInfoString.split("：")[1]);
                                break;
                            case "分类":
                                book.setCategory(otherInfoString.split("：")[1]);
                                break;
                            case "更新":
                                book.setUpdateTime(DateUtils.parseNormal(otherInfoString.split("：")[1]));
                                break;
                            case "最新":
                                Elements newest_ele = otherInfo.getElementsByTag("a");
                                if (newest_ele != null && !newest_ele.isEmpty()) {
                                    newestChapter.setChapterTitle(otherInfo.text().split("：")[1]);
                                    newestChapter.setChapterUrl(newest_ele.get(0).attr("href"));
                                }
                                break;
                        }

                    }
                }
            }
        }

        // get chapterListUrl by book website
        String chapterListUrl = null;
        Elements chapterList = doc.getElementsByClass("intro");
        for (Element infoNode : chapterList) {
            if (infoNode.toString().contains("完整目录")) {
                if (infoNode.child(0) != null) {
                    chapterListUrl = url + infoNode.child(0).attr("href");
                }
            }
        }

        try {
            doc = Jsoup.connect(chapterListUrl).get();
            if (doc != null) {
                Elements chapters = doc.getElementsByClass("chapter");
                if (chapters != null && !chapters.isEmpty()) {
                    Elements li = chapters.select("li");
                    //System.out.print(li.toString());
                    for (Element chapter : li) {
                        Element chapterLink = chapter.child(0);
                        if (chapterLink != null) {
                            System.out.print(url + chapterLink.attr("href"));
                            System.out.println(chapterLink.text());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }

    @Test
    public void testParseChapter() {
        Element doc;
        try {
            doc = Jsoup.connect("http://m.biquge.com/0_168/1214399.html").get();
            if (doc != null) {
                // get chapter title
                Element titleElement = doc.getElementById("nr_title");
                if (titleElement != null) {
                    System.out.println(titleElement.text());
                }


                // get contents
                Element contentElement = doc.getElementById("nr1");
                if (contentElement != null) {
                    System.out.println(contentElement.text());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getWebParser() {
        String url = "http://www.biquge.com/0_168/2494428.html";

        if (url.contains("http://") && url.length() > 7) {
            System.out.println(url.indexOf("/", 7));
            System.out.println(url.substring(0, url.indexOf("/", 7)));
        }
    }

    @Test
    public void testPiaoTianBookList() {
        Element doc;
        String url = "https://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%D0%A1%CB%B5&Submit=+%CB%D1+%CB%F7+&page=1";
        String host = "http://m.piaotian.com";
        String mUrl = "http://m.piaotian.com/s.php";
        String keyword = "五行天";
        try {
            //System.out.print(url);
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
            //keyword = java.net.URLEncoder.encode(keyword, "gb2312");
            Map<String, String> data = new HashMap<>();
            data.put("type", "articlename");
            data.put("s", keyword);
            data.put("submit", "");
            doc = Jsoup.connect(mUrl).headers(headers).data(data).postDataCharset("gb2312").post();
            //doc = Jsoup.connect(url).headers(headers).
            //doc = Jsoup.connect(url).get();
            if (doc != null) {
                //System.out.print(doc.toString());

                //@@@ searchBook() parse book list
                Elements list = doc.getElementsByClass("line");
                for (Element book : list) {
                    //System.out.println(book.toString());
                    for (Element info : book.children()) {
                        if (info.hasAttr("href")) {
                            String href = info.attr("href");
                            if (href.contains("sort")) {
                                //System.out.println("category = " + info.text());
                            }
                            if (href.contains("book")) {
                                // book name and book url
                                System.out.println("bookName = " + info.text() + ", book_url = " + host + href);
                                Element bookDoc = Jsoup.connect(host + href).headers(headers).get();
                                //System.out.println(bookDoc.toString());

                                // book cover url
                                Element img = bookDoc.getElementsByTag("img").get(0);
                                System.out.println("img link = " + img.attr("src"));

                                // book info
                                Element bookInfo = bookDoc.getElementsByClass("block_txt2").get(0);
                                //System.out.println(bookInfo.toString());
                                for(Element p: bookInfo.children()){
                                    if(!p.text().isEmpty()){
                                        String tmp = p.text();
                                        if (tmp.contains("：")){
                                            if(tmp.contains("作者")){
                                                System.out.println("author = " + tmp.split("：")[1]);
                                            }else if(tmp.contains("分类")){
                                                System.out.println("category = " + tmp.split("：")[1]);
                                            }else if(tmp.contains("更新")){
                                                System.out.println("update time = " + tmp.split("：")[1]);
                                            }else if(tmp.contains("最新")){
                                                System.out.println("newest chapter link = " + host + p.child(0).attr("href")
                                                        + ", name = " + tmp.split("：")[1]);

                                                // @@@ getChapter()
                                                doc = Jsoup.connect(host + p.child(0).attr("href")).get();
                                                //System.out.println(doc.toString());
                                                System.out.println(doc.getElementById("nr_title").text());
                                                System.out.println(doc.getElementById("nr1").text());
                                            }
                                        }
                                    }
                                   // System.out.println(p.text());
                                }

                                // description
                                Element description = bookDoc.getElementsByClass("intro_info").get(0);
                                System.out.println("description = " + description.text());

                                // contents url
                                Element content = bookDoc.getElementsByClass("ablum_read").get(0).child(1);
                                System.out.println("content link = " + host + content.child(0).attr("href"));

                                //@@@ getContents() parse contents link
                                String contentsUrl = (host + content.child(0).attr("href")).replace("http://m", "http://www");
                                System.out.println("getContents: url = " + contentsUrl);
                                doc = Jsoup.connect(contentsUrl).headers(headers).get();
                                if (doc != null) {
                                    //System.out.println("getContents: doc = " +doc.toString());
                                    for(Element element: doc.getElementsByTag("li")){
                                        if (element.children().size() > 0) {
                                            contentsUrl = contentsUrl.replace("index.html", "").replace("http://www", "http://m");
                                            String chapterUrl = contentsUrl + element.child(0).attr("href");
//                                            System.out.print("chapter url = " + chapterUrl);
//                                            System.out.println(", chapter name = " + element.text());
                                        }
                                    }
                                }

                            }
                            if (href.contains("author")) {
                                //System.out.println("author = " + info.text());
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}