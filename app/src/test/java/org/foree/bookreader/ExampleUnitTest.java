package org.foree.bookreader;

import android.util.Log;

import org.foree.bookreader.data.book.Book;
import org.foree.bookreader.data.book.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;

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
    public void convertUrl2Id(){
        String url = "/0_168/2512063.html";
        String url2 = "http://m.bxwx9.org/b/98/98289//0_168/2512063.html";
        String id = null;

        String[] subString = url2.split("/|\\.");
        id = subString[subString.length-2];

        assertEquals(id, "2512063");
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
        if(updates != null && !updates.isEmpty()){
            Element bookInfos = updates.get(0);
            Elements bookNames = bookInfos.getElementsByTag("h2");
            if(bookNames != null && !bookNames.isEmpty()){
                book.setBookName(bookNames.get(0).toString());
            }

            Elements bookOthers = bookInfos.getElementsByTag("p");
            if(bookOthers != null && !bookOthers.isEmpty()){
                for(Element otherInfo: bookOthers){
                    String otherInfoString = otherInfo.text();
                    //System.out.println(otherInfo.toString());
                    //Log.d(TAG, otherInfo.toString());
                    if ( otherInfoString.contains("：")){
                        //System.out.println(otherInfo.text().split("：")[1]);
                        switch (otherInfoString.split("：")[0]){
                            case "作者":
                                book.setAuthor(otherInfoString.split("：")[1]);
                                break;
                            case "分类":
                                book.setCategory(otherInfoString.split("：")[1]);
                                break;
                            case "更新":
                                book.setUpdateTime(otherInfoString.split("：")[1]);
                                break;
                            case "最新":
                                Elements newest_ele = otherInfo.getElementsByTag("a");
                                if(newest_ele !=null && !newest_ele.isEmpty()){
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
                if( chapters != null && !chapters.isEmpty()) {
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
    public void testParseArticle(){
        Element doc;
        try {
            doc = Jsoup.connect("http://m.biquge.com/0_168/1214399.html").get();
            if (doc != null) {
                // get article title
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

        if (url.contains("http://") && url.length() > 7 ){
            System.out.println(url.indexOf("/", 7));
            System.out.println(url.substring(0,url.indexOf("/", 7)));
        }
    }

}