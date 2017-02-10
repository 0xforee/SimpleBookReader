package org.foree.bookreader.data.book;

import java.util.List;

/**
 * Created by foree on 16-7-26.
 */
public class Book {
    private String bookName;
    private String updateTime;
    private String category;
    private String author;
    private String bookUrl;
    private List<Chapter> chapters;
    private String description;
    private Chapter newestChapter;
    // recentChapterId默认为-1，在openBook时检查病初始化
    private int recentChapterId;

    public Book() {
    }

    public Book(String bookName, String bookUrl) {
        this(bookName, bookUrl, "", "", "", "", -1);
    }

    public Book(String bookName, String bookUrl, String updateTime, String category, String author, String description, int recentChapterId) {
        this.bookUrl = bookUrl;
        this.bookName = bookName;
        this.updateTime = updateTime;
        this.category = category;
        this.author = author;
        this.description = description;
        this.recentChapterId = recentChapterId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapterList) {
        this.chapters = chapterList;
    }

    public Chapter getNewestChapter() {
        return newestChapter;
    }

    public void setNewestChapter(Chapter newestChapter) {
        this.newestChapter = newestChapter;
    }


    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public int getRecentChapterId() {
        return recentChapterId;
    }

    public void setRecentChapterId(int recentChapterId) {
        this.recentChapterId = recentChapterId;
    }
}
