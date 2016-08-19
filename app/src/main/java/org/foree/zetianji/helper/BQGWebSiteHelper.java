package org.foree.zetianji.helper;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.foree.zetianji.net.NetCallback;
import org.foree.zetianji.net.NetWorkApiHelper;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by foree on 16-7-26.
 * 小说的数据来源
 */
public class BQGWebSiteHelper extends AbsWebSiteHelper{
    private static final String TAG = BQGWebSiteHelper.class.getSimpleName();
    private String websiteCharSet;
    private String host_url;
    private String index_page;

    public BQGWebSiteHelper() {
    }

    public BQGWebSiteHelper(WebSiteInfo webSiteInfo) {
        this.websiteCharSet = webSiteInfo.getWeb_char();
        this.host_url = webSiteInfo.getHost_url();
        this.index_page = webSiteInfo.getIndex_page();
    }

    @Override
    public String getWebsiteCharSet() {
        return websiteCharSet;
    }

    private String getHost_url() {
        return host_url;
    }

    private String getIndex_page() {
        return index_page;
    }

    @Override
    public void getNovel(final NetCallback<Novel> netCallback) {
        NetWorkApiHelper.newInstance().getRequest(host_url+index_page, websiteCharSet, new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response){
                        Log.d(TAG, "getNovel: " + response);
                        if (netCallback != null){
                            parseNovel(response, netCallback);
                        }
                    }
                }
                ,new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse (VolleyError error){
                        Log.e(TAG, "onErrorResponse " + error.getMessage());
                        if (netCallback != null){
                            netCallback.onFail(error.getMessage());
                        }

                    }
                }

        );
    }

    @Override
    public void getChapterContent(String chapter_url, String websiteCharSet, final NetCallback<String> netCallback) {
        NetWorkApiHelper.newInstance().getRequest(chapter_url, websiteCharSet, new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response){
                        Log.d(TAG, "getNovel: " + response);
                        if (netCallback != null){
                            parseChapterContent(response, netCallback);
                        }
                    }
                }
                ,new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse (VolleyError error){
                        Log.e(TAG, "onErrorResponse " + error.getMessage());
                        if (netCallback != null){
                            netCallback.onFail(error.getMessage());
                        }

                    }
                }

        );
    }

    // 解析得到小说的属性
    private void parseNovel(final String data, final NetCallback<Novel> netCallback){
        new Thread(){
            @Override
            public void run() {
                super.run();
                Novel novel = new Novel();
                Chapter newestChapter = new Chapter();

                Document doc = Jsoup.parse(data);
                Elements updates = doc.select("[property~=og:novel*]");
                for(Element update: updates){
                    Log.i(TAG, update.toString());
                    switch (update.attr("property")){
                        case "og:novel:category":
                            novel.setCategory(update.attr("content"));
                            break;
                        case "og:novel:author":
                            novel.setAuthor(update.attr("content"));
                            break;
                        case "og:novel:book_name":
                            novel.setBook_name(update.attr("content"));
                            break;
                        case "og:novel:update_time":
                            novel.setUpdate_time(update.attr("content"));
                            break;
                        case "og:novel:latest_chapter_name":
                            newestChapter.setTitle(update.attr("content"));
                            break;
                        case "og:novel:latest_chapter_url":
                            newestChapter.setUrl(update.attr("content"));
                            break;
                    }
                }
                novel.setNewest_chapter(newestChapter);

                List<Chapter> chapters = new ArrayList<>();
                Elements elements_contents = doc.select("dd");
                Document contents = Jsoup.parse(elements_contents.toString());
                Elements elements_a = contents.getElementsByTag("a");
                for(Element link: elements_a){
                    Chapter chapter = new Chapter();
                    chapter.setTitle(link.text());
                    chapter.setHostUrl(host_url);
                    chapter.setUrl(decodeUrl(link.attr("href")));
//          Log.i("HH", link.text());
//          Log.i("HH", link.attr("href"));
                    chapters.add(chapter);
                }
                Collections.reverse(chapters);
                novel.setChapter_list(chapters);

                netCallback.onSuccess(novel);

            }
        }.start();

    }

    // 将章节的编码解析出来
    private String decodeUrl(String url){
        if (url.contains(index_page)){
            return host_url + url;
        } else
            return host_url + index_page + url;
    }

    // 解析章节的内容
    private void parseChapterContent(final String data, final NetCallback<String> netCallback){
        new Thread(){
            @Override
            public void run() {
                super.run();
                Document doc = Jsoup.parse(data);
                Element content = doc.getElementById("content");
                if( content != null) {
                    content.select("script").remove();
                    Log.d(TAG, content.toString());
                    netCallback.onSuccess(content.toString());
                }else{
                    netCallback.onFail("content is null");
                }
            }
        }.start();
    }
}
