package org.foree.bookreader.bean.book;

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
    private boolean offline;
    /**
     * 存放章节的分页结果
     */
    private ArrayList<String> pages = new ArrayList<>();

    public Chapter() {
    }

    public Chapter(String chapterTitle, String chapterUrl) {
        this.chapterTitle = chapterTitle;
        this.chapterUrl = chapterUrl;
    }

    public Chapter(String chapterTitle, String chapterUrl, String bookUrl, int chapterId, boolean offline) {
        this.chapterTitle = chapterTitle;
        this.chapterUrl = chapterUrl;
        this.bookUrl = bookUrl;
        this.chapterId = chapterId;
        this.offline = offline;
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

    public void addPage(String text) {
        pages.add(text);
    }

    public String getPage(int index) {
        return pages.get(index);
    }

    public void clearPages() {
        pages.clear();
    }

    public int numberOfPages() {
        return pages.size();
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
