package org.foree.bookreader.book;

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
    private List<Chapter> chapterList;
    private Chapter newestChapter;

    public Book() {
    }

    public Book(String bookName, String bookUrl) {
        this(bookName, bookUrl, "", "", "");
    }

    public Book(String bookName, String bookUrl, String updateTime, String category, String author) {
        this.bookUrl = bookUrl;
        this.bookName = bookName;
        this.updateTime = updateTime;
        this.category = category;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Chapter> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<Chapter> chapterList) {
        this.chapterList = chapterList;
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

}
