package org.foree.bookreader.data.book;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by foree on 16-7-26.
 */
public class Chapter implements Serializable {
    private String chapterTitle;
    private String chapterUrl;
    private String bookUrl;
    private int chapterId;
    private String contents;
    private ArrayList<String> pages = new ArrayList<>();

    public Chapter() {
    }

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

    public ArrayList<String> getPages() {
        return pages;
    }

    public void addPage(String text) {
        pages.add(text);
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }


}
