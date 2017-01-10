package org.foree.bookreader.book;

import java.io.Serializable;

/**
 * Created by foree on 16-7-26.
 */
public class Chapter implements Serializable{
    private String chapterTitle;
    private String chapterUrl;

    public Chapter(){}

    public Chapter(String chapterTitle, String chapterUrl) {
        this.chapterTitle = chapterTitle;
        this.chapterUrl = chapterUrl;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }
}
