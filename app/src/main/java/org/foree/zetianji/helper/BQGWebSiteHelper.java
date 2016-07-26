package org.foree.zetianji.helper;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.foree.zetianji.NetCallback;
import org.foree.zetianji.NetWorkApiHelper;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.book.ZeTianJi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by foree on 16-7-26.
 */
public class BQGWebSiteHelper extends AbsWebSiteHelper{
    private static final String TAG = BQGWebSiteHelper.class.getSimpleName();
    private String websiteCharSet = "utf-8";
    private String host_url = "http://www.biquge.com";
    private String index_page="/0_168/";

    public String getWebsiteCharSet() {
        return websiteCharSet;
    }

    public String getHost_url() {
        return host_url;
    }

    public String getIndex_page() {
        return index_page;
    }

    public void getNovel(final NetCallback<Novel> netCallback) {
        NetWorkApiHelper.newInstance().getRequest(host_url+index_page, websiteCharSet, new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response){
                        Log.d(TAG, "getNovel: " + response);
                        if (netCallback != null){
                            netCallback.onSuccess(parseNovel(response));
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

    public void getChapterContent(String chapter_url, final NetCallback<String> netCallback) {
        NetWorkApiHelper.newInstance().getRequest(chapter_url, websiteCharSet, new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response){
                        Log.d(TAG, "getNovel: " + response);
                        if (netCallback != null){
                            netCallback.onSuccess(parseChapterContent(response));
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

    private Novel parseNovel(String data){
        ZeTianJi ztj = new ZeTianJi();

        Document doc = Jsoup.parse(data);
        Elements elements_contents = doc.select("dd");
        Elements updates = doc.select("[property~=.*update_time]");
        for(Element update: updates){
            Log.i("MM", update.toString());
            ztj.setUpdate_time(update.attr("content"));
        }

        List<Chapter> chapters = new ArrayList<>();
        Document contents = Jsoup.parse(elements_contents.toString());
        Elements elements_a = contents.getElementsByTag("a");
        for(Element link: elements_a){
            Chapter chapter = new Chapter();
            chapter.setTitle(link.text());
            chapter.setUrl(decodeUrl(link.attr("href")));
//          Log.i("HH", link.text());
//          Log.i("HH", link.attr("href"));
            chapters.add(chapter);
        }
        Collections.reverse(chapters);
        ztj.setChapter_list(chapters);

        return ztj;

    }

    private String decodeUrl(String url){
        if (url.contains(index_page)){
            return host_url + url;
        } else
            return host_url + index_page + url;
    }

    private String parseChapterContent(String data){
        Document doc = Jsoup.parse(data);
        Element content = doc.getElementById("content");
        Log.d(TAG, content.toString());
        return content.toString();
    }
}
