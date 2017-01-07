package org.foree.bookreader.book;

import java.io.Serializable;

/**
 * Created by foree on 16-7-26.
 */
public class Chapter implements Serializable{
    private String hostUrl;
    private String title;
    private String url;

    public Chapter(){}

    public Chapter(String title, String url, String hostUrl) {
        this.hostUrl = hostUrl;
        this.title = title;
        this.url = url;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
