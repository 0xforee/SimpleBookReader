package org.foree.bookreader.data.book;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by foree on 17-1-7.
 */

public class Article implements Serializable {
    private String title;
    private String contents;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;

    private ArrayList<String> pages = new ArrayList<>();

    public ArrayList<String> getPages() {
        return pages;
    }

    public void addPage(String text) {
        pages.add(text);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

}
