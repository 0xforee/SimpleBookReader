package org.foree.bookreader.book;

import java.io.Serializable;

/**
 * Created by foree on 16-7-26.
 */
public class Chapter implements Serializable{
    private String chapterTitle;
    private String chapterUrl;
    private String bookUrl;
    private int chapterId;

    public Chapter(){}

    public Chapter(String chapterTitle, String chapterUrl) {
        this.chapterTitle = chapterTitle;
        this.chapterUrl = chapterUrl;
    }

    public Chapter(String chapterTitle, String chapterUrl, String bookUrl, int chapterId) {
        this.chapterTitle = chapterTitle;
        this.chapterUrl = chapterUrl;
        this.bookUrl = bookUrl;
        this.chapterId = chapterId;
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

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }
}
