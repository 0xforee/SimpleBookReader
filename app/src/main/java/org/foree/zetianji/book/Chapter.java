package org.foree.zetianji.book;

import java.io.Serializable;

/**
 * Created by foree on 16-7-26.
 */
public class Chapter implements Serializable{
    private String title;
    private String url;

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
